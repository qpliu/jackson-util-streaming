package com.yrek.jackson.streaming;

import java.io.StringWriter;
import java.util.HashMap;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;

import org.junit.Assert;
import org.junit.Test;

public class DeletingParserTest {
    private String generate(JsonFactory jf, JsonParser jp) throws Exception {
        jp.nextToken();
        StringWriter sw = new StringWriter();
        JsonGenerator jg = jf.createGenerator(sw);
        jg.copyCurrentStructure(jp);
        jg.flush();
        return sw.toString();
    }

    @Test
    public void testDelete() throws Exception {
        final String testJson = "{\"a\":{\"a\":[1,2,3,{\"b\":false,\"c\":null}],\"b\":1},\"b\":[1,2,3],\"c\":true,\"d\":4}";
        JsonFactory jf = new JsonFactory();
        HashMap<String,Object> deletes = new HashMap<String,Object>();
        HashMap<String,Object> adeletes = new HashMap<String,Object>();
        deletes.put("a", adeletes);
        adeletes.put("a", null);
        deletes.put("d", null);

        Assert.assertEquals("{\"a\":{\"b\":1},\"b\":[1,2,3],\"c\":true}", generate(jf, new DeletingParser(jf.createParser(testJson), deletes)));
    }
}
