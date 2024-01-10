package com.caesarealabs.rpc4k.runtime.implementation

import com.caesarealabs.rpc4k.runtime.api.*
import io.ktor.websocket.*
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue




public class KtorWebsocketEventConnection(private val session: DefaultWebSocketSession): EventConnection {
    override val id: String = UUID.randomUUID().toString()
    override suspend fun send(bytes: ByteArray) {
        session.send(Frame.Text(true, bytes))
    }
}



internal class KtorEventManager: EventManager<KtorWebsocketEventConnection> {
    private val subscriptions: MutableMap<String, ConcurrentLinkedQueue<KtorSubscription>> = ConcurrentHashMap()

    override suspend fun subscribe(subscription: EventMessage.Subscribe, connection: KtorWebsocketEventConnection) {
        subscriptions.concurrentAdd(subscription.event, KtorSubscription(connection, subscription))
    }

    override suspend fun unsubscribe(event: String, listenerId: String): Boolean {
        val list = subscriptions[event] ?: return false
        return list.removeIf { it.info.listenerId == listenerId }
    }

     override suspend fun dropClient(connection: KtorWebsocketEventConnection) {
        for (list in subscriptions.values) {
            list.removeIf { it.connection == connection }
        }
    }

    override suspend fun match(event: String, watchedObjectId: String?): List<KtorSubscription> {
        val subscribers = subscriptions[event] ?: listOf()
        if (watchedObjectId == null) return subscribers.toList() // No specific object - return all

        // Specific object - return matching
        return subscribers.filter { it.info.target == watchedObjectId }
    }
}


internal typealias KtorSubscription = EventSubscription<KtorWebsocketEventConnection>

