package org.extism.sdk.parameters;

public class IntegerParameter extends Parameter<Integer> {

    @Override
    public Parameters add(Parameters params, Integer value, int position) {
        params.set(item -> {
            item.t = 0;
            item.v.setType(java.lang.Integer.TYPE);
            item.v.i32 = value;
            return item;
        }, position);
        return params;
    }


    public Parameters addAll(Parameters params, Integer... values) {
        for (int i = 0; i < values.length; i++) {
            final int position = i;
            params.set(item -> {
                item.t = 0;
                item.v.setType(java.lang.Integer.TYPE);
                item.v.i32 = values[position];
                return item;
            }, i);
        }

        return params;
    }
}
