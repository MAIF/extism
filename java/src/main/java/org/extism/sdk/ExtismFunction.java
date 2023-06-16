package org.extism.sdk;

import org.extism.sdk.framework.NewFramework;

import java.util.Optional;

public interface ExtismFunction<T extends HostUserData> {
    void invoke(
            Internal plugin,
            NewFramework.ExtismVal[] params,
            NewFramework.ExtismVal[] returns,
            Optional<T> data
    );
}
