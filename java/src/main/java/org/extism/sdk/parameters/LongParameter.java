package org.extism.sdk.parameters;

import org.extism.sdk.LibExtism;

public class LongParameter extends Parameter<Long> {

    @Override
    public Parameters add(Parameters params, Long value, int position) {
        params.set(item -> {
            item.t = 1;
            item.v.setType(java.lang.Long.TYPE);
            item.v.i64 = value;
            return item;
        }, position);
        return params;
    }
}
