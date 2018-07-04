package org.jsonbuddy.pojo;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.jsonbuddy.JsonArray;
import org.jsonbuddy.JsonFactory;
import org.jsonbuddy.JsonNode;
import org.jsonbuddy.JsonNull;
import org.jsonbuddy.JsonNumber;
import org.jsonbuddy.JsonObject;
import org.jsonbuddy.pojo.testclasses.ClassImplementingInterface;
import org.jsonbuddy.pojo.testclasses.ClassWithBigNumbers;
import org.jsonbuddy.pojo.testclasses.ClassWithDifferentTypes;
import org.jsonbuddy.pojo.testclasses.ClassWithEnum;
import org.jsonbuddy.pojo.testclasses.ClassWithFieldInterface;
import org.jsonbuddy.pojo.testclasses.ClassWithGetterInterface;
import org.jsonbuddy.pojo.testclasses.ClassWithInterfaceListAndMapMethods;
import org.jsonbuddy.pojo.testclasses.ClassWithJsonElements;
import org.jsonbuddy.pojo.testclasses.ClassWithMap;
import org.jsonbuddy.pojo.testclasses.ClassWithTime;
import org.jsonbuddy.pojo.testclasses.CombinedClassWithSetter;
import org.jsonbuddy.pojo.testclasses.InterfaceWithMethod;
import org.jsonbuddy.pojo.testclasses.JsonGeneratorOverrides;
import org.jsonbuddy.pojo.testclasses.SimpleWithName;
import org.junit.Test;

public class JsonGeneratorTest {

    @Test
    public void shouldHandleSimpleClass() throws Exception {
        SimpleWithName simpleWithName = new SimpleWithName("Darth Vader");
        JsonNode generated = JsonGenerator.generate(simpleWithName);
        assertThat(generated).isInstanceOf(JsonObject.class);
        JsonObject jsonObject = (JsonObject) generated;
        assertThat(jsonObject.stringValue("name").get()).isEqualTo("Darth Vader");
    }

    @Test
    public void shouldHandleSimpleValues() throws Exception {
        assertThat(JsonGenerator.generate(null)).isEqualTo(new JsonNull());
        assertThat(JsonGenerator.generate("Darth")).isEqualTo(JsonFactory.jsonString("Darth"));
        assertThat(JsonGenerator.generate(42)).isEqualTo(JsonFactory.jsonNumber(42));

    }

    @Test
    public void shoulHandleFloats() throws Exception {
        JsonNode jsonNode = JsonGenerator.generate(3.14f);
        JsonNumber jsonDouble = (JsonNumber) jsonNode;
        assertThat(new Double(jsonDouble.doubleValue()).floatValue()).isEqualTo(3.14f);
    }

    @Test
    public void shouldHandleList() throws Exception {
        List<String> stringlist = Arrays.asList("one", "two", "three");

        JsonNode generate = JsonGenerator.generate(stringlist);
        assertThat(generate).isInstanceOf(JsonArray.class);
        JsonArray array = (JsonArray) generate;
        assertThat(array.strings()).isEqualTo(stringlist);
    }

    @Test
    public void shouldHandleArray() throws Exception {
        String[] stringarray = { "one", "two", "three" };

        JsonNode generate = JsonGenerator.generate(stringarray);
        assertThat(generate).isInstanceOf(JsonArray.class);
        JsonArray array = (JsonArray) generate;
        assertThat(array.strings()).containsExactly(stringarray);
    }

    @Test
    public void shouldHandleListWithClasses() throws Exception {
        List<SimpleWithName> simpleWithNames = Arrays.asList(new SimpleWithName("Darth"), new SimpleWithName("Anakin"));
        JsonArray array = (JsonArray) JsonGenerator.generate(simpleWithNames);

        List<JsonObject> objects = array.objects(o -> o);

        assertThat(objects.get(0).stringValue("name").get()).isEqualTo("Darth");
        assertThat(objects.get(1).stringValue("name").get()).isEqualTo("Anakin");
    }

    @Test
    public void shouldHandleNestedLists() {
        List<List<String>> nestedList = Arrays.asList(Arrays.asList("Vader", "Sidious"), Arrays.asList("Anakin"));

        JsonArray array = (JsonArray) JsonGenerator.generate(nestedList);

        assertThat(array.requiredArray(0).strings()).containsExactly("Vader", "Sidious");
        assertThat(array.requiredArray(1).strings()).containsExactly("Anakin");
    }

    @Test
    public void shouldHandleClassWithGetter() throws Exception {
        CombinedClassWithSetter combinedClassWithSetter = new CombinedClassWithSetter();
        combinedClassWithSetter.setPerson(new SimpleWithName("Darth Vader"));
        combinedClassWithSetter.setOccupation("Dark Lord");

        JsonObject jsonObject = (JsonObject) JsonGenerator.generate(combinedClassWithSetter);

        assertThat(jsonObject.stringValue("occupation").get()).isEqualTo("Dark Lord");
        Optional<JsonObject> person = jsonObject.objectValue("person");

        assertThat(person).isPresent();
        assertThat(person.get()).isInstanceOf(JsonObject.class);
        assertThat(person.get().requiredString("name")).isEqualTo("Darth Vader");

    }


    @Test
    public void shouldHandleOverriddenValues() throws Exception {
        JsonGeneratorOverrides overrides = new JsonGeneratorOverrides();
        JsonObject generate = (JsonObject) JsonGenerator.generate(overrides);

        assertThat(generate.requiredLong("myOverriddenValue")).isEqualTo(42);

    }

    @Test
    public void shoulHandleEmbeddedJson() throws Exception {
        ClassWithJsonElements classWithJsonElements = new ClassWithJsonElements("Darth Vader",
                JsonFactory.jsonObject().put("title", "Dark Lord"),
                JsonFactory.jsonArray().add("Luke").add("Leia"));
        JsonObject generate = (JsonObject) JsonGenerator.generate(classWithJsonElements);

        assertThat(generate.requiredString("name")).isEqualTo("Darth Vader");
        assertThat(generate.requiredObject("myObject").requiredString("title")).isEqualTo("Dark Lord");
        assertThat(generate.requiredArray("myArray").stringStream().collect(Collectors.toList())).containsExactly("Luke", "Leia");
    }

    @Test
    public void shouldMakeMapsIntoObjects() throws Exception {
        Map<String, String> map = new HashMap<>();
        map.put("name", "Darth Vader");
        ClassWithMap classWithMap = new ClassWithMap(map);
        JsonObject generate = (JsonObject) JsonGenerator.generate(classWithMap);
        assertThat(generate.requiredObject("properties").requiredString("name")).isEqualTo("Darth Vader");
    }

    @Test
    public void shouldHandleDifferentSimpleTypes() throws Exception {
        ClassWithDifferentTypes classWithDifferentTypes = new ClassWithDifferentTypes("my text", 42, true, false);
        JsonObject generated = (JsonObject) JsonGenerator.generate(classWithDifferentTypes);
        assertThat(generated.requiredString("text")).isEqualTo("my text");
        assertThat(generated.requiredLong("number")).isEqualTo(42);
        assertThat(generated.requiredBoolean("bool")).isTrue();
    }

    @Test
    public void shouldHandleClassWithTime() throws Exception {
        OffsetDateTime dateTime = OffsetDateTime.of(2015, 8, 13, 21, 14, 18, 321, ZoneOffset.UTC);
        ClassWithTime classWithTime = new ClassWithTime();
        classWithTime.setTime(dateTime.toInstant());

        JsonObject jsonNode = (JsonObject) JsonGenerator.generate(classWithTime);
        assertThat(jsonNode.requiredString("time")).isEqualTo("2015-08-13T21:14:18.000000321Z");
    }

    @Test
    public void shouldHandleClassWithEnum() throws Exception {
        ClassWithEnum classWithEnum = new ClassWithEnum();
        JsonObject jso = (JsonObject) JsonGenerator.generate(classWithEnum);
        assertThat(jso.requiredString("enumNumber")).isEqualTo("ONE");
    }

    @Test
    public void shouldHandleNumbers() throws Exception {
        assertThat(JsonGenerator.generate(12L)).isEqualTo(new JsonNumber(12L));
        assertThat(JsonGenerator.generate(12)).isEqualTo(new JsonNumber(12));
    }

    @Test
    public void shouldHandleMapWithKeyOtherThanString() throws Exception {
        Map<Long,String> myLongMap = new HashMap<>();
        myLongMap.put(42L, "Meaning of life");
        JsonNode generated = JsonGenerator.generate(myLongMap);
        assertThat(generated).isInstanceOf(JsonObject.class);
        JsonObject jsonObject = (JsonObject) generated;
        assertThat(jsonObject.requiredString("42")).isEqualTo("Meaning of life");
    }

    @Test
    public void shouldHandleBigInteger() throws Exception {
        ClassWithBigNumbers bn = new ClassWithBigNumbers();
        bn.setOneBigInt(BigInteger.valueOf(42L));

        JsonNode generated = JsonGenerator.generate(bn);
        assertThat(generated).isInstanceOf(JsonObject.class);
        JsonObject jsonObject = (JsonObject) generated;
        assertThat(jsonObject.requiredLong("oneBigInt")).isEqualTo(42L);

    }

    @Test
    public void shouldHandleBigDecimal() throws Exception {
        ClassWithBigNumbers bn = new ClassWithBigNumbers();
        bn.setOneBigDec(BigDecimal.valueOf(3.14d));

        JsonNode generated = JsonGenerator.generate(bn);
        assertThat(generated).isInstanceOf(JsonObject.class);
        JsonObject jsonObject = (JsonObject) generated;
        assertThat(jsonObject.requiredDouble("oneBigDec")).isEqualTo(3.14d);

    }

    @Test
    public void shouldMaskMethodsNotInInterfaceWhenUsingGetter() throws Exception {
        InterfaceWithMethod myInterface = new ClassImplementingInterface("myPublic", "mySecret");
        ClassWithGetterInterface classWithGetterInterface = new ClassWithGetterInterface(myInterface);
        JsonNode generated = JsonGenerator.generate(classWithGetterInterface);
        assertThat(generated).isInstanceOf(JsonObject.class);
        JsonObject jsonObject = (JsonObject) generated;
        JsonObject childObj = jsonObject.requiredObject("myInterface");
        assertThat(childObj.requiredString("publicvalue")).isEqualTo("myPublic");
        assertThat(childObj.stringValue("privatevalue").isPresent()).isFalse();
    }

    @Test
    public void shouldMaskMethodsNotInInterfaceWhenUsingField() throws Exception {
        InterfaceWithMethod myInterface = new ClassImplementingInterface("myPublic", "mySecret");
        ClassWithFieldInterface classWithFieldInterface = new ClassWithFieldInterface(myInterface);
        JsonNode generated = JsonGenerator.generate(classWithFieldInterface);
        assertThat(generated).isInstanceOf(JsonObject.class);
        JsonObject jsonObject = (JsonObject) generated;
        JsonObject childObj = jsonObject.requiredObject("myInterface");
        assertThat(childObj.requiredString("publicvalue")).isEqualTo("myPublic");
        assertThat(childObj.stringValue("privatevalue").isPresent()).isFalse();
    }

    @Test
    public void shouldBeAbleToSpesifyGeneratorClass() throws Exception {
        InterfaceWithMethod interfaceWithMethod = new ClassImplementingInterface("myPublic", "mySecret");
        JsonNode generated = JsonGenerator.generateWithSpecifyingClass(interfaceWithMethod, InterfaceWithMethod.class);
        JsonObject jsonObject = (JsonObject) generated;
        assertThat(jsonObject.requiredString("publicvalue")).isEqualTo("myPublic");
        assertThat(jsonObject.stringValue("privatevalue").isPresent()).isFalse();
    }

    @Test
    public void shouldHandleAnonymousClass() throws Exception {
        InterfaceWithMethod interfaceWithMethod = new InterfaceWithMethod() {
            @Override
            public String getPublicvalue() {
                return "Hello world";
            }
        };
        JsonNode generated = JsonGenerator.generateWithSpecifyingClass(interfaceWithMethod, InterfaceWithMethod.class);
        JsonObject jsonObject = (JsonObject) generated;
        assertThat(jsonObject.requiredString("publicvalue")).isEqualTo("Hello world");
    }

    @Test
    public void shouldHandleInterfaceTypesInMethodList() throws Exception {
        ClassWithInterfaceListAndMapMethods classWithInterfaceListAndMapMethods = new ClassWithInterfaceListAndMapMethods();
        List<InterfaceWithMethod> myList = new ArrayList<>();
        myList.add(new ClassImplementingInterface("mypub","myPriv"));
        classWithInterfaceListAndMapMethods.setMyList(myList);

        JsonNode generated = JsonGenerator.generate(classWithInterfaceListAndMapMethods);

        assertThat(generated).isInstanceOf(JsonObject.class);
        JsonObject jsonObject = (JsonObject) generated;
        JsonArray interfacelist = jsonObject.requiredArray("myList");
        assertThat(interfacelist).hasSize(1);

        JsonObject interfaceobj = interfacelist.get(0, JsonObject.class);
        assertThat(interfaceobj.requiredString("publicvalue")).isEqualTo("mypub");
        assertThat(interfaceobj.stringValue("privatevalue").isPresent()).isFalse();
   }

    @Test
    public void shouldHandleInterfaceTypesInMethodMaps() throws Exception {
        ClassWithInterfaceListAndMapMethods classWithInterfaceListAndMapMethods = new ClassWithInterfaceListAndMapMethods();
        Map<String,InterfaceWithMethod> myMap = new HashMap<>();
        myMap.put("mykey",new ClassImplementingInterface("mypub","myPriv"));
        classWithInterfaceListAndMapMethods.setMyMap(myMap);

        JsonNode generated = JsonGenerator.generate(classWithInterfaceListAndMapMethods);

        assertThat(generated).isInstanceOf(JsonObject.class);
        JsonObject jsonObject = (JsonObject) generated;
        JsonObject interfacemap = jsonObject.requiredObject("myMap");
        assertThat(interfacemap.keys()).hasSize(1);
        JsonObject interfaceobj = interfacemap.requiredObject("mykey");
        assertThat(interfaceobj.requiredString("publicvalue")).isEqualTo("mypub");
        assertThat(interfaceobj.stringValue("privatevalue").isPresent()).isFalse();
    }


}
