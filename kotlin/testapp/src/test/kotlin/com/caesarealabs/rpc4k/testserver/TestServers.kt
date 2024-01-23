@file:Suppress("TestFunctionName")

package com.caesarealabs.rpc4k.testserver

import com.caesarealabs.rpc4k.generated.rpc4k
import com.caesarealabs.rpc4k.runtime.api.startServer
import com.caesarealabs.rpc4k.test.BasicApi
import com.caesarealabs.rpc4k.testapp.AllEncompassingService
import kotlin.test.Test

class TestServers {
    @Test
    fun myApi() {
        BasicApi.rpc4k.startServer { BasicApi() }
    }

    @Test
    fun allEncompassingService() {
        AllEncompassingService.rpc4k.startServer { AllEncompassingService(it) }
    }
}