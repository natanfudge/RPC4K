@file:Suppress("unused")

package io.github.natanfudge.rpc4k.runtime.impl

import io.github.natanfudge.rpc4k.runtime.api.client.RpcHttpClient
import io.github.natanfudge.rpc4k.runtime.api.format.JsonFormat
import io.github.natanfudge.rpc4k.runtime.api.format.SerializationFormat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationStrategy


class Interceptors(val parameterInterceptors: List<(Any?) -> Unit>)

class DecoderContext(val format: SerializationFormat, val interceptors: Interceptors)

object Rpc4kGeneratedServerUtils {
    fun <T> encodeResponse(
        format: SerializationFormat,
        serializer: SerializationStrategy<T>,
        response: T
    ) = format.serialize(serializer, response)

    fun <T> encodeFlowResponse(
        format: SerializationFormat,
        serializer: SerializationStrategy<T>,
        response: Flow<T>
    ) =
        response.map { format.serialize(serializer, it) }

    fun <T> decodeParameter(
        context: DecoderContext,
        serializer: DeserializationStrategy<T>,
        args: List<ByteArray>,
        index: Int
    ): T {
        require(index < args.size) { "Too many parameters" }
        val result = context.format.deserialize(serializer, args[index])
        context.interceptors.parameterInterceptors.forEach { it(result) }
        return result
    }


    fun invalidRoute(route: String): Nothing =
        throw IllegalArgumentException("Unexpected route: $route")
}


object Rpc4KGeneratedClientUtils {
    //    private fun RpcClient.
    suspend fun <T> send(
        client: RpcClientComponents,
        route: String,
        returnType: KSerializer<T>,
        vararg parameters: Pair<Any?, KSerializer<out Any?>>,
    ): T {
        val body = encodeBody(parameters.toList(), client)
        val response = client.http.request(route, body)
        return client.format.deserialize(returnType, response)
    }

    suspend fun <T> sendFlow(
        client: RpcClientComponents,
        route: String,
        returnType: KSerializer<T>,
        vararg parameters: Pair<Any?, KSerializer<out Any?>>,
    ): Flow<T> {
        val body = encodeBody(parameters.toList(), client)
        val response = client.http.flowRequest(route, body)
        return response.map {
            client.format.deserialize(returnType, it)
        }
    }

    private fun encodeBody(
        parameters: List<Pair<Any?, KSerializer<out Any?>>>,
        client: RpcClientComponents
    ): ByteArray {
        val body = parameters
            .map { (value, serializer) ->
                @Suppress("UNCHECKED_CAST")
                client.format.serialize(serializer as KSerializer<Any?>, value)
            }
            .encodeAndJoin()
        return body
    }
}


class RpcClientComponents(val format: SerializationFormat = JsonFormat, val http: RpcHttpClient)