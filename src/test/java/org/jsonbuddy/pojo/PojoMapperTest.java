package org.jsonbuddy.pojo;


import org.jsonbuddy.JsonArray;
import org.jsonbuddy.JsonObject;
import org.jsonbuddy.pojo.testclasses.*;
import org.junit.Test;

import java.time.Instant;
import java.util.Arrays;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class PojoMapperTest {
    @Test
    public void shouldHandleEmptyClass() throws Exception {
        JsonObject empty = new JsonObject();
        SimpleWithNameGetter result = PojoMapper.map(empty, SimpleWithNameGetter.class);
        assertThat(result).isNotNull();
    }

    @Test
    public void shouldHandleClassWithSimpleValueGetter() throws Exception {
        JsonObject jsonObject = new JsonObject().put("name", "Darth Vader");
        SimpleWithNameGetter result = PojoMapper.map(jsonObject, SimpleWithNameGetter.class);
        assertThat(result.getName()).isEqualTo("Darth Vader");
    }

    @Test
    public void shouldIgnoreUnmappedValues() throws Exception {
        JsonObject jsonObject = new JsonObject().put("namex", "Darth Vader");
        SimpleWithNameGetter result = PojoMapper.map(jsonObject, SimpleWithNameGetter.class);
        assertThat(result.getName()).isNull();
    }

    @Test
    public void shouldHandleClassWithFinalField() throws Exception {
        JsonObject jsonObject = new JsonObject().put("name", "Darth Vader");
        SimpleWithName result = PojoMapper.map(jsonObject, SimpleWithName.class);
        assertThat(result.name).isEqualTo("Darth Vader");
    }

    @Test
    public void shouldHandleCombinedClass() throws Exception {
        JsonObject jsonObject = new JsonObject()
                .put("person", new JsonObject().put("name", "Darth Vader"))
                .put("occupation", "Dark Lord of Sith");
        CombinedClass combinedClass = PojoMapper.map(jsonObject, CombinedClass.class);

        assertThat(combinedClass.occupation).isEqualTo("Dark Lord of Sith");
        assertThat(combinedClass.person.name).isEqualTo("Darth Vader");
    }

    @Test
    public void shouldHandleCombinedClassWithGetterSetter() throws Exception {
        JsonObject jsonObject = new JsonObject()
                .put("person", new JsonObject().put("name", "Darth Vader"))
                .put("occupation", "Dark Lord of Sith");
        CombinedClassWithSetter combinedClassWithSetter = PojoMapper.map(jsonObject, CombinedClassWithSetter.class);

        assertThat(combinedClassWithSetter.getPerson().name).isEqualTo("Darth Vader");
        assertThat(combinedClassWithSetter.getOccupation()).isEqualTo("Dark Lord of Sith");
    }


    @Test
    public void shouldHandleDifferentTypes() throws Exception {
        JsonObject jsonObject = new JsonObject()
                .put("text", "the meaning")
                .put("number", 42)
                .put("bool", true)
                .put("falseBool", false);
        ClassWithDifferentTypes differentTypes = PojoMapper.map(jsonObject, ClassWithDifferentTypes.class);
        assertThat(differentTypes.text).isEqualTo("the meaning");
        assertThat(differentTypes.number).isEqualTo(42);
        assertThat(differentTypes.bool).isTrue();
        assertThat(differentTypes.bool).isTrue();
    }

    @Test
    public void shouldHandleLists() throws Exception {
        JsonObject jsonObject = new JsonObject()
                .put("name", "Darth")
                .put("children", Arrays.asList("Luke", "Leia"));
        ClassWithList classWithList = PojoMapper.map(jsonObject, ClassWithList.class);
        assertThat(classWithList.children).containsExactly("Luke", "Leia");
    }

    @Test
    public void shouldUseOwnMapper() throws Exception {
        JsonObject jsonObject = new JsonObject().put("secret", "Darth");
        PojoMapper pojoMapper = PojoMapper.create().registerClassBuilder(SimpleWithNameGetter.class, new JsonPojoBuilder<SimpleWithNameGetter>() {
            @Override
            public SimpleWithNameGetter build(Object jsonNode) {
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
        JsonObject jsonObject = new JsonObject().put("time", now);
        ClassWithTime classWithTime = PojoMapper.map(jsonObject, ClassWithTime.class);
        assertThat(classWithTime.getTime()).isEqualTo(now);
    }

    @Test
    public void shouldMapToPojoFromArray() throws Exception {
        assertThat(PojoMapper.map(new JsonArray(), String.class)).isEmpty();
    }

    @Test
    public void shouldHandleEmbeddedJsonElements() throws Exception {
        JsonObject jsonObject = new JsonObject()
                .put("name", "Darth Vader")
                .put("myObject", new JsonObject().put("title", "Dark Lord"))
                .put("myArray", new JsonArray().addAll(Arrays.asList("Luke", "Leia")));
        ClassWithJsonElements classWithJsonElements = PojoMapper.map(jsonObject, ClassWithJsonElements.class);

        assertThat(classWithJsonElements.name).isEqualTo("Darth Vader");
        assertThat(classWithJsonElements.myObject.requiredString("title")).isEqualTo("Dark Lord");
        assertThat(classWithJsonElements.myArray.stringStream().collect(Collectors.toList())).containsExactly("Luke", "Leia");

    }

    @Test
    public void shouldHandleClassWithAnnotation() throws Exception {
        JsonObject jsonObject = new JsonObject()
                .put("name", "Darth Vader");
        ClassWithAnnotation classWithAnnotation = PojoMapper.map(jsonObject, ClassWithAnnotation.class);
        assertThat(classWithAnnotation.value).isEqualTo("overridden");
    }

    @Test
    public void shouldHandleClassWithOverriddenNull() throws Exception {
        JsonObject jsonObject = new JsonObject()
                .put("name", "Darth Vader")
                .put("myHack",new JsonObject().put("wont matter","nope"));
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
        JsonObject jsonObject = new JsonObject()
                .put("name", "Darth Vader")
                .put("myHack",new JsonArray().add("Hola"));
        CombinedClassWithAnnotation combinedClassWithAnnotation = PojoMapper.map(jsonObject, CombinedClassWithAnnotation.class);
        assertThat(combinedClassWithAnnotation.myHack.value).isEqualTo("overridden");
    }

    @Test
    public void shouldHandleClassWithMap() throws Exception {
        JsonObject jsonObject = new JsonObject()
                .put("properties", new JsonObject().put("firstname", "Darth").put("lastname", "Vader"));
        ClassWithMap classWithMap = PojoMapper.map(jsonObject, ClassWithMap.class);

        assertThat(classWithMap.properties.get("firstname")).isEqualTo("Darth");
        assertThat(classWithMap.properties.get("lastname")).isEqualTo("Vader");
    }

    @Test
    public void shouldHandleClassWithPrivateConstructor() throws Exception {
        JsonObject jsonObject = new JsonObject().put("name", "Darth Vader");
        ClassWithPrivateConstructor privateConstr = PojoMapper.map(jsonObject, ClassWithPrivateConstructor.class);
        assertThat(privateConstr.name).isEqualTo("Darth Vader");
    }

    @Test
    public void shouldConvertTextToNumberIfNessesary() throws Exception {
        JsonObject jsonObject = new JsonObject().put("text", "Darth Vader").put("number", "42");
        ClassWithDifferentTypes classWithDifferentTypes = PojoMapper.map(jsonObject, ClassWithDifferentTypes.class);
        assertThat(classWithDifferentTypes.number).isEqualTo(42);

    }

    @Test
    public void shouldHandleClassWithEmbeddedMap() throws Exception {
        JsonObject jsonObject = new JsonObject()
                .put("names",
                        new JsonObject()
                                .put("darth", new JsonObject().put("name", "Darth Vader")));
        ClassWithEmbeddedMap withEmbeddedMap = PojoMapper.map(jsonObject, ClassWithEmbeddedMap.class);
        assertThat(withEmbeddedMap.names.get("darth").name).isEqualTo("Darth Vader");

    }

    @Test
    public void shouldHandleClassWithGetSetEmbeddedMap() throws Exception {
        JsonObject jsonObject = new JsonObject()
                .put("names",
                        new JsonObject()
                                .put("darth", new JsonObject().put("name", "Darth Vader")));
        ClassWithEmbeddedGetSetMap withEmbeddedMap = PojoMapper.map(jsonObject, ClassWithEmbeddedGetSetMap.class);
        assertThat(withEmbeddedMap.getNames().get("darth").name).isEqualTo("Darth Vader");
    }

    @Test
    public void shouldHandleClassWithEnum() throws Exception {
        JsonObject jsonObject = new JsonObject().put("enumNumber", "TWO");
        ClassWithEnum classWithEnum = PojoMapper.map(jsonObject, ClassWithEnum.class);
        assertThat(classWithEnum.enumNumber).isEqualTo(EnumClass.TWO);
    }

    @Test
    public void shouldHandleClassWithMapWithList() throws Exception {
        JsonObject jsonObject = new JsonObject()
                .put("parentAndChildren",
                        new JsonObject().put("Darth", new JsonArray().add("Luke").add("Leia")));
        ClassWithMapWithList withList = PojoMapper.map(jsonObject, ClassWithMapWithList.class);
        assertThat(withList.parentAndChildren.get("Darth")).containsExactly("Luke","Leia");

    }


}