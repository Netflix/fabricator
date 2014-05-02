package com.netflix.fabricator.component.mapping;

import java.util.Properties;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import com.netflix.config.ConfigurationManager;
import com.netflix.fabricator.annotations.Type;
import com.netflix.fabricator.annotations.TypeImplementation;
import com.netflix.fabricator.archaius.ArchaiusConfigurationModule;
import com.netflix.fabricator.component.ComponentManager;
import com.netflix.fabricator.component.SynchronizedComponentManager;
import com.netflix.fabricator.guice.ComponentModuleBuilder;

public class NamedDeserializerTest {
    private static final Logger LOG = LoggerFactory.getLogger(NamedDeserializerTest.class);
    
    @Type("foo")
    public static interface SomeType {
        public String serialize(Object obj) throws Exception;
    }
    
    @TypeImplementation("impl")
    public static class SomeTypeImpl1 implements SomeType {
        public static class Builder {
            private Foo serializer;
            
            public Builder withSerializer(Foo serializer) {
                this.serializer = serializer;
                return this;
            }
            
            public SomeTypeImpl1 build() {
                return new SomeTypeImpl1(this);
            }
        }
        
        public static Builder builder() {
            return new Builder();
        }
        
        private final Foo serializer;
        
        public SomeTypeImpl1(Builder builder) {
            this.serializer = builder.serializer;
        }
        
        public String serialize(Object obj) throws Exception {
            return serializer.call(obj);
        }
    }
    
    @Test
    public void testConfiguredDeserializer() throws Exception {
        final Properties props = new Properties();
        props.put("1.foo.type",       "impl");
        props.put("1.foo.serializer", "jackson");
        props.put("2.foo.type",       "impl");
        props.put("2.foo.serializer", "base64");
        props.put("3.foo.type",       "impl");
        props.put("3.foo.serializer", "string");
        props.put("4.foo.type",       "impl");
        props.put("4.foo.serializer", "custom");
        
        ConfigurationManager.loadProperties(props);

        Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                install(new ArchaiusConfigurationModule());
                install(new FooModule());
                install(new ComponentModuleBuilder<SomeType>()
                        .manager(SynchronizedComponentManager.class)
                        .implementation(SomeTypeImpl1.class)
                        .build(SomeType.class));
                
                bind(Foo.class).annotatedWith(Names.named("custom")).toInstance(new Foo() {
                    @Override
                    public <T> String call(T entity) throws Exception {
                        return "custom";
                    }
                });
            }
        });
        
        ComponentManager<SomeType> manager = injector.getInstance(Key.get(new TypeLiteral<ComponentManager<SomeType>>() {}));
        SomeType o1 = manager.get("1");
        SomeType o2 = manager.get("2");
        SomeType o3 = manager.get("3");
        SomeType o4 = manager.get("4");
        
        LOG.info(o1.serialize("foo"));
        LOG.info(o2.serialize("foo"));
        LOG.info(o3.serialize("foo"));
        LOG.info(o4.serialize("foo"));
    }
}
