package com.netflix.fabricator;

import java.lang.reflect.Method;

public interface PropertyBinderResolver {
    PropertyBinder get(Method method);
}
