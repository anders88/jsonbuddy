package org.jsonbuddy;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class JsonParser {
    public static JsonNode parse(Reader reader) {
        JsonParser jsonParser = new JsonParser(reader);
        JsonFactory jsonFactory = jsonParser.parseValue();
        return Optional.ofNullable(jsonFactory).map(JsonFactory::create).orElse(null);
    }


    public static JsonNode parse(InputStream inputStream) {
        return parse(new InputStreamReader(inputStream));
    }

    public static JsonNode parse(String input) {
        return parse(new StringReader(input));
    }


    private Reader reader;
    private char lastRead;
    private boolean finished;

    private JsonParser(Reader reader) {
        this.reader = reader;
        readNext();
    }

    private void readNext()  {
        int read;
        try {
            read = reader.read();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (read == -1) {
            finished = true;
            return;
        }
        this.lastRead = (char) read;
    }



    private JsonFactory parseValue() {
        while (!finished) {
            switch (lastRead) {
                case '{':
                    return parseObject();
                case '[':
                    return parseArray();
                case '"':
                    return parseStringValue();
                case 't':
                case 'f':
                    return parseBooleanValue();
            }
            readNext();
        }
        return null;
    }

    private JsonSimpleValueFactory<JsonBooleanValue> parseBooleanValue() {
        boolean isTrue = (lastRead == 't');
        String expect = isTrue ? "true" : "false";
        expectValue(expect);
        return isTrue ? JsonSimpleValueFactory.trueValue() : JsonSimpleValueFactory.falseValue();
    }

    private void expectValue(String value) {
        for (int i=0;i<value.length();i++) {
            readNext();
        }
    }

    private JsonArrayFactory parseArray() {
        JsonArrayFactory jsonArrayFactory = JsonFactory.jsonArray();
        while (lastRead != ']') {
            readNext();
            JsonFactory jsonFactory = parseValue();
            jsonArrayFactory.add(jsonFactory);
            readUntil(']',',');
        }
        return jsonArrayFactory;
    }

    private JsonSimpleValueFactory<JsonTextValue> parseStringValue() {
        readNext();
        String value = readUntil('"');
        return JsonSimpleValueFactory.text(value);
    }

    private JsonObjectFactory parseObject() {
        JsonObjectFactory jsonObjectFactory = JsonFactory.jsonObject();
        while (lastRead != '}') {
            readUntil('}','"');
            if (lastRead == '}') {
                return jsonObjectFactory;
            }
            readNext();
            String key = readUntil('"');
            readUntil(':');
            JsonFactory value = parseValue();
            jsonObjectFactory.withValue(key,value);
            readUntil(',','}');
        }
        return jsonObjectFactory;
    }

    private String readUntil(Character... readUntil) {
        List<Character> until = Arrays.asList(readUntil);
        StringBuilder res = new StringBuilder();
        while (!(finished || until.contains(lastRead))) {
            res.append(lastRead);
            readNext();
        }
        return res.toString();
    }
}
