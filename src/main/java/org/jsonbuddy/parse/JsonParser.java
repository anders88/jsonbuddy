package org.jsonbuddy.parse;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.jsonbuddy.JsonArray;
import org.jsonbuddy.JsonBoolean;
import org.jsonbuddy.JsonFactory;
import org.jsonbuddy.JsonNode;
import org.jsonbuddy.JsonNull;
import org.jsonbuddy.JsonNumber;
import org.jsonbuddy.JsonObject;
import org.jsonbuddy.JsonValue;

public class JsonParser {

    public static JsonNode parse(Reader reader) throws JsonParseException {
        JsonParser jsonParser = new JsonParser(reader);
        return jsonParser.parseValue();
    }

    public static JsonNode parse(InputStream inputStream) throws JsonParseException  {
        return parse(new InputStreamReader(inputStream));
    }

    public static JsonNode parse(String input) throws JsonParseException  {
        return parse(new StringReader(input));
    }

    public static JsonObject parseToObject(InputStream inputStream) throws JsonParseException  {
        return parseToObject(new InputStreamReader(inputStream));
    }

    public static JsonObject parseToObject(String input) throws JsonParseException  {
        return parseToObject(new StringReader(input));
    }

    public static JsonObject parseToObject(Reader reader) throws JsonParseException {
        JsonParser jsonParser = new JsonParser(reader);
        return toObject(jsonParser.parseValue());
    }

    private static JsonObject toObject(JsonNode result) {
        if (!(result instanceof JsonObject)) {
            throw new JsonParseException("Expected json object got " + Optional.ofNullable(result).map(Object::getClass).map(Object::toString).orElse("null"));
        }
        return (JsonObject) result;
    }

    public static JsonArray parseToArray(InputStream inputStream) throws JsonParseException  {
        return parseToArray(new InputStreamReader(inputStream));
    }

    public static JsonArray parseToArray(String input) throws JsonParseException  {
        return parseToArray(new StringReader(input));
    }

    public static JsonArray parseToArray(Reader reader) throws JsonParseException {
        JsonParser jsonParser = new JsonParser(reader);
        return toArray(jsonParser.parseValue());
    }

    private static JsonArray toArray(JsonNode result) {
        if (!(result instanceof JsonArray)) {
            throw new JsonParseException("Expected json array got " + Optional.ofNullable(result).map(Object::getClass).map(Object::toString).orElse("null"));
        }
        return (JsonArray) result;
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



    private JsonNode parseValue() {
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
            if (!(Character.isWhitespace(lastRead))) {
                throw new JsonParseException("Unexpected character '" + lastRead + "'");
            }
            readNext();
        }
        return null;
    }


    private JsonValue parseNumberValue() {
        StringBuilder val = new StringBuilder();
        boolean isDouble = false;
        while (!finished && (Character.isDigit(lastRead) || ".eE-".contains("" + lastRead))) {
            isDouble = isDouble || ".eE".contains("" + lastRead);
            val.append(lastRead);
            readNext();
        }
        if (!finished && (!(Character.isSpaceChar(lastRead) || "}],".contains("" + lastRead))) && (!"\n\r\t".contains("" + lastRead))) {
            throw new JsonParseException("Illegal value '" + val + lastRead + "'");
        }
        if (isDouble) {
            return new JsonNumber(Double.parseDouble(val.toString()));
        }
        return new JsonNumber(Long.parseLong(val.toString()));
    }


    private JsonNull parseNullValue() {
        expectValue("null");
        return new JsonNull();
    }

    private JsonValue parseBooleanValue() {
        boolean isTrue = (lastRead == 't');
        String expect = isTrue ? "true" : "false";
        expectValue(expect);
        return new JsonBoolean(isTrue);
    }

    private void expectValue(String value) {
        StringBuilder res = new StringBuilder();
        for (int i=0;i<value.length() && !finished;i++) {
            res.append(lastRead);
            readNext();
        }
        if (!res.toString().equals(value)) {
            throw new JsonParseException(String.format("Unexpected value %s",res.toString()));
        }
    }

    private JsonArray parseArray() {
        JsonArray jsonArray = new JsonArray();
        while (!(finished || lastRead == ']')) {
            readNext();
            if (lastRead == ']') {
                break;
            }
            JsonNode jsonArrayValue = parseValue();
            jsonArray.add(jsonArrayValue);
            readSpaceUntil("Expected , or ] in array", ']', ',');
        }
        if (finished) {
            throw new JsonParseException("Expected , or ] in array");
        } else {
            readNext();
        }
        return jsonArray;
    }

    private JsonValue parseStringValue() {
        readNext();
        String value = readText();
        return JsonFactory.jsonString(value);
    }

    private JsonObject parseObject() {
        JsonObject jsonObject = new JsonObject();
        while (!(finished || lastRead == '}')) {
            readSpaceUntil("JsonObject not closed. Expected }", '}', '"');
            if (lastRead == '}') {
                readNext();
                return jsonObject;
            }
            readNext();
            String key = readText();
            readSpaceUntil("Expected value for objectkey " + key, ':');
            readNext();
            if (finished) {
                throw new JsonParseException("Expected value for key " + key);
            }
            JsonNode value = parseValue();
            jsonObject.put(key, value);
            readSpaceUntil("JsonObject not closed. Expected }", ',', '}');
        }
        if (finished) {
            throw new JsonParseException("JsonObject not closed. Expected }");
        }
        readNext();
        return jsonObject;
    }

    private String readText() {
        StringBuilder res = new StringBuilder();
        while (!(finished || lastRead == '"')) {
            if (lastRead == '\\') {
                readNext();
                if (finished) {
                    throw new JsonParseException("JsonString not closed. Ended in escape sequence");
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
        if (finished) {
            throw new JsonParseException("JsonString not closed. Expected \"");
        }
        return res.toString();
    }

    private void readSpaceUntil(String errormessage, Character... readUntil) {
        List<Character> until = Arrays.asList(readUntil);
        if (until.contains(lastRead)) {
            return;
        }
        readNext();
        while (!(finished || until.contains(lastRead))) {
            if (!Character.isWhitespace(lastRead)) {
                throw new JsonParseException(errormessage);
            }
            readNext();
        }
        if (finished) {
            throw new JsonParseException(errormessage);
        }
    }

}
