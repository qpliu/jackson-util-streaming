package com.yrek.jackson.util.streaming;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.util.JsonParserDelegate;

public class DeletingParser extends JsonParserDelegate {
    private final Map<String,Object> deletes;
    private final LinkedList<Map<String,Object>> frames;
    private String fieldName;

    public DeletingParser(JsonParser d, Map<String,Object> deletes) {
        super(d);
        this.deletes = deletes;
        this.frames = new LinkedList<Map<String,Object>>();
        this.fieldName = null;
    }

    private boolean deleted(String key) {
        Map<String,Object> frame = frames.peek();
        if (frame == null || !frame.containsKey(key))
            return false;
        if (frame.get(key) instanceof Map)
            return false;
        return true;
    }

    @Override
    public JsonToken nextToken() throws IOException, JsonParseException {
        for (;;) {
            JsonToken token = super.nextToken();
            if (token == null)
                return null;
            switch (token) {
            case START_OBJECT:
                if (frames.isEmpty()) {
                    frames.push(deletes);
                } else {
                    Map<String,Object> frame = frames.peek();
                    if (frame == null) {
                        frames.push(null);
                    } else {
                        @SuppressWarnings("unchecked") Map<String,Object> tmp = (Map<String,Object>) frame.get(fieldName);
                        frames.push(tmp);
                    }
                }
                return token;
            case END_OBJECT:
                frames.pop();
                return token;
            case FIELD_NAME:
                fieldName = getText();
                if (!deleted(fieldName))
                    return token;
                super.nextToken();
                skipChildren();
                break;
            default:
                return token;
            }
        }
    }
}
