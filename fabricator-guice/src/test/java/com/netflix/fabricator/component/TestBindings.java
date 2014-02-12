package com.netflix.fabricator.component;

import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.netflix.fabricator.annotations.Type;
import com.netflix.fabricator.component.exception.ComponentAlreadyExistsException;
import com.netflix.fabricator.component.exception.ComponentCreationException;
import com.netflix.fabricator.guice.ComponentModuleBuilder;
import com.netflix.fabricator.properties.PropertiesConfigurationModule;

public class TestBindings {
    @Type("somecomponent") 
    public static class SomeComponent  {
        public static class Builder {
            private Class<?> clazz = null;
            public void withClazz(Class<?> clazz) {
                this.clazz = clazz;
            }
            
            public SomeComponent build() {
                return new SomeComponent(this);
            }
        }
        
        private final Class<?> clazz;
        
        public static Builder builder() {
            return new Builder();
        }

        private SomeComponent(Builder builder) {
            this.clazz = builder.clazz;
        }

        public Class<?> getClazz() {
            return clazz;
        }
    }
    
    @Test
    public void test() throws ComponentCreationException, ComponentAlreadyExistsException {

        Properties props = new Properties();
        props.setProperty("id1.somecomponent.clazz", "java.lang.String");
        
        Injector injector = Guice.createInjector(
                new PropertiesConfigurationModule(props),
                new ComponentModuleBuilder<SomeComponent>() 
                    .manager(SynchronizedComponentManager.class)
                    .build(SomeComponent.class)
                );
            
        ComponentManager<SomeComponent> manager = injector.getInstance(Key.get(new TypeLiteral<ComponentManager<SomeComponent>>() {}));
            
        SomeComponent component = manager.get("id1");
        Assert.assertEquals(String.class, component.getClazz());
    }
}
