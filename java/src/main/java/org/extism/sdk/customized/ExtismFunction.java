package org.extism.sdk.customized;

import java.util.Optional;

public interface ExtismFunction<T extends HostUserData> {
    void invoke(
            Internal plugin,
            Bridge.ExtismVal[] params,
            Bridge.ExtismVal[] returns,
            Optional<T> data
    );
}
