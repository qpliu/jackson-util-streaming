package com.yrek.jackson.util.streaming;

import java.io.InputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.Base64Variant;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.SerializableString;

public class PatchingGenerator extends FilterJsonGenerator {
    private static class Frame {
        final Object patches;
        final HashSet<String> seen;
        final boolean replace;
        final boolean skip;

        Frame(Object patches, boolean replace, boolean skip) {
            this.patches = patches;
            this.seen = new HashSet<String>();
            this.replace = replace;
            this.skip = skip;
        }
    }

    private static final Frame SKIP_FRAME = new Frame(null, false, true);
    private static final Frame COPY_FRAME = new Frame(null, false, false);

    private final LinkedList<Frame> frames;

    public PatchingGenerator(JsonGenerator d, Map<String,Object> patches) {
        super(d);
        this.frames = new LinkedList<Frame>();
        frames.push(new Frame(patches, false, false));

    }

    private boolean skipOrReplace() throws IOException, JsonGenerationException {
        if (frames.peek().replace) {
            writePatchValue(frames.peek().patches);
            return true;
        }
        return skip();
    }

    private boolean skip() {
        return frames.peek().skip;
    }

    @Override
    public void writeStartArray() throws IOException, JsonGenerationException {
        boolean skip = skipOrReplace();
        frames.push(skip ? SKIP_FRAME : COPY_FRAME);
        if (!skip)
            super.writeStartArray();
    }

    @Override
    public void writeEndArray() throws IOException, JsonGenerationException {
        if (!skip())
            super.writeEndArray();
        frames.pop();
    }

    @Override
    public void writeStartObject() throws IOException, JsonGenerationException {
        Frame frame = frames.peek();
        frames.push(frame);
        if (!frame.skip)
            super.writeStartObject();
        else if (frame.replace)
            writePatchValue(frame.patches);
    }

    @Override
    public void writeEndObject() throws IOException, JsonGenerationException {
        frames.pop();
        if (!skip()) {
            Frame frame = frames.peek();
            if (frame.patches != null && frame.patches instanceof Map)
                for (Map.Entry<?,?> e : ((Map<?,?>) frame.patches).entrySet())
                    if (!frame.seen.contains(e.getKey().toString())) {
                        super.writeFieldName(e.getKey().toString());
                        writePatchValue(e.getValue());
                    }
            super.writeEndObject();
        }
    }

    @Override
    public void writeFieldName(String name) throws IOException, JsonGenerationException {
        frames.pop();
        Frame objectFrame = frames.peek();
        if (objectFrame.skip) {
            frames.push(SKIP_FRAME);
        } else {
            objectFrame.seen.add(name);
            Object patches = null;
            if (objectFrame.patches != null && objectFrame.patches instanceof Map)
                patches = ((Map) objectFrame.patches).get(name);
            frames.push(new Frame(patches, patches != null, objectFrame.skip || (patches != null && !(patches instanceof Map))));
            super.writeFieldName(name);
        }
    }

    @Override
    public void writeFieldName(SerializableString name) throws IOException, JsonGenerationException {
        writeFieldName(name.getValue());
    }

    private void writePatchValue(Object value) throws IOException, JsonGenerationException {
        if (value == null) {
            super.writeNull();
        } else if (value instanceof String) {
            super.writeString((String) value);
        } else if (value instanceof Integer) {
            super.writeNumber(((Integer) value).intValue());
        } else if (value instanceof Long) {
            super.writeNumber(((Long) value).longValue());;
        } else if (value instanceof Double) {
            super.writeNumber(((Double) value).doubleValue());
        } else if (value instanceof Float) {
            super.writeNumber(((Float) value).floatValue());
        } else if (value instanceof Boolean) {
            super.writeBoolean(Boolean.TRUE.equals(value));
        } else if (value instanceof List) {
            super.writeStartArray();
            for (Object item : (List) value)
                writePatchValue(item);
            super.writeEndArray();
        } else if (value instanceof Map) {
            super.writeStartObject();
            for (Map.Entry<?,?> e : ((Map<?,?>) value).entrySet()) {
                super.writeFieldName(e.getKey().toString());
                writePatchValue(e.getValue());
            }
            super.writeEndObject();
        }
    }

    @Override
    public void writeString(String text) throws IOException,JsonGenerationException {
        if (!skipOrReplace())
            super.writeString(text);
    }

    @Override
    public void writeString(char[] text, int offset, int len) throws IOException, JsonGenerationException {
        if (!skipOrReplace())
            super.writeString(text, offset, len);
    }

    @Override
    public void writeString(SerializableString text) throws IOException, JsonGenerationException {
        if (!skipOrReplace())
            super.writeString(text);
    }

    @Override
    public void writeRawUTF8String(byte[] text, int offset, int length) throws IOException, JsonGenerationException {
        if (!skipOrReplace())
            super.writeRawUTF8String(text, offset, length);
    }

    @Override
    public void writeUTF8String(byte[] text, int offset, int length) throws IOException, JsonGenerationException {
        if (!skipOrReplace())
            super.writeUTF8String(text, offset, length);
    }

    @Override
    public void writeRaw(String text) throws IOException, JsonGenerationException {
        if (!skipOrReplace())
            super.writeRaw(text);
    }

    @Override
    public void writeRaw(String text, int offset, int len) throws IOException, JsonGenerationException {
        if (!skipOrReplace())
            super.writeRaw(text, offset, len);
    }

    @Override
    public void writeRaw(SerializableString raw) throws IOException, JsonGenerationException {
        if (!skipOrReplace())
            super.writeRaw(raw);
    }

    @Override
    public void writeRaw(char[] text, int offset, int len) throws IOException, JsonGenerationException {
        if (!skipOrReplace())
            super.writeRaw(text, offset, len);
    }

    @Override
    public void writeRaw(char c) throws IOException, JsonGenerationException {
        if (!skipOrReplace())
            super.writeRaw(c);
    }

    @Override
    public void writeRawValue(String text) throws IOException, JsonGenerationException {
        if (!skipOrReplace())
            super.writeRawValue(text);
    }

    @Override
    public void writeRawValue(String text, int offset, int len) throws IOException, JsonGenerationException {
        if (!skipOrReplace())
            super.writeRawValue(text, offset, len);
    }

    @Override
    public void writeRawValue(char[] text, int offset, int len) throws IOException, JsonGenerationException {
        if (!skipOrReplace())
            super.writeRawValue(text, offset, len);
    }

    @Override
    public void writeBinary(Base64Variant b64variant, byte[] data, int offset, int len) throws IOException, JsonGenerationException {
        if (!skipOrReplace())
            super.writeBinary(b64variant, data, offset, len);
    }

    @Override
    public int writeBinary(Base64Variant b64variant, InputStream data, int dataLength) throws IOException, JsonGenerationException {
        if (skipOrReplace()) {
            data.skip(dataLength);
            return dataLength;
        } else {
            return super.writeBinary(b64variant, data, dataLength);
        }
    }

    @Override
    public void writeNumber(short v) throws IOException, JsonGenerationException {
        if (!skipOrReplace())
            super.writeNumber(v);
    }

    @Override
    public void writeNumber(int v) throws IOException, JsonGenerationException {
        if (!skipOrReplace())
            super.writeNumber(v);
    }

    @Override
    public void writeNumber(long v) throws IOException, JsonGenerationException {
        if (!skipOrReplace())
            super.writeNumber(v);
    }

    @Override
    public void writeNumber(BigInteger v) throws IOException, JsonGenerationException {
        if (!skipOrReplace())
            super.writeNumber(v);
    }

    @Override
    public void writeNumber(double v) throws IOException, JsonGenerationException {
        if (!skipOrReplace())
            super.writeNumber(v);
    }

    @Override
    public void writeNumber(float v) throws IOException, JsonGenerationException {
        if (!skipOrReplace())
            super.writeNumber(v);
    }

    @Override
    public void writeNumber(BigDecimal v) throws IOException, JsonGenerationException {
        if (!skipOrReplace())
            super.writeNumber(v);
    }

    @Override
    public void writeNumber(String encodedValue) throws IOException, JsonGenerationException, UnsupportedOperationException {
        if (!skipOrReplace())
            super.writeNumber(encodedValue);
    }

    @Override
    public void writeBoolean(boolean state) throws IOException, JsonGenerationException {
        if (!skipOrReplace())
            super.writeBoolean(state);
    }

    @Override
    public void writeNull() throws IOException, JsonGenerationException {
        if (!skipOrReplace())
            super.writeNull();
    }

    @Override
    public void writeObject(Object pojo) throws IOException,JsonProcessingException {
        if (!skipOrReplace())
            super.writeObject(pojo);
    }
}
