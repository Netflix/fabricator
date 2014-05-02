package com.netflix.fabricator.guice.mapping;

import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.util.Types;
import com.netflix.fabricator.ConfigurationNode;
import com.netflix.fabricator.component.ComponentFactory;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;

/**
 * Created by hyuan on 1/17/14.
 */
public class CompositeExistingBinding implements BindingReslove {
    @Override
    public boolean execute(String name, Object obj, ConfigurationNode node, Class<?> argType, Injector injector, Method method) throws Exception {
        ParameterizedType subType = Types.newParameterizedType(ComponentFactory.class, argType);
        Key<ComponentFactory<?>> subKey = (Key<ComponentFactory<?>>) Key.get(subType);
        Binding<ComponentFactory<?>> binding = injector.getExistingBinding(subKey);
        if (binding != null) {
            ComponentFactory<?> factory = injector.getInstance(subKey);
            if (factory != null) {
                Object subObject = factory.create(node);
                method.invoke(obj, subObject);
                return true;
            }
        }
        return false;
    }
}
