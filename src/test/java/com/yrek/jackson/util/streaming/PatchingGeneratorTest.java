package com.yrek.jackson.util.streaming;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;

import org.junit.Assert;
import org.junit.Test;

public class PatchingGeneratorTest {
    @Test
    public void testPatch() throws Exception {
        Map<String,Object> patches = new LinkedHashMap<String,Object>();
        ArrayList<Object> list = new ArrayList<Object>();
        patches.put("d", list);
        list.add(false);
        list.add(null);
        list.add(1);
        patches.put("e", 5);
        Map<String,Object> apatch = new LinkedHashMap<String,Object>();
        patches.put("a", apatch);
        apatch.put("a", 0);
        apatch.put("c", 2);

        final String testJson = "{\"a\":{\"a\":[1,2,3,{\"b\":false,\"c\":null}],\"b\":1},\"b\":[1,2,3],\"c\":true,\"d\":4}";
        final String patchedJson = "{\"a\":{\"a\":0,\"b\":1,\"c\":2},\"b\":[1,2,3],\"c\":true,\"d\":[false,null,1],\"e\":5}";
        JsonFactory jf = new JsonFactory();
        JsonParser jp = jf.createParser(testJson);
        StringWriter sw = new StringWriter();
        JsonGenerator jg = new PatchingGenerator(jf.createGenerator(sw), patches);
        jp.nextToken();
        jg.copyCurrentStructure(jp);
        jg.flush();
        Assert.assertEquals(patchedJson, sw.toString());
    }
}
