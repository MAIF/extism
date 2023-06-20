package org.extism.sdk.otoroshi;

import java.util.Optional;

public class OtoroshiLinearMemoryOptions {

    private final Integer min;
    private final Optional<Integer> max;

    public OtoroshiLinearMemoryOptions(Integer min, Optional<Integer> max) {
        this.max = max;
        this.min = min;
    }

    public Integer getMin() {
        return min;
    }

    public Optional<Integer> getMax() {
        return max;
    }
}
