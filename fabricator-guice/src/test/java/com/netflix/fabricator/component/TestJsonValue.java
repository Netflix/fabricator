package com.netflix.fabricator.component;

import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.netflix.fabricator.annotations.Type;
import com.netflix.fabricator.component.TestBindings.SomeComponent;
import com.netflix.fabricator.component.TestBindings.SomeComponent.Builder;
import com.netflix.fabricator.component.exception.ComponentAlreadyExistsException;
import com.netflix.fabricator.component.exception.ComponentCreationException;
import com.netflix.fabricator.guice.ComponentModuleBuilder;
import com.netflix.fabricator.properties.PropertiesConfigurationModule;

public class TestJsonValue {
    private static String json = 
            "{"
           + "   \"type\":\"somecomponent\","
           + "   \"a\":\"_a\""
           + "}";

    @Type("somecomponent") 
    public static class SomeComponent  {
        public static class Builder {
            private String a = null;
            
            public void withA(String a) {
                this.a = a;
            }
            
            public SomeComponent build() {
                return new SomeComponent(this);
            }
        }
        
        private final String a;
        
        public static Builder builder() {
            return new Builder();
        }

        private SomeComponent(Builder builder) {
            this.a = builder.a;
        }

        public String getA() {
            return a;
        }
    }
    
    @Test
    public void test() throws ComponentCreationException, ComponentAlreadyExistsException {

        Properties props = new Properties();
        props.setProperty("id1.somecomponent", json);
        
        Injector injector = Guice.createInjector(
                new PropertiesConfigurationModule(props),
                new ComponentModuleBuilder<SomeComponent>() 
                    .manager(SynchronizedComponentManager.class)
                    .build(SomeComponent.class)
                );
            
        ComponentManager<SomeComponent> manager = injector.getInstance(Key.get(new TypeLiteral<ComponentManager<SomeComponent>>() {}));
            
        SomeComponent component = manager.get("id1");
        Assert.assertEquals("_a", component.getA());
    }
}
