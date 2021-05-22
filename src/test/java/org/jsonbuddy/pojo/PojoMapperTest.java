package org.jsonbuddy.pojo;


import org.jsonbuddy.JsonArray;
import org.jsonbuddy.JsonFactory;
import org.jsonbuddy.JsonNode;
import org.jsonbuddy.JsonNull;
import org.jsonbuddy.JsonObject;
import org.jsonbuddy.pojo.testclasses.ClassContainingAnnotated;
import org.jsonbuddy.pojo.testclasses.ClassContainingOverriddenAsSetter;
import org.jsonbuddy.pojo.testclasses.ClassWithAnnotation;
import org.jsonbuddy.pojo.testclasses.ClassWithBigNumbers;
import org.jsonbuddy.pojo.testclasses.ClassWithDifferentTypes;
import org.jsonbuddy.pojo.testclasses.ClassWithEmbeddedGetSetMap;
import org.jsonbuddy.pojo.testclasses.ClassWithEmbeddedMap;
import org.jsonbuddy.pojo.testclasses.ClassWithEnum;
import org.jsonbuddy.pojo.testclasses.ClassWithEnumCollection;
import org.jsonbuddy.pojo.testclasses.ClassWithGetterInterface;
import org.jsonbuddy.pojo.testclasses.ClassWithInterfaceListAndMapMethods;
import org.jsonbuddy.pojo.testclasses.ClassWithJdkValueTypes;
import org.jsonbuddy.pojo.testclasses.ClassWithJsonElements;
import org.jsonbuddy.pojo.testclasses.ClassWithList;
import org.jsonbuddy.pojo.testclasses.ClassWithMap;
import org.jsonbuddy.pojo.testclasses.ClassWithMapWithList;
import org.jsonbuddy.pojo.testclasses.ClassWithNumberSet;
import org.jsonbuddy.pojo.testclasses.ClassWithNumbers;
import org.jsonbuddy.pojo.testclasses.ClassWithOptional;
import org.jsonbuddy.pojo.testclasses.ClassWithOptionalProperty;
import org.jsonbuddy.pojo.testclasses.ClassWithPojoOverride;
import org.jsonbuddy.pojo.testclasses.ClassWithPrimitiveValues;
import org.jsonbuddy.pojo.testclasses.ClassWithPrivateConstructor;
import org.jsonbuddy.pojo.testclasses.ClassWithTime;
import org.jsonbuddy.pojo.testclasses.CombinedClass;
import org.jsonbuddy.pojo.testclasses.CombinedClassWithAnnotation;
import org.jsonbuddy.pojo.testclasses.CombinedClassWithSetter;
import org.jsonbuddy.pojo.testclasses.EnumClass;
import org.jsonbuddy.pojo.testclasses.InterfaceWithEnum;
import org.jsonbuddy.pojo.testclasses.InterfaceWithMethod;
import org.jsonbuddy.pojo.testclasses.PojoMapperOverride;
import org.jsonbuddy.pojo.testclasses.SimpleWithName;
import org.jsonbuddy.pojo.testclasses.SimpleWithNameGetter;
import org.junit.Test;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SuppressWarnings("ConstantConditions")
public class PojoMapperTest {
    @Test
    public void shouldHandleEmptyClass() {
        JsonObject empty = JsonFactory.jsonObject();
        SimpleWithNameGetter result = PojoMapper.map(empty, SimpleWithNameGetter.class);
        assertThat(result).isNotNull();
    }

    @Test
    public void shouldHandleClassWithSimpleValueGetter() {
        JsonObject jsonObject = JsonFactory.jsonObject().put("fullName", "Darth Vader");
        SimpleWithNameGetter result = PojoMapper.map(jsonObject, SimpleWithNameGetter.class);
        assertThat(result.getFullName()).isEqualTo("Darth Vader");
    }

    @Test
    public void shouldHandlePropertiesWithUnderscore() {
        JsonObject jsonObject = JsonFactory.jsonObject().put("full_name", "Darth Vader");
        SimpleWithNameGetter result = PojoMapper.map(jsonObject, SimpleWithNameGetter.class);
        assertThat(result.getFullName()).isEqualTo("Darth Vader");
    }

    @Test
    public void shouldIgnoreUnmappedValues() {
        JsonObject jsonObject = JsonFactory.jsonObject().put("namex", "Darth Vader");
        SimpleWithNameGetter result = PojoMapper.map(jsonObject, SimpleWithNameGetter.class);
        assertThat(result.getFullName()).isNull();
    }

    @Test
    public void shouldHandleClassWithFinalField() {
        JsonObject jsonObject = JsonFactory.jsonObject().put("name", "Darth Vader");
        SimpleWithName result = PojoMapper.map(jsonObject, SimpleWithName.class);
        assertThat(result.name).isEqualTo("Darth Vader");
    }

    @Test
    public void shouldHandleCombinedClass() {
        JsonObject jsonObject = JsonFactory.jsonObject()
                .put("person", JsonFactory.jsonObject().put("name", "Darth Vader"))
                .put("occupation", "Dark Lord of Sith");
        CombinedClass combinedClass = PojoMapper.map(jsonObject, CombinedClass.class);

        assertThat(combinedClass.occupation).isEqualTo("Dark Lord of Sith");
        assertThat(combinedClass.person.name).isEqualTo("Darth Vader");
    }

    @Test
    public void shouldHandleCombinedClassWithGetterSetter() {
        JsonObject jsonObject = JsonFactory.jsonObject()
                .put("person", JsonFactory.jsonObject().put("name", "Darth Vader"))
                .put("occupation", "Dark Lord of Sith");
        CombinedClassWithSetter combinedClassWithSetter = PojoMapper.map(jsonObject, CombinedClassWithSetter.class);

        assertThat(combinedClassWithSetter.getPerson().name).isEqualTo("Darth Vader");
        assertThat(combinedClassWithSetter.getOccupation()).isEqualTo("Dark Lord of Sith");
    }

    public static class ClassWithGenericSetter {
        private List<String> propertiesx = new ArrayList<>();

        public void setProperties(List<String> properties) {
            this.propertiesx = properties;
        }

        public List<String> getProperties() {
            return propertiesx;
        }
    }

    @Test
    public void shouldHandleClassWithGenericSetter() {
        JsonObject jsonObject = new JsonObject()
                .put("properties", new JsonArray().add("one").add("two"));
        assertThat(PojoMapper.map(jsonObject, ClassWithGenericSetter.class).getProperties())
            .containsExactly("one", "two");
    }


    @Test
    public void shouldHandleDifferentTypes() {
        JsonObject jsonObject = JsonFactory.jsonObject()
                .put("text", "the meaning")
                .put("number", 42)
                .put("bool", JsonFactory.jsonTrue())
                .put("falseBool", JsonFactory.jsonFalse());
        ClassWithDifferentTypes differentTypes = PojoMapper.map(jsonObject, ClassWithDifferentTypes.class);
        assertThat(differentTypes.text).isEqualTo("the meaning");
        assertThat(differentTypes.number).isEqualTo(42);
        assertThat(differentTypes.bool).isTrue();
        assertThat(differentTypes.bool).isTrue();
    }

    @Test
    public void shouldHandleNumbersAsText() {
        JsonObject json = new JsonObject().put("intValue", "13").put("longValue", "14");
        ClassWithNumbers object = PojoMapper.map(json, ClassWithNumbers.class);
        assertThat(object.getIntValue()).isEqualTo(13);
        assertThat(object.getLongValue()).isEqualTo(14);
    }

    @Test
    public void shouldHandleNullNumbers() {
        JsonObject json = new JsonObject().put("intValue", null).put("longValue", 12);
        ClassWithNumbers object = PojoMapper.map(json, ClassWithNumbers.class);
        assertThat(object.getLongValue()).isEqualTo(12L);
        assertThat(object.getIntValue()).isNull();
    }

    @Test
    public void shouldThrownOnIllegalAssigments() {
        JsonObject json = new JsonObject().put("intValue", true);
        assertThatThrownBy(() -> PojoMapper.map(json, ClassWithNumbers.class))
            .isInstanceOf(CanNotMapException.class)
            .hasMessageContaining("intValue");
    }

    @Test
    public void shouldHandleLists() {
        JsonObject jsonObject = JsonFactory.jsonObject()
                .put("name", "Darth")
                .put("children", Arrays.asList("Luke", "Leia"));
        ClassWithList classWithList = PojoMapper.map(jsonObject, ClassWithList.class);
        assertThat(classWithList.children).containsExactly("Luke", "Leia");
    }

    @Test
    public void shouldHandleClassWithInstant() {
        Instant now = Instant.now();
        JsonObject jsonObject = JsonFactory.jsonObject().put("time", now);
        ClassWithTime classWithTime = PojoMapper.map(jsonObject, ClassWithTime.class);
        assertThat(classWithTime.getTime()).isEqualTo(now);
    }

    @Test
    public void shouldThrowOnInvalidInstant() {
        JsonObject jsonObject = JsonFactory.jsonObject().put("time", "noon tomorrow");
        assertThatThrownBy(() -> PojoMapper.map(jsonObject, ClassWithTime.class))
                .isInstanceOf(CanNotMapException.class)
                .hasMessageContaining(DateTimeParseException.class.getName())
                .hasMessageContaining("noon tomorrow");
    }

    @Test
    public void shouldMapToPojoFromArray() {
        assertThat(PojoMapper.map(JsonFactory.jsonArray(), String.class)).isEmpty();
    }

    @Test
    public void shouldHandleEmbeddedJsonElements() {
        JsonObject jsonObject = JsonFactory.jsonObject()
                .put("name", "Darth Vader")
                .put("myObject", JsonFactory.jsonObject().put("title", "Dark Lord"))
                .put("myArray", JsonFactory.jsonArray().addAll(Arrays.asList("Luke", "Leia")));
        ClassWithJsonElements classWithJsonElements = PojoMapper.map(jsonObject, ClassWithJsonElements.class);

        assertThat(classWithJsonElements.name).isEqualTo("Darth Vader");
        assertThat(classWithJsonElements.myObject.requiredString("title")).isEqualTo("Dark Lord");
        assertThat(classWithJsonElements.myArray.stringStream().collect(Collectors.toList())).containsExactly("Luke", "Leia");
    }

    @Test
    public void shouldHandleClassWithAnnotation() {
        JsonObject jsonObject = JsonFactory.jsonObject()
                .put("name", "Darth Vader");
        ClassWithAnnotation classWithAnnotation = PojoMapper.map(jsonObject, ClassWithAnnotation.class);
        assertThat(classWithAnnotation.value).isEqualTo("overridden");
    }

    @Test
    public void shouldHandleClassWithOverriddenNull() {
        JsonObject jsonObject = JsonFactory.jsonObject()
                .put("name", "Darth Vader")
                .put("myHack",JsonFactory.jsonObject().put("wont matter","nope"));
        PojoMapperOverride.returnNull = true;
        CombinedClassWithAnnotation classWithAnnotation;
        try {
            classWithAnnotation = PojoMapper.map(jsonObject, CombinedClassWithAnnotation.class);
        } finally {
            PojoMapperOverride.returnNull = false;

        }
        assertThat(classWithAnnotation.myHack).isNull();

    }


    @Test
    public void shouldHandleClassWithMap() {
        JsonObject jsonObject = JsonFactory.jsonObject()
                .put("properties", JsonFactory.jsonObject().put("firstname", "Darth").put("lastname", "Vader"));
        ClassWithMap classWithMap = PojoMapper.map(jsonObject, ClassWithMap.class);

        assertThat(classWithMap.properties.get("firstname")).isEqualTo("Darth");
        assertThat(classWithMap.properties.get("lastname")).isEqualTo("Vader");
    }

    @Test
    public void shouldHandleClassWithPrivateConstructor() {
        JsonObject jsonObject = JsonFactory.jsonObject().put("name", "Darth Vader");
        ClassWithPrivateConstructor privateConstr = PojoMapper.map(jsonObject, ClassWithPrivateConstructor.class);
        assertThat(privateConstr.name).isEqualTo("Darth Vader");
    }

    @Test
    public void shouldConvertTextToNumberIfNessesary() {
        JsonObject jsonObject = JsonFactory.jsonObject().put("text", "Darth Vader").put("number", "42");
        ClassWithDifferentTypes classWithDifferentTypes = PojoMapper.map(jsonObject, ClassWithDifferentTypes.class);
        assertThat(classWithDifferentTypes.number).isEqualTo(42);

    }

    @Test
    public void shouldHandleClassWithEmbeddedMap() {
        JsonObject jsonObject = JsonFactory.jsonObject()
                .put("names",
                        JsonFactory.jsonObject()
                                .put("darth", JsonFactory.jsonObject().put("name", "Darth Vader")));
        ClassWithEmbeddedMap withEmbeddedMap = PojoMapper.map(jsonObject, ClassWithEmbeddedMap.class);
        assertThat(withEmbeddedMap.names.get("darth").name).isEqualTo("Darth Vader");

    }

    @Test
    public void shouldHandleClassWithGetSetEmbeddedMap() {
        JsonObject jsonObject = new JsonObject()
                .put("names",
                        new JsonObject().put("darth", new JsonObject().put("name", "Darth Vader")));
        ClassWithEmbeddedGetSetMap withEmbeddedMap = PojoMapper.map(jsonObject, ClassWithEmbeddedGetSetMap.class);
        assertThat(withEmbeddedMap.getNames().get("darth").name).isEqualTo("Darth Vader");
    }

    @Test
    public void shouldHandleClassWithEnum() {
        JsonObject jsonObject = new JsonObject().put("enumNumber", "TWO");
        ClassWithEnum classWithEnum = PojoMapper.map(jsonObject, ClassWithEnum.class);
        assertThat(classWithEnum.enumNumber).isEqualTo(EnumClass.TWO);
    }

    @Test
    public void shouldThrowExceptionOnInvalidEnum() {
        JsonObject jsonObject = new JsonObject().put("enumNumber", "bad-value");
        assertThatThrownBy(() -> PojoMapper.map(jsonObject, ClassWithEnum.class))
            .isInstanceOf(CanNotMapException.class)
            .hasMessageContaining("bad-value")
            .hasMessageContaining("EnumClass")
            .hasMessageContaining("ONE");
    }

    @Test
    public void shouldHandleClassWithEnumCollection() {
        JsonObject jsonObject = new JsonObject().put("enumCollection",
                new JsonArray().add(EnumClass.THREE).add(EnumClass.ONE));
        ClassWithEnumCollection o = PojoMapper.map(jsonObject, ClassWithEnumCollection.class);
        assertThat(o.enumCollection).containsExactly(EnumClass.THREE, EnumClass.ONE);
    }

    @Test
    public void shouldHandleClassWithMapWithList() {
        JsonObject jsonObject = new JsonObject()
                .put("parentAndChildren", new JsonObject().put("Darth", new JsonArray().add("Luke").add("Leia")));
        ClassWithMapWithList withList = PojoMapper.map(jsonObject, ClassWithMapWithList.class);
        assertThat(withList.parentAndChildren.get("Darth")).containsExactly("Luke","Leia");
    }

    @Test
    public void shouldGiveSensibleErrorOnAttemptToMapJsonObjectToList() {
        JsonObject jsonObject = new JsonObject()
                .put("parentAndChildren", new JsonObject().put("Darth", new JsonObject().put("name", "Luke")));
        assertThatThrownBy(() -> PojoMapper.map(jsonObject, ClassWithMapWithList.class))
                .isInstanceOf(CanNotMapException.class)
                .hasMessageContaining("Cannot map JsonObject to interface java.util.List");
    }

    @Test
    public void shouldGiveSensibleErrorOnAttemptToMapJsonArrayToMap() {
        JsonObject jsonObject = new JsonObject()
                .put("parentAndChildren", new JsonArray().add("Luke").add("Leia"));
        assertThatThrownBy(() -> PojoMapper.map(jsonObject, ClassWithMapWithList.class))
                .isInstanceOf(CanNotMapException.class)
                .hasMessageContaining("Cannot map JsonArray to interface java.util.Map");
    }

    @Test
    public void shouldHandleOptionalField() {
        ClassWithOptional classWithOptional = PojoMapper.map(JsonFactory.jsonObject(), ClassWithOptional.class);
        assertThat(classWithOptional.optStr).isNull();

        classWithOptional = PojoMapper.map(JsonFactory.jsonObject().put("optStr",new JsonNull()), ClassWithOptional.class);
        assertThat(classWithOptional.optStr).isEmpty();

        classWithOptional = PojoMapper.map(JsonFactory.jsonObject().put("optStr","abc"), ClassWithOptional.class);
        assertThat(classWithOptional.optStr).isPresent().contains("abc");

    }

    @Test
    public void shouldHandleOptionalProperty() {
        ClassWithOptionalProperty classWithOptional = PojoMapper.map(JsonFactory.jsonObject(), ClassWithOptionalProperty.class);
        assertThat(classWithOptional.getOptStr()).isNull();

        classWithOptional = PojoMapper.map(JsonFactory.jsonObject().put("optStr",new JsonNull()), ClassWithOptionalProperty.class);
        assertThat(classWithOptional.getOptStr().isPresent()).isFalse();

        classWithOptional = PojoMapper.map(JsonFactory.jsonObject().put("optStr","abc"), ClassWithOptionalProperty.class);
        assertThat(classWithOptional.getOptStr()).isPresent().contains("abc");
    }

    @Test
    public void shouldHandleIntegerToFloatingPointConversion() {
        ClassWithNumbers classWithNumbers = PojoMapper.map(JsonFactory.jsonObject().put("floatValue", 17), ClassWithNumbers.class);
        assertThat(classWithNumbers.getFloatValue()).isEqualTo(17.0f);

        classWithNumbers = PojoMapper.map(JsonFactory.jsonObject().put("floatValue", 17L), ClassWithNumbers.class);
        assertThat(classWithNumbers.getFloatValue()).isEqualTo(17.0f);

        classWithNumbers = PojoMapper.map(JsonFactory.jsonObject().put("doubleValue", 89), ClassWithNumbers.class);
        assertThat(classWithNumbers.getDoubleValue()).isEqualTo(89.0);

        classWithNumbers = PojoMapper.map(JsonFactory.jsonObject().put("doubleValue", 89L), ClassWithNumbers.class);
        assertThat(classWithNumbers.getDoubleValue()).isEqualTo(89.0);
    }

    @Test
    public void shouldHandleListWithNulls() {
        List<String> withNull = Arrays.asList("one",null,"two");
        JsonArray arr = JsonArray.fromStringList(withNull);
        JsonObject jsonObject = JsonFactory.jsonObject().put("name", "darth").put("children", arr);
        ClassWithList classWithList = PojoMapper.map(jsonObject, ClassWithList.class);
        assertThat(classWithList.children.get(1)).isNull();
    }

    @Test
    public void shouldThrowOnNonApplicableClasses() {
        assertThatThrownBy(() -> PojoMapper.map(new JsonObject(), LocalDate.class))
            .isInstanceOf(CanNotMapException.class);
    }

    @Test
    public void shouldHandleListOfOverriddenValues() {
        JsonArray jsonArray = JsonFactory.jsonArray()
                .add(JsonFactory.jsonObject())
                .add(JsonFactory.jsonObject());

        List<ClassWithAnnotation> result = PojoMapper.map(jsonArray, ClassWithAnnotation.class);
        assertThat(result).hasSize(2);
        assertThat(result.get(0).value).isEqualTo("overridden");
        assertThat(result.get(1).value).isEqualTo("overridden");
    }

    @Test
    public void shouldMapListOfNumber() {
        assertThat(PojoMapper.map(new JsonArray().add(1L).add(3L), Long.class))
                .isEqualTo(Arrays.asList(1L, 3L));
    }

    @Test
    public void shouldMapJsonArrayToJsonArray() {
        assertThat((Object)PojoMapper.mapType(new JsonArray().add(1L).add(3L), JsonArray.class))
                .isEqualTo(new JsonArray().add(1L).add(3L));
    }

    @Test
    public void shouldMapListOfUUID() {
        UUID uuid1 = UUID.randomUUID();
        UUID uuid2 = UUID.randomUUID();
        assertThat(PojoMapper.map(new JsonArray().add(uuid1.toString()).add(uuid2.toString()), UUID.class))
                .isEqualTo(Arrays.asList(uuid1, uuid2));
    }

    @Test
    public void shouldHandleCombined() {
        JsonObject jsonObject = JsonFactory.jsonObject()
                .put("name", "Darth Vader")
                .put("myHack",JsonFactory.jsonArray().add("Hola"));
        CombinedClassWithAnnotation combinedClassWithAnnotation = PojoMapper.map(jsonObject, CombinedClassWithAnnotation.class);
        assertThat(combinedClassWithAnnotation.myHack.value).isEqualTo("overridden");
    }


    @Test
    public void shouldHandleClassContainingAnnotated() {
        JsonObject obj = JsonFactory.jsonObject().put("annonlist",
                JsonFactory.jsonArray()
                        .add(JsonFactory.jsonObject().put("value", "one"))
                        .add(JsonFactory.jsonObject().put("value", "two")));
        ClassContainingAnnotated containingAnnotated = PojoMapper.map(obj, ClassContainingAnnotated.class);
        List<ClassWithPojoOverride> result = containingAnnotated.annonlist;
        assertThat(result).hasSize(2);
        assertThat(result.get(0).value).isEqualTo("overridden one");
        assertThat(result.get(1).value).isEqualTo("overridden two");

    }

    @Test
    public void shouldHandleClassContainingMethodAnnotated() {
        JsonObject obj = JsonFactory.jsonObject().put("annonlist",
                JsonFactory.jsonArray()
                        .add(JsonFactory.jsonObject().put("value", "one"))
                        .add(JsonFactory.jsonObject().put("value", "two")));
        ClassContainingOverriddenAsSetter containingAnnotated = PojoMapper.map(obj, ClassContainingOverriddenAsSetter.class);
        List<ClassWithPojoOverride> result = containingAnnotated.getAnnonlist();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).value).isEqualTo("overridden one");
        assertThat(result.get(1).value).isEqualTo("overridden two");
    }

    @Test
    public void shouldHandleBigNumbers() {
        JsonObject obj = JsonFactory.jsonObject()
                .put("oneBigInt", 42L)
                .put("oneBigDec", 3.14d);
        ClassWithBigNumbers classWithBigNumbers = PojoMapper.map(obj, ClassWithBigNumbers.class);

        assertThat(classWithBigNumbers.getOneBigInt()).isEqualTo(BigInteger.valueOf(42L));
        assertThat(classWithBigNumbers.getOneBigDec()).isEqualTo(BigDecimal.valueOf(3.14d));
    }

    @Test
    public void shouldParseToInterface() {
        JsonObject jsonObject = JsonFactory.jsonObject().put("publicvalue", "A public value");
        InterfaceWithMethod interfaceWithMethod = PojoMapper.map(jsonObject, InterfaceWithMethod.class, new DynamicInterfaceMapper());
        assertThat(interfaceWithMethod).isNotNull();
        assertThat(interfaceWithMethod.getPublicvalue()).isEqualTo("A public value");
    }

    @Test
    public void shouldNotHandleInterfacesWithoiyMapOption() {
        JsonObject jsonObject = JsonFactory.jsonObject().put("publicvalue", "A public value");
        assertThatThrownBy(() -> PojoMapper.map(jsonObject, InterfaceWithMethod.class))
            .isInstanceOf(CanNotMapException.class);
    }

    @Test
    public void shouldHandleInterfacesAsFields() {
        JsonObject jsonObject = JsonFactory.jsonObject()
                .put("myInterface", JsonFactory.jsonObject().put("publicvalue", "A public value"));
        ClassWithGetterInterface classWithGetterInterface = PojoMapper.map(jsonObject, ClassWithGetterInterface.class,new DynamicInterfaceMapper());
        assertThat(classWithGetterInterface.getMyInterface().getPublicvalue()).isEqualTo("A public value");
    }

    @Test
    public void shoudHandleNullValues() {
        JsonObject jsonObject = JsonFactory.jsonObject()
                .put("myInterface", JsonFactory.jsonObject().put("publicvalue", null));
        ClassWithGetterInterface classWithGetterInterface = PojoMapper.map(jsonObject, ClassWithGetterInterface.class,new DynamicInterfaceMapper());
        assertThat(classWithGetterInterface.getMyInterface()).isNotNull();
        assertThat(classWithGetterInterface.getMyInterface().getPublicvalue()).isNull();
    }

    @Test
    public void shoulHandleAbsentValues() {
        JsonObject jsonObject = JsonFactory.jsonObject()
                .put("myInterface", JsonFactory.jsonObject());
        ClassWithGetterInterface classWithGetterInterface = PojoMapper.map(jsonObject, ClassWithGetterInterface.class,DynamicInterfaceMapper.mapperThatMapsAllGetters());
        assertThat(classWithGetterInterface.getMyInterface()).isNotNull();
        assertThat(classWithGetterInterface.getMyInterface().getPublicvalue()).isNull();
    }

    @Test
    public void shouldHandleInterfaceInLists() {
        JsonObject jsonObject = JsonFactory.jsonObject().put("myList", JsonArray.fromNodeList(Collections.singletonList(
                JsonFactory.jsonObject().put("publicvalue", "A public value")
        )));

        ClassWithInterfaceListAndMapMethods result = PojoMapper.map(jsonObject, ClassWithInterfaceListAndMapMethods.class,new DynamicInterfaceMapper());
        assertThat(result.getMyList()).hasSize(1);
        assertThat(result.getMyList().get(0).getPublicvalue()).isEqualTo("A public value");
    }

    @Test
    public void shouldHandleInterfaceInMaps() {
        JsonObject jsonObject = JsonFactory.jsonObject().put("myMap", JsonFactory.jsonObject().put("intkey",
                JsonFactory.jsonObject().put("publicvalue", "A public value")));
        ClassWithInterfaceListAndMapMethods result = PojoMapper.map(jsonObject, ClassWithInterfaceListAndMapMethods.class, new DynamicInterfaceMapper());
        assertThat(result.getMyMap().get("intkey").getPublicvalue()).isEqualTo("A public value");
    }

    @Test
    public void shouldHandleInputWithSingleCharKey() {
        JsonObject jsonObject = JsonFactory.jsonObject().put("", "dsfds");
        SimpleWithName simpleWithName = PojoMapper.map(jsonObject, SimpleWithName.class);
        assertThat(simpleWithName.name).isNull();
    }

    @Test
    public void shouldMapToFieldOfSetOfNumbers() {
        JsonObject jsonObject = JsonFactory.jsonObject()
                .put("numberSet", JsonFactory.jsonArray().add(1).add(2).add(3))
                .put("mapOfSetOfString", JsonFactory.jsonObject()
                        .put("first", JsonFactory.jsonArray().add("4").add("5").add("6")));
        ClassWithNumberSet o = PojoMapper.map(jsonObject, ClassWithNumberSet.class);
        assertThat(o.numberSet).containsOnly(1L, 2L, 3L);
        assertThat(o.mapOfSetOfString.get("first")).containsOnly("4", "5", "6");
    }

    @Test
    public void shoulHandleBothInterfaceAndEnum() {
        JsonObject jsonObject = JsonFactory.jsonObject().put("name","Darth").put("enumNumber",EnumClass.THREE.toString());
        InterfaceWithEnum interfaceWithEnum = PojoMapper.map(jsonObject, InterfaceWithEnum.class, new DynamicInterfaceMapper(), new EnumMapper());
        assertThat(interfaceWithEnum.getName()).isEqualTo("Darth");
        assertThat(interfaceWithEnum.getEnumNumber()).isEqualTo(EnumClass.THREE);
    }

    @Test
    public void shouldMapJdkValueTypes() throws MalformedURLException, URISyntaxException, UnknownHostException {
        ClassWithJdkValueTypes o = new ClassWithJdkValueTypes();
        o.uuids = Arrays.asList(UUID.randomUUID(), UUID.randomUUID());
        o.localDate = LocalDate.now();
        o.instant = Instant.now();
        o.url = new URL("https://github.com/anders88/jsonbuddy");
        o.uri = new URI("https://github.com/jhannes/");
        o.inetAddress = InetAddress.getByName("127.0.0.1");
        JsonNode json = JsonGenerator.generate(o);
        ClassWithJdkValueTypes deserialized = PojoMapper.mapType(json, ClassWithJdkValueTypes.class);
        assertThat(deserialized).usingRecursiveComparison().isEqualTo(o);
    }

    @Test
    public void shouldRoundtripPrimitives() throws NoSuchMethodException {
        Random random = new Random();
        ClassWithPrimitiveValues o = new ClassWithPrimitiveValues();
        o.boolValue = random.nextBoolean();
        o.byteValue = (byte) random.nextInt(0xff);
        o.shortValue = (short) random.nextInt(0x8fff);
        o.intValue = random.nextInt();
        o.longValue = random.nextLong();
        o.doubleValue = random.nextDouble();
        o.floatValue = random.nextInt();

        JsonNode json = JsonGenerator.generate(o);
        Type type = getClass().getMethod("primitivesFactory").getGenericReturnType();
        ClassWithPrimitiveValues deserialized = PojoMapper.mapType(json, type);
        assertThat(deserialized).usingRecursiveComparison().isEqualTo(o);
    }

    public ClassWithPrimitiveValues primitivesFactory() {
        return null;
    }

    @Test
    public void shouldDecodeGenericTypes() throws NoSuchMethodException {
        JsonNode list = new JsonArray()
                .add(new JsonObject().put("name", "Johannes"))
                .add(new JsonObject().put("name", "Anders"));
        Type type = getClass().getMethod("listFactory").getGenericReturnType();
        List<SimpleWithName> result = PojoMapper.mapType(list, type);
        assertThat(result)
                .extracting("name")
                .contains("Johannes", "Anders");
    }

    @Test
    public void shouldMapStreams() throws NoSuchMethodException {
        JsonNode list = new JsonArray()
                .add(new JsonObject().put("name", "Johannes"))
                .add(new JsonObject().put("name", "Anders"));
        Type type = getClass().getMethod("streamFactory").getGenericReturnType();
        Stream<SimpleWithName> result = PojoMapper.mapType(list, type);
        assertThat(result)
                .extracting("name")
                .contains("Johannes", "Anders");
    }

    @Test
    public void shouldHandleMapOfObjects() throws NoSuchMethodException {
        JsonNode list = new JsonObject()
                .put("johannes", new JsonObject().put("name", "Johannes"))
                .put("anders", new JsonObject().put("name", "Anders"));
        Type type = getClass().getMethod("mapFactory").getGenericReturnType();
        Map<String, SimpleWithName> result = PojoMapper.mapType(list, type);
        assertThat(result.get("johannes").name)
                .contains("Johannes");
    }

    @Test
    public void shouldReturnArrayForArray() {
        JsonArray array = new JsonArray().add("one").add("two");
        assertThat((Object)PojoMapper.create().mapToPojo(array, JsonArray.class, String.class)).isEqualTo(array);
        assertThat((Object)PojoMapper.mapType(array, JsonArray.class)).isEqualTo(array);
    }

    @Test
    public void shouldThrowOnUnsupportedCollectionType() {
        JsonArray array = new JsonArray().add("one").add("two");
        assertThatThrownBy(() -> PojoMapper.create().mapToPojo(array, ArrayList.class, String.class))
            .isInstanceOf(CanNotMapException.class)
            .hasMessageContaining(ArrayList.class.getName());
    }

    public List<SimpleWithName> listFactory() {
        return null;
    }

    public Stream<SimpleWithName> streamFactory() {
        return null;
    }

    public Map<String, SimpleWithName> mapFactory() {
        return null;
    }


    @Test
    public void shouldDecodeJsonArray() {
        JsonNode list = new JsonArray()
                .add(new JsonObject().put("name", "Johannes"))
                .add(new JsonObject().put("name", "Anders"));
        JsonArray result = PojoMapper.mapType(list, JsonArray.class);
        assertThat(result).isEqualTo(list);
    }
}
