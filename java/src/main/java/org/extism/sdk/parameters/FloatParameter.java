package org.extism.sdk.parameters;

import org.extism.sdk.LibExtism;

public class FloatParameter extends Parameter<Float> {

    @Override
    public Parameters add(Parameters params, Float value, int position) {
        params.set(item -> {
            item.t = 2;
            item.v.setType(java.lang.Float.TYPE);
            item.v.f32 = value;
            return item;
        }, position);
        return params;
    }
}
