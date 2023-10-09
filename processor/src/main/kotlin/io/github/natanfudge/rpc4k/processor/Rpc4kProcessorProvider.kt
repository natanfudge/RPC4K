package io.github.natanfudge.rpc4k.processor

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.ksp.writeTo
import io.github.natanfudge.rpc4k.processor.old.applyIf
import io.github.natanfudge.rpc4k.processor.utils.*
import io.github.natanfudge.rpc4k.processor.utils.checkRequirement
import io.github.natanfudge.rpc4k.processor.utils.getPublicApiFunctions
import io.github.natanfudge.rpc4k.processor.utils.isSerializable
import io.github.natanfudge.rpc4k.runtime.api.Api
import kotlin.system.measureTimeMillis

internal class Rpc4kProcessorProvider : SymbolProcessorProvider {
    override fun create(
        environment: SymbolProcessorEnvironment
    ): SymbolProcessor = Rpc4kProcessor(environment)
}

internal class Rpc4kProcessor(private val env: SymbolProcessorEnvironment) : SymbolProcessor {
    //TODO: make sure to use the kotlinpoet ksp integration and see stuff is happening incrementally

    private var processed = false

    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (processed) return listOf()
        processed = true

        env.logger.info("Processing @Api")
        val time = measureTimeMillis {
            for (symbol in resolver.getSymbolsWithAnnotation(Api::class.qualifiedName!!)) {
                if (symbol.validate()) {
                    with(env) {
                        // Api is only applicable to classes
                        generateRpc(symbol as KSClassDeclaration)
                    }
                }
            }
        }
        env.logger.warn("Generating RPC Classes took ${time}ms")

        // Nothing needs to be deferred
        return listOf()
    }

    context(SymbolProcessorEnvironment)
    private fun generateRpc(apiClass: KSClassDeclaration) {
        val time = measureTimeMillis {
            apiClass.getPublicApiFunctions().forEach { checkIsSerializable(it) }
            val api = KspToApiDefinition.convert(apiClass)
            ApiDefinitionToClientCode.convert(api).writeTo(codeGenerator, false, listOf(apiClass.containingFile!!))
            ApiDefinitionToServerCode.convert(api).writeTo(codeGenerator, false, listOf(apiClass.containingFile!!))
        }

        env.logger.warn("Generated RPC classes for: ${apiClass.qualifiedName!!.asString()} in $time millis")
    }

    context(SymbolProcessorEnvironment)
    private fun checkIsSerializable(function: KSFunctionDeclaration) {
        for (parameter in function.parameters) {
            checkIsSerializable(parameter.type)
        }
        checkIsSerializable(function.nonNullReturnType())
    }

    context(SymbolProcessorEnvironment)
    private fun checkIsSerializable(type: KSTypeReference, target: KSNode = type, typeArgument: Boolean = false) {
        val resolved = type.resolve()
        target.checkRequirement(env, resolved.isSerializable()) {
            "Type used in API method '${resolved.declaration.qualifiedName!!.asString()}' must be Serializable".appendIf(typeArgument) {" (in type argument of $target)"}
        }
        resolved.arguments.forEach { checkIsSerializable(it.nonNullType(), target = target, typeArgument = true) }
    }
}

