package com.netflix.fabricator.archaius;

import javax.inject.Singleton;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.MapBinder;
import com.netflix.fabricator.ComponentConfigurationResolver;
import com.netflix.fabricator.TypeConfigurationResolver;

@Singleton
public class ArchaiusConfigurationModule extends AbstractModule {
    @Override
    protected void configure() {
        MapBinder.newMapBinder(binder(), String.class, ComponentConfigurationResolver.class);
        bind(TypeConfigurationResolver.class).to(ArchaiusTypeConfigurationResolver.class);
    }
    
    // These implementations of hashCode and equals guarantee that Guice will dedup modules that installed multiple times
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
 
    @Override
    public boolean equals(Object other) {
        return getClass().equals(other.getClass());
    }
}
