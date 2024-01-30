package org.extism.sdk.coraza.proxywasm;

import org.extism.sdk.HostUserData;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class VMData extends HostUserData {

    public String configuration;
    public Map<String, byte[]> properties = new HashMap<>();

    private int tickPeriod = -1;

    public VMData(Map<String, byte[]> headers) {
        this.initializeProperties(headers);
    }

    public void initializeProperties(Map<String, byte[]> headers) {
        properties.put("plugin_root_id", new String("proxy-wasm").getBytes(StandardCharsets.UTF_8));
        properties.put("source.address", new String("127.0.0.1").getBytes(StandardCharsets.UTF_8));
        properties.put("source.port", String.valueOf(8080).getBytes(StandardCharsets.UTF_8));
        properties.put("destination.address", new String("127.0.0.1").getBytes(StandardCharsets.UTF_8));
        properties.put("destination.port",  String.valueOf(12345).getBytes(StandardCharsets.UTF_8));

        for (Map.Entry<String, byte[]> entry : headers.entrySet()) {
            properties.put("request." + entry.getKey(), entry.getValue());
            properties.put(":" + entry.getKey(), entry.getValue());
        }
    }

    public void setTickPeriod(int tickPeriod) {
        this.tickPeriod = tickPeriod;
    }
}
