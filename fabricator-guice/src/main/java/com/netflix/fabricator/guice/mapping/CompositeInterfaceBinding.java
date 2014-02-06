package com.netflix.fabricator.guice.mapping;

import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.util.Types;
import com.netflix.fabricator.ComponentConfiguration;
import com.netflix.fabricator.component.ComponentFactory;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * Created by hyuan on 1/17/14.
 */
public class CompositeInterfaceBinding implements BindingReslove {
    private final String propertyName;

    public CompositeInterfaceBinding(String propertyName) {
        this.propertyName = propertyName;
    }

    @Override
    public boolean execute(String name, Object obj, ComponentConfiguration config, Class<?> argType, Injector injector, Method method) throws Exception {
        ComponentConfiguration subConfig = config.getChild(propertyName);
        if (argType.isInterface()) {
            TypeLiteral<Map<String, ComponentFactory<?>>> mapType =
                    (TypeLiteral<Map<String, ComponentFactory<?>>>) TypeLiteral.get(
                            Types.mapOf(String.class,
                                    Types.newParameterizedType(
                                            ComponentFactory.class,
                                            argType)));
            
            Key<Map<String, ComponentFactory<?>>> mapKey = Key.get(mapType);

            Binding<Map<String, ComponentFactory<?>>> binding = injector.getExistingBinding(mapKey);
            
            if (binding != null) {
                if (subConfig.getType() != null) {
                    Map<String, ComponentFactory<?>> map = binding
                            .getProvider().get();
                    ComponentFactory<?> factory = map
                            .get(subConfig.getType());
                    if (factory != null) {
                        Object subObject = factory
                                .create(subConfig);
                        method.invoke(obj, subObject);
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
