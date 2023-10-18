import {CodeBuilder} from "./CodeBuilder";
import {isBuiltinType, typescriptRpcType} from "./TypescriptRpcType";
import {Rpc4TsClientGenerationOptions} from "./ClientGenerator";
import {ApiDefinition, RpcParameter, RpcType, RpcTypeNames} from "../runtime/ApiDefinition";
import {stripDefaultTypeValues} from "../runtime/impl/ApiDefinitionsDefaults";

export function generateAccessor(api: ApiDefinition, options: Rpc4TsClientGenerationOptions): string {
    const builder = new CodeBuilder()

    function libraryPath(path: string): string {
        if (options.localLibPaths) return `../../src/runtime/${path}`
        else {
            throw new Error("Not implemented yet - need to see what the file structure is")
        }
    }

    builder.addImport(["RpcClient"], libraryPath("RpcClient"))
        .addImport(["SerializationFormat"], libraryPath("SerializationFormat"))
        .addImport(["GeneratedCodeUtils"], libraryPath("impl/GeneratedCodeUtils"))
        .addImport(["Rpc4aTypeAdapter"], libraryPath("impl/Rpc4aTypeAdapter"))
        .addImport(getReferencedGeneratedTypeNames(api), `./${api.name}Models`)
        .addImport(["UserProtocolRuntimeModels"], `./${api.name}RuntimeModels`)

    builder.addClass(`${api.name}Api`, (clazz) => {
        clazz.addProperty({name: "private readonly client", type: "RpcClient"})
            .addProperty({name: "private readonly format", type: "SerializationFormat"})
            .addProperty({
                name: "private readonly adapter",
                type: "Rpc4aTypeAdapter",
                initializer: "GeneratedCodeUtils.createTypeAdapter(UserProtocolRuntimeModels)"
            })
            .addConstructor([["client", "RpcClient"], ["format", "SerializationFormat"]], constructor => {
                constructor.addAssignment("this.client", "client")
                    .addAssignment("this.format", "format")
            })

        for (const method of api.methods) {
            // We wrap the return type with a promise because api methods are network calls
            const returnType: RpcType = {
                name: "Promise",
                isNullable: false,
                typeArguments: [method.returnType],
                isTypeParameter: false,
                inlinedType: undefined
            }
            clazz.addFunction(
                method.name,
                method.parameters.map(param => [param.name, typescriptRpcType(param.type)]),
                typescriptRpcType(returnType),
                (body) => {
                    const args = [
                        "this.client", "this.format", "this.adapter", `"${method.name}"`,
                        arrayLiteral(method.parameters.map(param => param.name)),
                        // Get rid of default values to be the type that is manually inputted be shorter
                        arrayLiteral(method.parameters.map(param => stringifyToJsObject(stripDefaultTypeValues(param.type))))
                    ]
                    if (method.returnType.name !== RpcTypeNames.Void) {
                        // Add return type if it's not null
                        args.push(stringifyToJsObject(stripDefaultTypeValues(method.returnType)))
                    }
                    body.addReturningFunctionCall(
                        "GeneratedCodeUtils.request",
                        // Add all the parameters as a trailing argument to GeneratedCodeUtils.request
                        args
                    )
                })
        }
    })

    return builder.build()
}

function stringifyToJsObject(obj: unknown): string {
    return JSON.stringify(obj).replace(/"(\w+)"\s*:/g, '$1:')
}

function arrayLiteral(list: string[]): string {
    return "[" + list.join(", ") + "]"
}

/**
 * Gets all the custom generated classes that the api client accessor referenced so we can import them
 */
function getReferencedGeneratedTypeNames(api: ApiDefinition): string[] {
    const names: Set<string> = new Set()
    for (const method of api.methods) {
        for (const param of method.parameters) {
            addReferencedGeneratedTypeNames(param.type, names)
        }
        addReferencedGeneratedTypeNames(method.returnType, names)
    }
    return Array.from(names)
}

function addReferencedGeneratedTypeNames(type: RpcType, addTo: Set<string>) {
    if (type.inlinedType !== undefined) {
        addReferencedGeneratedTypeNames(type.inlinedType, addTo)
    } else {
        if (!isBuiltinType(type)) {
            addTo.add(type.name)
        }
        for (const typeArgument of type.typeArguments) {
            addReferencedGeneratedTypeNames(typeArgument, addTo)
        }
    }
}
