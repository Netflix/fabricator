package com.netflix.fabricator.guice;

import java.lang.reflect.Method;

import com.netflix.fabricator.BindingComponentFactory;
import com.netflix.fabricator.ConfigurationNode;
import com.netflix.fabricator.InjectionSpi;
import com.netflix.fabricator.PropertyBinder;
import com.netflix.fabricator.PropertyBinderResolver;
import com.netflix.fabricator.component.ComponentFactory;
import com.netflix.fabricator.component.bind.SimplePropertyBinderFactoryResolver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.spi.BindingTargetVisitor;
import com.google.inject.spi.ProviderInstanceBinding;
import com.google.inject.spi.ProviderWithExtensionVisitor;
import com.google.inject.spi.Toolable;
import com.netflix.fabricator.guice.mapping.ComponentFactoryBinding;
import com.netflix.fabricator.guice.mapping.ComponentManagerBinding;
import com.netflix.fabricator.guice.mapping.CompositeExistingBinding;
import com.netflix.fabricator.guice.mapping.CompositeInterfaceBinding;
import com.netflix.fabricator.guice.mapping.CompositeNoExistingBinding;
import com.netflix.fabricator.guice.mapping.MapBinderBinding;
import com.netflix.fabricator.guice.mapping.NamedInjectionBinding;
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
        final PropertyInjection simplePropertyInjection = new PropertyInjection(argType, injector, method);
        simplePropertyInjection
                .addStrategy(new NamedInjectionBinding())
                .addStrategy(new MapBinderBinding())
                .addStrategy(new ComponentManagerBinding(propertyName));
        
        final PropertyInjection compositePropertyInjection = new PropertyInjection(argType, injector, method);
        compositePropertyInjection
                .addStrategy(new CompositeInterfaceBinding())
                .addStrategy(new CompositeExistingBinding())
                .addStrategy(new MapBinderBinding())
                ;
        
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
                        if (simplePropertyInjection.execute(value, obj, node)) {
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
                    return compositePropertyInjection.execute(null, obj, node);
                }
            }
        };        
    }

    @Override
    public <T> T getInstance(Class<T> clazz) {
        return injector.getInstance(clazz);
    }

    @Override
    public void injectMembers(Object obj) {
        injector.injectMembers(obj);
    }
}
