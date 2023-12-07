// noinspection PointlessBooleanExpressionJS

const MaxLineLength = 120;
const tabWidth = 4;

/**
 * Minimalist and ultra simple code generator based on appending to a string
 */
export class CodeBuilder {
    private code = ""
    private currentIndent = 0

    build() {
        if (this.currentIndent !== 0) {
            throw new Error(`${this.currentIndent} indents were not unindented`)
        }
        return this.code
    }

    //TODO: introduce a new import architecture that doesn't import unnecessary stuff, i think use the same way that kotlinpoet does it
    addImport(identifiers: string[], path: string): CodeBuilder {
        const identifiersJoined = identifiers.join(", ")
        const unwrappedLine = `import {${identifiersJoined}} from "${path}"`
        if (this.isTooLong(unwrappedLine.length)) {
            return this._addLineOfCode(`import {\n${this.indentList(identifiers).join(",\n")}\n} from "${path}"`)
        } else {
            return this._addLineOfCode(unwrappedLine)
        }
    }

    addInterface({name, typeParameters}: {
        name: string,
        typeParameters?: string[]
    }, interfaceBuilder: (builder: InterfaceBuilder) => void): CodeBuilder {
        return this._addBlock(`export interface ${name}${this.typeParametersString(typeParameters)}`, () => {
            interfaceBuilder(new InterfaceBuilder(this))
        })._addLineOfCode("") // Add empty line
    }

    addClass({name, typeParameters}: {
        name: string,
        typeParameters?: string[]
    }, classBuilder: (builder: ClassBuilder) => void): CodeBuilder {
        return this._addBlock(`export class ${name}${this.typeParametersString(typeParameters)}`, () => {
            classBuilder(new ClassBuilder(this))
        })._addLineOfCode("")// Add empty line
    }

    addUnionType({name, typeParameters, types}: {
        name: string,
        typeParameters?: string[],
        types: string[]
    }): CodeBuilder {
        const prefix = `export type ${name}${this.typeParametersString(typeParameters)} = `
        const typesJoined = types.length === 0 ? "never" : types.join(" | ")
        if (this.isTooLong(prefix.length + typesJoined.length)) {
            return this._addLineOfCode(prefix + this.indentList(types).join(" |\n"))
        } else {
            return this._addLineOfCode(prefix + typesJoined)
        }
    }

    addTypeAlias({name, typeParameters, type}: {
        name: string,
        typeParameters?: string[],
        type: string
    }): CodeBuilder {
        const prefix = `export type ${name}${this.typeParametersString(typeParameters)} = `
        return this._addLineOfCode(prefix + type)
    }

    typeParametersString(params: string[] | undefined): string {
        if (params === undefined || params.length === 0) return ""
        return `<${params.join(", ")}>`
    }

    addConst(name: string, value: string): CodeBuilder {
        return this._addLineOfCode(`export const ${name} = ${value}`)
    }

    addTopLevelFunction(name: string, parameters: [string, string][], returnType: string | undefined, body: (body: BodyBuilder) => void): CodeBuilder {
        return this._addFunction(`export function ${name}`, parameters, returnType, body)
    }

    ///////////////////// Internal ////////////////

    _indent(): CodeBuilder {
        this.currentIndent++
        return this
    }

    _unindent(): CodeBuilder {
        if (this.currentIndent === 0) {
            throw new Error("Unindented more indent than exists")
        }
        this.currentIndent--
        return this
    }

    _addLineOfCode(code: string, addNewline: boolean = true): CodeBuilder {
        this.code += ("\t".repeat(this.currentIndent) + code )
        if(addNewline) this.code += "\n"
        return this
    }

    _addCode(code: string): CodeBuilder {
        this.code += code
        return this
    }

    _addFunction(name: string, parameters: [string, string][], returnType: string | undefined, body: (body: BodyBuilder) => void): CodeBuilder {
        const returnTypeString = returnType === undefined ? "" : `: ${returnType}`
        //TODO: implement optional parameters
        const parametersString = this.parameterList(
            this.blockStart(name).length + returnTypeString.length, parameters.map(([name, type]) => `${name}: ${type}`)
        )
        return this._addBlock(name + parametersString + returnTypeString, () => {
            body(new BodyBuilder(this))
        })
    }

    _addParameterListLineOfCode(prefix: string, list: string[], addNewline: boolean = true): CodeBuilder {
        return this._addLineOfCode(prefix + this.parameterList(prefix.length, list), addNewline)
    }

    /**
     * Takes care of wrapping safely
     * @param otherCodeLength the amount of characters that this line contains other than the parameter list.
     * Required for knowing whether to wrap parameters.
     */
    private parameterList(otherCodeLength: number, list: string[]): string {
        const joined = "(" + list.join(", ") + ")"
        if (this.isTooLong(otherCodeLength + joined.length)) {
            // Chop argument list into separate lines if it's too long
            return "(\n" + this.indentList(list).join(",\n") + "\n" + this.indent(")")
        } else {
            return joined
        }
    }

    private isTooLong(codeLength: number): boolean {
        return this.getLineLength(codeLength) > MaxLineLength
    }

    private getLineLength(codeLength: number): number {
        return this.currentIndent * tabWidth + codeLength
    }

    private indentList(list: string[]): string[] {
        return list.map(s => "\t".repeat(this.currentIndent + 1) + s)
    }

    private indent(str: string): string {
        return "\t".repeat(this.currentIndent) + str
    }


    _addBlock(blockName: string, blockBuilder: () => void): CodeBuilder {
        this._addLineOfCode(this.blockStart(blockName))
        this._indent()
        blockBuilder()
        this._unindent()
        return this._addLineOfCode("}")
    }

    private blockStart(prefix: string) {
        return `${prefix} {`
    }
}


export class InterfaceBuilder {
    protected codegen: CodeBuilder

    constructor(codegen: CodeBuilder) {
        this.codegen = codegen
    }

    addProperty({name, type, optional, initializer}: { name: string, type?: string, optional?: boolean, initializer?: string }): InterfaceBuilder {
        const optionalString = optional === true ? "?" : ""
        const initializerString = initializer === undefined ? "" : ` = ${initializer}`
        const typeHint: string = type !== undefined ? `: ${type}` : ""
        this.codegen._addLineOfCode(`${name}${optionalString}${typeHint}${initializerString}`)
        return this
    }


}

export class ClassBuilder extends InterfaceBuilder {
    constructor(codegen: CodeBuilder) {
        super(codegen)
    }

    addProperty(property: { name: string, type?: string, optional?: boolean, initializer?: string }): ClassBuilder {
        return super.addProperty(property) as ClassBuilder
    }

    addConstructor(parameterList: [string, string][], body: (builder: BodyBuilder) => void): ClassBuilder {
        return this.addFunction("constructor", parameterList, undefined, body)
    }

    addFunction(name: string, parameterList: [string, string][], returnType: string | undefined, body: (builder: BodyBuilder) => void) {
        this.codegen._addFunction(name, parameterList, returnType, body)
        return this
    }

    addNewline(): ClassBuilder {
        this.codegen._addLineOfCode("")
        return this
    }

    addComment(comment: string): ClassBuilder {
        this.codegen._addLineOfCode(`// ${comment}`)
        return this
    }

}

export class BodyBuilder {
    protected codegen: CodeBuilder

    constructor(codegen: CodeBuilder) {
        this.codegen = codegen
    }

    addAssignment(variable: string, value: string): BodyBuilder {
        this.codegen._addLineOfCode(`${variable} = ${value}`)
        return this
    }
    addEmptyLine() :BodyBuilder {
        this.codegen._addLineOfCode("")
        return this
    }

    addReturningFunctionCall(functionName: string, args: string[], addNewline: boolean = true): BodyBuilder {
        this.codegen._addParameterListLineOfCode(`return ${functionName}`, args, addNewline)
        return this
    }

    addCast(type: string): BodyBuilder {
        this.codegen._addLineOfCode(`as ${type}`, false)
        return this
    }

}

//     addReturningFunctionCall(functionName: string, args: string[], chaining: (builder: FunctionChainingBuilder) => void = () => {}): FunctionChainingBuilder {
//         const builder = new FunctionChainingBuilder(this.codegen)
//         chaining(builder)
//         this.codegen._addParameterListLineOfCode(`return ${functionName}`, args, builder.build())
//         return new FunctionChainingBuilder(this.codegen)
//     }
// export class FunctionChainingBuilder {
//     private chains: string[] = []
//
//     constructor(codegen: CodeBuilder) {
//     }
//
//     addCast(type: string): BodyBuilder {
//         this.chains.push(`as ${type}`)
//     }
//
//     build(): string {
//
//     }
//
// }