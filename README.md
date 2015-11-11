## Status
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.jsonbuddy/jsonbuddy/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.jsonbuddy/jsonbuddy)
[![Build Status](https://travis-ci.org/anders88/jsonbuddy.png)](https://travis-ci.org/anders88/jsonbuddy)
# jsonbuddy

A JSON parser for Java 8. The aim of jsonbuddy is to make building
and traversing ad hoc JSON object structures fluent and easy.

In order to achieve this, jsonbuddy:

* Uses lambdas to simplify mapping to JSON objects and arrays
* Uses lambdas to simplify extracting data from JSON objects and arrays
* Uses Optional values instead of nulls
* Has convenience methods that throws if values are missing


# Features
Parses json to a java object structure. Generates json

# Usage
# Maven
Jsononbuddy is on maven central. Add to your pom
```xml
<dependency>
	<groupId>org.jsonbuddy</groupId>
	<artifactId>jsonbuddy</artifactId>
	<version>0.5.1</version>
</dependency>
```

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
JsonObject o = (JsonObject)JsonParser.parseToObject(jsonString);
String name = node.requiredString("name"); // = Darth Vader
```
You can also parse from an InputString or a reader. If you expect the json you can use the convenience method
```java
String jsonString = "{\"name\":\"Darth Vader\"}";
JsonObject jsonObject = JsonJsonParser.parseToObject(jsonString);
```
This will cast an exception if the result is not an object. The same method can be used for json arrays.

## Traversing parsed result
```java
JsonObject object = new JsonObject()
        .put("bool", true)
        .put("double", 0.0)
        .put("enum", Thread.State.WAITING)
        .put("instant", instant)
        .put("node", new JsonArray())
        .put("array", Arrays.asList("a", "b"));

object.requiredBoolean("bool"); // true
object.booleanValue("missing"); // Optional.empty
object.requiredArray("array").strings(); // List<String> of "a", "b"
```


## Building json (JsonNode to String)
Generating json as string
```java
JsonObject jsonObject = JsonFactory.jsonObject()
        .put("name", "Darth Vader");
String jsonString = jsonObject.toJson(); // {"name":"Darth Vader"}
```
You can also send a PrintWriter, and the result will be written to the writer.

## Json to POJO
...


# Todos
* Strict mode in Pojo mapping
* Better fetching in nested elements
* BigDecimal support


# Version history

Version | Description
------- | -------------
0.1.0   | Skeleton version. Parses and generates json. No error handling or performance tuning.
0.2.0   | New API without factories. Errorhandling. Basic testing and tuning
0.3.0   | NA - see version 0.3.1
0.3.1   | Converting to POJO. Support of java.time.Instant. Convenience methods and bugfixes.
0.4.0   | Bugfixes and more convenience methods. 
0.4.1   | Bugfix. Handling spaces after numbers
0.4.2   | Bugfix. Json Instant sends as string
0.5.0   | Renamed method to make naming more consequent. Now uses put instead of withValue. We use jsonString, JsonNumber (who can hold all kinds of numbers integers and numbers with decimals). JsonBoolean and JsonNull. SimpleValue are now called JsonValue.
0.5.1   | Bugfix. Parsing nested tables

# Licence
Copyright © 2015 Anders Karlsen

Distributed under the Apache License, Version 2.0  (http://www.apache.org/licenses/LICENSE-2.0)