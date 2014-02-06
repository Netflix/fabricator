package com.netflix.fabricator.guice;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.spi.BindingTargetVisitor;
import com.google.inject.spi.ProviderInstanceBinding;
import com.google.inject.spi.ProviderWithExtensionVisitor;
import com.google.inject.spi.Toolable;
import com.netflix.fabricator.component.ComponentFactory;

public class ComponentFactoryFactoryProvider<T> implements ProviderWithExtensionVisitor<ComponentFactory<T>>  {
    private Class<? extends ComponentFactory<T>>  clazz;
    private ComponentFactory<T>                 factory;
    
    public ComponentFactoryFactoryProvider(final Class<? extends ComponentFactory<T>> clazz) {
        this.clazz = clazz;
    }
    
    @Override
    public ComponentFactory<T> get() {
        return factory;
    }

    /**
     * This is needed for 'initialize(injector)' below to be called so the provider
     * can get the injector after it is instantiated.
     */
    @Override
    public <B, V> V acceptExtensionVisitor(
            BindingTargetVisitor<B, V> visitor,
            ProviderInstanceBinding<? extends B> binding) {
        return visitor.visit(binding);
    }

    @Inject
    @Toolable
    void initialize(Injector injector) {
        this.factory = (ComponentFactory<T>) injector.getInstance(clazz);
    }
}
