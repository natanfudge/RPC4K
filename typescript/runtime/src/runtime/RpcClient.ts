import {Rpc} from "./impl/Rpc";
import {SerializationFormat} from "./SerializationFormat";

export interface RpcClient {
    send(rpc: Rpc, format: SerializationFormat): Promise<Uint8Array>
}