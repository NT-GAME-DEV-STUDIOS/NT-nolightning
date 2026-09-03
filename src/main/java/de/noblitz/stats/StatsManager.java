package de.noblitz.stats;

import java.util.concurrent.atomic.AtomicLong;

public class StatsManager {

    private final AtomicLong blockedStrikes = new AtomicLong(0);
    private final AtomicLong preventedFires = new AtomicLong(0);
    private final AtomicLong protectedEntities = new AtomicLong(0);
    private final AtomicLong preventedTransformations = new AtomicLong(0);

    public void incrementBlockedStrikes() {
        blockedStrikes.incrementAndGet();
    }

    public void incrementPreventedFires() {
        preventedFires.incrementAndGet();
    }

    public void incrementProtectedEntities() {
        protectedEntities.incrementAndGet();
    }

    public void incrementPreventedTransformations() {
        preventedTransformations.incrementAndGet();
    }

    public long getBlockedStrikes() {
        return blockedStrikes.get();
    }

    public long getPreventedFires() {
        return preventedFires.get();
    }

    public long getProtectedEntities() {
        return protectedEntities.get();
    }

    public long getPreventedTransformations() {
        return preventedTransformations.get();
    }

    public void reset() {
        blockedStrikes.set(0);
        preventedFires.set(0);
        protectedEntities.set(0);
        preventedTransformations.set(0);
    }
}
