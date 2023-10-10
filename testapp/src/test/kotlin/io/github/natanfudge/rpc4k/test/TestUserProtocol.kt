package io.github.natanfudge.rpc4k.test

import com.example.*
import io.github.natanfudge.rpc4k.runtime.api.RpcClientException
import io.github.natanfudge.rpc4k.runtime.api.RpcResponseException
import io.github.natanfudge.rpc4k.runtime.api.old.server.RpcServer
import io.github.natanfudge.rpc4k.test.util.rpcExtension
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.extension.RegisterExtension
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.isEqualTo
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class TestUserProtocol {
    companion object {
        @JvmField
        @RegisterExtension
        val userExtension = rpcExtension(UserProtocol())
        @JvmField
        @RegisterExtension
        val simpleExtension = rpcExtension(SimpleProtocol())
    }

    @Test
    fun testUsage(): Unit = runBlocking {
        val protocol = userExtension.api
        val response = protocol.createLobby(PlayerId(123), "alo")
        assertEquals(CreateLobbyResponse(126), response)
        val response2 = protocol.killSomeone(111, PlayerId(5), Unit)
        assertEquals(116.toUInt(), response2)
        protocol.someShit(1, 2)
        protocol.someShit(1, 2)
        val mapForEntry = mapOf(1 to 1)
        protocol.moreTypes(
            listOf(),
            listOf(),
            1 to 2,
            Triple(Unit, PlayerId(1), ""),
            mapForEntry.entries.first()
        )
        val result = protocol.test(1 to 2)
        assertEquals(Triple(1, 2, "3") to 4.0, result)
        protocol.nullable(null, listOf(null))
        println("fioi")
        println("fioi")
        protocol.someShit(1, 2)

        protocol.genericTest("")
    }

    @Test
    fun testNullableTypes() : Unit = runBlocking {
        val protocol = userExtension.api
        expectThat(protocol.heavyNullable(UserProtocol.HeavyNullableTestMode.EntirelyNull)).isEqualTo(null)
        expectThat(protocol.heavyNullable(UserProtocol.HeavyNullableTestMode.NullList)).isEqualTo(GenericThing(null, null, listOf()))
        expectThat(protocol.heavyNullable(UserProtocol.HeavyNullableTestMode.NullString)).isEqualTo(GenericThing(listOf(null, "test"), null, listOf()))
    }

    @Test
    fun testManual(): Unit = runBlocking {
        val protocol = simpleExtension.api
        val response = protocol.bar(2)
        assertEquals(3, response)
    }

    @Test
    fun testExceptions(): Unit = runBlocking {
        expectThrows<RpcResponseException> {
            userExtension.api.errorTest()
        }.get { code == 500 }

        expectThrows<RpcResponseException> {
            userExtension.api.requirementTest()
        }.get { code == 400 }
    }

}