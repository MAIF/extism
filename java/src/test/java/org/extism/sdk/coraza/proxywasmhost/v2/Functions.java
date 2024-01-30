package org.extism.sdk.coraza.proxywasmhost.v2;

import org.extism.sdk.HostFunction;
import org.extism.sdk.LibExtism;
import org.extism.sdk.coraza.proxywasm.VMData;
import org.extism.sdk.coraza.proxywasmhost.internal.imports.v2.ProxyWasmState;

import java.util.Optional;

public class Functions {

    private HostFunction[] functions;

    public Functions(VMData vmData, ProxyWasmState state) {
        functions = new HostFunction[]{
                new HostFunction<>(
                        "proxy_log",
                        this.parameters(3),
                        this.parameters(1),
                        (plugin, params, returns, data) ->
                                state.ProxyLog(plugin, (VMData) data.get(), params[0].v.i32, params[1].v.i32, params[2].v.i32),
                        Optional.of(vmData)
                ).withNamespace("env"),
                new HostFunction<>(
                        "proxy_get_buffer_bytes",
                        this.parameters(5),
                        this.parameters(1),
                        (plugin, params, returns, data) ->
                                state.ProxyGetBuffer(plugin, (VMData) data.get(),
                                        params[0].v.i32,
                                        params[1].v.i32,
                                        params[2].v.i32,
                                        params[3].v.i32,
                                        params[4].v.i32),
                        Optional.of(vmData)
                ).withNamespace("env"),
                new HostFunction<>(
                        "proxy_set_effective_context",
                        this.parameters(1),
                        this.parameters(1),
                        (plugin, params, returns, data) ->
                                state.ProxySetEffectiveContext(plugin, params[0].v.i32),
                        Optional.empty()
                ).withNamespace("env"),
                new HostFunction<>(
                        "proxy_get_header_map_pairs",
                        this.parameters(3),
                        this.parameters(1),
                        (plugin, params, returns, data) ->
                                state.ProxyGetHeaderMapPairs(plugin, (VMData) data.get(), params[0].v.i32, params[1].v.i32, params[2].v.i32),
                        Optional.of(vmData)
                ).withNamespace("env"),
                new HostFunction<>(
                        "proxy_set_buffer_bytes",
                        this.parameters(5),
                        this.parameters(1),
                        (plugin, params, returns, data) ->
                                state.ProxySetBuffer(plugin, (VMData) data.get(),
                                        params[0].v.i32,
                                        params[1].v.i32,
                                        params[2].v.i32,
                                        params[3].v.i32,
                                        params[4].v.i32),
                        Optional.of(vmData)
                ).withNamespace("env"),
                new HostFunction<>(
                        "proxy_get_header_map_value",
                        this.parameters(5),
                        this.parameters(1),
                        (plugin, params, returns, data) ->
                                state.ProxyGetHeaderMapValue(plugin,
                                        (VMData) data.get(),
                                        params[0].v.i32,
                                        params[1].v.i32,
                                        params[2].v.i32,
                                        params[3].v.i32,
                                        params[4].v.i32),
                        Optional.of(vmData)
                ).withNamespace("env"),
                new HostFunction<>(
                        "proxy_get_property",
                        this.parameters(4),
                        this.parameters(1),
                    (plugin, params, returns, data) ->
                        state.ProxyGetProperty(plugin, (VMData) data.get(), params[0].v.i32, params[1].v.i32, params[2].v.i32, params[3].v.i32),
                    Optional.of(vmData)
                ).withNamespace("env"),
                new HostFunction<>(
                    "proxy_increment_metric",
                        new LibExtism.ExtismValType[]{LibExtism.ExtismValType.I32,LibExtism.ExtismValType.I64},
                        this.parameters(1),
                    (plugin, params, returns, data) ->
                            state.ProxyIncrementMetricValue(plugin, (VMData) data.get(), params[0].v.i32, params[1].v.i64),
                    Optional.of(vmData)
                ).withNamespace("env"),
                new HostFunction<>(
                        "proxy_define_metric",
                        this.parameters(4),
                        this.parameters(1),
                        (plugin, params, returns, data) ->
                            state.ProxyDefineMetric(plugin, params[0].v.i32, params[1].v.i32, params[2].v.i32, params[3].v.i32),
                        Optional.empty()
                ).withNamespace("env"),
                new HostFunction<>(
                        "proxy_set_tick_period_milliseconds",
                        this.parameters(1),
                        this.parameters(1),
                        (plugin, params, returns, data) ->
                            state.ProxySetTickPeriodMilliseconds((VMData) data.get(), params[0].v.i32),
                        Optional.of(vmData)
                ).withNamespace("env"),
                new HostFunction<>(
                        "proxy_replace_header_map_value",
                        this.parameters(5),
                        this.parameters(1),
                        (plugin, params, returns, data) ->
                                state.ProxyReplaceHeaderMapValue(plugin,
                                        (VMData) data.get(),
                                        params[0].v.i32,
                                        params[1].v.i32,
                                        params[2].v.i32,
                                        params[3].v.i32,
                                        params[4].v.i32),
                        Optional.of(vmData)
                ).withNamespace("env"),
                new HostFunction<>(
                    "proxy_send_local_response",
                        this.parameters(8),
                        this.parameters(1),
                        (plugin, params, returns, data) ->
                                state.ProxySendHttpResponse(plugin,
                                        params[0].v.i32,
                                        params[1].v.i32,
                                        params[2].v.i32,
                                        params[3].v.i32,
                                        params[4].v.i32,
                                        params[5].v.i32,
                                        params[6].v.i32,
                                        params[7].v.i32),
                    Optional.empty()
                ).withNamespace("env")
        };
    }

    public HostFunction[] all() {
        return functions;
    }

    public LibExtism.ExtismValType[] parameters(int n) {
        LibExtism.ExtismValType[] params = new LibExtism.ExtismValType[n];
        for (int i = 0; i < n; i++) {
            params[i] = LibExtism.ExtismValType.I32;
        }
        return params;
    }

}
