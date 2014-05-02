package com.netflix.fabricator.archaius;

import java.util.Map;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;

import com.netflix.config.ConfigurationManager;
import com.netflix.fabricator.ConfigurationNode;

public class ArchaiusTypeConfigurationResolverTest {
    @Test
    public void testReadAll() {
        Properties properties = new Properties();
        properties.put("id1.sometype.a", "_a1");
        properties.put("id2.sometype", "{\"type\":\"_type\",\"a\":\"_a2\"}");
        properties.put("id3.sometype", "{\"type\":\"_type\",\"a\":\"_a3\"}");
        
        System.out.println(properties);
        properties.put("id1.someothertype.a", "_a");
        
        ConfigurationManager.loadProperties(properties);
        
        ArchaiusTypeConfigurationResolver resolver = new ArchaiusTypeConfigurationResolver(null);
        Map<String, ConfigurationNode> someTypeConfigs = resolver.getConfigurationFactory("sometype").getAllConfigurations();
        Assert.assertEquals(3, someTypeConfigs.keySet().size());
        Assert.assertEquals("_a1", someTypeConfigs.get("id1").getChild("a").getValue(String.class));
        Assert.assertEquals("_a2", someTypeConfigs.get("id2").getChild("a").getValue(String.class));
        Assert.assertEquals("_a3", someTypeConfigs.get("id3").getChild("a").getValue(String.class));
        
        Map<String, ConfigurationNode> someOtherTypeConfigs = resolver.getConfigurationFactory("someothertype").getAllConfigurations();
        Assert.assertEquals(1, someOtherTypeConfigs.keySet().size());
        Assert.assertEquals("_a", someOtherTypeConfigs.get("id1").getChild("a").getValue(String.class));
    }
}
