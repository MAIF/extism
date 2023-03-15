package org.extism.sdk.parameters;

public abstract class Parameter<T> {
    public abstract Parameters add(Parameters params, T value, int position);
}
