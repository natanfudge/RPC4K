import {Json} from "../../src/serialization/json/Json";
import {NumberSerializer, StringSerializer} from "../../src/serialization/BuiltinSerializers";
import {Rpc4tsSerializers, TestEnum, TestUnion, Tree} from "./TestModels";

test("Test fromString", () => {
    const string = `{"x":1,"y":"hello","z":false}`
    const obj = ({x: 1, y: "hello", z: false})
    const json = new Json()

    const res = json.decodeFromString(Rpc4tsSerializers.foo(), string)

    expect(res).toEqual(obj)

    console.log(res)
})


test("Test GenericThing serialization from string", () => {
    const string = `{"x":1,"w":["Asdf"],"a":"3"}`
    const obj = ({x: 1, w: ["Asdf"], a: "3"})
    const json = new Json()
    const res = json.decodeFromString(Rpc4tsSerializers.genericThing(NumberSerializer, StringSerializer), string)
    expect(res).toEqual(obj)
})

//TODO: Support easy construction of nested objects

test("Test AnotherModelHolder serialization from string", () => {
    const string = `{"t":{"x":1,"w":["Asdf"],"a":"3"}}`
    const obj = ({t: ({x: 1, w: ["Asdf"], a: "3"})})
    const json = new Json()
    const res = json.decodeFromString(Rpc4tsSerializers.anotherModelHolder(NumberSerializer), string)
    expect(res).toEqual(obj)
})

test("Test Tree serialization from string", () => {
    const string = `{"item":"test","children":[{"item":"foo","children":[{"item":"Bar","children":[]}]},{"item":"Biz","children":[]}]}`
    const obj: Tree<string> = {
        item: "test",
        children: [
            {
                item: "foo",
                children: [
                    {
                        item: "Bar",
                        children: []
                    }
                ]
            },
            {
                item: "Biz",
                children: []
            }
        ]
    }
    const json = new Json()
    const res = json.decodeFromString(Rpc4tsSerializers.rpc4ts_serializer_Tree(StringSerializer), string)
    expect(res).toEqual(obj)
})


test("Test Enum serialization from string", () => {
    const string = `"a"`
    const enumValue: TestEnum = "a"
    const json = new Json()
    const res = json.decodeFromString(Rpc4tsSerializers.testEnum(), string)
    expect(res).toEqual(enumValue)
})

test("Test Polymorphic serialization from string", () => {
    //@ts-ignore
    const obj: TestUnion<string> = ({
        t: ({
            x: "Sdf",
            w: ["123"],
            a: "we"
        })
    })
    const string = `{"type":"AnotherModelHolder","t":{"x":"Sdf","w":["123"],"a":"we"}}`
    const json = new Json()
    const res = json.decodeFromString(Rpc4tsSerializers.testUnion(StringSerializer), string)
    expect(res).toEqual(obj)
})

