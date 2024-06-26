@file:Suppress("TestFunctionName")

package com.caesarealabs.rpc4k.testserver

import com.caesarealabs.rpc4k.generated.server
import com.caesarealabs.rpc4k.runtime.api.RpcServerSetup
import com.caesarealabs.rpc4k.runtime.api.createServer
import com.caesarealabs.rpc4k.test.BasicApi
import com.caesarealabs.rpc4k.testapp.AllEncompassingService
import kotlin.test.Test

class TestServers {
    @Test
    fun myApi() {
        RpcServerSetup.managedKtor(BasicApi(), BasicApi.server()).createServer().start(wait = true)
    }

    @Test
    fun allEncompassingService() {
        RpcServerSetup.managedKtor(AllEncompassingService(),AllEncompassingService.server()).createServer().start(wait = true)
    }
}