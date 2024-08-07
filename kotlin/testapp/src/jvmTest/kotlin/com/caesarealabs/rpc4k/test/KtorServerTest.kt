package com.caesarealabs.rpc4k.test

import com.caesarealabs.rpc4k.generated.rpc4k
import com.caesarealabs.rpc4k.runtime.jvm.user.testing.junit
import com.caesarealabs.rpc4k.runtime.user.Api
import com.caesarealabs.rpc4k.testapp.BasicApi
import com.caesarealabs.rpc4k.testapp.Dog
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import org.junit.jupiter.api.extension.RegisterExtension
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import kotlin.test.Test


class KtorServerTest {
    companion object {
        @JvmField
        @RegisterExtension
        val extension = BasicApi.rpc4k.junit { BasicApi(it) }
    }


    @Test
    fun `Basic RPCs work`(): Unit = runBlocking {
        val client = extension.client
        val dog = Dog("asdf", "shiba", 2)
        client.putDog(dog)
        val dogs = client.getDogs(2, "shiba")
        expectThat(dogs).isEqualTo(listOf(dog))
    }
}


