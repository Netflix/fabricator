package com.netflix.fabricator.guice;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.BindingTargetVisitor;
import com.google.inject.spi.ProviderInstanceBinding;
import com.google.inject.spi.ProviderWithExtensionVisitor;
import com.google.inject.spi.Toolable;
import com.netflix.fabricator.component.ComponentManager;
import com.netflix.fabricator.component.exception.ComponentAlreadyExistsException;
import com.netflix.fabricator.component.exception.ComponentCreationException;

/**
 * Special provider that links a ComponentManager with a named instance of a 
 * Component so that the component may be inject by
 * 
 * void SomeServiceConstructor(@Named("componentId") ComponentType component) {
 * }
 * 
 * To use,
 * 
 *  install(ComponentModuleBuilder<ComponentType>().builder()
 *      .named("componentId")
 *      .build());
 *      
 * @author elandau
 *
 * @param <T>
 */
public class NamedInstanceProvider<T> implements ProviderWithExtensionVisitor<T>  {
    private ComponentManager<T> manager;
    private final String id;
    private TypeLiteral<ComponentManager<T>> typeLiteral;
    
    public NamedInstanceProvider(String id, TypeLiteral<ComponentManager<T>> typeLiteral) {
        this.id = id;
        this.typeLiteral = typeLiteral;
    }

    @Override
    public T get() {
        try {
            return manager.get(id);
        } 
        catch (ComponentCreationException e) {
            throw new RuntimeException(e);
        }
        catch (ComponentAlreadyExistsException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <B, V> V acceptExtensionVisitor(
            BindingTargetVisitor<B, V> visitor,
            ProviderInstanceBinding<? extends B> binding) {
        return visitor.visit(binding);
    }

    @Inject
    @Toolable
    void initialize(Injector injector) {
        manager = injector.getInstance(Key.get(typeLiteral));
    }
}
