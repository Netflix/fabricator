package com.netflix.fabricator;

/**
 * Encapsulate a representation of a property and different method variations
 * for setting it. 
 * 
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
 */
public class PropertyInfo {
    private String          name;
    private PropertyBinder  simple;
    private PropertyBinder  dynamic;
    private PropertyBinder  binding;
    
    public PropertyInfo(String name) {
        this.name = name;
    }
    
    public void addSimple(PropertyBinder simple) {
        this.simple = simple;
    }
    
    public void addDynamic(PropertyBinder dynamic) {
        this.dynamic = dynamic;
    }
    
    public void addBinding(PropertyBinder binding) {
        this.binding = binding;
    }
    
    public void apply(Object obj, ConfigurationSource mapper) throws Exception {
             if (binding != null && binding.bind(obj, mapper)) {}
        else if (dynamic != null && dynamic.bind(obj, mapper)) {}
        else if (simple  != null && simple.bind(obj, mapper))  {}
    }
    
    public String getName() {
        return name;
    }
}
