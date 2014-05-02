package com.netflix.fabricator;

import java.util.Map;
import java.util.Properties;

import com.google.common.annotations.Beta;
import com.google.common.collect.Maps;
import com.netflix.fabricator.component.ComponentFactory;
import com.netflix.fabricator.component.ComponentManager;
import com.netflix.fabricator.component.bind.SimplePropertyBinderFactoryResolver;
import com.netflix.fabricator.properties.PropertiesTypeConfigurationResolver;

/**
 * Standalone version of Fabricator instead of integrating with a DI framework.
 * This should mainly be used for testing or when a DI framework is not desired.
 * @author elandau
 */
@Beta
public class Fabricator {
    public static class Builder {
        private TypeConfigurationResolver configurationResolver;
        
        public Builder forProperties(Properties props) {
            configurationResolver = new PropertiesTypeConfigurationResolver(props, null);
            return this;
        }
        
        public Fabricator build() {
            return new Fabricator(this);
        }
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    private final TypeConfigurationResolver resolver;
    private final Map<Class<?>, ComponentManager<?>> factories = Maps.newHashMap();
    private final PropertyBinderResolver binderResolver = new SimplePropertyBinderFactoryResolver();
    
    Fabricator(Builder builder) {
        this.resolver = builder.configurationResolver;
    }
    
    public <T> T get(String id, Class<T> type) throws Exception {
        ComponentManager<T> manager = (ComponentManager<T>) factories.get(type);
        if (manager == null) {
            ComponentFactory<T> factory = new BindingComponentFactory<T>(type, binderResolver, null).get();
            
//          manager = new SynchronizedComponentManager<T>(ComponentType.from(type), );
//          factories.put(type, factory);
//          private final Map<Class<?>, ComponentManager<?>> factories = Maps.newHashMap();

        }
        
//      public SynchronizedComponentManager(
//              ComponentType<T>                 type,
//              Map<String, ComponentFactory<T>> factories, 
//              TypeConfigurationResolver        config) {

        return null;
    }
}
