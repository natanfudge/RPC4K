package com.caesarealabs.rpc4k.runtime.jvm.api

import com.caesarealabs.rpc4k.runtime.api.*
import com.caesarealabs.rpc4k.runtime.api.components.JsonFormat
import com.caesarealabs.rpc4k.runtime.implementation.Rpc4kLogger
import com.caesarealabs.rpc4k.runtime.user.Rpc4kIndex
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.KSerializer
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.RequestBody.Companion.toRequestBody
import okio.IOException
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.resume

//public class OkHttpRpcClientFactory(private val client: OkHttpClient = OkHttpClient()) : RpcClientFactory {
//    override fun build(url: String, websocketUrl: String): RpcClient = OkHttpRpcClient(url, websocketUrl, client)
//}

public class OkHttpRpcClient(
    private val url: String, private val websocketUrl: String,
    private val client: OkHttpClient = OkHttpClient()
) :
    RpcClient {

    override suspend fun send(rpc: Rpc, format: SerializationFormat, serializers: List<KSerializer<*>>): ByteArray {
        val data = rpc.toByteArray(format, serializers)
        val response = client.request(Request(url.toHttpUrl(), body = data.toRequestBody()))
        fun exception(message: String): Nothing = throw RpcResponseException(message, rpc, format, this, response.body.string(), response.code)
        when (response.code) {
            200 -> return response.body.bytes()
            400 -> exception("Request was not valid. The client may not be up to date")
            404 -> exception("Could not find the server at url '$url'.")
            500 -> exception("The server crashed handling the request")
            else -> exception("The server returned an unexpected status code: ${response.code}.")
        }
    }

    override val events: EventClient
        get() = OkHttpWebsocketEventClient(websocketUrl, client)
}

private suspend fun OkHttpClient.request(request: Request): Response = suspendCancellableCoroutine { cont ->
    newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            cont.cancel(e)
        }

        override fun onResponse(call: Call, response: Response) {
            cont.resume(response)
        }
    })
}

private class OkHttpWebsocketEventClient(url: String, client: OkHttpClient) : AbstractEventClient() {
    val webSocket by lazy {
        client.newWebSocket(Request(url.toHttpUrl()), object : WebSocketListener() {
            override fun onMessage(webSocket: WebSocket, text: String) {
                handleMessage(S2CEventMessage.fromString(text))
            }
        })
    }

    override suspend fun send(message: ByteArray) {
        //TODO: kind of inefficient bytes -> string conversion
        webSocket.send(message.decodeToString())
    }
}

public fun <C> Rpc4kIndex<*, C, *>.okHttpClient(url: String, format: SerializationFormat = JsonFormat()): C {
    val websocketUrl = "$url/events"
    return createNetworkClient(OkHttpRpcClient(url, websocketUrl), format)
}