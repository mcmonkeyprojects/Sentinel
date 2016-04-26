package org.mcmonkey.sentinel;

import java.util.UUID;

public class SentinelCurrentTarget {

    public UUID targetID;

    public long ticksLeft;

    @Override
    public int hashCode() {
        return targetID.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof SentinelCurrentTarget && ((SentinelCurrentTarget) o).targetID.equals(targetID);
    }
}
