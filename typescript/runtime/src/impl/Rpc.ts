import {SerializationFormat} from "../SerializationFormat";
import {TsSerializer} from "../serialization/TsSerializer";
import {TupleSerializer} from "../serialization/BuiltinSerializers";

export class Rpc {
    readonly method: string;
    readonly methodArgs: unknown[]

    constructor(method: string, args: unknown[]) {
        this.method = method;
        this.methodArgs = args;
        // Check if the method name contains ':'
        if (method.includes(':')) {
            throw new Error(`Method name must not contain ':', but it did: "${method}"`);
        }
    }

    toString(): string {
        return `${this.method}(${this.methodArgs.join(', ')})`;
    }

    /**
     * See docs/rpc_format.png
     */
    toByteArray(format: SerializationFormat, argSerializers: TsSerializer<any>[]): Uint8Array {
        // eslint-disable-next-line prefer-rest-params
        return new Uint8Array([
                ...new TextEncoder().encode(this.method),
                ColonCode,
                ...format.encode(new TupleSerializer(argSerializers), this.methodArgs)
            ]
        );
    }
}

const ColonCode = 58 // :