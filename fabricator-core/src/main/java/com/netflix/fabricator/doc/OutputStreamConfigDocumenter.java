package com.netflix.fabricator.doc;

import java.util.Map.Entry;

import javax.annotation.PostConstruct;

import com.google.inject.Binding;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Singleton;
import com.google.inject.multibindings.MapBinderBinding;
import com.google.inject.multibindings.MultibinderBinding;
import com.google.inject.multibindings.MultibindingsTargetVisitor;
import com.google.inject.spi.DefaultBindingTargetVisitor;
import com.netflix.fabricator.PropertyInfo;
import com.netflix.fabricator.component.ComponentFactory;

@Singleton
public class OutputStreamConfigDocumenter {
    private final Injector injector;
    
    @Inject
    public OutputStreamConfigDocumenter(Injector injector) {
        this.injector = injector;
    }
    
    public static class Visitor
        extends DefaultBindingTargetVisitor<Object, MapBinderBinding<?>>
        implements MultibindingsTargetVisitor<Object, MapBinderBinding<?>> {
        public MapBinderBinding<?> visit(MapBinderBinding<?> mapBinder) {
            if (mapBinder.getValueTypeLiteral().getRawType().isAssignableFrom(ComponentFactory.class)) {
                System.out.println(mapBinder.getValueTypeLiteral());
                
                for (Entry<?, Binding<?>> entry : mapBinder.getEntries()) {
                    ComponentFactory<?> factory = (ComponentFactory<?>) entry.getValue().getProvider().get();
                    System.out.println(String.format("  for type='%s' use class '%s' with properties: ", entry.getKey(), factory.getRawType().getSimpleName()));
                    
                    for (Entry<String, PropertyInfo> propEntry : factory.getProperties().entrySet()) {
                        PropertyInfo prop = propEntry.getValue();
                        String   name = propEntry.getKey();
          
//                        System.out.println(String.format("    %-20s %-10s %-10s", name, prop.getType().getSimpleName(), prop.isDynamic() ? "(dynamic)" : ""));
                    }
                }
            }
            return mapBinder;
        }

        public MapBinderBinding<?> visit(MultibinderBinding<?> multibinder) {
            return null;
        }
    }
    
    @PostConstruct
    public void init() {
        for (Entry<Key<?>, Binding<?>> entry : this.injector.getAllBindings().entrySet()) {
            Key<?> key = entry.getKey();
            Binding<?> binding = entry.getValue();
            binding.acceptTargetVisitor(new Visitor());
        }
    }
}
