package org.extism.sdk.coraza.proxywasmhost;

import org.extism.sdk.ExtismException;
import org.extism.sdk.HostFunction;
import org.extism.sdk.LibExtism;
import org.extism.sdk.Plugin;
import org.extism.sdk.coraza.proxywasm.VMData;
import org.extism.sdk.coraza.proxywasmhost.internal.imports.v2.ProxyWasmState;
import org.extism.sdk.coraza.proxywasmhost.v2.Functions;
import org.extism.sdk.coraza.proxywasmhost.v2.Types;
import org.extism.sdk.manifest.Manifest;
import org.extism.sdk.wasmotoroshi.LinearMemory;
import org.extism.sdk.wasmotoroshi.LinearMemoryOptions;
import org.extism.sdk.wasmotoroshi.Parameters;
import org.extism.sdk.wasmotoroshi.Results;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class ProxyWasmPlugin {

    public Plugin plugin;
    private VMData vmData;
    private int instanceIndex;

//    private String configuration = "{\n" +
//            "  \"directives_map\": {\n" +
//            "    \"default\": [\n" +
//            "      \"SecDebugLogLevel 9\",\n" +
//            "      \"SecRuleEngine On\",\n" +
//            "      \"SecRule REQUEST_URI \\\"@streq /admin\\\" \\\"id:101,phase:1,t:lowercase,deny\\\"\",\n" +
//            "      \"SecRule REMOTE_ADDR \\\"@rx .*\\\" \\\"id:1,phase:1,deny,status:403\\\"\"\n" +
//            "    ]\n" +
//            "  },\n" +
//            "  \"rules\": [\n" +
//            "    \"SecDebugLogLevel 9\",\n" +
//            "    \"SecRuleEngine On\",\n" +
//            "    \"SecRule REQUEST_URI \\\"@streq /admin\\\" \\\"id:101,phase:1,t:lowercase,deny\\\"\",\n" +
//            "    \"SecRule REMOTE_ADDR \\\"@rx .*\\\" \\\"id:1,phase:1,deny,status:403\\\"\"\n" +
//            "  ],\n" +
//            "  \"default_directive\": \"default\"\n" +
//            "}";
    private String configuration = "{\n" +
        "                            \"directives_map\": {\n" +
        "                                \"default\": [\n" +
        "                                    \"SecDebugLogLevel 9\",\n" +
        "                                    \"SecRuleEngine On\",\n" +
        "                                    \"SecRule REQUEST_URI \\\"@streq /admin\\\" \\\"id:101,phase:1,t:lowercase,deny\\\"\"\n" +
        "                                ]\n" +
        "                            },\n" +
        "                            \"default_directives\": \"default\"\n" +
        "                        }";

    private int vmConfigurationSize = 0;
    private int pluginConfigurationSize = configuration.getBytes(StandardCharsets.UTF_8).length;
    private ProxyWasmState state;

    private Manifest manifest;

    public ProxyWasmPlugin(Manifest manifest, VMData vmData) {
        this.manifest = manifest;

        vmData.configuration = configuration;
        this.state = new ProxyWasmState(100, 20);

        this.vmData = vmData;
    }

    private void proxyOnContexCreate(int contextId, int rootContextId) {
        Parameters prs = new Parameters(2)
                .pushInts(contextId, rootContextId);

        plugin.callWithoutResults("proxy_on_context_create", prs);
    }

    private boolean proxyOnVmStart() {
        Parameters prs = new Parameters(2).pushInts(0, vmConfigurationSize);

        Results proxyOnVmStartAction = plugin.call("proxy_on_vm_start", prs, 1);

        return proxyOnVmStartAction.getValues()[0].v.i32 != 0;
    }

    public boolean proxyOnConfigure(int rootContextId) {
        Parameters prs = new Parameters(2).pushInts(rootContextId, pluginConfigurationSize);

        Results proxyOnConfigureAction = plugin.call("proxy_on_configure", prs, 1);

        return proxyOnConfigureAction.getValues()[0].v.i32 != 0;
    }

    public void proxyStart() {
        plugin.callWithoutParams("_start", 0);
    }

    public void proxyCheckABIVersion() {
        plugin.callWithoutParams("proxy_abi_version_0_2_0", 0);
    }

    public void proxyOnRequestHeaders(int contextId) {
        int endOfStream = 1;
        int sizeHeaders = 0;

        Parameters prs = new Parameters(3).pushInts(contextId, sizeHeaders, endOfStream);

        Results requestHeadersAction = plugin.call("proxy_on_request_headers", prs, 1);

        System.out.println(Types.Result.values()[requestHeadersAction.getValues()[0].v.i32]);
    }

    public ProxyWasmPlugin start() {
        this.plugin = new Plugin(manifest, true, new Functions(vmData, state).all());

        proxyStart();

        proxyCheckABIVersion();

        // according to ABI, we should create a root context id before any operations
        proxyOnContexCreate(this.state.rootContextId, 0);

        if (!proxyOnVmStart()) {
            throw new ExtismException("Failed to start vm");
        }

        if (!proxyOnConfigure(this.state.rootContextId)) {
            throw new ExtismException("Failed to configure");
        }

//        System.out.println(plugin.getContext().error(plugin));

        return this;
    }

    public ProxyWasmPlugin run() {
        proxyOnContexCreate(this.state.contextId, this.state.rootContextId);
        proxyOnRequestHeaders(this.state.contextId);

        return this;
    }

    public ProxyWasmPlugin stop() {
        // TODO - call proxyOnDone, proxyOnDelete
        return this;
    }

}
