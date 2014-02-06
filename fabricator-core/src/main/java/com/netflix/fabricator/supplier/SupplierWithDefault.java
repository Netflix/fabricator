package com.netflix.fabricator.supplier;

import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.netflix.fabricator.supplier.ListenableSupplier;

/**
 * Implementation of a ListenableSupplier that gets it's value from an optional
 * ListenableSupplier but returns a default value if the source returns {$code null}.
 * 
 * <pre> {@code

  public class Service {
    public class Builder {
      // Define a SupplierWithDefault in your code.  
      private final SupplierWithDefault<String> param1 = SupplierWithDefault.from("DefaultValue");
      
      // Assign an optional 'source'.  This will be called by a Mapper that supports dynamic
      // values (@see ArchaiusConfigurationProviderMapper).
      public Builder withParam1(ListenableSupplier<String> source) {
          param1.setSource(source);
          return this;
      }
      
      public Service build() {
          return new Service(this);
      }
    }
    
    // The service's reference to the supplier
    private final ListenableSupplier<String> param1;
    
    protected Service(Builder builder) {
        this.param1 = builder.param1;
        
        // Set an optional callback notification for when the value changes
        param1.onChange(new Function<String, Void>() {
            public Void apply(String newValue) {
                System.out.println("Got a new value for newValue");
            }
        });
    }
  
  }} </pre>
  
 * @author elandau
 *
 * @param <T>
 */
public class SupplierWithDefault<T> implements ListenableSupplier<T> {
    private final T               defaultValue;
    private ListenableSupplier<T> source;
    
    public SupplierWithDefault(T defaultValue) {
        this.defaultValue = defaultValue;
        this.source       = new ListenableSupplier<T>() {
            @Override
            public T get() {
                return SupplierWithDefault.this.defaultValue;
            }

            @Override
            public void onChange(Function<T, Void> func) {
//                throw new RuntimeException("Change notification not supported");
            }
        };
    }
    
    public static <T> SupplierWithDefault<T> from(T defaultValue) {
        return new SupplierWithDefault<T>(defaultValue);
    }
    
    @Override
    public T get() {
        T value = source.get();
        if (value == null) {
            return defaultValue;
        }
        return value;
    }
    
    /**
     * Set the ListenableSupplier from which the value will be read and a notification
     * can be subscribed to.  
     * @param supplier
     */
    public void setSource(ListenableSupplier<T> supplier) {
        source = supplier;
    }
    
    /**
     * Set a 'fixed' value to be returned.
     * @param value
     */
    public void setValue(T value) {
        addOverride(Suppliers.ofInstance(value));
    }

    /**
     * Set the onChange callback on the source.  Note that the function reference is lost 
     * if source is changed
     */
    public void onChange(Function<T, Void> func) {
        source.onChange(func);
    }

    /**
     * Assign a simple Supplier that does not provide change notification
     * This will result in an exception being thrown if the client code tries to listen
     * for changes
     * @param supplier
     */
    public void addOverride(final Supplier<T> supplier) {
        source = new ListenableSupplier<T>() {
            @Override
            public T get() {
                return supplier.get();
            }

            @Override
            public void onChange(Function<T, Void> func) {
                throw new RuntimeException("Change notification not supported");
            }
        };
    }
}
