package com.netflix.fabricator.component;

import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.netflix.fabricator.component.exception.ComponentAlreadyExistsException;
import com.netflix.fabricator.component.exception.ComponentCreationException;
import com.netflix.fabricator.guice.ComponentModuleBuilder;
import com.netflix.fabricator.properties.PropertiesConfigurationModule;

public class TestLifecycle {
    
    @Test
    public void test() throws ComponentCreationException, ComponentAlreadyExistsException {

        Properties props = new Properties();
        props.setProperty("id1.simple.clazz", "java.lang.String");
        
        Injector injector = Guice.createInjector(
                new PropertiesConfigurationModule(props),
                new ComponentModuleBuilder<SimpleComponent>() 
                    .manager(SynchronizedComponentManager.class)
                    .build(SimpleComponent.class)
                );
            
        ComponentManager<SimpleComponent> manager = injector.getInstance(Key.get(new TypeLiteral<ComponentManager<SimpleComponent>>() {}));
            
        SimpleComponent component = manager.get("id1");
        Assert.assertTrue(component.wasPostConstructCalled());
        
        manager.remove("id1");
        Assert.assertTrue(component.wasPreDestroyCalled());
    }
}
