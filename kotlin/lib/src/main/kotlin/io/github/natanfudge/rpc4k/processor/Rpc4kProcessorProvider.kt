package io.github.natanfudge.rpc4k.processor

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.ksp.writeTo
import io.github.natanfudge.rpc4k.processor.utils.checkRequirement
import io.github.natanfudge.rpc4k.processor.utils.findDuplicate
import io.github.natanfudge.rpc4k.processor.utils.getClassesWithAnnotation
import io.github.natanfudge.rpc4k.runtime.api.Api
import kotlin.system.measureTimeMillis

internal class Rpc4kProcessorProvider : SymbolProcessorProvider {
    override fun create(
        environment: SymbolProcessorEnvironment
    ): SymbolProcessor = Rpc4kProcessor(environment)
}

internal class Rpc4kProcessor(private val env: SymbolProcessorEnvironment) : SymbolProcessor {
    private var processed = false
    private val validator = ApiClassValidator(env)

    override fun process(resolver: Resolver): List<KSAnnotated> = with(env) {
        if (processed) return listOf()
        processed = true

        env.logger.info("Processing @Api")
        val time = measureTimeMillis {
            val apiClasses = resolver.getClassesWithAnnotation(Api::class)
                .filter { validator.validate(it) }
                .toHashSet()

            for (symbol in apiClasses) {
                generateRpc(symbol)
            }
        }
        env.logger.warn("Generating RPC Classes took ${time}ms")

        // Nothing needs to be deferred
        return listOf()
    }


    context(SymbolProcessorEnvironment)
    private fun generateRpc(apiClass: KSClassDeclaration) {
        val time = measureTimeMillis {
            val api = KspToApiDefinition.toApiDefinition(apiClass)

            val nameDuplicate = api.models.findDuplicate { it.name }

            // Since models are not namespaced, they cannot contain duplicate names
            apiClass.checkRequirement(env, nameDuplicate == null) { "There's two types with the name '$nameDuplicate', which is not allowed" }
            if (nameDuplicate != null) return

            val file = apiClass.containingFile!!
            val files = listOf(file)
            if (apiClass.shouldGenerateClient()) {
                ApiDefinitionToClientCode.convert(api, userClassIsInterface = apiClass.classKind == ClassKind.INTERFACE)
                    .writeTo(codeGenerator, false, files)
            }
            ApiDefinitionToServerCode.convert(api).writeTo(codeGenerator, false, files)
            ApiDefinitionToRpc.writeRpcJsons(api, codeGenerator, file)
        }

        env.logger.warn("Generated RPC classes for: ${apiClass.qualifiedName!!.asString()} in $time millis")
    }

}

fun KSClassDeclaration.shouldGenerateClient(): Boolean {
    // Checks if the @Api annotation has the only argument (generateClient) set to true
    return annotations.first { it.shortName.asString() == Api::class.simpleName }.arguments[0].value == true
}