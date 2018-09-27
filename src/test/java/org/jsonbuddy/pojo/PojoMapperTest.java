package org.jsonbuddy.pojo;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.jsonbuddy.JsonArray;
import org.jsonbuddy.JsonFactory;
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
import org.jsonbuddy.pojo.testclasses.ClassWithGetterInterface;
import org.jsonbuddy.pojo.testclasses.ClassWithInterfaceListAndMapMethods;
import org.jsonbuddy.pojo.testclasses.ClassWithJsonElements;
import org.jsonbuddy.pojo.testclasses.ClassWithList;
import org.jsonbuddy.pojo.testclasses.ClassWithMap;
import org.jsonbuddy.pojo.testclasses.ClassWithMapWithList;
import org.jsonbuddy.pojo.testclasses.ClassWithNumberSet;
import org.jsonbuddy.pojo.testclasses.ClassWithNumbers;
import org.jsonbuddy.pojo.testclasses.ClassWithOptional;
import org.jsonbuddy.pojo.testclasses.ClassWithOptionalProperty;
import org.jsonbuddy.pojo.testclasses.ClassWithPojoOverride;
import org.jsonbuddy.pojo.testclasses.ClassWithPrivateConstructor;
import org.jsonbuddy.pojo.testclasses.ClassWithTime;
import org.jsonbuddy.pojo.testclasses.CombinedClass;
import org.jsonbuddy.pojo.testclasses.CombinedClassWithAnnotation;
import org.jsonbuddy.pojo.testclasses.CombinedClassWithSetter;
import org.jsonbuddy.pojo.testclasses.EnumClass;
import org.jsonbuddy.pojo.testclasses.InterfaceWithMethod;
import org.jsonbuddy.pojo.testclasses.PojoMapperOverride;
import org.jsonbuddy.pojo.testclasses.SimpleWithName;
import org.jsonbuddy.pojo.testclasses.SimpleWithNameGetter;
import org.junit.Test;

public class PojoMapperTest {
    @Test
    public void shouldHandleEmptyClass() throws Exception {
        JsonObject empty = JsonFactory.jsonObject();
        SimpleWithNameGetter result = PojoMapper.map(empty, SimpleWithNameGetter.class);
        assertThat(result).isNotNull();
    }

    @Test
    public void shouldHandleClassWithSimpleValueGetter() throws Exception {
        JsonObject jsonObject = JsonFactory.jsonObject().put("name", "Darth Vader");
        SimpleWithNameGetter result = PojoMapper.map(jsonObject, SimpleWithNameGetter.class);
        assertThat(result.getName()).isEqualTo("Darth Vader");
    }

    @Test
    public void shouldIgnoreUnmappedValues() throws Exception {
        JsonObject jsonObject = JsonFactory.jsonObject().put("namex", "Darth Vader");
        SimpleWithNameGetter result = PojoMapper.map(jsonObject, SimpleWithNameGetter.class);
        assertThat(result.getName()).isNull();
    }

    @Test
    public void shouldHandleClassWithFinalField() throws Exception {
        JsonObject jsonObject = JsonFactory.jsonObject().put("name", "Darth Vader");
        SimpleWithName result = PojoMapper.map(jsonObject, SimpleWithName.class);
        assertThat(result.name).isEqualTo("Darth Vader");
    }

    @Test
    public void shouldHandleCombinedClass() throws Exception {
        JsonObject jsonObject = JsonFactory.jsonObject()
                .put("person", JsonFactory.jsonObject().put("name", "Darth Vader"))
                .put("occupation", "Dark Lord of Sith");
        CombinedClass combinedClass = PojoMapper.map(jsonObject, CombinedClass.class);

        assertThat(combinedClass.occupation).isEqualTo("Dark Lord of Sith");
        assertThat(combinedClass.person.name).isEqualTo("Darth Vader");
    }

    @Test
    public void shouldHandleCombinedClassWithGetterSetter() throws Exception {
        JsonObject jsonObject = JsonFactory.jsonObject()
                .put("person", JsonFactory.jsonObject().put("name", "Darth Vader"))
                .put("occupation", "Dark Lord of Sith");
        CombinedClassWithSetter combinedClassWithSetter = PojoMapper.map(jsonObject, CombinedClassWithSetter.class);

        assertThat(combinedClassWithSetter.getPerson().name).isEqualTo("Darth Vader");
        assertThat(combinedClassWithSetter.getOccupation()).isEqualTo("Dark Lord of Sith");
    }


    @Test
    public void shouldHandleDifferentTypes() throws Exception {
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
    public void shouldHandleNumbersAsText() throws Exception {
        JsonObject json = new JsonObject().put("intValue", "13").put("longValue", "14");
        ClassWithNumbers object = PojoMapper.map(json, ClassWithNumbers.class);
        assertThat(object.getIntValue()).isEqualTo(13);
        assertThat(object.getLongValue()).isEqualTo(14);
    }

    @Test
    public void shouldHandleNullNumbers() throws Exception {
        JsonObject json = new JsonObject().put("intValue", null).put("longValue", 12);
        ClassWithNumbers object = PojoMapper.map(json, ClassWithNumbers.class);
        assertThat(object.getLongValue()).isEqualTo(12L);
        assertThat(object.getIntValue()).isNull();
    }

    @Test
    public void shouldThrownOnIllegalAssigments() throws Exception {
        JsonObject json = new JsonObject().put("intValue", true);
        assertThatThrownBy(() -> PojoMapper.map(json, ClassWithNumbers.class))
            .isInstanceOf(CanNotMapException.class)
            .hasMessageContaining("intValue");
    }

    @Test
    public void shouldHandleLists() throws Exception {
        JsonObject jsonObject = JsonFactory.jsonObject()
                .put("name", "Darth")
                .put("children", Arrays.asList("Luke", "Leia"));
        ClassWithList classWithList = PojoMapper.map(jsonObject, ClassWithList.class);
        assertThat(classWithList.children).containsExactly("Luke", "Leia");
    }


    @Test
    public void shouldHandleClassWithInstant() throws Exception {
        Instant now = Instant.now();
        JsonObject jsonObject = JsonFactory.jsonObject().put("time", now);
        ClassWithTime classWithTime = PojoMapper.map(jsonObject, ClassWithTime.class);
        assertThat(classWithTime.getTime()).isEqualTo(now);
    }

    @Test
    public void shouldMapToPojoFromArray() throws Exception {
        assertThat(PojoMapper.map(JsonFactory.jsonArray(), String.class)).isEmpty();
    }

    @Test
    public void shouldHandleEmbeddedJsonElements() throws Exception {
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
    public void shouldHandleClassWithAnnotation() throws Exception {
        JsonObject jsonObject = JsonFactory.jsonObject()
                .put("name", "Darth Vader");
        ClassWithAnnotation classWithAnnotation = PojoMapper.map(jsonObject, ClassWithAnnotation.class);
        assertThat(classWithAnnotation.value).isEqualTo("overridden");
    }

    @Test
    public void shouldHandleClassWithOverriddenNull() throws Exception {
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
    public void shouldHandleClassWithMap() throws Exception {
        JsonObject jsonObject = JsonFactory.jsonObject()
                .put("properties", JsonFactory.jsonObject().put("firstname", "Darth").put("lastname", "Vader"));
        ClassWithMap classWithMap = PojoMapper.map(jsonObject, ClassWithMap.class);

        assertThat(classWithMap.properties.get("firstname")).isEqualTo("Darth");
        assertThat(classWithMap.properties.get("lastname")).isEqualTo("Vader");
    }

    @Test
    public void shouldHandleClassWithPrivateConstructor() throws Exception {
        JsonObject jsonObject = JsonFactory.jsonObject().put("name", "Darth Vader");
        ClassWithPrivateConstructor privateConstr = PojoMapper.map(jsonObject, ClassWithPrivateConstructor.class);
        assertThat(privateConstr.name).isEqualTo("Darth Vader");
    }

    @Test
    public void shouldConvertTextToNumberIfNessesary() throws Exception {
        JsonObject jsonObject = JsonFactory.jsonObject().put("text", "Darth Vader").put("number", "42");
        ClassWithDifferentTypes classWithDifferentTypes = PojoMapper.map(jsonObject, ClassWithDifferentTypes.class);
        assertThat(classWithDifferentTypes.number).isEqualTo(42);

    }

    @Test
    public void shouldHandleClassWithEmbeddedMap() throws Exception {
        JsonObject jsonObject = JsonFactory.jsonObject()
                .put("names",
                        JsonFactory.jsonObject()
                                .put("darth", JsonFactory.jsonObject().put("name", "Darth Vader")));
        ClassWithEmbeddedMap withEmbeddedMap = PojoMapper.map(jsonObject, ClassWithEmbeddedMap.class);
        assertThat(withEmbeddedMap.names.get("darth").name).isEqualTo("Darth Vader");

    }

    @Test
    public void shouldHandleClassWithGetSetEmbeddedMap() throws Exception {
        JsonObject jsonObject = JsonFactory.jsonObject()
                .put("names",
                        JsonFactory.jsonObject()
                                .put("darth", JsonFactory.jsonObject().put("name", "Darth Vader")));
        ClassWithEmbeddedGetSetMap withEmbeddedMap = PojoMapper.map(jsonObject, ClassWithEmbeddedGetSetMap.class);
        assertThat(withEmbeddedMap.getNames().get("darth").name).isEqualTo("Darth Vader");
    }

    @Test
    public void shouldHandleClassWithEnum() throws Exception {
        JsonObject jsonObject = JsonFactory.jsonObject().put("enumNumber", "TWO");
        ClassWithEnum classWithEnum = PojoMapper.map(jsonObject, ClassWithEnum.class);
        assertThat(classWithEnum.enumNumber).isEqualTo(EnumClass.TWO);
    }

    @Test
    public void shouldHandleClassWithMapWithList() throws Exception {
        JsonObject jsonObject = JsonFactory.jsonObject()
                .put("parentAndChildren",
                        JsonFactory.jsonObject().put("Darth", JsonFactory.jsonArray().add("Luke").add("Leia")));
        ClassWithMapWithList withList = PojoMapper.map(jsonObject, ClassWithMapWithList.class);
        assertThat(withList.parentAndChildren.get("Darth")).containsExactly("Luke","Leia");

    }

    @Test
    public void shouldHandleOptionalField() throws Exception {
        ClassWithOptional classWithOptional = PojoMapper.map(JsonFactory.jsonObject(), ClassWithOptional.class);
        assertThat(classWithOptional.optStr).isNull();

        classWithOptional = PojoMapper.map(JsonFactory.jsonObject().put("optStr",new JsonNull()), ClassWithOptional.class);
        assertThat(classWithOptional.optStr.isPresent()).isFalse();

        classWithOptional = PojoMapper.map(JsonFactory.jsonObject().put("optStr","abc"), ClassWithOptional.class);
        assertThat(classWithOptional.optStr).isPresent().contains("abc");

    }

    @Test
    public void shouldHandleOptionalProperty() throws Exception {
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
    public void shouldHandleListWithNulls() throws Exception {
        List<String> withNull = Arrays.asList("one",null,"two");
        JsonArray arr = JsonArray.fromStringList(withNull);
        JsonObject jsonObject = JsonFactory.jsonObject().put("name", "darth").put("children", arr);
        ClassWithList classWithList = PojoMapper.map(jsonObject, ClassWithList.class);
        assertThat(classWithList.children.get(1)).isNull();

    }

    @Test
    public void shouldThrowOnNonApplicableClasses() throws Exception {
        assertThatThrownBy(() -> PojoMapper.map(new JsonObject(), LocalDate.class))
            .isInstanceOf(CanNotMapException.class);
    }

    @Test
    public void shouldHandleListOfOverriddenValues() throws Exception {
        JsonArray jsonArray = JsonFactory.jsonArray()
                .add(JsonFactory.jsonObject())
                .add(JsonFactory.jsonObject());

        List<ClassWithAnnotation> result = PojoMapper.map(jsonArray, ClassWithAnnotation.class);
        assertThat(result).hasSize(2);
        assertThat(result.get(0).value).isEqualTo("overridden");
        assertThat(result.get(1).value).isEqualTo("overridden");
    }

    @Test
    public void shouldHandleCombined() throws Exception {
        JsonObject jsonObject = JsonFactory.jsonObject()
                .put("name", "Darth Vader")
                .put("myHack",JsonFactory.jsonArray().add("Hola"));
        CombinedClassWithAnnotation combinedClassWithAnnotation = PojoMapper.map(jsonObject, CombinedClassWithAnnotation.class);
        assertThat(combinedClassWithAnnotation.myHack.value).isEqualTo("overridden");
    }


    @Test
    public void shouldHandleClassContainingAnnotated() throws Exception {
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
    public void shouldHandleClassContainingMethodAnnotated() throws Exception {
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
    public void shouldHandleBigNumbers() throws Exception {
        JsonObject obj = JsonFactory.jsonObject()
                .put("oneBigInt", 42L)
                .put("oneBigDec", 3.14d);
        ClassWithBigNumbers classWithBigNumbers = PojoMapper.map(obj, ClassWithBigNumbers.class);

        assertThat(classWithBigNumbers.getOneBigInt()).isEqualTo(BigInteger.valueOf(42L));
        assertThat(classWithBigNumbers.getOneBigDec()).isEqualTo(BigDecimal.valueOf(3.14d));
    }

    @Test
    public void shouldParseToInterface() throws Exception {
        JsonObject jsonObject = JsonFactory.jsonObject().put("publicvalue", "A public value");
        InterfaceWithMethod interfaceWithMethod = PojoMapper.map(jsonObject, InterfaceWithMethod.class, new DynamicInterfaceMapper());
        assertThat(interfaceWithMethod).isNotNull();
        assertThat(interfaceWithMethod.getPublicvalue()).isEqualTo("A public value");
    }

    @Test
    public void shouldNotHandleInterfacesWithoiyMapOption() throws Exception {
        JsonObject jsonObject = JsonFactory.jsonObject().put("publicvalue", "A public value");
        assertThatThrownBy(() -> PojoMapper.map(jsonObject, InterfaceWithMethod.class))
            .isInstanceOf(CanNotMapException.class);
    }

    @Test
    public void shouldHandleInterfacesAsFields() throws Exception {
        JsonObject jsonObject = JsonFactory.jsonObject()
                .put("myInterface", JsonFactory.jsonObject().put("publicvalue", "A public value"));
        ClassWithGetterInterface classWithGetterInterface = PojoMapper.map(jsonObject, ClassWithGetterInterface.class,new DynamicInterfaceMapper());
        assertThat(classWithGetterInterface.getMyInterface().getPublicvalue()).isEqualTo("A public value");
    }

    @Test
    public void shouldHandleInterfaceInLists() throws Exception {
        JsonObject jsonObject = JsonFactory.jsonObject().put("myList", JsonArray.fromNodeList(Collections.singletonList(
                JsonFactory.jsonObject().put("publicvalue", "A public value")
        )));

        ClassWithInterfaceListAndMapMethods result = PojoMapper.map(jsonObject, ClassWithInterfaceListAndMapMethods.class,new DynamicInterfaceMapper());
        assertThat(result.getMyList()).hasSize(1);
        assertThat(result.getMyList().get(0).getPublicvalue()).isEqualTo("A public value");
    }

    @Test
    public void shouldHandleInterfaceInMaps() throws Exception {
        JsonObject jsonObject = JsonFactory.jsonObject().put("myMap", JsonFactory.jsonObject().put("intkey",
                JsonFactory.jsonObject().put("publicvalue", "A public value")));
        ClassWithInterfaceListAndMapMethods result = PojoMapper.map(jsonObject, ClassWithInterfaceListAndMapMethods.class,new DynamicInterfaceMapper());
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
}