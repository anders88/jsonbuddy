# jsonbuddy
The json parser for Java 8. This project is under construction. The api may change.

# Features
Parses json to a java object structure. Generates json

# Usage
## Usage summary
Convert from | Convert to|Use
------------ | ----------|-----
Text,input stream or reader | JsonNode | JsonParser.parse(input)
JsonNode | String or writer | jsonNode.toJson(writer) or jsNode.toString()
JsonNode | POJO | PojoMapper.map(jsonNode,POJO.class)
POJO     | JsonNode | JsonGenerator.generate(pojo)

## Parsing json (String to JsonNode)
Parsing a string to a jsonnode
```java
String jsonString = "{\"name\":\"Darth Vader\"}";
JsonNode node = JsonParser.parse(jsonString);
String name = node.requiredString("name"); // = Darth Vader
```
You can also parse from an InputString or a reader. If you expect the json you can use the convinience method
```java
String jsonString = "{\"name\":\"Darth Vader\"}";
JsonObject jsonObject = JsonJsonParser.parseToObject(jsonString);
```
This will cast an exception if the result is not an object. The same method can be used for json arrays.

## Traversing parsed result
...


## Generating json (Jsnode to String)
Generating json as string
```java
JsonObject jsonObject = JsonFactory.jsonObject()
        .withValue("name", "Darth Vader");
String jsonString = jsonObject.toJson(); // {"name":"Darth Vader"}
```
You can also send a PrintWriter, and the result will be written to the writer.

## Json to POJO
...

# Planned in version 0.4.0
* Object.withValue with streams as input to add arrays
* Even more forgiving when dealing with numbers and string (converts as appropriate)
* Better fetching in nested elements

# Todos later
* Strict mode in Pojo mapping

# Version history

Version | Description
------- | -------------
0.1.0   | Skeleton version. Parses and generates json. No error handling or performance tuning.
0.2.0   | New api without factories. Errorhandling. Basic testing and tuning
0.3.0   | NA - see version 0.3.1
0.3.1   | Converting to POJO. Support of java.time.Instant. Convenience methods and bugfixes.

# Licence
Copyright © 2015 Anders Karlsen

Distributed under the Apache License, Version 2.0  (http://www.apache.org/licenses/LICENSE-2.0)