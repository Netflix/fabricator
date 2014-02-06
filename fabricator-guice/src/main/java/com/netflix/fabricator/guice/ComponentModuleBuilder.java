package com.netflix.fabricator.guice;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.Scopes;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.name.Names;
import com.google.inject.util.Types;
import com.netflix.fabricator.Builder;
import com.netflix.fabricator.ComponentType;
import com.netflix.fabricator.annotations.SubType;
import com.netflix.fabricator.annotations.Type;
import com.netflix.fabricator.component.ComponentFactory;
import com.netflix.fabricator.component.ComponentManager;

/**
 * Utility class for creating a binding between a type string name and an
 * implementation using the builder pattern.
 * 
 * @author elandau
 *
 * @param <T>
 */
public class ComponentModuleBuilder<T> {
    private Map<String, Provider<ComponentFactory<T>>> bindings = Maps.newHashMap();
    private Set<String> ids = Sets.newHashSet();
    private Map<String, T> instances = Maps.newHashMap();
    private Class<? extends ComponentManager> managerClass;
    
    public Module build(final Class<T> type) {
        return new AbstractModule() {
            @Override 
            protected void configure() {
                if (managerClass != null) {
                    Type typeAnnot = type.getAnnotation(Type.class);
                    Preconditions.checkNotNull("Missing @Type annotation for " + type.getCanonicalName());
                    TypeLiteral<ComponentType<T>> componentType = (TypeLiteral<ComponentType<T>>) TypeLiteral.get(Types.newParameterizedType(ComponentType.class, type));
                    bind(componentType)
                        .toInstance(new ComponentType<T>(typeAnnot.value()));
                    
                    TypeLiteral<ComponentManager<T>> managerType     = (TypeLiteral<ComponentManager<T>>) TypeLiteral.get(Types.newParameterizedType(ComponentManager.class, type));
                    TypeLiteral<ComponentManager<T>> managerTypeImpl = (TypeLiteral<ComponentManager<T>>) TypeLiteral.get(Types.newParameterizedType(managerClass, type));
                    bind(managerType)
                        .to(managerTypeImpl)
                        .in(Scopes.SINGLETON);
                }

                
                // Create the multi binder for this type.
                MapBinder<String, ComponentFactory<T>> factories = (MapBinder<String, ComponentFactory<T>>) MapBinder.newMapBinder(
                        binder(), 
                        TypeLiteral.get(String.class), 
                        TypeLiteral.get(Types.newParameterizedType(ComponentFactory.class, type))
                    );

                // Add different sub types to the multi binder
                for (Entry<String, Provider<ComponentFactory<T>>> entry : bindings.entrySet()) {
                    factories.addBinding(entry.getKey()).toProvider(entry.getValue());
                }
                
                // Add specific named ids
                for (String id : ids) {
                    bind(type)
                        .annotatedWith(Names.named(id))
                        .toProvider(new NamedInstanceProvider(id, TypeLiteral.get(Types.newParameterizedType(ComponentManager.class, type))));
                }
                
                // Add externally provided named instances
                for (Entry<String, T> entry : instances.entrySet()) {
                    bind(type)
                        .annotatedWith(Names.named(entry.getKey()))
                        .toInstance(entry.getValue());
                }
            }
        };
    }

    /**
     * Identifies a specific subclass of the component type.  The mapper will create
     * an instance of class 'type' whenever it sees the value 'id' for the type 
     * field in the configuration specification (i.e. .properties or .json data)
     * 
     * @param subTypeName
     * @param subType
     */
    public ComponentModuleBuilder<T> implementation(String subTypeName, Class<? extends T> subType) {
        bindings.put(subTypeName, new GuiceBindingComponentFactoryProvider<T>(subType));
        return this;
    }

    public ComponentModuleBuilder<T> implementation(Class<? extends T> type) {
        SubType subType = type.getAnnotation(SubType.class);
        Preconditions.checkNotNull(subType);
        bindings.put(subType.value(), new GuiceBindingComponentFactoryProvider<T>((Class<T>) type));
        return this;
    }
    
    public ComponentModuleBuilder<T> factory(String subType, final Class<? extends ComponentFactory<T>> factory) {
        bindings.put(subType, new ComponentFactoryFactoryProvider<T>(factory));
        return this;
    }

    public ComponentModuleBuilder<T> manager(Class<? extends ComponentManager> clazz) {
        managerClass = clazz;
        return this;
    }

    /**
     * Specify a builder (as a Factory) on which configuration will be mapped and the
     * final object created when the builder's build() method is called.  Use this
     * when you don't have access to the implementing class.
     * 
     * Example usage,
     * 
     *  install(new ComponentMouldeBuilder<SomeComponent>()
     *      .builder("type", MyCompomentBuilder.class)
     *      .build();
     *      
     *  public class MyComponentBuilder implements Builder<SomeComponent> {
     *      public MyComponentBuilder withSomeProperty(String value) {
     *          ...
     *      }
     *      
     *      ...
     *      
     *      public SomeComponent build() {
     *          return new SomeComponentImpl(...);
     *      }
     *  }
     * 
     * @param type
     * @param builder
     * @return
     */
    public ComponentModuleBuilder<T> builder(String type, Class<? extends Builder<T>> builder) {
        bindings.put(type, new GuiceBindingComponentFactoryProvider<T>(builder));
        return this;
    }

    /**
     * Indicate a specific instance for id.  This makes it possible to inject an instance
     * using @Named('id') instead of the ComponentManager
     * 
     * @param id
     * @return
     */
    public ComponentModuleBuilder<T> named(String id) {
        ids.add(id);
        return this;
    }
    
    /**
     * A a named instance of an existing component.  No configuration will be done here.
     * @param id
     * @param instance
     * @return
     */
    public ComponentModuleBuilder<T> named(String id, T instance) {
        instances.put(id, instance);
        return this;
    }
}
