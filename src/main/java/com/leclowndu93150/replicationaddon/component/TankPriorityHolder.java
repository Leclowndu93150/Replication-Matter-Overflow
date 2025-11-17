package com.leclowndu93150.replicationaddon.component;

import net.minecraft.util.Mth;

/**
 * Shared contract for blocks that expose configurable tank priority.
 */
public interface TankPriorityHolder {

    int MIN_PRIORITY = -10;
    int MAX_PRIORITY = 10;

    int getTankPriority();

    void setTankPriority(int priority);

    default void adjustTankPriority(int delta) {
        setTankPriority(getTankPriority() + delta);
    }

    default int clampPriority(int priority) {
        return Mth.clamp(priority, MIN_PRIORITY, MAX_PRIORITY);
    }
}
