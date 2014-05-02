package com.netflix.fabricator.guice.mapping;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.Map;

import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import com.google.inject.util.Types;
import com.netflix.fabricator.ConfigurationNode;
import com.netflix.fabricator.component.ComponentFactory;

public class ComponentFactoryBinding implements BindingReslove {
    @Override
    public boolean execute(String name, Object obj, ConfigurationNode config, Class<?> argType, Injector injector, Method method) throws Exception {
        
        ParameterizedType componentFactoryProviderType = Types.newParameterizedTypeWithOwner(Provider.class, ComponentFactory.class, argType);
        
        // Look for a MapBinder binding
        TypeLiteral<Map<String, ?>> mapLiteral = (TypeLiteral<Map<String, ?>>) TypeLiteral.get(
                Types.mapOf(String.class, componentFactoryProviderType));
        Binding<Map<String, ?>> mapBinding = injector.getExistingBinding(Key.get(mapLiteral));
        if (mapBinding != null) {
            Map<String, ?> map = mapBinding.getProvider().get();
            if (map.containsKey(name)) {
                method.invoke(obj, map.get(name));
                return true;
            }
        }
        return false;
    }
}
