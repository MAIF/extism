package org.extism.sdk;

import com.google.gson.JsonParser;
import org.extism.sdk.manifest.Manifest;
import org.extism.sdk.manifest.MemoryOptions;
import org.extism.sdk.support.JsonSerde;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.extism.sdk.TestWasmSources.CODE;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ManifestTests {

    @Test
    public void shouldSerializeManifestWithWasmSourceToJson() {
        var paths = new HashMap<String, String>();
        paths.put("/tmp/foo", "/tmp/extism-plugins/foo");
        var manifest = new Manifest(List.of(CODE.pathWasmSource()), null, null, null, paths);
        var json = JsonSerde.toJson(manifest);
        assertNotNull(json);

        var object = JsonParser.parseString(json).getAsJsonObject();

        var arr = object.getAsJsonArray("wasm");
        assertThat(arr).isNotNull();
        assertThat(arr.size()).isEqualTo(1);

        var allowedPaths = object.getAsJsonObject("allowed_paths");
        assertThat(allowedPaths).isNotNull();
        assertThat(allowedPaths.size()).isEqualTo(1);
    }

    @Test
    public void shouldSerializeManifestWithWasmSourceAndMemoryOptionsToJson() {

        var manifest = new Manifest(List.of(CODE.pathWasmSource()), new MemoryOptions(4));
        var json = JsonSerde.toJson(manifest);
        assertNotNull(json);

        var object = JsonParser.parseString(json).getAsJsonObject();

        var arr = object.getAsJsonArray("wasm");
        assertThat(arr).isNotNull();
        assertThat(arr.size()).isEqualTo(1);

        var memory = object.getAsJsonObject("memory").get("max").getAsInt();
        assertThat(memory).isEqualTo(4);
    }

    @Test
    public void codeWasmFromFileAndBytesShouldProduceTheSameHash() {

        var byteHash = CODE.byteArrayWasmSource().hash();
        var fileHash = CODE.pathWasmSource().hash();

        assertThat(byteHash).isEqualTo(fileHash);
    }
}
