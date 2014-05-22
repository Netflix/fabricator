package com.netflix.fabricator.guice.mapping;

import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;
import com.netflix.fabricator.ConfigurationNode;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * Created by hyuan on 1/16/14.
 */
public class NamedInjectionBinding implements BindingReslove {

    @Override
    public boolean execute(String name, Object obj, ConfigurationNode config, Class<?> argType, Injector injector, Method method) throws Exception {
        
        Binding<?> binding;
        
        Type pType = method.getGenericParameterTypes()[0];
        if (pType != null) {
            binding = injector.getExistingBinding(Key.get(pType, Names.named(name)));
        }
        else {
            binding = injector.getExistingBinding(Key.get(argType, Names.named(name)));
        }
        
        if (binding != null) {
            method.invoke(obj, binding.getProvider().get());
            return true;
        }
        return false;
    }
}
