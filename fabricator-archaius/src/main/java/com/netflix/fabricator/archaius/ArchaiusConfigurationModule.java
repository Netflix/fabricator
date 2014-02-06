package com.netflix.fabricator.archaius;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.MapBinder;
import com.netflix.fabricator.ComponentConfigurationResolver;
import com.netflix.fabricator.TypeConfigurationResolver;

public class ArchaiusConfigurationModule extends AbstractModule {
    @Override
    protected void configure() {
        MapBinder.newMapBinder(binder(), String.class, ComponentConfigurationResolver.class);
        bind(TypeConfigurationResolver.class).to(ArchaiusTypeConfigurationResolver.class);
    }
}
