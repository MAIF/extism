package org.extism.sdk.parameters;

import org.extism.sdk.framework.NewFramework;

public class Results extends Parameters {

    public Results(int length) {
        super(length);
    }

    public Results(NewFramework.ExtismVal.ByReference ptr, int length) {
        super(ptr, length);
    }
}
