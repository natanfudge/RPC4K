import {RpcType} from "rpc4ts-runtime";
import {tsReferenceToString, TsType, TsTypes} from "./codegen/FormatString"
import {RpcTypeNames} from "./RpcTypeUtils";

/**
 * Requires the service name to resolve import paths of API models
 */
export function typescriptRpcType(type: RpcType, serviceName: string): TsType {
    // if (type.inlinedType !== undefined) return typescriptRpcType(type.inlinedType)
    // Handle | null adding
    const withoutNull = typescriptRpcTypeIgnoreOptional(type, serviceName)
    if (type.isNullable) {
        return TsTypes.nullable(withoutNull)
    } else {
        return withoutNull
    }

    // const nullableString = type.isNullable ? " | null" : ""
    // // Handle other things
    // return typescriptRpcTypeIgnoreOptional(type) + nullableString
}

// We keep this constant for now to make things simpler

function typescriptRpcTypeIgnoreOptional(type: RpcType, serviceName: string): TsType {
    // If it's a type parameter we don't care if it's a builtin type, we treat it as a type parameter.
    if (type.isTypeParameter) return TsTypes.typeParameter(type.name)
    const builtinType = resolveBuiltinType(type, serviceName)
    if (builtinType !== undefined) return builtinType
    const typeArguments = type.typeArguments.map(arg => typescriptRpcType(arg, serviceName))
    // const typeArgumentString = type.typeArguments.length === 0 ? ""
    //     : `<${).join(", ")}>`

    return modelType(type.name, serviceName, typeArguments) /*TsTypes.create(modelName(type.name), ModelFile, ...typeArguments)*/

    // return modelName(type.name) + typeArgumentString
}

export function isBuiltinType(type: RpcType, serviceName: string): boolean {
    return resolveBuiltinType(type, serviceName) !== undefined
}

function resolveBuiltinType(type: RpcType, serviceName: string): TsType | undefined {
    switch (type.name) {
        case "bool" :
            return TsTypes.BOOLEAN
        case "i8":
        case "i16":
        case "i32":
        case "i64":
        case "f32":
        case "f64":
            return TsTypes.NUMBER
        case "char":
        case "string":
        case "uuid":
            return TsTypes.STRING
        case "duration":
            // Durations are Dayjs.Duration in typescript
            return TsTypes.DURATION
        case "i8array":
            return TsTypes.i8Array
        case "ui8array":
            return TsTypes.ui8Array
        case  RpcTypeNames.Time:
            // Dates are Dayjs in typescript
            return TsTypes.DAYJS
        case RpcTypeNames.Arr: {
            const typeArgs = type.typeArguments
            if (typeArgs.length !== 1) {
                throw new Error(`Array type had an unexpected amount of type arguments: ${typeArgs.length}`)
            }

            const elementType = typeArgs[0]
            const elementTypeReference = typescriptRpcType(elementType, serviceName)

            return TsTypes.array(elementTypeReference)


            // // Add brackets in case the element type is nullable to avoid ambiguity
            // const elementTypeStringWithBrackets = elementType.isNullable ? `(${elementTypeReference})` : elementTypeReference
            // // Typescript arrays are T[]
            // return `${elementTypeStringWithBrackets}[]`
        }
        case RpcTypeNames.Rec : {
            const typeArgs = type.typeArguments
            if (typeArgs.length !== 2) {
                throw new Error(`Record type had an unexpected amount of type arguments: ${typeArgs.length}`)
            }
            const keyType = typescriptRpcType(typeArgs[0], serviceName)
            const underlyingKeyType = typescriptRpcType(resolveToUnderlying(typeArgs[0]), serviceName)
            if (underlyingKeyType !== TsTypes.STRING && underlyingKeyType !== TsTypes.NUMBER) {
                // NiceToHave: Support complex keys in Typescript
                throw new Error(`Unsupported map key type: ${tsReferenceToString(keyType)} in type: ${JSON.stringify(type)}`)
            }
            const valueType = typescriptRpcType(typeArgs[1], serviceName)
            return TsTypes.record(keyType, valueType)
            // Typescript Records are Record<K,V>
            // return `Record<${keyType}, ${valueType}>`
        }
        case RpcTypeNames.Tuple: {
            return TsTypes.tuple(type.typeArguments.map(arg => typescriptRpcType(arg, serviceName)))
            // Typescript tuples are [T1, T2, ..., Tn]
            // return `[${.join(", ")}]`
        }
        case RpcTypeNames.Void:
            return TsTypes.VOID
        default:
            return undefined
    }
}

function resolveToUnderlying(type: RpcType): RpcType {
    if (type.inlinedType !== undefined) {
        return resolveToUnderlying(type.inlinedType)
    } else {
        return type
    }
}

/**
 * Converts the Rpc representation of a struct name to the typescript representation
 */
export function modelName(name: string): string {
    // Treat "Foo.Bar" as "FooBar"
    return name.replace(/\./g, "")
}

/**
 * Converts the Rpc representation of a struct name to the typescript representation
 */
export function modelType(name: string, serviceName: string, typeArguments?: TsType[]): TsType {
    // Treat "Foo.Bar" as "FooBar"
    // const withoutDot = name.replace(/\./g, "")
    return TsTypes.create(modelName(name), MODELS_FILE(serviceName), ...(typeArguments ?? []))
}

export function MODELS_FILE(serviceName: string): string {
    return `./rpc4ts_${serviceName}Models`
}