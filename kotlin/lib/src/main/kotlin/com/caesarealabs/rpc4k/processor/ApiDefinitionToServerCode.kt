package com.caesarealabs.rpc4k.processor

import com.caesarealabs.rpc4k.processor.ApiDefinitionUtils.ignoreExperimentalWarnings
import com.caesarealabs.rpc4k.processor.utils.poet.*
import com.caesarealabs.rpc4k.runtime.api.GeneratedServerHelper
import com.caesarealabs.rpc4k.runtime.api.Rpc4kIndex
import com.caesarealabs.rpc4k.runtime.implementation.GeneratedCodeUtils
import com.caesarealabs.rpc4k.runtime.implementation.kotlinPoet
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy

/**
 * Converts
 * ```
 * @Api
 * class MyApi {
 *     private val dogs = mutableListOf<Dog>()
 *     fun getDogs(num: Int, type: String): List<Dog> {
 *         return dogs.filter { it.type == type }.take(num)
 *     }
 *
 *     fun putDog(dog: Dog) {
 *         dogs.add(dog)
 *     }
 * }
 * ```
 * into
 * ```
 * public fun BasicApi.Companion.server(): BasicApiServerImpl = BasicApiServerImpl()
 *
 * @Suppress("UNCHECKED_CAST")
 * public class BasicApiServerImpl: GeneratedServerHelper<BasicApi> {
 *     override suspend fun handle(request: ByteArray, method: String, setup: RpcSetup<BasicApi, *>): ByteArray? = when (method) {
 *         "getDogs" -> respond(setup, request, listOf(Int.serializer(), String.serializer()), ListSerializer(Dog.serializer())
 *         ) {
 *             setup.handler.getDogs(it[0] as Int, it[1] as String)
 *         }
 *
 *         "putDog" -> respond(setup, request, listOf(Dog.serializer()),
 *             VoidUnitSerializer()
 *         ) {
 *             setup.handler.putDog(it[0] as Dog)
 *         }
 *
 *         else -> null
 *     }
 * }
 * ```
 *
 * Which makes running client code much easier.
 */
internal class ApiDefinitionToServerCode(private val api: RpcApi) {
    companion object {
        private const val SetupParamName: String = "setup"
        private const val UserHandlerPropertyName = "handler"
        private val wildcardType = WildcardTypeName.producerOf(ANY.copy(nullable = true))
        private val rpcSetupOf = ClassName("com.caesarealabs.rpc4k.runtime.api", "RpcSetupOf")

        private const val RequestParamName = "request"
        private const val MethodParamName = "method"

        private val respondUtilsMethod = GeneratedCodeUtils::class.methodName("respond")
        private val transformEventUtilsMethod = GeneratedCodeUtils::class.methodName("transformEvent")
        private const val InvokerSuffix = "EventInvoker"

        private const val DispatcherDataParamName = "dispatcherData"
        private const val SubscriptionDataParamName = "subscriptionData"
        private const val EventParamName = "event"

    }

    private val invokerName = "${api.name.simple}${InvokerSuffix}"
    private val invokerClassName = ClassName(GeneratedCodeUtils.Package, invokerName)
    private val clientClassName = ClassName(GeneratedCodeUtils.Package, api.name.simple + GeneratedCodeUtils.ClientSuffix)
    private val serverClassName = api.name.kotlinPoet
    private val rpcSetup = rpcSetupOf.parameterizedBy(WildcardTypeName.producerOf(serverClassName))

    //TODO: replace
    //      1.
    //  private val setup: RpcSetupOf<out AllEncompassingService> -->
//     private val config: EventConfig<AllEncompassingService>,
    // 2. Get rid of 'create invoker' and such, we don't need them anymore.
    // 3. Generate this as the body of invokers:
    //       GeneratedCodeUtils.invokeEvent(config, "eventTest",listOf(String.serializer()),String.serializer(),) {
    //          config.handler.eventTest(dispatchParam, it[0] as String)
    //      }
    // - Make sure to do target.toString() when targets exist
    // 4. Get rid of events routers

    //TODO:
    // Later...
    // 1. Fixup broken functions and interfaces
    // 2. Use Rpc4kIndex for tests
    // 3. Replace the API to all be extensions on Rpc4kIndex:
    //   MyApi.rpc4k.createServer(...)
    //   MyApi.rpc4k.createClient(...)
    //   MyApi.rpc4k.junitExtension(...)

    fun convert(): FileSpec {
        val className = "${api.name.simple}${GeneratedCodeUtils.ServerSuffix}"
        return fileSpec(GeneratedCodeUtils.Package, className) {
            // I know what I'm doing, Kotlin!
            addAnnotation(AnnotationSpec.builder(Suppress::class).addMember("%S", "UNCHECKED_CAST").build())
            ignoreExperimentalWarnings()

            // KotlinPoet doesn't handle extension methods well
            addImport("kotlinx.serialization.builtins", "serializer")
            addImport("kotlinx.serialization.builtins", "nullable")

            addFunction(serverConstructorExtension(generatedClassName = className))
            addProperty(rpc4kGeneratedSuiteExtension())

            addApiHelperClass(className)
            addInvokerClass()
        }
    }

    private fun FileSpec.Builder.addApiHelperClass(className: String) {
        addClass(className) {
            addSuperinterface(
                GeneratedServerHelper::class.asClassName().parameterizedBy(
                    serverClassName, invokerClassName
                )
            )
            addFunction(handleRequestMethod())
            addFunction(handleEventMethod())
            addFunction(invokerProviderFunction())
        }
    }

    //   override fun createInvoker(setup: RpcSetupOf<out AllEncompassingService>): AllEncompassingServiceEventInvoker {
//    return AllEncompassingServiceEventInvoker()
//  }

    private fun invokerProviderFunction(): FunSpec = funSpec("createInvoker") {
        addModifiers(KModifier.OVERRIDE)
        addParameter(SetupParamName, rpcSetup)
        returns(invokerClassName)
        addCode("return %T($SetupParamName)", invokerClassName)
    }

    /**
     * Making the generated class available with an extension function makes it more resilient to name changes
     *   since you will no longer need to directly reference the generated class.
     *   Looks like:
     *   ```
     *   fun MyApi.Companion.server(api: MyApi, format: SerializationFormat, server: RpcServer) = MyApiServerImpl(api, format, server)
     *   ```
     */
    private fun serverConstructorExtension(generatedClassName: String) =
        extensionFunction(serverClassName.companion(), "server") {
            returns(ClassName(GeneratedCodeUtils.Package, generatedClassName))
            addStatement("return $generatedClassName()")
        }

    /**
     * val MyApi.Companion.server = object : GeneratedSuiteFactory<MyApi, AllEncompassingServiceClientImpl, AllEncompassingServiceEventInvoker> {
     *     override val createInvoker = ::AllEncompassingServiceEventInvoker
     *     override val createMemoryClient get() =  TODO()
     *     override val createNetworkClient = ::AllEncompassingServiceClientImpl
     * }
     *
     */
    private fun rpc4kGeneratedSuiteExtension(): PropertySpec {
        val suiteType = Rpc4kIndex::class.asTypeName().parameterizedBy(serverClassName, clientClassName, invokerClassName)
        return extensionProperty(serverClassName.companion(), "rpc4k", suiteType) {
            addCode("""
                            |return object: %T {
                            |   override val createInvoker = ::%T
                            |   override val createMemoryClient get() = TODO()
                            |   override val createNetworkClient get() = ::%T
                            |}
                            |""".trimMargin(),
                suiteType,invokerClassName, clientClassName
            )
        }
    }



    /**
     * Generates:
     * ```
     *     override suspend fun handle(request: ByteArray, method: String, setup: RpcSetup<BasicApi, *>): ByteArray? = when (method) {
     *         "getDogs" -> respond(setup, request, listOf(Int.serializer(), String.serializer()), ListSerializer(Dog.serializer())
     *         ) {
     *             setup.handler.getDogs(it[0] as Int, it[1] as String)
     *         }
     *
     *         "putDog" -> respond(setup, request, listOf(Dog.serializer()),
     *             VoidUnitSerializer()
     *         ) {
     *             setup.handler.putDog(it[0] as Dog)
     *         }
     *
     *         else -> null
     *     }
     * ```
     */
    private fun handleRequestMethod(): FunSpec = funSpec("handleRequest") {
        // This overrides GeneratedServerHandler
        addModifiers(KModifier.OVERRIDE, KModifier.SUSPEND)

        addParameter(RequestParamName, ByteArray::class)
        addParameter(MethodParamName, String::class)
        addParameter(SetupParamName, rpcSetup)

        returns(BYTE_ARRAY.copy(nullable = true))

        addControlFlow("return when($MethodParamName)") {
            for (method in api.methods) {
                addEndpointHandler(method)
            }
            addCode("else -> null\n")
        }
    }




    /**
     * Produces
     * ```
     *     override suspend fun handleEvent(dispatcherData: Any?, subscriptionData: ByteArray, event: String,
     *                                      setup: RpcServerSetup<out SmartEventResponder, *>): ByteArray? = when (event) {
     *         "test" -> GeneratedCodeUtils.transformEvent(setup, subscriptionData, listOf(String.serializer(), Int.serializer()),
     *             Float.serializer()
     *         ) {
     *             with(dispatcherData as AddedRow) {
     *                 setup.handler.testEvent(it[0] as String, it[1] as Int)
     *             }
     *         }
     *
     *         "test2" -> GeneratedCodeUtils.transformEvent(setup, subscriptionData, listOf(Int.serializer()),
     *             Float.serializer()
     *         ) {
     *             with(dispatcherData as TestEvent2Context) {
     *                 setup.handler.testEvent2(it[0] as Int)
     *             }
     *         }
     *
     *         else -> null
     *     }
     *                             ```
     *
     *
     */
    private fun handleEventMethod(): FunSpec = funSpec("handleEvent") {
        // This overrides GeneratedServerHandler
        addModifiers(KModifier.OVERRIDE, KModifier.SUSPEND)

        addParameter(DispatcherDataParamName, List::class.asClassName().parameterizedBy(wildcardType))
        addParameter(SubscriptionDataParamName, ByteArray::class)
        addParameter(EventParamName, String::class)
        addParameter(SetupParamName, rpcSetup)

        returns(BYTE_ARRAY.copy(nullable = true))
        addControlFlow("return when($EventParamName)") {
            for (method in api.events) {
                addEventHandler(method)
            }
            addCode("else -> null\n")
        }
    }

    /**
     * Generates:
     * ```
     * respond(setup, request, listOf(Int.serializer(), String.serializer()), ListSerializer(Dog.serializer())) {
     *     setup.handler.getDogs(it[0] as Int, it[1] as String)
     * }
     * ```
     */
    private fun FunSpec.Builder.addEndpointHandler(rpc: RpcFunction) {
        addCode("%S -> ".formatWith(rpc.name))


        val arguments = listOf(
            SetupParamName,
            RequestParamName,
            ApiDefinitionUtils.listOfSerializers(rpc),
            rpc.returnType.toSerializerString()
        )

        addControlFlow(respondUtilsMethod.withArgumentList(arguments)) {
            functionHandleCall(rpc)
        }
    }


    private fun FunSpec.Builder.addEventHandler(rpc: RpcEventEndpoint) {
        addCode("%S -> ".formatWith(rpc.name))


        val arguments = listOf(
            SetupParamName,
            SubscriptionDataParamName,
            ApiDefinitionUtils.listOfEventSerializers(rpc),
            rpc.returnType.toSerializerString()
        )

        addControlFlow(transformEventUtilsMethod.withArgumentList(arguments)) {
//            addControlFlow("with(this as %T)", rpc.dispatcherType.typeName) {
            eventTransformCall(rpc)
//            }
        }
    }

    private fun FunSpec.Builder.functionHandleCall(rpc: RpcFunction) {
        addStatement("$SetupParamName.$UserHandlerPropertyName.${rpc.name}".withMethodArguments(functionArguments(rpc)))
    }

    private fun functionArguments(rpc: RpcFunction) = rpc.parameters.mapIndexed { i, arg ->
        "it[$i] as %T".formatWith(arg.type.typeName)
    }

    private fun FunSpec.Builder.eventTransformCall(rpc: RpcEventEndpoint) {
        addStatement("$SetupParamName.$UserHandlerPropertyName.${rpc.name}".withMethodArguments(eventArguments(rpc)))
    }

    private fun eventArguments(rpc: RpcEventEndpoint): List<FormattedString> {
        // We draw from both lists, according to which parameter is a dispatch param and which is an event param.
        var eventIndex = 0
        var dispatchIndex = 0
        return rpc.parameters.map { parameter ->
            val dispatchValue = parameter.isDispatch || parameter.isTarget
            // We use the dispatch value for the @EventTarget value, which allows us to have the real value without serialization.
            val targetList = if (dispatchValue) DispatcherDataParamName else "it"
            val index = if (dispatchValue) dispatchIndex++ else eventIndex++
            "$targetList[$index] as %T".formatWith(parameter.value.type.typeName)
        }
    }




    /**
     * Generates something like this:
     * ```kotlin
     * class GeneratedEventInvokerExample: GeneratedEventInvoker<SmartEventResponder>() {
     *     suspend fun theoreticalGeneratedFunctionTest(title: String, row: AddedRow) {
     *         GeneratedCodeUtils.invokeEvent("test", listOf(row), title, setup)
     *     }
     *
     *     suspend fun theoreticalGeneratedFunctionTest2(row: TestEvent2Context) {
     *         GeneratedCodeUtils.invokeEvent("test2", listOf(row), null, setup)
     *     }
     * }
     * ```
     */
    private fun FileSpec.Builder.addInvokerClass() {
        addClass(invokerName) {

            addPrimaryConstructor {
                addConstructorProperty(SetupParamName, rpcSetup.copy(nullable = true), KModifier.PRIVATE)
            }
//            superclass(GeneratedEventInvoker::class.asClassName().parameterizedBy(apiDefinition.name.kotlinPoet))

            for (event in api.events) {
                addFunction(eventInvoker(event))
            }
        }
    }


    private fun eventInvoker(event: RpcEventEndpoint) = funSpec("invoke${event.name.replaceFirstChar { it.uppercaseChar() }}") {
        val dispatchParameters = event.parameters.filter { it.isDispatch || it.isTarget }.map { it.value }
        addModifiers(KModifier.SUSPEND)
        for (parameter in dispatchParameters) {
            addParameter(parameter.name, parameter.type.typeName)
        }
        //NiceToHave: watched object id

        val targetParameter = event.targetParameter?.name

        // Type inference fails when there are no params so we need to explicitly pass <Nothing> as the type param
        val listOfCall = if (dispatchParameters.isEmpty()) "listOf<Nothing>()"
        else "listOf(${dispatchParameters.joinToString { it.name }})"

        val codeUtilsFunction = if (targetParameter != null) "invokeTargetedEvent" else "invokeEvent"

        addCode(
            "%T.${codeUtilsFunction}(%S, ${listOfCall}, ${SetupParamName}!!, ${targetParameter ?: ""})",
            GeneratedCodeUtils::class,
            event.name
        )
    }
}


