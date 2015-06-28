package org.jsonbuddy.pojo;


import org.jsonbuddy.JsonFactory;
import org.jsonbuddy.JsonObject;
import org.jsonbuddy.pojo.testclasses.*;
import org.junit.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

public class PojoMapperTest {
    @Test
    public void shouldHandleEmptyClass() throws Exception {
        JsonObject empty = JsonFactory.jsonObject();
        SimpleWithNameGetter result = PojoMapper.map(empty, SimpleWithNameGetter.class);
        assertThat(result).isNotNull();
    }

    @Test
    public void shouldHandleClassWithSimpleValueGetter() throws Exception {
        JsonObject jsonObject = JsonFactory.jsonObject().withValue("name", "Darth Vader");
        SimpleWithNameGetter result = PojoMapper.map(jsonObject, SimpleWithNameGetter.class);
        assertThat(result.getName()).isEqualTo("Darth Vader");
    }

    @Test
    public void shouldHandleClassWithFinalField() throws Exception {
        JsonObject jsonObject = JsonFactory.jsonObject().withValue("name", "Darth Vader");
        SimpleWithName result = PojoMapper.map(jsonObject, SimpleWithName.class);
        assertThat(result.name).isEqualTo("Darth Vader");
    }

    @Test
    public void shouldHandleCombinedClass() throws Exception {
        JsonObject jsonObject = JsonFactory.jsonObject()
                .withValue("person", JsonFactory.jsonObject().withValue("name", "Darth Vader"))
                .withValue("occupation", "Dark Lord of Sith");
        CombinedClass combinedClass = PojoMapper.map(jsonObject, CombinedClass.class);

        assertThat(combinedClass.occupation).isEqualTo("Dark Lord of Sith");
        assertThat(combinedClass.person.name).isEqualTo("Darth Vader");
    }

    @Test
    public void shouldHandleCombinedClassWithGetterSetter() throws Exception {
        JsonObject jsonObject = JsonFactory.jsonObject()
                .withValue("person", JsonFactory.jsonObject().withValue("name", "Darth Vader"))
                .withValue("occupation", "Dark Lord of Sith");
        CombinedClassWithSetter combinedClassWithSetter = PojoMapper.map(jsonObject, CombinedClassWithSetter.class);

        assertThat(combinedClassWithSetter.getPerson().name).isEqualTo("Darth Vader");
        assertThat(combinedClassWithSetter.getOccupation()).isEqualTo("Dark Lord of Sith");
    }


    @Test
    public void shouldHandleDifferentTypes() throws Exception {
        JsonObject jsonObject = JsonFactory.jsonObject()
                .withValue("text", "the meaning")
                .withValue("number", 42)
                .withValue("bool", JsonFactory.jsonTrue());
        ClassWithDifferentTypes differentTypes = PojoMapper.map(jsonObject, ClassWithDifferentTypes.class);
        assertThat(differentTypes.text).isEqualTo("the meaning");
        assertThat(differentTypes.number).isEqualTo(42);
        assertThat(differentTypes.bool).isTrue();
    }

    @Test
    public void shouldHandleLists() throws Exception {
        JsonObject jsonObject = JsonFactory.jsonObject()
                .withValue("name", "Darth")
                .withValue("children", Arrays.asList("Luke", "Leia"));
        ClassWithList classWithList = PojoMapper.map(jsonObject, ClassWithList.class);
        assertThat(classWithList.children).containsExactly("Luke","Leia");
    }

    @Test
    public void shouldUseOwnMapper() throws Exception {
        JsonObject jsonObject = JsonFactory.jsonObject().withValue("secret", "Darth");
        PojoMapper pojoMapper = PojoMapper.create().registerClassBuilder(SimpleWithNameGetter.class, new JsonPojoBuilder<SimpleWithNameGetter>() {
            @Override
            public SimpleWithNameGetter build(JsonObject jsonObject) {

                SimpleWithNameGetter res = new SimpleWithNameGetter();
                res.setName(jsonObject.value("secret").get().textValue());
                return res;
            }
        });
        SimpleWithNameGetter simpleWithNameGetter = pojoMapper.mapToPojo(jsonObject, SimpleWithNameGetter.class);
        assertThat(simpleWithNameGetter.getName()).isEqualTo("Darth");

    }
}