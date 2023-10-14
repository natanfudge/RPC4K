package io.github.natanfudge.rpc4k.processor.utils.poet

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.MemberName
import io.github.natanfudge.rpc4k.processor.RpcClass
import io.github.natanfudge.rpc4k.processor.utils.ClassBasedKotlinSerializer
import io.github.natanfudge.rpc4k.processor.utils.KotlinSerializer
import io.github.natanfudge.rpc4k.processor.utils.getKSerializer

/**
 * Represents a string like `"%T.serializer()"` formatted with a value like `Int`.
 * This allows creating utility functions that return both the format [string] and the [formatArguments] for it.
 */
internal data class FormattedString(val string: String, val formatArguments: List<Any>) {

    operator fun plus(other: FormattedString) =
        FormattedString(this.string + other.string, formatArguments + other.formatArguments)


    operator fun plus(string: String) = copy(string = this.string + string)

}

internal fun FunSpec.Builder.addStatement(format: FormattedString) = addStatement(format.string, *format.formatArguments.toTypedArray())
internal fun FunSpec.Builder.addCode(format: FormattedString) = addCode(format.string, *format.formatArguments.toTypedArray())

internal fun String.formatWith(vararg args: Any) = FormattedString(this, args.toList())
internal fun String.formatString() = FormattedString(this, listOf())
internal fun String.plusFormat(format: FormattedString) = FormattedString(this + format.string, format.formatArguments)

internal fun List<FormattedString>.join(separator: String = ", "): FormattedString {
    return FormattedString(
        string = joinToString(separator) { it.string },
        formatArguments = flatMap { it.formatArguments }
    )
}

internal fun MemberName.withArgumentList(arguments: List<Any>): FormattedString {
    return withFormatStringArguments(arguments.map {
        when (it) {
            is FormattedString -> it
            is String -> FormattedString(it, listOf())
            else -> error("Unexpected format argument '$it!'")
        }
    })
}

internal fun FormattedString.withMethodArguments(arguments: List<FormattedString>): FormattedString = this + "(" + arguments.join() + ")"

internal fun MemberName.withFormatStringArguments(arguments: List<FormattedString>): FormattedString = "%M(".formatWith(this) + arguments.join() + ")"

internal fun RpcClass.toSerializerString(): FormattedString {
    return getKSerializer().toSerializerString()
}

private fun KotlinSerializer.toSerializerString(): FormattedString  {
    val withoutNullable = when(this) {
        is ClassBasedKotlinSerializer ->"%T.serializer".formatWith(ClassName.bestGuess(className)).withMethodSerializerArguments(typeArguments)
        is KotlinSerializer.BuiltinToplevel -> MemberName("kotlinx.serialization.builtins", functionName)
            .withSerializerArguments(typeArguments)
    }
    // Add .nullable if needed
    return if (isNullable) {
        withoutNullable + ".nullable"
    } else {
        withoutNullable
    }
}

private fun FormattedString.withMethodSerializerArguments(args: List<KotlinSerializer>) = withMethodArguments(
    args.map { it.toSerializerString() }
)

private fun MemberName.withSerializerArguments(args: List<KotlinSerializer>) = withArgumentList(
    args.map { it.toSerializerString() }
)