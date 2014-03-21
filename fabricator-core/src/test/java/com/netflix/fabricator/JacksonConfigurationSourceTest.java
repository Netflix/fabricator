package com.netflix.fabricator;

import java.util.Properties;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;

import com.google.inject.Guice;
import com.netflix.fabricator.jackson.JacksonComponentConfiguration;
import com.netflix.governator.lifecycle.LifecycleManager;

public class JacksonConfigurationSourceTest {
    private static String json = 
            "{\"properties\":{"
           + "   \"a\":\"_a\"," 
           + "   \"b\":\"_b\"," 
           + "   \"c\":\"_c\"" 
           + "}"
           + "}";
    

    @Test
    public void test() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(json);

        Properties prop1 = new Properties();
        prop1.setProperty("a", "_a");
        prop1.setProperty("b", "_b");
        prop1.setProperty("c", "_c");
        
        JacksonComponentConfiguration source = new JacksonComponentConfiguration("key1", "type1", node);
        Properties prop2 = source.getValue("properties", Properties.class);
        Assert.assertEquals(prop1, prop2);
        System.out.println(prop1);
    }
}
