package org.extism.sdk.parameters;

import org.extism.sdk.LibExtism;

public class DoubleParameter extends Parameter<Double> {

    @Override
    public Parameters add(Parameters params, Double value, int position) {
        params.set(item -> {
            item.t = 3;
            item.v.setType(java.lang.Double.TYPE);
            item.v.f64 = value;
            return item;
        }, position);
        return params;
    }
}
