package org.extism.sdk.manifest;

import com.google.gson.annotations.SerializedName;
import org.extism.sdk.wasm.WasmSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Manifest {

    @SerializedName("wasm")
    private ArrayList<WasmSource> sources;

    @SerializedName("memory")
    private MemoryOptions memoryOptions;

    // FIXME remove this and related stuff if not supported in java-sdk
    @SerializedName("allowed_hosts")
    private ArrayList<String> allowedHosts;

    @SerializedName("config")
    private Map<String, String> config;

    public Manifest() {
        this(new ArrayList<>(), null, null, null);
    }

    public Manifest(WasmSource source) {
        this(new ArrayList<>(List.of(source)));
    }

    public Manifest(ArrayList<WasmSource> sources) {
        this(sources, null, null, null);
    }

    public Manifest(ArrayList<WasmSource> sources, MemoryOptions memoryOptions) {
        this(sources, memoryOptions, null, null);
    }

    public Manifest(ArrayList<WasmSource> sources, MemoryOptions memoryOptions, Map<String, String> config) {
        this(sources, memoryOptions, config, null);
    }

    public Manifest(ArrayList<WasmSource> sources, MemoryOptions memoryOptions, Map<String, String> config, ArrayList<String> allowedHosts) {
        this.sources = sources;
        this.memoryOptions = memoryOptions;
        this.config = config;
        this.allowedHosts = allowedHosts;
    }

    public void addSource(WasmSource source) {
        this.sources.add(source);
    }

    public List<WasmSource> getSources() {
        return Collections.unmodifiableList(sources);
    }

    public MemoryOptions getMemoryOptions() {
        return memoryOptions;
    }

    public Map<String, String> getConfig() {
        if (config == null || config.isEmpty()) {
            return Collections.emptyMap();
        }
        return Collections.unmodifiableMap(config);
    }

    public List<String> getAllowedHosts() {
        if (allowedHosts == null || allowedHosts.isEmpty()) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(allowedHosts);
    }
}
