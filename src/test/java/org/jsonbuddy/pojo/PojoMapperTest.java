package org.jsonbuddy.pojo;


import org.jsonbuddy.JsonFactory;
import org.jsonbuddy.JsonObject;
import org.jsonbuddy.pojo.testclasses.CombinedClass;
import org.jsonbuddy.pojo.testclasses.CombinedClassWithSetter;
import org.jsonbuddy.pojo.testclasses.SimpleWithName;
import org.jsonbuddy.pojo.testclasses.SimpleWithNameGetter;
import org.junit.Test;

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
}