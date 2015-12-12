package org.jsonbuddy.pojo;


import org.jsonbuddy.JsonFactory;
import org.jsonbuddy.JsonNode;
import org.jsonbuddy.JsonNull;
import org.jsonbuddy.JsonObject;
import org.jsonbuddy.pojo.testclasses.*;
import org.junit.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.StrictAssertions.assertThatThrownBy;

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
            .isInstanceOf(IllegalArgumentException.class)
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
    public void shouldUseOwnMapper() throws Exception {
        JsonObject jsonObject = JsonFactory.jsonObject().put("secret", "Darth");
        PojoMapper pojoMapper = PojoMapper.create().registerClassBuilder(SimpleWithNameGetter.class, new JsonPojoBuilder<SimpleWithNameGetter>() {
            @Override
            public SimpleWithNameGetter build(JsonNode jsonNode) {
                SimpleWithNameGetter res = new SimpleWithNameGetter();
                res.setName(((JsonObject)jsonNode).requiredString("secret"));
                return res;
            }
        });
        SimpleWithNameGetter simpleWithNameGetter = pojoMapper.mapToPojo(jsonObject, SimpleWithNameGetter.class);
        assertThat(simpleWithNameGetter.getName()).isEqualTo("Darth");
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
    public void shouldHandleCombined() throws Exception {
        JsonObject jsonObject = JsonFactory.jsonObject()
                .put("name", "Darth Vader")
                .put("myHack",JsonFactory.jsonArray().add("Hola"));
        CombinedClassWithAnnotation combinedClassWithAnnotation = PojoMapper.map(jsonObject, CombinedClassWithAnnotation.class);
        assertThat(combinedClassWithAnnotation.myHack.value).isEqualTo("overridden");
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
    public void shouldThrowOnNonApplicableClasses() throws Exception {
        assertThatThrownBy(() -> PojoMapper.map(new JsonObject(), LocalDate.class))
            .isInstanceOf(CanNotMapException.class);
    }
}