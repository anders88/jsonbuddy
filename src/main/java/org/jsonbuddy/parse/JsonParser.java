package org.jsonbuddy.parse;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import org.jsonbuddy.JsonArray;
import org.jsonbuddy.JsonBoolean;
import org.jsonbuddy.JsonFactory;
import org.jsonbuddy.JsonNode;
import org.jsonbuddy.JsonNull;
import org.jsonbuddy.JsonNumber;
import org.jsonbuddy.JsonObject;
import org.jsonbuddy.JsonValue;

/**
 * Create a JsonNode from an input Reader. Use {@link #parse} to parse any
 * primitive or complex JsonNode, {@link #parseToArray(Reader)} to parse a JsonArray
 * or {@link #parseToObject(Reader)} to parse a JsonObject.
 */
public class JsonParser {

    /**
     * Parse the reader as a JsonNode. Will return a JsonArray, JsonArray
     * or a JsonValue.
     *
     * @throws JsonParseException if a JSON syntax error was encountered
     * @throws IOException if there was an error reading the data from the Reader
     */
    public static JsonNode parseNode(Reader reader) throws IOException, JsonParseException {
        return new JsonParser(reader).parseValue();
    }

    /**
     * Parse the String as a JsonNode. Will return a JsonArray, JsonArray
     * or a JsonValue.
     *
     * @throws JsonParseException if a JSON syntax error was encountered
     */
    public static JsonNode parse(String input) throws JsonParseException  {
        try {
            return parseNode(new StringReader(input));
        } catch (IOException e) {
            throw new RuntimeException("Should never happen with StringReader", e);
        }
    }

    /**
     * Parse the InputStream as a JsonNode. Will return a JsonArray, JsonArray
     * or a JsonValue.
     *
     * @throws JsonParseException if a JSON syntax error was encountered
     */
    public static JsonNode parse(InputStream inputStream) throws JsonParseException, IOException {
        return parseNode(new InputStreamReader(inputStream));
    }

    /**
     * @see JsonObject#parse(String)
     * @deprecated Use {@link JsonObject#read} instead
     */
    public static JsonObject parseToObject(String input) throws JsonParseException  {
        return JsonObject.parse(input);
    }

    /**
     * @see JsonObject#read(Reader)
     * @deprecated Use {@link JsonObject#read} instead
     */
    public static JsonObject parseToObject(Reader reader) throws JsonParseException {
        try {
            return JsonObject.read(reader);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @see JsonObject#read(InputStream)
     * @deprecated Use {@link JsonObject#read} instead
     */
    public static JsonObject parseToObject(InputStream inputStream) throws JsonParseException {
        try {
            return JsonObject.read(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @see JsonArray#parse(String)
     * @deprecated Use {@link JsonArray#parse} instead
     */
    public static JsonArray parseToArray(String input) throws JsonParseException  {
        return JsonArray.parse(input);
    }

    /**
     * @see JsonArray#read(InputStream)
     * @deprecated Use {@link JsonArray#parse} instead
     */
    public static JsonArray parseToArray(InputStream input) throws JsonParseException  {
        try {
            return JsonArray.read(input);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @see JsonArray#parse(String)
     * @deprecated Use {@link JsonArray#parse} instead
     */
    public static JsonArray parseToArray(Reader input) throws JsonParseException {
        try {
            return JsonArray.read(input);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Parse base64encoded JSON string to JSONNode. Will return a JsonArray, JsonArray
     * or a JsonValue.

     * @throws JsonParseException if a JSON syntax error was encountered
     * @throws IllegalArgumentException if input not base64encoded
     */
    public static JsonNode parseFromBase64encodedString(String base64encodedJson) throws IllegalArgumentException {
        return parse(new String(Base64.getUrlDecoder().decode(base64encodedJson)));
    }

    private Reader reader;
    private char lastRead;
    private boolean finished;

    private JsonParser(Reader reader) throws IOException {
        this.reader = reader;
        readNext();
    }

    private void readNext() throws IOException {
        int read = reader.read();
        if (read == -1) {
            finished = true;
            return;
        }
        this.lastRead = (char) read;
    }

    private JsonNode parseValue() throws IOException {
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


    private JsonValue parseNumberValue() throws IOException {
        StringBuilder val = new StringBuilder();
        boolean isDouble = false;
        while (!finished && (Character.isDigit(lastRead) || ".eE-+".contains("" + lastRead))) {
            isDouble = isDouble || ".eE".contains("" + lastRead);
            val.append(lastRead);
            readNext();
        }
        if (!finished && (!(Character.isSpaceChar(lastRead) || "}],".contains("" + lastRead))) && (!"\n\r\t".contains("" + lastRead))) {
            throw new JsonParseException("Illegal value '" + val + lastRead + "'");
        }
        if (val.length() > 20) {
            return new JsonNumber(new BigDecimal(val.toString()));
        }
        if (isDouble) {
            return new JsonNumber(Double.parseDouble(val.toString()));
        }
        return new JsonNumber(Long.parseLong(val.toString()));
    }


    private JsonNull parseNullValue() throws IOException {
        expectValue("null");
        return new JsonNull();
    }

    private JsonValue parseBooleanValue() throws IOException {
        boolean isTrue = (lastRead == 't');
        String expect = isTrue ? "true" : "false";
        expectValue(expect);
        return new JsonBoolean(isTrue);
    }

    private void expectValue(String value) throws IOException {
        StringBuilder res = new StringBuilder();
        for (int i=0;i<value.length() && !finished;i++) {
            res.append(lastRead);
            readNext();
        }
        if (!res.toString().equals(value)) {
            throw new JsonParseException(String.format("Unexpected value %s",res.toString()));
        }
    }

    private JsonArray parseArray() throws IOException {
        JsonArray jsonArray = new JsonArray();
        while (lastRead != ']') {
            do {
                readNext();
            } while (Character.isWhitespace(lastRead));
            if (lastRead == ']') {
                break;
            }
            JsonNode jsonArrayValue = parseValue();
            jsonArray.add(jsonArrayValue);
            readSpaceUntil("Expected , or ] in array", ']', ',');
        }
        readNext();
        return jsonArray;
    }

    private JsonValue parseStringValue() throws IOException {
        readNext();
        String value = readText();
        return JsonFactory.jsonString(value);
    }

    private JsonObject parseObject() throws IOException {
        JsonObject jsonObject = new JsonObject();
        while (lastRead != '}') {
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
        readNext();
        return jsonObject;
    }

    private String readText() throws IOException {
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
                        res.append(readUnicodeValue());
                        break;
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

    private String readUnicodeValue() throws IOException {
        StringBuilder code = new StringBuilder();
        for (int i=0;i<4;i++) {
            readNext();
            if (finished) {
                throw new JsonParseException("JsonString not closed. Ended in escape sequence");
            }
            code.append(lastRead);
        }

        int unicode;
        try {
            unicode = Integer.parseInt(code.toString(), 16);
        } catch (NumberFormatException e) {
            throw new JsonParseException("Illegal unicode sequence " + code);
        }
        return Character.toString((char)unicode);
    }

    private void readSpaceUntil(String errormessage, Character... readUntil) throws IOException {
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
