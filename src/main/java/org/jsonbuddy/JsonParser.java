package org.jsonbuddy;

import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class JsonParser {
    private Reader reader;
    private char lastRead;
    private boolean finished;

    private JsonParser(Reader reader) {
        this.reader = reader;
        readNext();
    }

    private void readNext()  {
        int read = 0;
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


    public static JsonNode parse(Reader reader) {
        JsonParser jsonParser = new JsonParser(reader);
        JsonFactory jsonFactory = jsonParser.parseValue();
        return Optional.ofNullable(jsonFactory).map(JsonFactory::create).orElse(null);
    }

    private JsonFactory parseValue() {
        while (!finished) {
            switch (lastRead) {
                case '{':
                    return parseObject();
                case '"':
                    return parseStringValue();
            }
            readNext();
        }
        return null;
    }

    private JsonSimpleValueFactory parseStringValue() {
        readNext();
        String value = readUntil('"');
        return JsonSimpleValueFactory.text(value);
    }

    private JsonObjectFactory parseObject() {
        JsonObjectFactory jsonObjectFactory = JsonFactory.jsonObject();
        while (true) {
            readUntil('}','"');
            if (lastRead == '}') {
                return jsonObjectFactory;
            }
            readNext();
            String key = readUntil('"');
            readUntil(':');
            JsonFactory value = parseValue();
            jsonObjectFactory.withValue(key,value);
            readNext();
        }
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
