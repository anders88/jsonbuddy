## Status
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.jsonbuddy/jsonbuddy/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.jsonbuddy/jsonbuddy)
[![Build](https://github.com/anders88/jsonbuddy/actions/workflows/maven.yml/badge.svg)](https://github.com/anders88/jsonbuddy/actions/workflows/maven.yml)
[![Coverage Status](https://coveralls.io/repos/anders88/jsonbuddy/badge.svg?branch=master&service=github)](https://coveralls.io/github/anders88/jsonbuddy?branch=master)

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
	<version>0.17</version>
</dependency>
```

### Usage summary

Convert from     | Convert to       | Use
-----------------|------------------|--------------------------------------------
String or Reader | JsonNode         | JsonParser.parseNode(input)
JsonNode         | String or Writer | jsonNode.toJson(writer) or JsonNode.toString()
JsonNode         | POJO             | PojoMapper.map(jsonNode,POJO.class)
POJO             | JsonNode         | JsonGenerator.generate(pojo)

### Parsing JSON (String to JsonNode)

Parsing a string to a JsonObject

```jshelllanguage
String jsonString = "{\"name\":\"Darth Vader\"}";
JsonObject o = JsonObject.parse(jsonString);
String name = node.requiredString("name"); // = Darth Vader
```

You can also parse from a Reader.

```jshelllanguage
String jsonString = "{\"name\":\"Darth Vader\"}";
JsonObject jsonObject = JsonObject.parse(jsonString);
```

This will cast an exception if the result is not an object. You can similary use `JsonArray.parse` to get a `JsonArray`.

### Building JSON (JsonNode to String)

Generating JSON as string

```jshelllanguage
JsonObject jsonObject = new JsonObject()
        .put("name", "Darth Vader");
String jsonString = jsonObject.toJson(); // {"name":"Darth Vader"}
```

You can also send a PrintWriter, and the result will be written to the writer.

The builder syntax is optimized for fluent building of complex hierarchies:

```jshelllanguage
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

```jshelllanguage
JsonObject orderJson = new JsonObject()
    .put("customer", new JsonObject()
            .put("name", order.getCustomer().getName())
            .put("address", order.getCustomer().getAddress()))
    .put("date", order.getOrderDate())
    .put("status", order.getStatus())
    .put("tags", JsonArray.fromStringList(order.getTagList()))
    .put("orderLines", JsonArray.map(order.getOrderLines(), line ->
            new JsonObject()
                .put("productId", line.getProductId())
                .put("amount", line.getAmount())
    ));
```


### Traversing parsed result (JsonObject to Java objects)

```jshelllanguage
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

```jshelllanguage
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


```jshelllanguage
JsonObject jsonObject = JsonFactory.jsonObject()
        .put("name", "Darth Vader");
PojoMapper pojoMapper = new PojoMapper();
SithLord darth = pojoMapper.mapToPojo(jsonObject);
darth.getName(); // Returns "Darth Vader"
```

### Mapping to interfaces
You can map Json to an interface using the DynamicInterfaceMapper mapping rule.

```java
public interface NameInterface {
    String getName();
}
public class Main {
    public static void main(String[] args) {
        JsonObject jsonobject = JsonFactory.jsonObject().put("name","Darth Vader");
        NameInterface nameInterface = PojoMapper.map(jsonObject, NameInterface.class,new DynamicInterfaceMapper());
        System.out.println(nameInterface.getName()); // = "Darth Vader"
    }
}
```

Jsonbuddy uses Bytebuddy (bytebuddy.net) to generate a runtime implementation of the interface. You need to supply bytebuddy as a maven dependency when using DynamicInterfaceMapper.

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
0.6.0   | Buffixes. A little documentation. Make apis of JsonArray and JsonObject more simular.
0.6.1   | ObjectStream from array. Parsing from InputStream reintroduced
0.7.0   | Using Number class to handle numbers. Supporting Optional. Minow tweaks
0.7.1   | Bugfix to handle integers as floats and doubles
0.7.2   | Bugfix to ovverriden classes as part of list in POJO mapping
0.8.0   | Unicode support (Supports \u)
0.8.1   | Bugfix to support unicode u008
0.9.0   | Pojo mapper supports BigDecimal and BigInteger
0.10.0  | Supports pojo mapping and generation with interfaces
0.10.1  | Bugfix Pojo mapper with empty key in jsonobject
0.11.0  | Using mappingrules instead of feature-enum to handle PojoMapping. Bytebuddy is optional dependency. Default use declaring classes when generating json.
0.12.0  | PutAll feature and parse from Base64
0.13.0  | Static fields are not used in JsonGeneration. PojoMapping rule supporting Enum. Better handling of null values in DynamicInterface mapping
0.13.1  | Bugfix. Fixes issue #26 and #27
0.13.2  | Bugfix. Generate objects that overridesjsongeneration when putting to jsonobject
0.14.0  | Support for UUID and fix for temporals
0.14.1  | Bugfix
0.14.2  | Bugfix parse exponential number
0.15    | Better support for reading and writing to the network, e.g. `JsonObject.parse(HttpURLConnection)`. Bugfix on parsing `"[  ]"`
0.16    | Bugfix
0.17    | More flexible generation and mapping with POJOs. Automatically map to any parametrized type with PojoMapper#mapToType. Customize mapping and generation work JsonGenerator#addConverter, PojoMapper#addStringConverter and PojoMapper#addNumberConverter
0.18    | Underscore serialization support, Include properties from superclass, Support for Optional types, Error handling for mapping of List and Maps


# Licence

Copyright © 2015-2020 Anders Karlsen

Distributed under the Apache License, Version 2.0  (http://www.apache.org/licenses/LICENSE-2.0)
