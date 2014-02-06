package com.netflix.fabricator.properties;

import java.util.Properties;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.MapBinder;
import com.netflix.fabricator.ConfigurationFactory;
import com.netflix.fabricator.MainConfigurationFactory;

public class PropertiesConfigurationModule extends AbstractModule {
    private final Properties props;
    
    public PropertiesConfigurationModule(Properties props) {
        this.props = props;
    }
    
    @Override
    protected void configure() {
        bind(Properties.class).toInstance(props);
        MapBinder.newMapBinder(binder(), String.class, ConfigurationFactory.class);
        bind(MainConfigurationFactory.class).to(PropertiesMainConfigurationFactory.class);
    }
}
