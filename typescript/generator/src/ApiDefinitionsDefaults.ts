import {ApiDefinition, RpcDefinition, RpcEventEndpoint, RpcModel, RpcModelKind} from "./ApiDefinition";
import {createRpcType} from "./RpcTypeUtils";


export function fillDefaultApiDefinitionValues(partialDefinition: ApiDefinition): ApiDefinition {
    // No simpler way to do this other than iterate through the entire object i think
    return {
        name: partialDefinition.name,
        models: partialDefinition.models.map(model => fillDefaultModelValues(model)),
        methods: partialDefinition.methods.map(method => fillDefaultMethodValues(method)),
        events: partialDefinition.events.map(event => fillDefaultEventValues(event))
    }
}

export function fillDefaultModelValues(partialModel: RpcModel): RpcModel {
    switch (partialModel.type) {
        case RpcModelKind.enum:
            return partialModel
        case RpcModelKind.struct: {
            const {properties, typeParameters, ...other} = partialModel
            return {
                properties: properties.map(({name, type, isOptional}) => {
                    return ({name, type: createRpcType(type), isOptional: isOptional ?? false})
                }),
                // Default for typeParameters is []
                typeParameters: typeParameters ?? [],
                ...other
            }
        }
        case RpcModelKind.union: {
            const {options, typeParameters, ...other} = partialModel
            return {
                options: options.map(value => createRpcType(value)),
                // Default for typeParameters is []
                typeParameters: typeParameters ?? [],
                ...other
            }
        }
        case RpcModelKind.inline : {
            const {inlinedType, typeParameters, ...other} = partialModel
            return {
                inlinedType: createRpcType(inlinedType),
                // Default for typeParameters is []
                typeParameters: typeParameters ?? [],
                ...other
            }
        }

    }
}


export function fillDefaultMethodValues(partialMethod: RpcDefinition): RpcDefinition {
    return {
        name: partialMethod.name,
        parameters: partialMethod.parameters.map(param => ({name: param.name, type: createRpcType(param.type)})),
        returnType: createRpcType(partialMethod.returnType)
    }
}


export function fillDefaultEventValues(partialMethod: RpcEventEndpoint): RpcEventEndpoint {
    return {
        name: partialMethod.name,
        parameters: partialMethod.parameters.map(param => ({isDispatch: param.isDispatch, isTarget: param.isTarget, value: ({name: param.value.name, type: createRpcType(param.value.type)})})),
        returnType: createRpcType(partialMethod.returnType)
    }
}


