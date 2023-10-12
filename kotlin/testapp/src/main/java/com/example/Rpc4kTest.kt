package com.example

import com.example.EnumArgs.*
import io.github.natanfudge.rpc4k.runtime.api.ApiClient
import io.github.natanfudge.rpc4k.runtime.api.ApiServer
import kotlinx.serialization.Serializable

@Serializable
data class PlayerId(val num: Long)

@Serializable
data class CreateLobbyResponse(val id: Long)

@ApiClient
@ApiServer
open class SimpleProtocol {
    companion object;
//    open suspend fun foo(thing: Int): Flow<Int> {
//        return flowOf(1 + thing, 2, 3)
//    }

    open suspend fun bar(thing: Int): Int {
        return thing + 1
    }
}

@ApiClient
@ApiServer
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


    //TODO
    open suspend fun polymorphicTest(obj: PolymorphicThing): PolymorphicThing {
        return obj
    }

    //TODO
    open suspend fun directPolymorphicAccess(obj: PolymorphicThing.Option1): PolymorphicThing.Option1 {
        return obj
    }

    //TODO
    open suspend fun polymorphicClassTest(obj: PolymorphicClass): PolymorphicClass {
        return obj
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