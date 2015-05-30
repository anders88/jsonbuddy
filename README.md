# jsonbuddy
Java8 json parser. This project is under construction. The api nay change.

# Features
Parses json to a java object structure. Generates json

# Usage
## Parsing json
Parsing a string to a node
```java
String jsonString = "{\"name\":\"Darth Vader \"}";
JsonNode node = JsonJsonParser.parse(jsonString);
```
## Generating json
Generating json as string
```java
JsonObject jsonObject = JsonObjectFactory.jsonObject()
        .withValue("name", JsonSimpleValueFactory.text("Darth Vader"))
        .create();
String jsonString = jsonObject.toJson();
```

# Todo
The following list is not complete
- Handling illegal json - Give a sensible errormessage
- Performance testing and tuning
- Javadoc for core functions
- Parse from and to POJOs

# Licence
Copyright © 2015 Anders Karlsen

Distributed under the Eclipse Public License (http://www.eclipse.org/legal/epl-v10.html)