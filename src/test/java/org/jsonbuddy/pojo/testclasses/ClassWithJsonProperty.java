package org.jsonbuddy.pojo.testclasses;

import org.jsonbuddy.JsonNode;

public class ClassWithJsonProperty {
    private JsonNode jsonNode;

    public void setJsonNode(JsonNode jsonNode) {
        this.jsonNode = jsonNode;
    }

    public JsonNode getJsonNode() {
        return jsonNode;
    }
}
