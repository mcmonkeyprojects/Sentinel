package org.mcmonkey.sentinel;

import java.util.UUID;

/**
 * Current target helper object.
 */
public class SentinelCurrentTarget {

    /**
     * The target's UUID.
     */
    public UUID targetID;

    /**
     * Remaining ticks before the target is invalid.
     */
    public long ticksLeft;

    /**
     * If true: the target is in line-of-sight and/or reachable and/or never been calculated.
     * If false: the target is out of LOS or unreachable.
     */
    public boolean hasLos;

    /**
     * Returns a hashcode for this instance.
     */
    @Override
    public int hashCode() {
        return targetID.hashCode();
    }

    /**
     * Returns whether this object equals another.
     */
    @Override
    public boolean equals(Object o) {
        return o instanceof SentinelCurrentTarget && ((SentinelCurrentTarget) o).targetID.equals(targetID);
    }
}
