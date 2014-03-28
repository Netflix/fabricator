package com.netflix.fabricator.component;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import com.google.common.collect.MapDifference.ValueDifference;
import com.netflix.fabricator.ComponentConfiguration;
import com.netflix.fabricator.ComponentConfigurationResolver;
import com.netflix.fabricator.ComponentType;
import com.netflix.fabricator.TypeConfigurationResolver;
import com.netflix.fabricator.component.exception.ComponentAlreadyExistsException;
import com.netflix.fabricator.component.exception.ComponentCreationException;
import com.netflix.governator.annotations.Configuration;
import com.netflix.governator.annotations.ConfigurationVariable;
import com.netflix.governator.annotations.binding.Background;

/**
 * Service responsible for refreshing a predef
 * @author elandau
 *
 */
public class BaseComponentRefreshService<T> {
    private static final Logger LOG = LoggerFactory.getLogger(BaseComponentRefreshService.class);
    
    public final long DEFAULT_REFRESH_RATE = 60;
    
    /**
     * The manager for these components.  
     */
    private final ComponentManager<T>       manager;
    
    /**
     * Executor for the refresh task
     */
    private final ScheduledExecutorService  executor;
    
    /**
     * Prefix based on the component type makes it possible to configure this for 
     * any component manager
     */
    @ConfigurationVariable(name="prefix")
    private final String componentName;

    @Configuration(value="${prefix}.refresh.refreshRateInSeconds")
    private long refreshRate = DEFAULT_REFRESH_RATE;
    
    @Configuration(value="${prefix}.refresh.enabled")
    private boolean enabled = false;
    
    /**
     * Future for refresh task
     */
    private ScheduledFuture<?> refreshFuture;
    
    /**
     * Resolver for the core configuration for these components
     */
    private final ComponentConfigurationResolver configResolver;
    
    /**
     * List of 'known' configurations.  We keep this outside of what's in the ComponentManager
     * so we can keep track of the raw configuration
     */
    private Map<String, ComponentConfiguration> configs = ImmutableMap.of();
    
    @Inject
    public BaseComponentRefreshService(
            ComponentManager<T>                  manager,
            ComponentType<T>                     type,
            TypeConfigurationResolver            config,
            @Background ScheduledExecutorService executor) {
        this.componentName  = type.getType();
        this.manager        = manager;
        this.executor       = executor;
        this.configResolver = config.getConfigurationFactory(type.getType());
    }
    
    @PostConstruct
    public void init() {
        if (enabled) {
            LOG.info(String.format("Starting '%s' refresh task", componentName));
            manager.apply(getUpdateTask());
            this.refreshFuture = executor.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    manager.apply(getUpdateTask());
                }
            }, refreshRate, refreshRate, TimeUnit.SECONDS);
        }
        else {
            LOG.info(String.format("'%s' refresh task diabled", componentName));
        }
    }
    
    private Runnable getUpdateTask() {
        return new Runnable() {
            @Override
            public void run() {
                // Get a snapshot of the current configuration
                Map<String, ComponentConfiguration> newConfigs = configResolver.getAllConfigurations();
                MapDifference<String, ComponentConfiguration> diff = Maps.difference(newConfigs, configs);
                
                // new configs
                for (Entry<String, ComponentConfiguration> entry : diff.entriesOnlyOnLeft().entrySet()) {
                    LOG.info("Adding config: " + entry.getKey() + " " + entry.getValue().toString());
                    try {
                        manager.load(entry.getValue());
                    } catch (ComponentAlreadyExistsException e) {
                    } catch (ComponentCreationException e) {
                        LOG.warn("Failed to create component " + entry.getKey(), e);
                    }
                }
                
                // removed configs
                for (Entry<String, ComponentConfiguration> entry : diff.entriesOnlyOnRight().entrySet()) {
                    LOG.info("Remove config: " + entry.getKey() + " " + entry.getValue().toString());
                    manager.remove(entry.getKey());
                }
                
                // modified configs
                for (Entry<String, ValueDifference<ComponentConfiguration>> entry : diff.entriesDiffering().entrySet()) {
                    LOG.info("Replace config: " + entry.getKey() + " " + entry.getValue().toString());
                    try {
                        manager.replace(entry.getValue().leftValue());
                    } catch (ComponentCreationException e) {
                        LOG.warn("Failed to create component " + entry.getKey(), e);
                    }
                }
            }
        };
    }
    
    @PreDestroy
    public void shutdown() {
        if (refreshFuture != null) {
            refreshFuture.cancel(true);
        }
    }
}
