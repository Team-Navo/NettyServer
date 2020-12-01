package Util;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class JsonParser {
    private static JSONParser parser = new JSONParser();

    public static JSONObject createJson(String data) throws ParseException {
        return (JSONObject)parser.parse(data);
    }
}

