// noinspection PointlessBooleanExpressionJS

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

    addInterface({name, typeParameters}: {name: string, typeParameters?: string[]}, interfaceBuilder: (builder: InterfaceBuilder) => void): CodeBuilder {
        return this._addBlock(`interface ${name}${this.typeParametersString(typeParameters)}`, () => {
            interfaceBuilder(new InterfaceBuilder(this))
        })._addLineOfCode("") // Add empty line
    }

    addClass(name: string, classBuilder: (builder: ClassBuilder) => void): CodeBuilder {
        return this._addBlock(`class ${name}`, () => {
            classBuilder(new ClassBuilder(this))
        })
    }
    addUnionType({name,typeParameters, types}:{name: string, typeParameters?: string[], types: string[]}): CodeBuilder {
        //TODO: handle wrapping
        return this._addLineOfCode(`type ${name}${this.typeParametersString(typeParameters)} = ` + types.join(" | "))
    }

     typeParametersString(params: string[] | undefined) : string {
        if(params === undefined || params.length === 0) return ""
         return `<${params.join(", ")}>`
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

    _addLineOfCode(code: string): CodeBuilder {
        this.code += ("\t".repeat(this.currentIndent) + code + "\n")
        return this
    }


    _addParameterListBlock(prefix: string, list: string[], blockBuilder: () => void): CodeBuilder {
        return this._addBlock(prefix + this.parameterList(this.blockStart(prefix), list), blockBuilder)
    }

    _addFunction(prefix: string, parameters: [string, string][], returnType: string | undefined, body: (body: BodyBuilder) => void): CodeBuilder {
        const returnTypeString = returnType === undefined ? "" : `: ${returnType}`
        const parametersString = this.parameterList(
            this.blockStart(prefix), parameters.map(([name, type]) => `${name}: ${type}`)
        )
        return this._addBlock(prefix + parametersString + returnTypeString, () => {
            body(new BodyBuilder(this))
        })
    }

    _addParameterListLineOfCode(prefix: string, list: string[]): CodeBuilder {
        return this._addLineOfCode(prefix + this.parameterList(prefix, list))
    }

    /**
     * Takes care of wrapping safely
     * @param codeBefore all code in the current line that appears before the parameter list. Must be passed
     * in order to calculate correct wrapping.
     */
    private parameterList(codeBefore: string, list: string[]): string {
        //TODO: handle wrapping
        return "(" + list.join(", ") + ")"
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

    // /**
    //  * Adds <blockName> {
    //  */
    // startBlock(blockName: string): CodeBuilder {
    //     this.addLineOfCode(blockName + " {")
    //     this.indent()
    //     return this
    // }
    //
    // /**
    //  * Adds }
    //  */
    // endBlock(): CodeBuilder {
    //     this.unindent()
    //
    //     return this
    // }


}

type THing<T> = T[] | Record<string, T>


export class InterfaceBuilder {
    protected codegen: CodeBuilder

    constructor(codegen: CodeBuilder) {
        this.codegen = codegen
    }

    addProperty({name, type, optional}: {name: string, type: string, optional?: boolean}): InterfaceBuilder {
        const optionalString = optional === true? "?" : ""
        this.codegen._addLineOfCode(`${name}${optionalString}: ${type}`)
        return this
    }


}

export class ClassBuilder extends InterfaceBuilder {
    constructor(codegen: CodeBuilder) {
        super(codegen)
    }

    addProperty(property: {name: string, type: string, optional?: boolean}): ClassBuilder {
        return super.addProperty(property) as ClassBuilder
    }

    addConstructor(parameterList: [string, string][], body: (builder: BodyBuilder) => void): ClassBuilder {
        this.codegen._addFunction("constructor", parameterList, undefined, body)
        return this
    }

    addFunction(name: string, parameterList: [string, string][], returnType: string | undefined, body: (builder: BodyBuilder) => void) {
        this.codegen._addFunction(name, parameterList, returnType, body)
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

    addReturningFunctionCall(functionName: string, args: string[]): BodyBuilder {
        this.codegen._addParameterListLineOfCode(`return ${functionName}`, args)
        return this
    }

}