## Status
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.jsonbuddy/jsonbuddy/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.jsonbuddy/jsonbuddy)
[![Build Status](https://travis-ci.org/anders88/jsonbuddy.png)](https://travis-ci.org/anders88/jsonbuddy)

# jsonbuddy

A JSON parser for Java 8. The aim of jsonbuddy is to make building
and traversing ad hoc JSON object structures fluent and easy.

In order to achieve this, jsonbuddy:

* Has many convenience methods to build and processes JSON
* Uses Optionals to return non-required values
* Has convenience methods that throws if values are missing
* Uses lambdas to simplify mapping to JSON objects and arrays
* Uses lambdas to simplify extracting data from JSON objects and arrays


## Features

* Parse JSON to untyped Java objects
* Build JSON object structures fluently
* Extract JSON values
* Automatically map Java object field values and properties to JSON
* Automatically read JSON into Java object with fields and properties

## Usage

### Maven

Jsononbuddy is on maven central. Add to your pom
```xml
<dependency>
	<groupId>org.jsonbuddy</groupId>
	<artifactId>jsonbuddy</artifactId>
	<version>0.5.1</version>
</dependency>
```

### Usage summary

Convert from     | Convert to       | Use
-----------------|------------------|--------------------------------------------
String or Reader | JsonNode         | JsonParser.parse(input)
JsonNode         | String or Writer | jsonNode.toJson(writer) or JsonNode.toString()
JsonNode         | POJO             | PojoMapper.map(jsonNode,POJO.class)
POJO             | JsonNode         | JsonGenerator.generate(pojo)

### Parsing JSON (String to JsonNode)

Parsing a string to a JsonNode

```java
String jsonString = "{\"name\":\"Darth Vader\"}";
JsonObject o = (JsonObject)JsonParser.parse(jsonString);
String name = node.requiredString("name"); // = Darth Vader
```

You can also parse from a Reader. If you expect the json to be an object you can use the convenience method parseToObject.

```java
String jsonString = "{\"name\":\"Darth Vader\"}";
JsonObject jsonObject = JsonJsonParser.parseToObject(jsonString);
```

This will cast an exception if the result is not an object. You can similary use parseToArray to get a JsonArray.

### Building JSON (JsonNode to String)

Generating JSON as string

```java
JsonObject jsonObject = new JsonObject()
        .put("name", "Darth Vader");
String jsonString = jsonObject.toJson(); // {"name":"Darth Vader"}
```

You can also send a PrintWriter, and the result will be written to the writer.

The builder syntax is optimized for fluent building of complex hierarchies:

```java
JsonObject orderJson = new JsonObject()
    .put("customer", new JsonObject()
            .put("name", "Darth Vader")
            .put("address", "Death Star"))
    .put("date", Instant.now())
    .put("status", OrderStatus.COMPLETE)
    .put("tags", JsonArray.fromStrings("urgent", "international"))
    .put("orderLines", new JsonArray()
            .add(new JsonObject().put("productId", 1).put("amount", 400.5))
            .add(new JsonObject().put("productId", 2).put("amount", 11.5)));
```

This also works well with complex Java objects:

```java
JsonObject orderJson = new JsonObject()
    .put("customer", new JsonObject()
            .put("name", order.getCustomer().getName())
            .put("address", order.getCustomer().getAddress()))
    .put("date", order.getOrderDate())
    .put("status", order.getStatus())
    .put("tags", JsonArray.fromStringList(order.getTagList()))
    .put("orderLines", JsonArray.map(order.getOrderLines(), line -> {
            return new JsonObject()
                    .put("productId", line.getProductId())
                    .put("amount", line.getAmount());
    }));
```


### Traversing parsed result (JsonObject to Java objects)

```java
JsonObject object = new JsonObject()
        .put("bool", true)
        .put("double", 0.0)
        .put("enum", Thread.State.WAITING)
        .put("node", new JsonArray())
        .put("array", Arrays.asList("a", "b"));

object.requiredBoolean("bool"); // true
object.booleanValue("missing"); // Optional.empty
object.requiredArray("array").strings(); // List<String> of "a", "b"
```

The syntax also works well for constructing complex objects

```java
Order order = new Order();
order.setCustomer(orderJson.objectValue("customer")
        .map(customerJson -> {
                Customer customer = new Customer();
                customer.setName(customerJson.requiredString("name"));
                customer.setAddress(customerJson.requiredString("address"));
                return customer;
        }).orElse(null));
order.setOrderDate(orderJson.requiredInstant("date"));
order.setStatus(orderJson.requiredEnum("status", OrderStatus.class)); // TODO
order.setTagList(orderJson.requiredArray("tags").strings());
order.setOrderLines(orderJson.requiredArray("orderLines").objects(
        lineJson ->  new OrderLine(lineJson.requiredLong("productId"), lineJson.requiredDouble("amount"))));
```

### Json to POJO


```java
JsonObject jsonObject = JsonFactory.jsonObject()
        .put("name", "Darth Vader");
PojoMapper pojoMapper = new PojoMapper();
SithLord darth = pojoMapper.mapToPojo(jsonObject);
darth.getName(); // Returns "Darth Vader"
```


# TODO

* Strict mode in Pojo mapping
* Better fetching in nested elements


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