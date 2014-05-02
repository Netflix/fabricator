package com.netflix.fabricator;


/**
 * I. Primitive type
 *  prefix.${id}.field1=boolean|number|string|list|...      withField1(String value)                                            
 *                                                          withField1(Supplier<String> value)
 *                                                          withField1(ListenableSupplier<String> value)
 *  
 * II. Named 
 *  1. prefix.${id}.field1=${name}                          withField1(Foo foo); 
 *  
 *                                                          bind(Foo.class).annotatedWith("name").to(FooImpl.class) // Has a named binding
 *                                                          MapBinder<String, Snack> mapbinder
 *                                                           = MapBinder.newMapBinder(binder(), String.class, Snack.class);
 *  
 * III. Policy (policy is an interface)                     
 *  1. prefix.${id}.policy1.type                            MapBinder<String, Snack> mapbinder
 *     prefix.${id}.policy1.field1=...                       = MapBinder.newMapBinder(binder(), String.class, Snack.class);
 * 
 * IV. Embedded (embedded is a complex type)                withFoo(Foo foo);   // No named binding
 *  1. prefix.${id}.embedded1.field1
 *     prefix.${id}.embedded2.field2
 *  
 * @author elandau
 *
 */
public interface PropertyBinder {
    public boolean bind(Object obj, ConfigurationNode node) throws Exception;
}
