package com.netflix.fabricator.jackson;

import java.util.Properties;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;

import com.netflix.fabricator.jackson.JacksonConfigurationSource;

public class JacksonConfigurationSourceTest {

    @Test
    public void test() throws Exception {
        String text = 
                "{\"properties\":{"
               + "   \"a\":\"_a\"," 
               + "   \"b\":\"_b\"," 
               + "   \"c\":\"_c\"" 
               + "}"
               + "}";
        
        ObjectMapper mapper = new ObjectMapper();
        JsonFactory factory = mapper.getJsonFactory(); // since 2.1 use mapper.getFactory() instead
        JsonParser jp = factory.createJsonParser(text);
        JsonNode node = mapper.readTree(jp);

        Properties prop1 = new Properties();
        prop1.setProperty("a", "_a");
        prop1.setProperty("b", "_b");
        prop1.setProperty("c", "_c");
        
        JacksonConfigurationSource source = new JacksonConfigurationSource("key1", "type1", node);
        Properties prop2 = source.getValue("properties", Properties.class);
        Assert.assertEquals(prop1, prop2);
        System.out.println(prop1);
    }

}
