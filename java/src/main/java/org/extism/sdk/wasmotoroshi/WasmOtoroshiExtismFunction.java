package org.extism.sdk.wasmotoroshi;

import java.util.Optional;

public interface WasmOtoroshiExtismFunction<T extends WasmOtoroshiHostUserData> {
    void invoke(
            WasmOtoroshiInternal plugin,
            WasmBridge.ExtismVal[] params,
            WasmBridge.ExtismVal[] returns,
            Optional<T> data
    );
}
