package org.extism.sdk.otoroshi;

import java.util.Optional;

public interface OtoroshiExtismFunction<T extends OtoroshiHostUserData> {
    void invoke(
            OtoroshiInternal plugin,
            Bridge.ExtismVal[] params,
            Bridge.ExtismVal[] returns,
            Optional<T> data
    );
}
