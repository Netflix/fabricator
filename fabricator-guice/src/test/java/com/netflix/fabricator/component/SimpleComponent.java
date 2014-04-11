package com.netflix.fabricator.component;

import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import com.netflix.fabricator.annotations.Type;

@Type("simple") 
public class SimpleComponent {
    public static class Builder {
        private String   str = null;
        private Class<?> cls = null;
        private Integer  i = null;
        private Boolean  b = null;
        private Double   d = null;
        private Short    shrt = null;
        private Properties props = null;
        
        public Builder withString(String str) {
            this.str = str;
            return this;
        }
        
        public Builder withInteger(Integer i) {
            this.i = i;
            return this;
        }
        
        public Builder withClass(Class<?> cs) {
            this.cls = cls;
            return this;
        }
        
        public Builder withBoolean(boolean b) {
            this.b = b;
            return this;
        }
        
        public Builder withDouble(double d) {
            this.d = d;
            return this;
        }
        
//        public Builder withShort(short shrt) {
//            this.shrt = shrt;
//            return this;
//        }
        
        public Builder withProperties(Properties props) {
            this.props = props;
            return this;
        }
        
        public SimpleComponent build() {
            return new SimpleComponent(this);
        }
    }
    
    private final Builder builder;
    
    public static Builder builder() {
        return new Builder();
    }

    private SimpleComponent(Builder builder) {
        this.builder = builder;
    }

    public String getString() {
        return builder.str;
    }
    
    public Boolean hasString() {
        return builder.str != null;
    }
    
    public Integer getInteger() {
        return builder.i;
    }
    
    public Boolean hasInteger() {
        return builder.i != null;
    }

    public Short getShort() {
        return builder.shrt;
    }
    
    public Boolean hasShort() {
        return builder.shrt != null;
    }

    public Double getDouble() {
        return builder.d;
    }
    
    public Boolean hasDouble() {
        return builder.d != null;
    }

    public Boolean getBoolean() {
        return builder.b;
    }
    
    public Boolean hasBoolean() {
        return builder.b != null;
    }

    public Class _getClass() {
        return builder.cls;
    }
    
    public Boolean hasClass() {
        return builder.cls != null;
    }

    public Properties getProperties() {
        return builder.props;
    }
    
    public Boolean hasProperties() {
        return builder.props != null;
    }

    private boolean postConstructCalled = false;
    @PostConstruct
    public void init() {
        this.postConstructCalled = true;
    }
    
    public boolean wasPostConstructCalled() {
        return postConstructCalled;
    }
    
    private boolean preDestroyCalled = false;
    @PreDestroy
    public void shutdown() {
        this.preDestroyCalled = true;
    }
    
    public boolean wasPreDestroyCalled() {
        return preDestroyCalled;
    }
    
}
