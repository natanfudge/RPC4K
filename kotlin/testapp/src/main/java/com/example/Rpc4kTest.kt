package com.example

import com.example.EnumArgs.*
import io.github.natanfudge.rpc4k.runtime.api.Api
import kotlinx.serialization.Serializable

@Serializable
data class PlayerId(val num: Long)

@Serializable
data class CreateLobbyResponse(val id: Long)

@Api(true)
open class SimpleProtocol {
    companion object;
//    open suspend fun foo(thing: Int): Flow<Int> {
//        return flowOf(1 + thing, 2, 3)
//    }

    open suspend fun bar(thing: Int): Int {
        return thing + 1
    }
}

@Api(true)
open class UserProtocol {

    companion object {
        fun distraction1() {}
        val distraction2: String = ""

    }

    val distraction3 = 2
    private fun distraction4() {}

    class Distraction5

//    open suspend fun starTest( x: List<*>) {
//
//    }

    open suspend fun createLobby(createdBy: PlayerId, otherThing: String): CreateLobbyResponse {
        println("Handled createlobby! $createdBy")
        return CreateLobbyResponse(createdBy.num + otherThing.length)
    }

    open suspend fun killSomeone(killer: Int, shit: PlayerId, bar: Unit): UInt {
        return (killer + shit.num).toUInt()
    }

    open suspend fun someShit(x: Int, y: Int): String {
        println("asdf")
        println("asdf")
        println("asdf")
        println("asdf")
        println("asdf")
        println("asdf")
        return "asdf"
    }

    open suspend fun moreTypes(
        list: List<PlayerId>,
        double: List<Set<String>>,
        pair: Pair<Int, Long>,
        triple: Triple<Unit, PlayerId, String>,
        entry: Map.Entry<Int, Int>
    ): Map<Long, Map<Set<List<PlayerId>>, Double>> {
        return mapOf()
    }

    open suspend fun test(
        pair: Pair<Int, Long>,
    ): Pair<Triple<Int, Int, String>, Double> {
        return Triple(1, 2, "3") to 4.0
    }

    open suspend fun nullable(mayNull: List<PlayerId>?, mayNull2: List<PlayerId?>) {

    }

    @Serializable
    enum class HeavyNullableTestMode {
        EntirelyNull,
        NullList,
        NullString
    }

    open suspend fun heavyNullable(mode: HeavyNullableTestMode): GenericThing<List<String?>?, List<String>?, List<String?>>? {
        return when (mode) {
            HeavyNullableTestMode.EntirelyNull -> null
            HeavyNullableTestMode.NullList -> GenericThing(null, null, listOf())
            HeavyNullableTestMode.NullString -> GenericThing(listOf(null, "test"), null, listOf())
        }
    }

//    open suspend fun flowTest(thing: Int): Flow<List<PlayerId>?> {
//        return flowOf(listOf(PlayerId(thing.toLong())))
//    }
//
//    open suspend fun sharedFlowTest(thing: Int): Flow<List<PlayerId>?> {
//        val flow = flowOf(listOf(PlayerId(thing.toLong())))
//        return flow.stateIn(CoroutineScope(currentCoroutineContext()))
//    }

    open suspend fun genericTest(thing: String): GenericThing<String, Int, Long> {
        return GenericThing("", 2, 3)
    }

    open suspend fun errorTest() {
        throw Exception("")
    }

    open suspend fun requirementTest() {
        // Literally 1984
        require(4 == 5)
    }

    open suspend fun noArgTest() {
        println("Halo")
    }

    open suspend fun requirementFail(value: Int) {
        require(value == 2) { "Value must be 2" }
    }


    open suspend fun withNullsTest(withNulls: WithNulls<String>): WithNulls<Int> {
        return WithNulls(x = listOf(1, null), y = withNulls.y)
    }

    open suspend fun enumArgsTest(enumArgs: EnumArgs): EnumArgs {
        return when (enumArgs) {
            Option1 -> Option5
            Option5 -> Option1
        }
    }

    open suspend fun directObjectTest(obj: TheObject): TheObject {
        return obj
    }


    open suspend fun polymorphicTest(obj: PolymorphicThing): PolymorphicThing {
        return obj
    }

    open suspend fun directPolymorphicAccess(obj: PolymorphicThing.Option1): PolymorphicThing.Option1 {
        return obj
    }

    open suspend fun polymorphicClassTest(obj: PolymorphicClass): PolymorphicClass {
        return obj
    }

    open suspend fun everyBuiltinType(obj: EveryBuiltinType): EveryBuiltinType {
        return obj
    }

    open suspend fun everyBuiltinTypeParams(
         a: Boolean,
         b: Byte,
         c: Short,
         d: Int,
         e: Long,
         f: Char,
         g: String,
         h: ByteArray,
         i: ShortArray,
         j: IntArray,
         k: LongArray,
         l: CharArray,
         m: List<Int>,
         n: Map<Int,Int>,
         o: Set<Int>,
         p: Pair<Int,Int>,
         q: Triple<Int,Int,Int>,
         r: Unit
    ): Triple<Int,Int,Int> {
        return q
    }

}


@Serializable
data class EveryBuiltinType(
    val a: Boolean,
    val b: Byte,
    val c: Short,
    val d: Int,
    val e: Long,
    val f: Char,
    val g: String,
    val h: ByteArray,
    val i: ShortArray,
    val j: IntArray,
    val k: LongArray,
    val l: CharArray,
    val m: List<Int>,
    val n: Map<Int,Int>,
    val o: Set<Int>,
    val p: Pair<Int,Int>,
    val q: Triple<Int,Int,Int>,
    val r: Unit
) {
    @Suppress("DuplicatedCode")
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EveryBuiltinType

        if (a != other.a) return false
        if (b != other.b) return false
        if (c != other.c) return false
        if (d != other.d) return false
        if (e != other.e) return false
        if (f != other.f) return false
        if (g != other.g) return false
        if (!h.contentEquals(other.h)) return false
        if (!i.contentEquals(other.i)) return false
        if (!j.contentEquals(other.j)) return false
        if (!k.contentEquals(other.k)) return false
        if (!l.contentEquals(other.l)) return false
        if (m != other.m) return false
        if (n != other.n) return false
        if (o != other.o) return false
        if (p != other.p) return false
        if (q != other.q) return false

        return true
    }

    override fun hashCode(): Int {
        var result = a.hashCode()
        result = 31 * result + b
        result = 31 * result + c
        result = 31 * result + d
        result = 31 * result + e.hashCode()
        result = 31 * result + f.hashCode()
        result = 31 * result + g.hashCode()
        result = 31 * result + h.contentHashCode()
        result = 31 * result + i.contentHashCode()
        result = 31 * result + j.contentHashCode()
        result = 31 * result + k.contentHashCode()
        result = 31 * result + l.contentHashCode()
        result = 31 * result + m.hashCode()
        result = 31 * result + n.hashCode()
        result = 31 * result + o.hashCode()
        result = 31 * result + p.hashCode()
        result = 31 * result + q.hashCode()
        result = 31 * result + r.hashCode()
        return result
    }
}


@Serializable
enum class EnumArgs(val x: Int) {
    Option1(3),
    Option5(8)
}

//TODO: data enums?
//TODO: what happens when you have objects?
//TODO: what about polymorphic things?

@Serializable
object TheObject

@Serializable
sealed interface PolymorphicThing {
    @Serializable
    data class Option1(val x: Int) : PolymorphicThing

    @Serializable
    data object Option2 : PolymorphicThing
}

@Serializable
sealed class PolymorphicClass {
    @Serializable
    data class Option4(val x: Int) : PolymorphicClass()

    @Serializable
    data object Option3 : PolymorphicClass()
}


@Serializable
data class GenericThing<T1, T2, T3>(val x: T1, val y: T2, val z: T3, val w: List<T3> = listOf())

@Serializable
data class WithNulls<T>(val x: List<T?>, val y: String?)