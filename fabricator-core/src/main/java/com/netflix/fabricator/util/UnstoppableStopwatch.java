package com.netflix.fabricator.util;

import com.google.common.base.Stopwatch;

import java.util.concurrent.TimeUnit;

public class UnstoppableStopwatch {
    private final Stopwatch sw;
    
    public UnstoppableStopwatch() {
        sw = Stopwatch.createStarted();
    }
    
    public long elapsed(TimeUnit units) {
        return sw.elapsed(units);
    }
}
