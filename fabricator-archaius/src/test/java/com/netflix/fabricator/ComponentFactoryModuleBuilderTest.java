package com.netflix.fabricator;

import java.util.Properties;

import javax.annotation.Nullable;

import com.netflix.fabricator.properties.PropertiesConfigurationModule;
import com.netflix.fabricator.supplier.SupplierWithDefault;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import com.netflix.config.ConfigurationManager;
import com.netflix.fabricator.archaius.ArchaiusConfigurationModule;
import com.netflix.fabricator.annotations.TypeImplementation;
import com.netflix.fabricator.annotations.Type;
import com.netflix.fabricator.component.ComponentManager;
import com.netflix.fabricator.component.SynchronizedComponentManager;
import com.netflix.fabricator.component.exception.ComponentAlreadyExistsException;
import com.netflix.fabricator.component.exception.ComponentCreationException;
import com.netflix.fabricator.guice.ComponentModuleBuilder;
import com.netflix.fabricator.supplier.ListenableSupplier;

public class ComponentFactoryModuleBuilderTest {

    private static final Logger LOG = LoggerFactory.getLogger(ComponentFactoryModuleBuilderTest.class);
    
    /**
     * Example of a subentity
     * 
     * @author elandau
     */
    public static class SubEntity {
        public static class Builder {
            private String str;
            
            public Builder withStr(String str) {
                this.str = str;
                return this;
            }
            
            public SubEntity build() {
                return new SubEntity(this);
            }
        }
        
        private final String str;
        
        private SubEntity(Builder init) {
            this.str = init.str;
        }
        
        public String getString() {
            return str;
        }

        @Override
        public String toString() {
            return "SubEntity [str=" + str + "]";
        }
    }
    
    /**
     * Interface for a policy
     * @author elandau
     *
     */
    @Type("policy")
    public static interface Policy {
    }
    
    /**
     * Implementation of a policy with one String arg
     * 
     * @author elandau
     */
    @TypeImplementation("pa")
    public static class PolicyA implements Policy {
        private final String  s;
        private final Long    l;
        private final Boolean b;
        private final Double  d;
        private final Integer i;
        
        public static class Builder {
            private String  s;
            private Long    l;
            private Boolean b;
            private Double  d;
            private Integer i;
           
            public Builder withString(String s) {
                this.s = s;
                return this;
            }
            
            public Builder withLong(Long l) {
                this.l = l;
                return this;
            }
            
            public Builder withInteger(Integer i) {
                this.i = i;
                return this;
            }
            
            public Builder withDouble(Double d) {
                this.d = d;
                return this;
            }
            
            public Builder withBoolean(Boolean b) {
                this.b = b;
                return this;
            }
            
            public PolicyA build() {
                return new PolicyA(this);
            }
        }
        
        private PolicyA(Builder init) {
            this.s = init.s;
            this.l = init.l;
            this.b = init.b;
            this.i = init.i;
            this.d = init.d;
            
        }

        public String getString() {
            return this.s;
        }

        @Override
        public String toString() {
            return "PolicyA [s=" + s + ", l=" + l + ", b=" + b + ", d=" + d
                    + ", i=" + i + "]";
        }
    }
    
    /**
     * A plicy with one string arg and a supplier
     * @author elandau
     *
     */
    @TypeImplementation("pb")
    public static class PolicyB implements Policy {
        private final Supplier<String> arg1;
        
        public static class Builder {
            private final SupplierWithDefault<String> arg1 = SupplierWithDefault.from("abc");
            
            public Builder withArg1(String arg1) {
                this.arg1.setValue(arg1);
                return this;
            }
            
            public Builder withArg1(Supplier<String> arg1) {
                this.arg1.addOverride(arg1);
                return this;
            }
            
            public PolicyB build() {
                return new PolicyB(this);
            }
        }
        
        private PolicyB(Builder init) {
            this.arg1 = init.arg1;
        }
        
        @Override
        public String toString() {
            return "PolicyB [arg1=" + arg1 + "]";
        }
    }
    
    /**
     * 
     * @author elandau
     *
     */
    @Type("some")
    public static abstract class SomeInterface {
    }
    
    public static class SomeInterfaceModule extends AbstractModule {
        @Override
        protected void configure() {
          install(new ComponentModuleBuilder<SomeInterface>()
                  .manager(SynchronizedComponentManager.class)
                  .build(SomeInterface.class));
        }
    }
    

    /**
     * 
     * @author elandau
     *
     */
    @TypeImplementation("a")
    public static class BaseA extends SomeInterface {
        public static class Builder {
            private String id;
            private String prop1;
            private Policy policy;
            private final SupplierWithDefault<String> dyn1 = new SupplierWithDefault<String>("dyn1_default");
            private final SupplierWithDefault<String> dyn2 = new SupplierWithDefault<String>("dyn2_default");
            
            public Builder withId(String id) {
                this.id = id;
                return this;
            }
            
            public Builder withProp1(String prop1) {
                this.prop1 = prop1;
                return this;
            }
            
            public Builder withPolicy(Policy policy) {
                this.policy = policy;
                return this;
            }
            
            public Builder withDyn1(Supplier<String> supplier) {
                LOG.info("dyn1=" + supplier.get());
                this.dyn1.addOverride(supplier);
                return this;
            }
            
            public Builder withDyn2(ListenableSupplier<String> supplier) {
                LOG.info("dyn2=" + supplier.get());
                this.dyn2.setSource(supplier);
                return this;
            }
            
            public BaseA build() {
                return new BaseA(this);
            }
        }
        
        private final String id;
        private final String prop1;
        private final Policy policy;
        private final Supplier<String> dyn1;
        private final ListenableSupplier<String> dyn2;

        private BaseA(Builder init) {
            this.id     = init.id;
            this.prop1  = init.prop1;
            this.policy = init.policy;
            this.dyn1   = init.dyn1;
            this.dyn2   = init.dyn2;
            
            this.dyn2.onChange(new Function<String, Void>() {
                public Void apply(@Nullable String input) {
                    LOG.info("Value has changed to : " + input);
                    return null;
                }
            });
        }

        @Override
        public String toString() {
            return "BaseA [id=" + id + ", prop1=" + prop1 + ", policy="
                    + policy + ", dyn1=" + dyn1.get() + ", dyn2=" + dyn2.get() + "]";
        }

    }
    
    /**
     * 
     * @author elandau
     *
     */
    @TypeImplementation("b")
    public static class BaseB extends SomeInterface {
        public static class Builder {
            private String id;
            
            public Builder withId(String id) {
                this.id = id;
                return this;
            }
            
            public BaseB build() {
                return new BaseB(this);
            }
        }
        
        private final String id;
        
        private BaseB(Builder init) {
            id = init.id;
        }

        @Override
        public String toString() {
            return "BaseB [id=" + id + "]";
        }
    }
    
    @TypeImplementation("c")
    public static class BaseC extends SomeInterface {
        public static class Builder {
            private SubEntity entity;
            private String id;
            
            public Builder withSubEntity(SubEntity entity) {
                this.entity = entity;
                return this;
            }
            
            public Builder withId(String id) {
                this.id = id;
                return this;
            }
            
            public BaseC build() {
                return new BaseC(this);
            }
        }
        
        private final SubEntity entity;
        private final String id;
        
        private BaseC(Builder init) {
            this.entity = init.entity;
            this.id    = init.id;
        }

        @Override
        public String toString() {
            return "BaseC [entity=" + entity + ", id=" + id + "]";
        }
    }
    
    @TypeImplementation("d")
    public static class BaseD extends SomeInterface {
        private final String  s;
        private final Long    l;
        private final Boolean b;
        private final Double  d;
        private final Integer i;
        private final Properties p;
        private final String  id;
        
        public static class Builder {
            private String  s;
            private Long    l;
            private Boolean b;
            private Double  d;
            private Integer i;
            private Properties p;
            private String id;
            
            public Builder withId(String id) {
                this.id = id;
                return this;
            }
            
            public Builder withString(String s) {
                this.s = s;
                return this;
            }
            
            public Builder withLong(long l) {
                this.l = l;
                return this;
            }
            
            public Builder withInteger(int i) {
                this.i = i;
                return this;
            }
            
            public Builder withDouble(double d) {
                this.d = d;
                return this;
            }
            
            public Builder withBoolean(boolean b) {
                this.b = b;
                return this;
            }
            
            public Builder withProperties(Properties props) {
                this.p = props;
                return this;
            }
            
            public BaseD build() {
                return new BaseD(this);
            }
        }
        
        private BaseD(Builder init) {
            this.s = init.s;
            this.l = init.l;
            this.b = init.b;
            this.i = init.i;
            this.d = init.d;
            this.p = init.p;
            this.id = init.id;
        }

        public String getString() {
            return this.s;
        }

        public Properties getProperties() {
            return this.p;
        }
        
        @Override
        public String toString() {
            return "BaseD [id=" + id
                    + ", s=" + s 
                    + ", l=" + l 
                    + ", b=" + b 
                    + ", d=" + d
                    + ", i=" + i 
                    + ", p=" + p 
                    + "]";
        }

    }
    
    public static class MyService {
        @Inject
        public MyService(final ComponentManager<SomeInterface> manager) throws ComponentCreationException, ComponentAlreadyExistsException {
            SomeInterface if1 = manager.get("id1");
            LOG.info(if1.toString());

            SomeInterface if2 = manager.get("id2");
            
            LOG.info(if2.toString());
            
            Properties prop2 = new Properties();
            prop2.put("type",  "b");
            prop2.put("prop1", "v1");
            prop2.put("prop2", "v2");
            
//            SomeInterface base3 = manager.get(new PropertyMapper("id2", prop2.getProperty("type"), prop2));
//            LOG.info(base3.toString());
        }
    }
    
    public static class MyServiceWithNamedComponent {
        @Inject
        public MyServiceWithNamedComponent(@Named("id1") SomeInterface iface) {
            
        }
    }
    
    @Test
    public void testNamed() {
        // 1.  Bootstrap on startup
        // 2.  Load by 'id'
        // 3.  Map with prefix
        final Properties props = new Properties();
        props.put("id1.some.type",          "a");
        props.put("id1.some.prop1",         "v1");

        Injector injector = Guice.createInjector(
            new PropertiesConfigurationModule(props),
            new SomeInterfaceModule(),
            new AbstractModule() {
                @Override
                protected void configure() {
                    install(new ComponentModuleBuilder<SomeInterface>()
                            .implementation(BaseA.class)
                            .implementation(BaseB.class)
                            .implementation(BaseC.class)
                            .implementation(BaseD.class)
                            .build(SomeInterface.class)
                            );
                    
                    install(new ComponentModuleBuilder<Policy>()
                            .implementation(PolicyA.class)
                            .implementation(PolicyB.class)
                            .build(Policy.class));
                }
            },
            new AbstractModule() {
                @Override
                protected void configure() {
                    install(new ComponentModuleBuilder<SomeInterface>()
                            .named("id1")
                            .build(SomeInterface.class)
                            );

                    bind(MyServiceWithNamedComponent.class);
                }
            }
        );
        
        MyServiceWithNamedComponent service = injector.getInstance(MyServiceWithNamedComponent.class);
    }
    
    @Test
    public void testProperies() throws Exception {
        // 1.  Bootstrap on startup
        // 2.  Load by 'id'
        // 3.  Map with prefix
        final Properties props = new Properties();
        
        // Properties is NOT a map
        props.put("id1.some.type",          "d");
        props.put("id1.some.properties",    "simplevalue");
        
        // Properties not provided
        props.put("id2.some.type",          "d");
        
        // Properties is a map
        props.put("id3.some.type",          "d");
        props.put("id3.some.properties.a",  "a");
        props.put("id3.some.properties.b.c","bc");
        props.put("id3.some.properties.d",  "true");
        props.put("id3.some.properties.e",  "123");
        
        ConfigurationManager.loadProperties(props);
        Injector injector = Guice.createInjector(
            new ArchaiusConfigurationModule(),
            new SomeInterfaceModule(),
            new AbstractModule() {
                @Override
                protected void configure() {
                    install(new ComponentModuleBuilder<SomeInterface>()
                            .implementation(BaseD.class)
                            .build(SomeInterface.class)
                            );
                }
            }
        );

        ComponentManager<SomeInterface> ifmanager = injector.getInstance(Key.get(new TypeLiteral<ComponentManager<SomeInterface>>() {}));
        
        BaseD iface2 = (BaseD)ifmanager.get("id2");
        BaseD iface3 = (BaseD)ifmanager.get("id3");
        
        LOG.info(iface2.toString());
        LOG.info(iface3.toString());
        
        try {
            // TODO: Need to fix this unit test
            BaseD iface1 = (BaseD)ifmanager.get("id1");
            LOG.info(iface1.toString());
//            Assert.fail();
        }
        catch (Exception e) {
            
        }
    }
    
    public static class SomeInterfaceProvider implements Provider<SomeInterface> {
        @Override
        public SomeInterface get() {
            LOG.info("hi");
            return null;
        }
    }
    
    @Test
    public void testProperties() throws Exception {
        // 1.  Bootstrap on startup
        // 2.  Load by 'id'
        // 3.  Map with prefix
        final Properties props = new Properties();
        props.put("id1.some.type",          "d");
        props.put("id1.some.string",        "str");
        props.put("id1.some.long",          "1");
        props.put("id1.some.boolean",       "true");
        props.put("id1.some.integer",       "2");
        props.put("id1.some.double",        "2.1");
        props.put("id1.some.dyn1",          "dyn1_value");
        props.put("id1.some.dyn2",          "dyn2_value");
        props.put("id1.some.policy.type",   "pa");
        props.put("id1.some.policy.arg2",   "pa_arg2");
        
        props.put("id2.some.type",          "c");
        props.put("id2.some.subEntity.str", "id2_subEntity_str");
        
        Injector injector = Guice.createInjector(
            new PropertiesConfigurationModule(props),
            new SomeInterfaceModule(),
            new AbstractModule() {
                @Override
                protected void configure() {
                    install(new ComponentModuleBuilder<SomeInterface>()
                            .implementation(BaseA.class)
                            .implementation(BaseB.class)
                            .implementation(BaseC.class)
                            .implementation(BaseD.class)
                            .build(SomeInterface.class)
                            );
                    
                    install(new ComponentModuleBuilder<Policy>()
                            .implementation(PolicyA.class)
                            .implementation(PolicyB.class)
                            .build(Policy.class));
                    
                    bind(SomeInterface.class)
                        .annotatedWith(Names.named("id1"))
                        .toProvider(SomeInterfaceProvider.class);
                }
            },
            new AbstractModule() {
                @Override
                protected void configure() {
                    bind(MyService.class);
                }
            }
        );
        
        MyService service = injector.getInstance(MyService.class);
        
        ComponentManager<SomeInterface> ifmanager = injector.getInstance(Key.get(new TypeLiteral<ComponentManager<SomeInterface>>() {}));
        SomeInterface if1 = ifmanager.get("id1");
        LOG.info(if1.toString());
        
        ConfigurationManager.getConfigInstance().setProperty("id1.some.dyn1", "dyn1_value_new");
        ConfigurationManager.getConfigInstance().setProperty("id1.some.dyn2", "dyn2_value_new");

//                Map<Object, Object> props2 = ConfigurationConverter.getMap(ConfigurationManager.getConfigInstance());
//                for (Entry<Object, Object> prop : props2.entrySet()) {
//                    LOG.info(prop.getKey() + " = " + prop.getValue());
//                }
        
        LOG.info(if1.toString());
    }

    @Test
    @Ignore
    public void testJson() throws Exception {
        Injector injector = Guice.createInjector(
            new PropertiesConfigurationModule(new Properties()),
            new SomeInterfaceModule(),
            new AbstractModule() {
                @Override
                protected void configure() {
                    install(new ComponentModuleBuilder<SomeInterface>()
                            .implementation(BaseA.class)
                            .implementation(BaseB.class)
                            .implementation(BaseC.class)
                            .build(SomeInterface.class)
                            );
                    
                    install(new ComponentModuleBuilder<Policy>()
                            .implementation(PolicyA.class)
                            .implementation(PolicyB.class)
                            .build(Policy.class));
                    
                    bind(SomeInterface.class)
                        .annotatedWith(Names.named("id1"))
                        .toProvider(SomeInterfaceProvider.class);
                }
            },
            new AbstractModule() {
                @Override
                protected void configure() {
                    bind(MyService.class);
                }
            }
        );
        
//        MyService service = injector.getInstance(MyService.class);
        
        ComponentManager<SomeInterface> ifmanager = injector.getInstance(Key.get(new TypeLiteral<ComponentManager<SomeInterface>>() {}));
        
        // 1.  Bootstrap on startup
        // 2.  Load by 'id'
        // 3.  Map with prefix
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode node = mapper.createObjectNode();
        node.put("type",   "a");
        node.put("prop1",  "v1");
        node.put("prop2",  "v2");
        node.put("dyn1",   "dyn1_value");
        node.put("dyn2",   "dyn2_value");
        
        ObjectNode policy = mapper.createObjectNode();
        policy.put("type", "pa");
        policy.put("arg1", "pa_arg2");
        node.put("policy", policy);


        
        
        SomeInterface if1 = ifmanager.get("id1");
        LOG.info(if1.toString());
        
        ConfigurationManager.getConfigInstance().setProperty("id1.some.dyn1", "dyn1_value_new");
        ConfigurationManager.getConfigInstance().setProperty("id1.some.dyn2", "dyn2_value_new");

//                Map<Object, Object> props2 = ConfigurationConverter.getMap(ConfigurationManager.getConfigInstance());
//                for (Entry<Object, Object> prop : props2.entrySet()) {
//                    LOG.info(prop.getKey() + " = " + prop.getValue());
//                }
        
        LOG.info(if1.toString());
    }
    
    @Test
    @Ignore
    public void testRegisterBeforeGet() throws Exception {
        Injector injector = Guice.createInjector(
            new PropertiesConfigurationModule(new Properties()),
            new SomeInterfaceModule()
        );
        
        ComponentManager<SomeInterface> manager = injector.getInstance(Key.get(new TypeLiteral<ComponentManager<SomeInterface>>() {}));
        manager.add("a", new SomeInterface() {});
        
//        final AtomicInteger changed = new AtomicInteger();
//        ListenableReference<SomeInterface> ref = manager.get("a");
//        ref.change(new ReferenceChangeListener<SomeInterface>() {
//            @Override
//            public void removed() {
//            }
//
//            @Override
//            public void changed(SomeInterface newComponent) {
//                changed.incrementAndGet();
//            }
//        });
//        
//        ref.get();
//        Assert.assertEquals(1, changed.get());
    }
    
    @Test
    @Ignore
    public void testRegisterAfterGet() throws Exception {
        Injector injector = Guice.createInjector(
            new PropertiesConfigurationModule(new Properties()),
            new SomeInterfaceModule()
        );
        
        ComponentManager<SomeInterface> manager = injector.getInstance(Key.get(new TypeLiteral<ComponentManager<SomeInterface>>() {}));
        manager.add("a", new SomeInterface() {});
        
//        final AtomicInteger changed = new AtomicInteger();
//        ListenableReference<SomeInterface> ref = manager.get("a");
//        ref.get();
//        ref.change(new ReferenceChangeListener<SomeInterface>() {
//            @Override
//            public void removed() {
//            }
//
//            @Override
//            public void changed(SomeInterface newComponent) {
//                changed.incrementAndGet();
//            }
//        });
//        Assert.assertEquals(0, changed.get());
    }
    
    @Test
    @Ignore
    public void testChangedComponent() throws Exception {
        Injector injector = Guice.createInjector(
            new PropertiesConfigurationModule(new Properties()),
            new SomeInterfaceModule()
        );
        
        ComponentManager<SomeInterface> manager = injector.getInstance(Key.get(new TypeLiteral<ComponentManager<SomeInterface>>() {}));
        manager.add("a", new SomeInterface() {});
        
//        final AtomicInteger changed = new AtomicInteger();
//        ListenableReference<SomeInterface> ref = manager.get("a");
//        ref.change(new ReferenceChangeListener<SomeInterface>() {
//            @Override
//            public void removed() {
//            }
//
//            @Override
//            public void changed(SomeInterface newComponent) {
//                changed.incrementAndGet();
//            }
//        });
//        manager.replace("a", new SomeInterface() {});
//        Assert.assertEquals(2, changed.get());
    }
    
    @Test
    @Ignore
    public void testRemovedComponent() throws Exception {
        Injector injector = Guice.createInjector(
            new PropertiesConfigurationModule(new Properties()),
            new SomeInterfaceModule()
        );
        
        ComponentManager<SomeInterface> manager = injector.getInstance(Key.get(new TypeLiteral<ComponentManager<SomeInterface>>() {}));
        manager.add("a", new SomeInterface() {});
        
//        final AtomicBoolean changed = new AtomicBoolean(false);
//        ListenableReference<SomeInterface> ref = manager.get("a");
//        ref.change(new ReferenceChangeListener<SomeInterface>() {
//            @Override
//            public void removed() {
//                changed.set(true);
//            }
//
//            @Override
//            public void changed(SomeInterface newComponent) {
//            }
//        });
//        ref.get();
//        manager.remove("a");
//        Assert.assertTrue(changed.get());
    }
}
