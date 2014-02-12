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

public class NoImplementationTest {
    
    @Type("somecomponent")
    public static class SomeComponent  {
        public static class Builder {
            private int value = 0;
            public void withProperty(int value) {
                this.value = value;
            }
            
            public SomeComponent build() {
                return new SomeComponent(this);
            }
        }
        
        private final int value;
        
        public static Builder builder() {
            return new Builder();
        }

        private SomeComponent(Builder builder) {
            this.value = builder.value;
        }

        public int getProperty() {
            return value;
        }
    }
    
    @Test
    public void test() throws ComponentCreationException, ComponentAlreadyExistsException {
        Properties props = new Properties();
        props.setProperty("id1.NIWS.property", "123");
        
        Injector injector = Guice.createInjector(
                new PropertiesConfigurationModule(props),
                new ComponentModuleBuilder<SomeComponent>() 
                    .manager(SynchronizedComponentManager.class)
                    .typename("NIWS")
                    .build(SomeComponent.class)
                );
            
        ComponentManager<SomeComponent> manager = injector.getInstance(Key.get(new TypeLiteral<ComponentManager<SomeComponent>>() {}));
            
        SomeComponent component = manager.get("id1");
        Assert.assertEquals(123, component.getProperty());
    }
}
