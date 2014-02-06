package com.netflix.fabricator.guice.mapping;

import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.util.Types;
import com.netflix.fabricator.ComponentConfiguration;
import com.netflix.fabricator.component.ComponentManager;

import java.lang.reflect.Method;

/**
 * Created by hyuan on 1/16/14.
 */
public class ComponentManagerBinding implements BindingReslove {
    private String propertyName;

    public ComponentManagerBinding(String propertyName) {
        this.propertyName = propertyName;
    }

    @Override
    public boolean execute(String name, Object obj, ComponentConfiguration config, Class<?> argType, Injector injector, Method method) throws Exception {
        TypeLiteral<ComponentManager<?>> managerLiteral = (TypeLiteral<ComponentManager<?>>) TypeLiteral.get(Types.newParameterizedType(ComponentManager.class, argType));
        Binding<ComponentManager<?>> managerBinding = injector.getExistingBinding(Key.get(managerLiteral));
        if (managerBinding != null) {
            ComponentManager<?> manager = managerBinding.getProvider().get();
            try {
                method.invoke(obj, manager.get(name));
                return true;
            }
            catch (Exception e) {
                throw new Exception(String.format(
                        "Unable to get component '%s' (%s) for property '%s' must be one of %s",
                        name, argType.getSimpleName(), propertyName, manager.getIds()), e);
            }
        }
        return false;    
    }
}
