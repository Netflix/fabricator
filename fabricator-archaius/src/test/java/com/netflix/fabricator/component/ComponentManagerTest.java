package com.netflix.fabricator.component;

import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Scopes;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Named;
import com.netflix.fabricator.ComponentType;
import com.netflix.fabricator.annotations.SubType;
import com.netflix.fabricator.annotations.Type;
import com.netflix.fabricator.component.ComponentManager;
import com.netflix.fabricator.component.ComponentManagerTest;
import com.netflix.fabricator.component.SynchronizedComponentManager;
import com.netflix.fabricator.component.exception.ComponentAlreadyExistsException;
import com.netflix.fabricator.component.exception.ComponentCreationException;
import com.netflix.fabricator.guice.ComponentModuleBuilder;
import com.netflix.fabricator.properties.PropertiesConfigurationModule;
import com.netflix.fabricator.supplier.ListenableSupplier;
import com.netflix.fabricator.supplier.SupplierWithDefault;
import com.netflix.config.ConfigurationManager;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.Properties;

/**
 * Test Driven development for Beaver library.
 * Prerequisites:
 * Environment:
 * 1. Guice injector environment to inject anything
 * 2. Archaius
 * 3. Jackson
 * 4. Properties files
 * Code:
 * 1. Classes with builder pattern built in.
 * 1.1. Different builder classes with focuses on different patterns.
 * 1.1.1. Simple properties
 * 1.1.1.1. Supplier argument type.
 * 1.1.1.2. Simple property type.
 * 1.1.1.3. Composite properties type.
 * 1.1.2. Supplier properties.
 * 1.1.2.1. With default values.
 * 1.1.2.2. Without default values.
 * 1.1.3. Compostite properties
 * 1.1.3.1. Argument type has to be composite.
 * 2. Prebuilt objects can be directly retrieved without construction
 * 3. Negative cases
 */
public class ComponentManagerTest {

    private static final Logger logger = LoggerFactory.getLogger(ComponentManagerTest.class);

    /**
     * Example of a subentity
     *
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
    @SubType("policy")
    public static interface Policy {
    }

    /**
     * Implementation of a policy with one String arg
     *
     * @author elandau
     */
    @SubType("pa")
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
    @SubType("pb")
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


    /**
     *
     * @author elandau
     *
     */
    @SubType("a")
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
                logger.info("dyn1=" + supplier.get());
                this.dyn1.addOverride(supplier);
                return this;
            }

            public Builder withDyn2(ListenableSupplier<String> supplier) {
                logger.info("dyn2=" + supplier.get());
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
                   logger.info("Value has changed to : " + input);
                    return null;
                }
            });
        }
        
        public Policy getPolicy() {
            return policy;
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
    @SubType("b")
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

    @SubType("c")
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

    @SubType("d")
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
        private SomeInterface if1;
        private SomeInterface if2;

        @Inject
        public MyService(final ComponentManager<SomeInterface> manager) throws ComponentCreationException, ComponentAlreadyExistsException {
            if1 = manager.get("id1");
            if2 = manager.get("id2");
        }
    }

    public static class MyServiceWithNamedComponent {
        private SomeInterface if1;
        private SomeInterface if2;

        @Inject
        public MyServiceWithNamedComponent(@Named("id1") SomeInterface if1, @Named("id2") SomeInterface if2) {
            this.if1 = if1;
            this.if2 = if2;
        }
    }
    @Before
    public void setUp() throws Exception {


    }

    public static class SomeInterfaceModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(new TypeLiteral<ComponentManager<SomeInterface>>(){})
                    .to(new TypeLiteral<SynchronizedComponentManager<SomeInterface>>(){})
                    .in(Scopes.SINGLETON);

            bind(new TypeLiteral<ComponentType<SomeInterface>>(){})
                    .toInstance(new ComponentType<SomeInterface>("some"));

            install(new ComponentModuleBuilder<SomeInterface>()
                    .build(SomeInterface.class));
        }
    }

    @Test
    public void testEmbeddedEntity() throws Exception {
        final Properties props = new Properties();
        props.put("id1.some.type",          "a");
        props.put("id1.some.string",        "str");
        props.put("id1.some.dyn1",          "dyn1_value");
        props.put("id1.some.dyn2",          "dyn2_value");
        props.put("id1.some.policy.type",   "pb");
        props.put("id1.some.policy.arg1",   "pb_arg1");

        Injector injector = Guice.createInjector(
                new PropertiesConfigurationModule(props),
                new SomeInterfaceModule(),
                new AbstractModule() {
                    @Override
                    protected void configure() {
                        install(new ComponentModuleBuilder<SomeInterface>()
                                .implementation(BaseA.class)
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
                        bind(MyService.class);
                    }
                }
        );

        ComponentManager<SomeInterface> ifmanager = injector.getInstance(Key.get(new TypeLiteral<ComponentManager<SomeInterface>>() {}));
        BaseA if1 = (BaseA)ifmanager.get("id1");
        logger.info("get id4 from manager: " + if1.toString());
        
        Assert.assertNotNull(if1.getPolicy());
    }
    
    @Test
    public void testComponentManager() throws ComponentCreationException, ComponentAlreadyExistsException {
        final Properties props = new Properties();
        props.put("id1.some.type",          "d");
        props.put("id1.some.string",        "str");
        props.put("id1.some.long",          "1");
        props.put("id1.some.boolean",       "true");
        props.put("id1.some.integer",       "2");
        props.put("id1.some.double",        "2.1");

        props.put("id2.some.type",          "c");
        props.put("id2.some.subEntity.str", "id2_subEntity_str");

        props.put("id3.some.type",          "b");

        props.put("id4.some.type",          "a");
        props.put("id4.some.string",        "str");
        props.put("id4.some.dyn1",          "dyn1_value");
        props.put("id4.some.dyn2",          "dyn2_value");
        props.put("id4.some.policy.type",   "pb");
        props.put("id4.some.policy.arg1",   "pb_arg1");

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
                                .implementation("pb", PolicyB.class)
                                .build(Policy.class));
                    }
                },
                new AbstractModule() {
                    @Override
                    protected void configure() {
                        install(new ComponentModuleBuilder<SomeInterface>()
                                .named("id1")
                                .named("id2")
                                .build(SomeInterface.class)
                        );
                    }
                }
        );

        ComponentManager<SomeInterface> ifmanager = injector.getInstance(Key.get(new TypeLiteral<ComponentManager<SomeInterface>>() {}));
        SomeInterface if1 = ifmanager.get("id1");
        logger.info("get id1 from manager: " + if1.toString());
        Assert.assertEquals(BaseD.class, if1.getClass());

        SomeInterface if2 = ifmanager.get("id2");
        logger.info("get id2 from manager: " + if2.toString());
        Assert.assertEquals(BaseC.class, if2.getClass());

        SomeInterface if3 = ifmanager.get("id3");
        logger.info("get id3 from manager: " + if3.toString());
        Assert.assertEquals(BaseB.class, if3.getClass());

        SomeInterface if4 = ifmanager.get("id4");
        logger.info("get id4 from manager: " + if4.toString());
        Assert.assertEquals(BaseA.class, if4.getClass());

        ConfigurationManager.getConfigInstance().setProperty("id1.some.dyn1", "dyn1_value_new");
        ConfigurationManager.getConfigInstance().setProperty("id1.some.dyn2", "dyn2_value_new");

        logger.info("if4 after setProperty: " + if4.toString());

        MyService service = injector.getInstance(MyService.class);
        Assert.assertEquals(if1, service.if1);
        Assert.assertEquals(if2, service.if2);

        MyServiceWithNamedComponent serviceWithNamedComponent = injector.getInstance(MyServiceWithNamedComponent.class);
        Assert.assertEquals(if1, serviceWithNamedComponent.if1);
        Assert.assertEquals(if2, serviceWithNamedComponent.if2);
    }

}
