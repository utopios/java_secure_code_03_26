package com.example.demo_jour_3.entity;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class MetricEntry {
    private final AtomicLong count = new AtomicLong(0);
    private final AtomicLong totalTime = new AtomicLong(0);
    private final AtomicLong maxTime = new AtomicLong(0);
    public void record(long durationMs) {
        count.incrementAndGet();
        totalTime.addAndGet(durationMs);
        maxTime.updateAndGet(current -> Math.max(current,durationMs));
    }

    public void reset() {
        count.set(0);
        totalTime.set(0);
        maxTime.set(0);
    }

    public Map<String, Object> toMap() {
        long localCount = count.get();
        return Map.of(
                "count", localCount,
                "totalTimeMs", totalTime.get(),
                "avgTimeMs", localCount > 0 ? Math.round(totalTime.get() * 10 / localCount) / 10.0 : 0.0,
                "maxTimeMs", maxTime.get()
        );
    }
}
