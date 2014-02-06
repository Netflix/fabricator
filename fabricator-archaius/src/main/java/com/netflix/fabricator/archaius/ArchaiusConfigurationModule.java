package com.netflix.fabricator.archaius;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.MapBinder;
import com.netflix.fabricator.ConfigurationFactory;
import com.netflix.fabricator.MainConfigurationFactory;

public class ArchaiusConfigurationModule extends AbstractModule {
    @Override
    protected void configure() {
        MapBinder.newMapBinder(binder(), String.class, ConfigurationFactory.class);
        bind(MainConfigurationFactory.class).to(ArchaiusMainConfigurationFactory.class);
    }
}
