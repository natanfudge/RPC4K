package io.github.natanfudge.rpc4k.runtime.api

import kotlinx.serialization.SerializationStrategy

/**
 * Generic interface for anything that sends information across the network.
 * Examples: HTTP Client, HTTP Server, WebSocket client/server.
 */
interface RpcClient {
    /**
     * Sends something across the network.
     * The transmitter should use the specified [format] with the specified [serializers] to serialize the arguments of [rpc].
     * In the future we could allow [RpcClient]s to have custom logic when it comes to how exactly to serialize the [rpc].
     *
     * @return The response body's bytes.
     */
    suspend fun send(rpc: Rpc, format: SerializationFormat, serializers: List<SerializationStrategy<*>>): ByteArray
}



