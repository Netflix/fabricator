package com.netflix.fabricator.component.mapping;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

public class FooModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(Foo.class).annotatedWith(Names.named("string")).toInstance(new FooImpl("string"));
        bind(Foo.class).annotatedWith(Names.named("base64")).toInstance(new FooImpl("base64"));
        bind(Foo.class).annotatedWith(Names.named("jackson")).toInstance(new FooImpl("jackson"));
    }
}
