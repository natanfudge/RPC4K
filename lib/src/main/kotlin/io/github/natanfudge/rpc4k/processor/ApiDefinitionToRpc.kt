package io.github.natanfudge.rpc4k.processor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.KSFile
import io.github.natanfudge.rpc4k.processor.utils.isBuiltinSerializableType
import io.github.natanfudge.rpc4k.processor.utils.writeFile
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


object ApiDefinitionToRpc {
    private val json = Json
//    fun writeRpcJson(definition: ApiDefinition, codeGenerator: CodeGenerator, containingFile: KSFile) {
//        codeGenerator.createNewFile(
//            packageName = "", dependencies = Dependencies(aggregating = false, containingFile), fileName = "${definition.name}Api", extensionName = "rpc.json"
//        ).use { it.write(json.encodeToString(definition).toByteArray())  }
//    }

    /**
     * We write down all the models the API uses so another process can generate the bindings in other languages for the models.
     * Unfortunately we can't do kotlin to typescript conversion in ksp because that requires actually compiling and running the project,
     * and ksp happens during compilation.
     */
    fun writeModelList(definition: ApiDefinition, codeGenerator: CodeGenerator, containingFile: KSFile) {
        val models = json.encodeToString(getAllModels(definition))
        codeGenerator.writeFile(models, Dependencies(aggregating = false, containingFile),"rpc4k/models/" + definition.name, extensionName = "models.txt")
    }

    private fun getAllModels(definition: ApiDefinition): Set<RpcType> {
        val set = hashSetOf<RpcType>()
        for (method in definition.methods) {
            addAllModels(method.returnType, set)
            for (arg in method.args) {
                addAllModels(arg.type, set)
            }
        }
        return set
    }

    private fun addAllModels(type: RpcType, set: HashSet<RpcType>) {
        if (!type.isBuiltinSerializableType()) {
            // JVM uses $ for inner class identification
            set.add(type)
        }
    }

}
