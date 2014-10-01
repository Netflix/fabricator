package com.netflix.fabricator.guice;

import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.spi.BindingTargetVisitor;
import com.google.inject.spi.ProviderInstanceBinding;
import com.google.inject.spi.ProviderWithExtensionVisitor;
import com.google.inject.spi.Toolable;
import com.netflix.fabricator.BindingComponentFactory;
import com.netflix.fabricator.ConfigurationNode;
import com.netflix.fabricator.InjectionSpi;
import com.netflix.fabricator.PropertyBinder;
import com.netflix.fabricator.PropertyBinderResolver;
import com.netflix.fabricator.component.ComponentFactory;
import com.netflix.fabricator.component.bind.SimplePropertyBinderFactoryResolver;
import com.netflix.fabricator.guice.mapping.EmbeddedComponentManagerBinding;
import com.netflix.fabricator.guice.mapping.NamedComponentManagerBinding;
import com.netflix.fabricator.guice.mapping.EmbeddedComponentFactoryBinding;
import com.netflix.fabricator.guice.mapping.EmbeddedMapToComponentFactoryBinding;
import com.netflix.fabricator.guice.mapping.NamedMapBinding;
import com.netflix.fabricator.guice.mapping.NamedBinding;
import com.netflix.fabricator.guice.mapping.PropertyInjection;

/**
 * Utility class for creating a binding between a type string name and an
 * implementation using the builder pattern.
 * 
 * TODO: PostConstruct and PreDestroy
 * 
 * @author elandau
 *
 * @param <T>
 * 
 */
public class GuiceBindingComponentFactoryProvider<T> implements ProviderWithExtensionVisitor<ComponentFactory<T>>, InjectionSpi {
    private static final Logger LOG = LoggerFactory.getLogger(GuiceBindingComponentFactoryProvider.class);
    
    private SettableInjector            injector = new SettableInjector();
    private BindingComponentFactory<T>  factory;
    private PropertyBinderResolver      binderResolver;
    private Class<?>                    clazz;
    
    public GuiceBindingComponentFactoryProvider(final Class<?> clazz) {
        this(clazz, new SettableInjector());
    }
    
    public GuiceBindingComponentFactoryProvider(final Class<?> clazz, SettableInjector injector) {
        this.binderResolver = new SimplePropertyBinderFactoryResolver(null, this);
        this.clazz          = clazz;

        if (injector != null)   
            initialize(injector);

    }
    
    @Override
    public ComponentFactory<T> get() {
        return factory.get();
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
        this.injector.set(injector);
        this.factory = new BindingComponentFactory<T>(clazz, binderResolver, this);
    }
    
    @Override
    public PropertyBinder createInjectableProperty(final String propertyName, Class<?> argType, Method method) {
        // Allowable bindings for named binding 
        final PropertyInjection namedPropertyInjection = new PropertyInjection(argType, injector, method);
        namedPropertyInjection
                .addStrategy(new NamedBinding())                                // T
                .addStrategy(new NamedMapBinding())                             // Map<String, T>
                .addStrategy(new NamedComponentManagerBinding(propertyName)     // ComponentManager<T>
                );
        
        // Allowable bindings for embedded structures
        final PropertyInjection embeddedPropertyInjection = new PropertyInjection(argType, injector, method);
        embeddedPropertyInjection
                .addStrategy(new EmbeddedComponentManagerBinding(propertyName)) // ComponentManager<T>
                .addStrategy(new EmbeddedMapToComponentFactoryBinding())        // Map<String, ComponentFactory<T>>
                .addStrategy(new EmbeddedComponentFactoryBinding())             // ComponentFactory<T>
                .addStrategy(new NamedMapBinding()                              // Does this belong here
                );
        
        //Build up a sequence of Binding resolving and value retrieving processes.
        //Any successful step will terminate the sequence
        return new PropertyBinder() {
            @Override
            public boolean bind(Object obj, ConfigurationNode node) throws Exception {
                // Property value is a simple 'string'
                // Look for 'named' binding or 'key' in a mapbinder
                if (node.isSingle()) {
                    String value = node.getValue(String.class);
                    if (value != null) {
                        if (namedPropertyInjection.execute(value, obj, node)) {
                            return true;
                        }
                    } 
                    else {
                        // Hmmm...
                    }
                    return false;
                }
                // Property is a structure
                else {
                    return embeddedPropertyInjection.execute(null, obj, node);
                }
            }
        };        
    }

    @Override
    public <S> S getInstance(Class<S> clazz) {
        return injector.getInstance(clazz);
    }

    @Override
    public void injectMembers(Object obj) {
        injector.injectMembers(obj);
    }
}
