package org.extism.sdk.wasmotoroshi;

import java.util.Optional;

public class LinearMemoryOptions {

    private final Integer min;
    private final Optional<Integer> max;

    public LinearMemoryOptions(Integer min, Optional<Integer> max) {
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
