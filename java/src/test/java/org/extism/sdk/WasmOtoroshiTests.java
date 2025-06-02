package org.extism.sdk;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.sun.jna.Pointer;
import org.extism.sdk.manifest.Manifest;
import org.extism.sdk.manifest.MemoryOptions;
import org.extism.sdk.wasm.WasmSourceResolver;
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

public class WasmOtoroshiTests {

    @Test
    public void shouldWorks() {
        Manifest manifest = new Manifest(Collections.singletonList(CODE.getRawAdditionPath()));

        LibExtism.ExtismValType[] parametersTypes = new LibExtism.ExtismValType[]{LibExtism.ExtismValType.I64};
        LibExtism.ExtismValType[] resultsTypes = new LibExtism.ExtismValType[]{LibExtism.ExtismValType.I64};

        ExtismFunction<HostUserData> helloWorldFunction = (plugin, params, returns, data) -> {
            System.out.println("Hello from Java Host Function!");
        };

        var f = new HostFunction<>(
                "hello_world",
                parametersTypes,
                resultsTypes,
                helloWorldFunction,
                Optional.empty()
        ).withNamespace("env");

        var functions = new HostFunction[]{f};

        List<Integer> test = new ArrayList<>(500);
        for (int i = 0; i < 500; i++) {
            test.add(i);
        }

        try(var instance = new Plugin(manifest, true, functions)) {
            test.parallelStream().forEach(number -> {
                try(var params = new Parameters(2)
                        .pushInts(2, 3)) {

                    Results result = instance.call("add", params, 1);
                    assertEquals(result.getValue(0).v.i32, 5);
                }
            });
        }
    }

    @Test
    public void shouldExistmCallWorks() throws Exception {
        var manifest = new Manifest(Collections.singletonList(CODE.pathWasmWebAssemblyFunctionSource()));

        LibExtism.ExtismValType[] parametersTypes = new LibExtism.ExtismValType[]{LibExtism.ExtismValType.I64};
        LibExtism.ExtismValType[] resultsTypes = new LibExtism.ExtismValType[]{LibExtism.ExtismValType.I64};

        var functions = new HostFunction[]{
                new HostFunction<>(
                        "hello_world",
                        parametersTypes,
                        resultsTypes,
                        (plugin, params, returns, data) -> {
                            System.out.println("Hello from Java Host Function !!");
                }, Optional.empty()
                ).withNamespace("env")
        };

        try (var instance = new Plugin(manifest, true, functions)) {
            instance.call("execute", "".getBytes(StandardCharsets.UTF_8));
        }
    }


    @Test
    public void shouldInvokeNativeFunction() {
        Manifest manifest = new Manifest(Arrays.asList(CODE.getRawAdditionPath()));
        String functionName = "add";

        Parameters params = new Parameters(2)
                .pushInts(2, 3);

        try (var instance = new Plugin(manifest, true, null, null)) {
            Results result = instance.call(functionName, params, 1);
            assertEquals(result.getValues()[0].v.i32, 5);

        }
    }

    int lastValidByte(byte[] arr) {
        for(int i=0; i<arr.length; i++) {
            if (arr[i] == 0) {
                return i;
            }
        }
        return arr.length;
    }

//    @Test
//    public void shouldSucceedInCreatingLinearMemoryWithCustomNamespace() {
//        var manifest = new Manifest(Collections.singletonList(CODE.pathWasmWebAssemblyFunctionSource()));
//
//        var parametersTypes = new LibExtism.ExtismValType[]{LibExtism.ExtismValType.I64};
//        var resultsTypes = new LibExtism.ExtismValType[]{LibExtism.ExtismValType.I64};
//
//        var functions = new HostFunction[]{
//                new HostFunction<>(
//                        "hello_world",
//                        parametersTypes,
//                        resultsTypes,
//                        (plugin, params, returns, data) -> {
//                            var memory = plugin.getLinearMemory("foo", "bar");
//
//                            var arraySize = 65356;
//                            var mem = memory.getByteArray(0, arraySize);
//                            var size = lastValidByte(mem);
//
//                            assertEquals("foo bar message", new String(Arrays.copyOf(mem, size), StandardCharsets.UTF_8));
//                            System.out.println("Hello from Java Host Function!");
//                        },
//                        Optional.empty()
//                ).withNamespace("env")
//        };
//
//        var memory = new LinearMemory("foo", "bar", new LinearMemoryOptions(1, Optional.empty()));
//
//        try(var instance = new Plugin(manifest, true, functions, new LinearMemory[]{
//                memory
//        })) {
//            var message = "foo bar message";
//            instance.writeBytes(message.getBytes(StandardCharsets.UTF_8), message.length(), 0, "bar", "foo");
//
//            instance.call("execute", "".getBytes(StandardCharsets.UTF_8));
//            instance.reset();
//
//            var linearMemory = instance.getMemory("foo", "bar");
//            var arraySize = 65356;
//            var mem = linearMemory.getByteArray(0, arraySize);
//            var size = lastValidByte(mem);
//            assertEquals("foo bar message", new String(Arrays.copyOf(mem, size), StandardCharsets.UTF_8));
//
//            instance.call("execute", "".getBytes(StandardCharsets.UTF_8));
//            instance.reset();
//        }
//    }

    @Test
    public void shouldGetMemoryBounds() {
        Manifest manifest = new Manifest(Collections.singletonList(CODE.pathWasmWebAssemblyFunctionSource()), new MemoryOptions(4));

        LibExtism.ExtismValType[] parametersTypes = new LibExtism.ExtismValType[]{LibExtism.ExtismValType.I64};
        LibExtism.ExtismValType[] resultsTypes = new LibExtism.ExtismValType[]{LibExtism.ExtismValType.I64};

        HostFunction[] functions = {
                new HostFunction<>(
                        "hello_world",
                        parametersTypes,
                        resultsTypes,
                        (plugin, params, returns, data) -> {
                            System.out.println("Hello from Java Host Function!");
                        },
                        Optional.empty()
                ).withNamespace("env")
        };

        var instance = new Plugin(manifest, true, functions, new LinearMemory[0]);
        instance.call("execute", "".getBytes(StandardCharsets.UTF_8));
        instance.free();
    }

    @Test
    public void shouldCreateLinearMemory() {
        var manifest = new Manifest(Collections.singletonList(CODE.pathWasmWebAssemblyFunctionSource()), new MemoryOptions(4));
        LibExtism.ExtismValType[] parametersTypes = new LibExtism.ExtismValType[]{LibExtism.ExtismValType.I64};
        LibExtism.ExtismValType[] resultsTypes = new LibExtism.ExtismValType[]{LibExtism.ExtismValType.I64};

        var functions = new HostFunction[]{
                new HostFunction<>(
                        "hello_world",
                        parametersTypes,
                        resultsTypes,
                        (plugin, params, returns, data) -> {
                            System.out.println("Hello from Java Host Function!");
                        },
                        Optional.empty()
                ).withNamespace("env")
        };

        var memory = new LinearMemory("huge-memory", new LinearMemoryOptions(0, Optional.of(2)));

        var instance = new Plugin(manifest, true, functions, new LinearMemory[]{memory});
        instance.call("execute", "".getBytes(StandardCharsets.UTF_8));
        instance.free();
    }

    @Test
    public void shouldPluginWithNewVersionRun() {
        var manifest = new Manifest(Collections.singletonList(CODE.getMajorRelease()), new MemoryOptions(50));

        var instance = new Plugin(manifest, true, null);
        instance.call("execute", "{}".getBytes(StandardCharsets.UTF_8));
        instance.free();
    }

    @Test
    public void shouldOPAWorks() {
        var opa = new OPA(CODE.getOPA());

        var values = opa.initialize();
        var result = opa.evaluate(
                (int)values.toArray()[0],
                (int)values.toArray()[1],
                "{\n" +
                        "    \"request\": {\n" +
                        "        \"headers\": {\n" +
                        "            \"foo\": \"bar\"\n" +
                        "        }\n" +
                        "    }\n" +
                        "}");

        assertEquals("[{\"result\":true}]", result);

        result = opa.evaluate(
                (int)values.toArray()[0],
                (int)values.toArray()[1],
                "{\n" +
                        "    \"request\": {\n" +
                        "        \"headers\": {\n" +
                        "            \"foo\": \"asdas\"\n" +
                        "        }\n" +
                        "    }\n" +
                        "}");

        assertEquals("[{\"result\":false}]", result);
    }

    @Test
    public void getEnvMemorySize() {
         var manifest = new Manifest(Collections.singletonList(CODE.pathWasmWebAssemblyFunctionSource()));

         var message = "foo bar message";
         String namespace = "env";
         String name = "memory";

        var parametersTypes = new LibExtism.ExtismValType[]{LibExtism.ExtismValType.I64};
        var resultsTypes = new LibExtism.ExtismValType[]{LibExtism.ExtismValType.I64};

        var functions = new HostFunction[]{
                new HostFunction<>(
                        "hello_world",
                        parametersTypes,
                        resultsTypes,
                        (plugin, params, returns, data) -> {
                            System.out.println("Hello from Java Host Function!");

                            var memory = plugin.linearMemoryGet(namespace, name);
                            var arraySize = 65356;
                            var mem = memory.getByteArray(0, arraySize);
                            var size = lastValidByte(mem);
                            assertEquals(message, new String(Arrays.copyOf(mem, size), StandardCharsets.UTF_8));
                        },
                        Optional.empty()
                ).withNamespace(namespace)
        };

        var memory = new LinearMemory(name, namespace, new LinearMemoryOptions(1, Optional.empty()));

        try(var instance = new Plugin(manifest, true, functions, new LinearMemory[]{
                memory
        })) {
            instance.writeBytes(message.getBytes(StandardCharsets.UTF_8), message.length(), 0, namespace, name);
            instance.call("execute", "".getBytes(StandardCharsets.UTF_8));

            System.out.println("Linear memory size : " + instance.getLinearMemorySize(namespace, name));
            instance.resetLinearMemory(namespace, name);

            checkLinearMemorySize(instance, namespace, name);
        }
    }

    void checkLinearMemorySize(Plugin instance, String namespace, String name) {
        var linearMemory = instance.getLinearMemory(namespace, name);
        var arraySize = 65356;
        var mem = linearMemory.getByteArray(0, arraySize);
        var size = lastValidByte(mem);
        assertEquals("", new String(Arrays.copyOf(mem, size), StandardCharsets.UTF_8));
    }

    @Test
    public void shouldCorazaWithoutSpecWasmWorks() {
        var allowedPaths = new HashMap<String, String>();
        allowedPaths.put("/tmp", "/tmp");

        var manifest = new Manifest(Collections.singletonList(CODE.getCorazaWithoutProxyWasmPath()),
                new MemoryOptions(5000), null, null, allowedPaths);

        var instance = new Plugin(manifest, true, null);

        JsonObject configuration = new JsonObject();
        String directives = "SecRuleEngine On\n SecRequestBodyAccess On\n SecResponseBodyAccess On\n Include @coraza\n Include @crs-setup\n Include @owasp_crs/*.conf\n SecRule REQUEST_URI \"@streq /admin\" \"id:101,phase:1,t:lowercase,deny,msg:'ADMIN PATH forbidden'\"\n SecRule REQUEST_HEADERS:foo \"@streq bar\" \"id:1001,phase:1,deny,status:403,msg:'Header foo cannot be bar'\"\n SecRule REQUEST_METHOD \"@pm HEAD\" \"id:1002,phase:1,deny,status:403,msg:'HTTP METHOD NOT AUTHORIZED'\"";
//        String directives = "SecRuleEngine On\n SecRule REQUEST_URI \"@streq /admin\" \"id:101,phase:1,t:lowercase,deny,msg:'ADMIN PATH forbidden'\"\n SecRule REQUEST_HEADERS:foo \"@streq bar\" \"id:1001,phase:1,deny,status:403,msg:'Header foo cannot be bar'\"\n SecRule REQUEST_METHOD \"@pm HEAD\" \"id:1002,phase:1,deny,status:403,msg:'HTTP METHOD NOT AUTHORIZED'\"";

        // Add the directives as a property
        configuration.addProperty("directives", directives);
        configuration.addProperty("inspect_bodies", true);

        Gson gson = new Gson();
        instance.initializeCoraza(gson.toJson(configuration));

//        var forbidden = "{ \"request\": { \"url\": \"/foo\", \"headers\": { \"foo\": \"barasdad\"}, \"method\": \"HEAD\" } }";
//        var result = instance.newCorazaTransaction(forbidden);
//        System.out.println(result);
//
//        var accepted = "{ \"request\": { \"url\": \"/foo\", \"headers\": { \"foo\": \"barasdad\"}, \"method\": \"GET\" } }";
//        result = instance.newCorazaTransaction(accepted);
//        System.out.println(result);
//
//        var test = "{\"request\":{\"url\":\"/\",\"method\":\"GET\",\"headers\":{\"Host\":\"coraza-next.oto.tools:9999\",\"Accept\":\"*/*\",\"User-Agent\":\"curl/8.7.1\",\"Remote-Address\":\"127.0.0.1:50651\",\"Timeout-Access\":\"<function1>\",\"Raw-Request-URI\":\"/\",\"Tls-Session-Info\":\"Session(1744190913083|SSL_NULL_WITH_NULL_NULL)\"}}}";
//        result = instance.newCorazaTransaction(test);
//        System.out.println(result);

//        String input = "username=admin&password=%uff41%uff42%uff43";
        String input = "username=admin&password='<script>alert(1)</script>";
//        String input = "{\"ok\":true}";
        byte[] bytes = input.getBytes(StandardCharsets.UTF_8);

        JsonArray byteArrayJson = new JsonArray();
        for (byte b : bytes) {
            byteArrayJson.add((int) b);
        }

        JsonObject headers = new JsonObject();
//        headers.addProperty("Content-Length", ""+bytes.length);
//        headers.addProperty("Content-Type", "application/x-www-form-urlencoded");
        headers.addProperty("Host", "coucou.oto.tools");

        JsonObject request = new JsonObject();
        request.addProperty("url", "/coucou");
        request.addProperty("method", "GET");
        request.add("headers", headers);
//        request.add("body", byteArrayJson);
        request.addProperty("proto", "HTTP/1.1");
        request.addProperty("status", 200);

        JsonObject jsonRequest = new JsonObject();
        jsonRequest.add("request", request);


        JsonObject response = new JsonObject();
        response.addProperty("proto", "HTTP1/1");
        response.addProperty("status", 200);

        JsonObject responseHeaders = new JsonObject();
        response.add("headers", responseHeaders);

        jsonRequest.add("response", response);

        gson = new Gson();
        var result = instance.newCorazaTransaction(gson.toJson(request));
//        var result = instance.processResponseTransaction(gson.toJson(jsonRequest));
        System.out.println(result);

        System.out.println("ERRORS: " + instance.corazaTransactionErrors());
    }

    @Test
    public void benchmarkMemory() {
        var manifest = new Manifest(List.of(CODE.pathTransformerGetToPost()), new MemoryOptions(50));

        var functionName = "execute";
        var input = "{\"snowflake\":\"1925810680351425394\",\"raw_request\":{\"url\":\"http://boris.oto.tools:9999/admin\",\"method\":\"GET\",\"headers\":{\"Host\":\"boris.oto.tools:9999\",\"Accept\":\"*/*\",\"user-agent\":\"oha/1.8.0\",\"Remote-Address\":\"127.0.0.1:52173\",\"Timeout-Access\":\"<function1>\",\"accept-encoding\":\"gzip, compress, deflate, br\",\"Raw-Request-URI\":\"/admin\",\"Tls-Session-Info\":\"Session(1747983975500|SSL_NULL_WITH_NULL_NULL)\"},\"query\":{},\"version\":\"HTTP/1.1\",\"client_cert_chain\":null,\"backend\":null,\"cookies\":[]},\"otoroshi_request\":{\"url\":\"https://request.otoroshi.io/admin\",\"method\":\"GET\",\"headers\":{\"Host\":\"request.otoroshi.io\",\"Accept\":\"*/*\",\"user-agent\":\"oha/1.8.0\",\"accept-encoding\":\"gzip, compress, deflate, br\"},\"query\":{},\"version\":\"HTTP/1.1\",\"client_cert_chain\":null,\"backend\":{\"id\":\"target_1\",\"hostname\":\"request.otoroshi.io\",\"port\":443,\"tls\":true,\"weight\":1,\"backup\":false,\"predicate\":{\"type\":\"AlwaysMatch\"},\"protocol\":\"HTTP/1.1\",\"ip_address\":null,\"tls_config\":{\"certs\":[],\"trusted_certs\":[],\"enabled\":false,\"loose\":false,\"trust_all\":false}},\"cookies\":[]},\"apikey\":null,\"user\":null,\"request\":{\"id\":339,\"method\":\"GET\",\"headers\":{\"Host\":\"boris.oto.tools:9999\",\"Accept\":\"*/*\",\"user-agent\":\"oha/1.8.0\",\"Remote-Address\":\"127.0.0.1:52173\",\"Timeout-Access\":\"<function1>\",\"accept-encoding\":\"gzip, compress, deflate, br\",\"Raw-Request-URI\":\"/admin\",\"Tls-Session-Info\":\"Session(1747983975500|SSL_NULL_WITH_NULL_NULL)\"},\"cookies\":[],\"tls\":false,\"uri\":\"/admin\",\"query\":{},\"path\":\"/admin\",\"version\":\"HTTP/1.1\",\"has_body\":false,\"remote\":\"127.0.0.1\",\"client_cert_chain\":null,\"path_params\":{}},\"config\":{\"source\":{\"kind\":\"wasmo\",\"path\":\"transformer-1.0.0-dev.wasm\",\"opts\":{}},\"memoryPages\":50,\"functionName\":\"execute\",\"config\":{},\"allowedHosts\":[],\"allowedPaths\":{},\"wasi\":true,\"opa\":false,\"httpWasm\":false,\"authorizations\":{\"httpAccess\":false,\"proxyHttpCallTimeout\":5000,\"globalDataStoreAccess\":{\"read\":false,\"write\":false},\"pluginDataStoreAccess\":{\"read\":false,\"write\":false},\"globalMapAccess\":{\"read\":false,\"write\":false},\"pluginMapAccess\":{\"read\":false,\"write\":false},\"proxyStateAccess\":false,\"configurationAccess\":false},\"instances\":1,\"killOptions\":{\"immortal\":false,\"max_calls\":2147483647,\"max_memory_usage\":0,\"max_avg_call_duration\":0,\"max_unused_duration\":300000}},\"global_config\":{\"KubernetesConfig\":{\"endpoint\":\"https://127.0.0.1:57507\",\"token\":\"eyJhbGciOiJSUzI1NiIsImtpZCI6Im9nbVNwQjhEaUFFWnRUTHNxaWNfcHh5dkNiOVh0aGJvQlUxWXZCcjJHamcifQ.eyJpc3MiOiJrdWJlcm5ldGVzL3NlcnZpY2VhY2NvdW50Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9uYW1lc3BhY2UiOiJkZWZhdWx0Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9zZWNyZXQubmFtZSI6Im15LXNlcnZpY2UtYWNjb3VudC10b2tlbiIsImt1YmVybmV0ZXMuaW8vc2VydmljZWFjY291bnQvc2VydmljZS1hY2NvdW50Lm5hbWUiOiJvdG9yb3NoaS1hZG1pbi11c2VyIiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9zZXJ2aWNlLWFjY291bnQudWlkIjoiNTI0OTYxZDMtY2MzYy00NDUwLWE0OWUtM2Y2M2JmNzMyOTEzIiwic3ViIjoic3lzdGVtOnNlcnZpY2VhY2NvdW50OmRlZmF1bHQ6b3Rvcm9zaGktYWRtaW4tdXNlciJ9.x5kyRh8jRWr-N5TKiO7YuDpoqhWQBFxV-ACPtKct2V-ehhfZD9OndvasXPX9sQQoCwD6yfUKSCDMK7I4Ia7kcrr3NhAz59auyfqVTo1iTIgskxjIYfnrfCDvoSJOANZtT5leUB8ajGB8Of1SyRLFTtgrClZ4tVwrb5mhG74hOkRN1yKGwy-M734Oi2PYsA-TNf_mz-8nO3ffL7Qftb71JjcAQEprXWaNSM3RB2LXBrNpkpxY6Gl-Ngz_V1QxsKggT0b8HiJj3JuHsWx4Qg4uT9WpmjGC_SZN0iBwSmu0Ofv-QDVJH9cLvZsgQGYGpDdjitUQJGhhXM0OdE31bQgNPg\",\"trust\":true,\"namespaces\":[\"foo\"],\"labels\":{},\"namespacesLabels\":{},\"ingressClasses\":[\"otoroshi\"],\"defaultGroup\":\"default\",\"ingresses\":true,\"crds\":true,\"crdsOverride\":true,\"coreDnsIntegration\":false,\"coreDnsIntegrationDryRun\":false,\"coreDnsAzure\":false,\"kubeLeader\":false,\"restartDependantDeployments\":true,\"useProxyState\":false,\"watch\":true,\"syncDaikokuApikeysOnly\":false,\"kubeSystemNamespace\":\"kube-system\",\"coreDnsConfigMapName\":\"coredns\",\"coreDnsDeploymentName\":\"coredns\",\"corednsPort\":53,\"otoroshiServiceName\":\"otoroshi-service\",\"otoroshiNamespace\":\"otoroshi\",\"clusterDomain\":\"cluster.local\",\"syncIntervalSeconds\":60,\"coreDnsEnv\":null,\"watchTimeoutSeconds\":60,\"watchGracePeriodSeconds\":5,\"mutatingWebhookName\":\"otoroshi-admission-webhook-injector\",\"validatingWebhookName\":\"otoroshi-admission-webhook-validation\",\"meshDomain\":\"otoroshi.mesh\",\"openshiftDnsOperatorIntegration\":false,\"openshiftDnsOperatorCoreDnsNamespace\":\"otoroshi\",\"openshiftDnsOperatorCoreDnsName\":\"otoroshi-dns\",\"openshiftDnsOperatorCoreDnsPort\":5353,\"kubeDnsOperatorIntegration\":false,\"kubeDnsOperatorCoreDnsNamespace\":\"otoroshi\",\"kubeDnsOperatorCoreDnsName\":\"otoroshi-dns\",\"kubeDnsOperatorCoreDnsPort\":5353,\"connectionTimeout\":5000,\"idleTimeout\":30000,\"callAndStreamTimeout\":30000,\"templates\":{\"service-group\":{},\"service-descriptor\":{},\"apikeys\":{},\"global-config\":{},\"jwt-verifier\":{},\"tcp-service\":{},\"certificate\":{},\"auth-module\":{},\"script\":{},\"data-exporters\":{},\"organizations\":{},\"teams\":{},\"admins\":{},\"webhooks\":{}}},\"NextGenProxyEngine\":{\"enabled\":true,\"debug\":false,\"debug_headers\":false,\"domains\":[\"*\"],\"routing_strategy\":\"tree\"},\"ClientCredentialService\":{\"domain\":\"*\",\"expiration\":3600000,\"defaultKeyPair\":\"otoroshi-jwt-signing\",\"secure\":true},\"ng\":[]},\"attrs\":{\"otoroshi.core.RequestTimestamp\":\"2025-05-23T09:07:00.685+02:00\",\"otoroshi.core.RequestWebsocket\":false,\"otoroshi.next.core.NgMatchedRoute\":{\"route_id\":\"route_bb5159b63-d038-4ac8-b7ce-8579db60bb45\",\"path\":\"\",\"path_params\":{},\"no_more_segments\":false},\"otoroshi.core.ElCtx\":{\"requestId\":\"1925810680351425394\",\"requestSnowflake\":\"1925810680351425394\",\"requestTimestamp\":\"2025-05-23T09:07:00.685+02:00\"},\"otoroshi.core.ForCurrentListenerOnly\":false,\"otoroshi.core.RequestNumber\":339,\"otoroshi.core.RequestCounterOut\":0,\"otoroshi.core.RequestStart\":1747984020685,\"otoroshi.next.core.ContextualPlugins\":{\"disabled_plugins\":[],\"excluded_plugins\":[],\"included_plugins\":[\"cp:otoroshi.next.plugins.OverrideHost\",\"cp:otoroshi.next.plugins.WasmRequestTransformer\"]},\"otoroshi.core.SnowFlake\":\"1925810680351425394\",\"otoroshi.next.core.MatchedRoutes\":[\"route_bb5159b63-d038-4ac8-b7ce-8579db60bb45\"],\"otoroshi.next.core.BodyAlreadyConsumed\":false,\"otoroshi.next.core.Report\":\"2511739cb-8495-425c-889e-65596bd692e0\",\"otoroshi.next.core.Backend\":{\"id\":\"target_1\",\"hostname\":\"request.otoroshi.io\",\"port\":443,\"tls\":true,\"weight\":1,\"backup\":false,\"predicate\":{\"type\":\"AlwaysMatch\"},\"protocol\":\"HTTP/1.1\",\"ip_address\":null,\"tls_config\":{\"certs\":[],\"trusted_certs\":[],\"enabled\":false,\"loose\":false,\"trust_all\":false}},\"otoroshi.core.ResponseEndPromise\":\"Future(<not completed>)\",\"otoroshi.core.CurrentListener\":\"standard-listener\",\"otoroshi.core.Request\":\"GET /admin\",\"otoroshi.core.RequestCounterIn\":0,\"otoroshi.next.core.Route\":{\"_loc\":{\"tenant\":\"default\",\"teams\":[\"default\"]},\"id\":\"route_bb5159b63-d038-4ac8-b7ce-8579db60bb45\",\"name\":\"BORIS\",\"description\":\"A new route\",\"tags\":[],\"metadata\":{\"created_at\":\"2025-05-21T10:31:59.934+02:00\",\"updated_at\":\"2025-05-22T15:55:18.133+02:00\"},\"enabled\":true,\"debug_flow\":false,\"export_reporting\":false,\"capture\":false,\"groups\":[\"default\"],\"bound_listeners\":[],\"frontend\":{\"domains\":[\"boris.oto.tools\"],\"strip_path\":true,\"exact\":false,\"headers\":{},\"cookies\":{},\"query\":{},\"methods\":[]},\"backend\":{\"targets\":[{\"id\":\"target_1\",\"hostname\":\"request.otoroshi.io\",\"port\":443,\"tls\":true,\"weight\":1,\"backup\":false,\"predicate\":{\"type\":\"AlwaysMatch\"},\"protocol\":\"HTTP/1.1\",\"ip_address\":null,\"tls_config\":{\"certs\":[],\"trusted_certs\":[],\"enabled\":false,\"loose\":false,\"trust_all\":false}}],\"root\":\"/\",\"rewrite\":false,\"load_balancing\":{\"type\":\"RoundRobin\"},\"client\":{\"retries\":10,\"max_errors\":20,\"retry_initial_delay\":50,\"backoff_factor\":2,\"call_timeout\":30000,\"call_and_stream_timeout\":120000,\"connection_timeout\":10000,\"idle_timeout\":60000,\"global_timeout\":30000,\"sample_interval\":2000,\"proxy\":{},\"custom_timeouts\":[],\"cache_connection_settings\":{\"enabled\":false,\"queue_size\":2048}},\"health_check\":{\"enabled\":false,\"url\":\"\",\"timeout\":5000,\"healthyStatuses\":[],\"unhealthyStatuses\":[]}},\"backend_ref\":null,\"plugins\":[{\"enabled\":true,\"debug\":false,\"plugin\":\"cp:otoroshi.next.plugins.OverrideHost\",\"include\":[],\"exclude\":[],\"config\":{},\"bound_listeners\":[],\"plugin_index\":{\"transform_request\":0}},{\"enabled\":true,\"debug\":false,\"plugin\":\"cp:otoroshi.next.plugins.WasmRequestTransformer\",\"include\":[],\"exclude\":[],\"config\":{\"source\":{\"kind\":\"wasmo\",\"path\":\"transformer-1.0.0-dev.wasm\",\"opts\":{}},\"memoryPages\":50,\"functionName\":\"execute\",\"config\":{},\"allowedHosts\":[],\"allowedPaths\":{},\"wasi\":true,\"opa\":false,\"httpWasm\":false,\"authorizations\":{\"httpAccess\":false,\"proxyHttpCallTimeout\":5000,\"globalDataStoreAccess\":{\"read\":false,\"write\":false},\"pluginDataStoreAccess\":{\"read\":false,\"write\":false},\"globalMapAccess\":{\"read\":false,\"write\":false},\"pluginMapAccess\":{\"read\":false,\"write\":false},\"proxyStateAccess\":false,\"configurationAccess\":false},\"instances\":1,\"killOptions\":{\"immortal\":false,\"max_calls\":2147483647,\"max_memory_usage\":0,\"max_avg_call_duration\":0,\"max_unused_duration\":300000}},\"bound_listeners\":[],\"plugin_index\":{\"transform_request\":1}}]}},\"route\":{\"_loc\":{\"tenant\":\"default\",\"teams\":[\"default\"]},\"id\":\"route_bb5159b63-d038-4ac8-b7ce-8579db60bb45\",\"name\":\"BORIS\",\"description\":\"A new route\",\"tags\":[],\"metadata\":{\"created_at\":\"2025-05-21T10:31:59.934+02:00\",\"updated_at\":\"2025-05-22T15:55:18.133+02:00\"},\"enabled\":true,\"debug_flow\":false,\"export_reporting\":false,\"capture\":false,\"groups\":[\"default\"],\"bound_listeners\":[],\"frontend\":{\"domains\":[\"boris.oto.tools\"],\"strip_path\":true,\"exact\":false,\"headers\":{},\"cookies\":{},\"query\":{},\"methods\":[]},\"backend\":{\"targets\":[{\"id\":\"target_1\",\"hostname\":\"request.otoroshi.io\",\"port\":443,\"tls\":true,\"weight\":1,\"backup\":false,\"predicate\":{\"type\":\"AlwaysMatch\"},\"protocol\":\"HTTP/1.1\",\"ip_address\":null,\"tls_config\":{\"certs\":[],\"trusted_certs\":[],\"enabled\":false,\"loose\":false,\"trust_all\":false}}],\"root\":\"/\",\"rewrite\":false,\"load_balancing\":{\"type\":\"RoundRobin\"},\"client\":{\"retries\":10,\"max_errors\":20,\"retry_initial_delay\":50,\"backoff_factor\":2,\"call_timeout\":30000,\"call_and_stream_timeout\":120000,\"connection_timeout\":10000,\"idle_timeout\":60000,\"global_timeout\":30000,\"sample_interval\":2000,\"proxy\":{},\"custom_timeouts\":[],\"cache_connection_settings\":{\"enabled\":false,\"queue_size\":2048}},\"health_check\":{\"enabled\":false,\"url\":\"\",\"timeout\":5000,\"healthyStatuses\":[],\"unhealthyStatuses\":[]}},\"backend_ref\":null,\"plugins\":[{\"enabled\":true,\"debug\":false,\"plugin\":\"cp:otoroshi.next.plugins.OverrideHost\",\"include\":[],\"exclude\":[],\"config\":{},\"bound_listeners\":[],\"plugin_index\":{\"transform_request\":0}},{\"enabled\":true,\"debug\":false,\"plugin\":\"cp:otoroshi.next.plugins.WasmRequestTransformer\",\"include\":[],\"exclude\":[],\"config\":{\"source\":{\"kind\":\"wasmo\",\"path\":\"transformer-1.0.0-dev.wasm\",\"opts\":{}},\"memoryPages\":50,\"functionName\":\"execute\",\"config\":{},\"allowedHosts\":[],\"allowedPaths\":{},\"wasi\":true,\"opa\":false,\"httpWasm\":false,\"authorizations\":{\"httpAccess\":false,\"proxyHttpCallTimeout\":5000,\"globalDataStoreAccess\":{\"read\":false,\"write\":false},\"pluginDataStoreAccess\":{\"read\":false,\"write\":false},\"globalMapAccess\":{\"read\":false,\"write\":false},\"pluginMapAccess\":{\"read\":false,\"write\":false},\"proxyStateAccess\":false,\"configurationAccess\":false},\"instances\":1,\"killOptions\":{\"immortal\":false,\"max_calls\":2147483647,\"max_memory_usage\":0,\"max_avg_call_duration\":0,\"max_unused_duration\":300000}},\"bound_listeners\":[],\"plugin_index\":{\"transform_request\":1}}]},\"request_body_bytes\":null,\"method\":\"POST\",\"headers\":{\"Host\":\"request.otoroshi.io\",\"Accept\":\"*/*\",\"user-agent\":\"oha/1.8.0\",\"accept-encoding\":\"gzip, compress, deflate, br\",\"OTOROSHI_WASM_PLUGIN_ID\":\"OTOROSHI_WASM_REQUEST_TRANSFORMER\",\"Content-Type\":\"application/json\"},\"body_json\":{\"foo\":\"bar\"}}";

        LibExtism.ExtismValType[] parametersTypes = new LibExtism.ExtismValType[]{
                LibExtism.ExtismValType.I32,
                LibExtism.ExtismValType.I32,
                LibExtism.ExtismValType.I64};

        ExtismFunction<HostUserData> helloWorldFunction = (plugin, params, returns, data) -> {
            System.out.println("Hello from Java Host Function!");
        };

        var proxy_log = new HostFunction<>(
                "proxy_log",
                parametersTypes,
                new LibExtism.ExtismValType[]{LibExtism.ExtismValType.I64},
                (plugin, params, returns, data) -> System.out.println("proxy_log"),
                Optional.empty()
        );

        var proxy_log_event = new HostFunction<>(
                "proxy_log_event",
                new LibExtism.ExtismValType[]{
                        LibExtism.ExtismValType.I64,
                        LibExtism.ExtismValType.I64},
                new LibExtism.ExtismValType[]{LibExtism.ExtismValType.I64},
                (plugin, params, returns, data) -> System.out.println("proxy_log_0event"),
                Optional.empty()
        );

        var proxy_get_attrs = new HostFunction<>(
                "proxy_get_attrs",
                new LibExtism.ExtismValType[]{LibExtism.ExtismValType.I64},
                new LibExtism.ExtismValType[]{LibExtism.ExtismValType.I64},
                (plugin, params, returns, data) -> System.out.println("proxy_get_attrs"),
                Optional.empty()
        );

        var proxy_get_attr = new HostFunction<>(
                "proxy_get_attr",
                new LibExtism.ExtismValType[]{LibExtism.ExtismValType.I64, LibExtism.ExtismValType.I64},
                new LibExtism.ExtismValType[]{LibExtism.ExtismValType.I64},
                (plugin, params, returns, data) -> System.out.println("proxy_get_attr"),
                Optional.empty()
        );

        var proxy_set_attr = new HostFunction<>(
                "proxy_set_attr",
                new LibExtism.ExtismValType[]{LibExtism.ExtismValType.I64, LibExtism.ExtismValType.I64},
                new LibExtism.ExtismValType[]{LibExtism.ExtismValType.I64},
                (plugin, params, returns, data) -> System.out.println("proxy_set_attr"),
                Optional.empty()
        );

        var proxy_del_attr = new HostFunction<>(
                "proxy_del_attr",
                new LibExtism.ExtismValType[]{LibExtism.ExtismValType.I64, LibExtism.ExtismValType.I64},
                new LibExtism.ExtismValType[]{LibExtism.ExtismValType.I64},
                (plugin, params, returns, data) -> System.out.println("proxy_del_attr"),
                Optional.empty()
        );

        var proxy_clear_attrs = new HostFunction<>(
                "proxy_clear_attrs",
                new LibExtism.ExtismValType[]{LibExtism.ExtismValType.I64},
                new LibExtism.ExtismValType[]{LibExtism.ExtismValType.I64},
                (plugin, params, returns, data) -> System.out.println("proxy_clear_attrs"),
                Optional.empty()
        );

        var functions = new HostFunction[]{proxy_log,
                proxy_log_event,
                proxy_get_attrs,
                proxy_get_attr,
                proxy_set_attr,
                proxy_del_attr,
                proxy_clear_attrs};

//        final Pointer[] snapshot = {null};
        var instance = new Plugin(manifest, true, functions);
        for (int i = 0; i < 5000; i++) {
            long start = System.nanoTime();
            var result = instance.call(functionName, input);
            instance.reset();

            long end = System.nanoTime();
            long elapsedNanos = end - start;
            double elapsedMillis = elapsedNanos / 1_000_000.0;

            if(elapsedMillis > 2)
                System.out.println(i + " Elapsed time: " + elapsedMillis + " ms");

            String method = JsonParser.parseString(result).getAsJsonObject().get("method").getAsString();

            if (!method.equals("POST")) {
                System.out.println(i);
                System.out.println("CA PLANTE");
            }
        }

        instance.free();
    }
}