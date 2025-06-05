package org.extism.sdk;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import org.extism.sdk.manifest.Manifest;
import org.extism.sdk.manifest.MemoryOptions;
import org.extism.sdk.wasmotoroshi.*;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.extism.sdk.TestWasmSources.CODE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import com.google.gson.JsonObject;
import com.google.gson.Gson;

public class WithoutExtism {

    @Test
    public void shouldWorks() {
        Manifest manifest = new Manifest(Collections.singletonList(CODE.getJavyPath()));

        try(var instance = new Plugin(manifest, true, null)) {
            var result = LibExtism.INSTANCE.raw_call(instance.pluginPointer, "execute");

            String error = instance.getExtensionPluginError();

            System.out.println(error);

            System.out.println(result);
//            assertEquals(result.getValue(0).v.i32, 5);
        }
    }
}