package org.jsonbuddy;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class JsonParser {
    public static JsonNode parse(Reader reader) throws JsonParseException {
        JsonParser jsonParser = new JsonParser(reader);
        JsonFactory jsonFactory = jsonParser.parseValue();
        return Optional.ofNullable(jsonFactory).map(JsonFactory::create).orElse(null);
    }


    public static JsonNode parse(InputStream inputStream) throws JsonParseException  {
        return parse(new InputStreamReader(inputStream));
    }

    public static JsonNode parse(String input) throws JsonParseException  {
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
                case 'n':
                    return parseNullValue();
            }
            if (lastRead == '-' || Character.isDigit(lastRead)) {
                return parseNumberValue();
            }
            readNext();
        }
        return null;
    }


    private JsonFactory parseNumberValue() {
        StringBuilder val = new StringBuilder();
        boolean isDouble = false;
        while (Character.isDigit(lastRead) || ".eE-".contains("" + lastRead)) {
            isDouble = isDouble || ".eE".contains("" + lastRead);
            val.append(lastRead);
            readNext();
        }
        if (isDouble) {
            return JsonSimpleValueFactory.doubleNumber(Double.parseDouble(val.toString()));
        }
        return JsonSimpleValueFactory.longNumber(Long.parseLong(val.toString()));
    }


    private JsonSimpleValueFactory<JsonNullValue> parseNullValue() {
        expectValue("null");
        return JsonSimpleValueFactory.nullValue();
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
        while (!(finished || lastRead == ']')) {
            readNext();
            JsonFactory jsonFactory = parseValue();
            jsonArrayFactory.add(jsonFactory);
            readUntil(Optional.of("JsonArray not closed. Expected ]"),']',',');
        }
        if (finished) {
            throw new JsonParseException("JsonArray not closed. Expected ]");
        }
        return jsonArrayFactory;
    }

    private JsonSimpleValueFactory<JsonTextValue> parseStringValue() {
        readNext();
        String value = readText();
        return JsonSimpleValueFactory.text(value);
    }

    private JsonObjectFactory parseObject() {
        JsonObjectFactory jsonObjectFactory = JsonFactory.jsonObject();
        while (!(finished || lastRead == '}')) {
            readUntil(Optional.of("JsonObject not closed. Expected }"),'}','"');
            if (lastRead == '}') {
                return jsonObjectFactory;
            }
            readNext();
            String key = readUntil(Optional.of("Expecting \" to end object key"), '"');
            readUntil(Optional.of("Expected value for objectkey " + key),':');
            JsonFactory value = parseValue();
            jsonObjectFactory.withValue(key,value);
            readUntil(Optional.of("JsonObject not closed. Expected }"),',','}');
        }
        if (finished) {
            throw new JsonParseException("JsonObject not closed. Expected }");
        }
        return jsonObjectFactory;
    }

    private String readText() {
        StringBuilder res = new StringBuilder();
        while (!(finished || lastRead == '"')) {
            if (lastRead == '\\') {
                readNext();
                if (finished) {
                    // Todo this is error
                    break;
                }
                switch (lastRead) {
                    case '"':
                        res.append("\"");
                        break;
                    case '\\':
                        res.append("\\");
                        break;
                    case '/':
                        res.append("/");
                        break;
                    case 'b':
                        res.append("\b");
                        break;
                    case 'f':
                        res.append("\f");
                        break;
                    case 'n':
                        res.append("\n");
                        break;
                    case 't':
                        res.append("\t");
                        break;
                    case 'u':
                        throw new RuntimeException("\\u Not supported yet");
                }
            } else {
                res.append(lastRead);
            }
            readNext();
        }
        return res.toString();
    }

    private String readUntil(Optional<String> errormessage,Character... readUntil) {
        List<Character> until = Arrays.asList(readUntil);
        StringBuilder res = new StringBuilder();
        while (!(finished || until.contains(lastRead))) {
            res.append(lastRead);
            readNext();
        }
        if (finished) {
            if (errormessage.isPresent()) {
                throw new JsonParseException(errormessage.get());
            }
            String expecting = until.stream().map(c -> "'" + c + "'").reduce((a, b) -> a + "," + b).get();
            throw new JsonParseException("Expecting one of " + expecting + " before end");
        }
        return res.toString();
    }
}
