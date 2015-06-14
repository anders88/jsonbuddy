# jsonbuddy
Java8 json parser. This project is under construction. The api nay change.

# Features
Parses json to a java object structure. Generates json

# Usage
## Parsing json
Parsing a string to a node
```java
String jsonString = "{\"name\":\"Darth Vader\"}";
JsonNode node = JsonJsonParser.parse(jsonString);
String name = node.requiredString("name"); // = Darth Vader
```
## Generating json
Generating json as string
```java
JsonObject jsonObject = JsonFactory.jsonObject()
        .withValue("name", "Darth Vader");
String jsonString = jsonObject.toJson(); // {"name":"Darth Vader"}
```

# Todo
The following list is not complete
- Parse from and to POJOs

# Version history

Version | Description
------- | -------------
0.1.0   | Skeleton version. Parses and generates json. No error handling or performance tuning.
0.2.0   | Not released yet.


# Licence
Copyright © 2015 Anders Karlsen

Distributed under the Eclipse Public License (http://www.eclipse.org/legal/epl-v10.html)