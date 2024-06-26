import {generateModels} from "./ModelGenerator"
import * as fs from "fs";
import {generateAccessor} from "./ClientAccessorGenerator"
import {fillDefaultApiDefinitionValues} from "./ApiDefinitionsDefaults"
import {generateSerializers} from "./SerializerGenerator"
import {ApiDefinition} from "./ApiDefinition";


export interface Rpc4TsClientGenerationOptions {
    /**
     * True only for tests in rpc4ts itself. Makes it so imports use a relative path instead of a package path.
     */
    localLibPaths: boolean
}

export function generateClientModel(definitionJson: string, writeTo: string, options: Rpc4TsClientGenerationOptions) {
    // return
    const startTime = new Date().getTime()
    // This value doesn't contain default values
    let rawApi: ApiDefinition
    try {
        rawApi = JSON.parse(definitionJson)
    } catch (e) {
        throw Error("Invalid json file provided for definition of API")
    }
    let api: ApiDefinition
    try {
        api = fillDefaultApiDefinitionValues(rawApi)
    } catch (e) {
        console.error(`Could not fill default values for given RPC definition: ${definitionJson}`)
        throw "Test"
        // throw e
    }
    const models = generateModels(api.models, api.name, options)
    const accessor = generateAccessor(api, rawApi, options)
    const serializers = generateSerializers(api.models, options, api.name)

    fs.mkdirSync(writeTo, {recursive: true})
    fs.writeFileSync(writeTo + `/rpc4ts_${api.name}Models.ts`, models)
    fs.writeFileSync(writeTo + `/rpc4ts_${api.name}Api.ts`, accessor)
    fs.writeFileSync(writeTo + `/rpc4ts_${api.name}Serializers.ts`, serializers)
    // const runtimeModelsName = `${api.name}RuntimeModels`
    // We write out a definition without the default values because it's easy to resolve them at runtime
    // fs.writeFileSync(writeTo + `${runtimeModelsName}.ts`, `export const ${runtimeModelsName} = \`${JSON.stringify(rawApi.models)}\``)
    const timePassed = new Date().getTime() - startTime
    console.log(`Wrote ${api.models.length} models and ${api.methods.length} endpoints for '${api.name}' in ${timePassed}ms`)
}

