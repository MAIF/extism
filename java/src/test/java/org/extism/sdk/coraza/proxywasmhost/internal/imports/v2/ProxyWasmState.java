package org.extism.sdk.coraza.proxywasmhost.internal.imports.v2;

import com.sun.jna.Pointer;
import org.extism.sdk.ExtismCurrentPlugin;
import org.extism.sdk.coraza.proxywasm.Either;
import org.extism.sdk.coraza.proxywasm.VMData;
import org.extism.sdk.coraza.proxywasmhost.common.IoBuffer;
import org.extism.sdk.coraza.proxywasmhost.v2.Types;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static org.extism.sdk.coraza.proxywasmhost.v2.Types.Result.*;
import static org.extism.sdk.coraza.proxywasmhost.v2.Types.Status.StatusOK;
import static org.extism.sdk.coraza.proxywasmhost.v2.Utils.DEBUG;
import static org.extism.sdk.coraza.proxywasmhost.v2.Utils.traceVmHost;

public class ProxyWasmState implements Api {

    // u32 is the fixed size of a uint32 in little-endian encoding.
    public final int u32Len = 4;

    public int contextId;
    public int rootContextId;

    public ProxyWasmState(int rootContextId, int contextId) {
        this.rootContextId = rootContextId;
        this.contextId = contextId;
    }

    @Override
    public Types.Result ProxyLog(ExtismCurrentPlugin plugin, VMData vmData, int logLevel, int messageData, int messageSize) {
        traceVmHost("proxy_log");

        Either<Types.Error, Map.Entry<Pointer, byte[]>> memory = GetMemory(plugin, vmData, messageData, messageSize);

        if (memory.isRight()) {
            System.out.println(new String(memory.getRight().getValue(), StandardCharsets.UTF_8));
            return ResultOk;
        } else {
            return Types.Error.toResult(memory.getLeft());
        }
    }

    @Override
    public Types.Result ProxyResumeStream(ExtismCurrentPlugin plugin, Types.StreamType streamType) {
        traceVmHost("proxy_resume_stream");
        return null;
    }

    @Override
    public Types.Result ProxyCloseStream(ExtismCurrentPlugin plugin, Types.StreamType streamType) {
        return null;
    }

    @Override
    public Types.Result ProxySendHttpResponse(ExtismCurrentPlugin plugin, int responseCode, int responseCodeDetailsData, int responseCodeDetailsSize, int responseBodyData, int responseBodySize, int additionalHeadersMapData, int additionalHeadersSize, int grpcStatus) {
        return null;
    }

    @Override
    public Types.Result ProxyResumeHttpStream(ExtismCurrentPlugin plugin, Types.StreamType streamType) {
        return null;
    }

    @Override
    public Types.Result ProxyCloseHttpStream(ExtismCurrentPlugin plugin, Types.StreamType streamType) {
        return null;
    }

    @Override
    public IoBuffer GetBuffer(ExtismCurrentPlugin plugin, VMData data, Types.BufferType bufferType) {

        switch (bufferType) {
            case BufferTypeHttpRequestBody:
                return GetHttpRequestBody(plugin, data);
            case BufferTypeHttpResponseBody:
                return GetHttpResponseBody(plugin, data);
            case BufferTypeDownstreamData:
                return GetDownStreamData(plugin, data);
            case BufferTypeUpstreamData:
                return GetUpstreamData(plugin, data);
//            case BufferTypeHttpCalloutResponseBody:
//                return GetHttpCalloutResponseBody(plugin, data);
            case BufferTypePluginConfiguration:
                return GetPluginConfig(plugin, data);
            case BufferTypeVmConfiguration:
                return GetVmConfig(plugin, data);
            case BufferTypeHttpCallResponseBody:
                return GetHttpCalloutResponseBody(plugin, data);
            default:
                return GetCustomBuffer(bufferType);
        }
    }

    @Override
    public Types.Result ProxyGetBuffer(ExtismCurrentPlugin plugin, VMData vmData, int bufType, int offset, int maxSize,
                                       int returnBufferData, int returnBufferSize) {
        traceVmHost("proxy_get_buffer");

        Either<Types.Error, Pointer> mem = GetMemory(plugin, vmData);

        if (mem.isLeft()) {
            return ResultBadArgument;
        } else {
            Pointer memory = mem.getRight();

            if (bufType > Types.BufferType.values().length) {
                return ResultBadArgument;
            }

            IoBuffer bufferTypePluginConfiguration = GetBuffer(
                    plugin,
                    vmData,
                    Types.BufferType.values()[bufType]);

            if (offset > offset + maxSize) {
                return ResultBadArgument;
            }

            if (offset + maxSize > bufferTypePluginConfiguration.length()) {
                maxSize = bufferTypePluginConfiguration.length() - offset;
            }

            bufferTypePluginConfiguration.drain(offset, offset + maxSize);

            System.out.println(String.format("%s, %d,%d, %d, %d,",  Types.BufferType.values()[bufType], offset, maxSize, returnBufferData, returnBufferSize));
            return copyIntoInstance(plugin, memory, bufferTypePluginConfiguration, returnBufferData, returnBufferSize);

        }
    }

    @Override
    public Types.Result ProxySetBuffer(ExtismCurrentPlugin plugin, VMData vmData, int bufferType, int offset, int size, int bufferData, int bufferSize) {
        traceVmHost("proxy_set_buffer");
        IoBuffer buf = GetBuffer(plugin, vmData, Types.BufferType.values()[bufferType]);
        if (buf == null) {
            return ResultBadArgument;
        }

        Pointer memory = plugin.customMemoryGet();

        byte[] content = new byte[bufferSize];
        memory.read(bufferData, content, 0, bufferSize);

        if (offset == 0) {
            if (size == 0 || size >= buf.length()) {
                buf.drain(buf.length(), -1);
                try {
                    buf.write(content);
                } catch (IOException e) {
                    return ResultInvalidMemoryAccess;
                }
            } else {
                return ResultBadArgument;
            }
        } else if(offset >= buf.length()) {
            try {
                buf.write(content);
            } catch (IOException e) {
                return ResultInvalidMemoryAccess;
            }
        } else {
            return ResultBadArgument;
        }

        return ResultOk;
    }

    @Override
    public Map<String, byte[]> GetMap(ExtismCurrentPlugin plugin, VMData vmData, Types.MapType mapType) {
        System.out.println("CALL MAP: " + mapType);
        switch (mapType) {
            case MapTypeHttpRequestHeaders:
                return GetHttpRequestHeader(plugin, vmData);
            case MapTypeHttpRequestTrailers:
                return GetHttpRequestTrailer(plugin, vmData);
            case MapTypeHttpRequestMetadata:
                return GetHttpRequestMetadata(plugin, vmData);
            case MapTypeHttpResponseHeaders:
                return GetHttpResponseHeader(plugin, vmData);
            case MapTypeHttpResponseTrailers:
                return GetHttpResponseTrailer(plugin, vmData);
            case MapTypeHttpResponseMetadata:
                return GetHttpResponseMetadata(plugin, vmData);
            case MapTypeHttpCallResponseHeaders:
                return GetHttpCallResponseHeaders(plugin, vmData);
            case MapTypeHttpCallResponseTrailers:
                return GetHttpCallResponseTrailer(plugin, vmData);
            case MapTypeHttpCallResponseMetadata:
                return GetHttpCallResponseMetadata(plugin, vmData);
            default:
                return GetCustomMap(plugin, vmData, mapType);
        }
    }

    @Override
    public void copyMapIntoInstance(Map<String, String> m, ExtismCurrentPlugin plugin, int returnMapData, int returnMapSize) {

    }

    @Override
    public int ProxyGetHeaderMapPairs(ExtismCurrentPlugin plugin, VMData vmData, int mapType, int returnDataPtr, int returnDataSize) {
        traceVmHost("proxy_get_map");
        Map<String, byte[]> header = GetMap(plugin, vmData, Types.MapType.values()[mapType]);

        if (header == null) {
            return ResultNotFound.ordinal();
        }

        Map<String, byte[]> cloneMap = new HashMap<>();

        int totalBytesLen = u32Len;

        for (Map.Entry<String, byte[]> entry : header.entrySet()) {
            String key = entry.getKey();
            byte[] value = entry.getValue();

            cloneMap.put(key, value);
            totalBytesLen += u32Len + u32Len;       // keyLen + valueLen
            totalBytesLen += key.length() + 1 + value.length + 1; // key + \0 + value + \0
        }

        // TODO - try to call proxy_on_memory_allocate
        int addr = plugin.customMemoryAlloc(totalBytesLen);

        // TODO - manage error
//        if err != nil {
//            return int32(v2.ResultInvalidMemoryAccess)
//        }

        Pointer memory = plugin.customMemoryGet();

        memory.setInt(addr, cloneMap.size());
//        if err != nil {
//            return int32(v2.ResultInvalidMemoryAccess)
//        }

        int lenPtr = addr + u32Len;
        int dataPtr = lenPtr + (u32Len+u32Len) * cloneMap.size();

        for (Map.Entry<String, byte[]> entry : cloneMap.entrySet()) {
            String k = entry.getKey();
            byte[] v = entry.getValue();

            memory.setInt(lenPtr, k.length());
            lenPtr += u32Len;
            memory.setInt(lenPtr, v.length);
            lenPtr += u32Len;

            memory.write(dataPtr, k.getBytes(StandardCharsets.UTF_8), 0, k.length());
            dataPtr += k.length();
            memory.setByte(dataPtr, (byte)0);
            dataPtr++;

            memory.write(dataPtr, v, 0, v.length);
            dataPtr += v.length;
            memory.setByte(dataPtr, (byte)0);
            dataPtr++;
        }

        memory.setInt(returnDataPtr, addr);
//        if err != nil {
//            return int32(v2.ResultInvalidMemoryAccess)
//        }

        memory.setInt(returnDataSize, totalBytesLen);
//        if err != nil {
//            return int32(v2.ResultInvalidMemoryAccess)
//        }

        return ResultOk.ordinal();
    }

    @Override
    public Types.Result ProxyGetHeaderMapValue(ExtismCurrentPlugin plugin, VMData vmData, int mapType, int keyData, int keySize, int valueData, int valueSize) {
        traceVmHost("proxy_get_header_map_value");
        Map<String, byte[]> m = GetMap(plugin, vmData, Types.MapType.values()[mapType]);

        if (m == null || keySize == 0) {
            return ResultNotFound;
        }

        Either<Types.Error, Map.Entry<Pointer, byte[]>> mem = GetMemory(plugin, vmData, keyData, keySize);

        if (mem.isLeft()) {
            return Types.Error.toResult(mem.getLeft());
        } else {
            byte[] key = mem.getRight().getValue();

            if (key.length == 0) {
                return ResultBadArgument;
            }

            byte[] value = m.get(new String(key, StandardCharsets.UTF_8));
            if (value != null) {
                return copyIntoInstance(
                     plugin,
                     mem.getRight().getKey(),
                     new IoBuffer(value),
                     valueData,
                     valueSize);
            } else {
                return ResultNotFound;
            }
        }
    }

    @Override
    public Types.Result ProxyReplaceHeaderMapValue(ExtismCurrentPlugin plugin, VMData vmData, int mapType, int keyData, int keySize, int valueData, int valueSize) {
        traceVmHost("proxy_set_map_value");
        Map<String, byte[]> m = GetMap(plugin, vmData, Types.MapType.values()[mapType]);

        if (m == null || keySize == 0) {
            return ResultNotFound;
        }

        Either<Types.Error, Map.Entry<Pointer, byte[]>> memKey = GetMemory(plugin, vmData, keyData, keySize);
        Either<Types.Error, Map.Entry<Pointer, byte[]>> memValue = GetMemory(plugin, vmData, valueData, valueSize);

        if (memKey.isLeft()) {
            return Types.Error.toResult(memKey.getLeft());
        } else if (memValue.isLeft()) {
            return Types.Error.toResult(memValue.getLeft());
        } else {
            byte[] key = memKey.getRight().getValue();
            byte[] value = memValue.getRight().getValue();

            if (key.length == 0) {
                return ResultBadArgument;
            }

            m.put(new String(key, StandardCharsets.UTF_8), value);

            return ResultOk;
        }
    }

    @Override
    public Types.Result ProxyOpenSharedKvstore(ExtismCurrentPlugin plugin, int kvstoreNameData, int kvstoreNameSiz, int createIfNotExist, int kvstoreID) {
        return null;
    }

    @Override
    public Types.Result ProxyGetSharedKvstoreKeyValues(ExtismCurrentPlugin plugin, int kvstoreID, int keyData, int keySize, int returnValuesData, int returnValuesSize, int returnCas) {
        return null;
    }

    @Override
    public Types.Result ProxySetSharedKvstoreKeyValues(ExtismCurrentPlugin plugin, int kvstoreID, int keyData, int keySize, int valuesData, int valuesSize, int cas) {
        return null;
    }

    @Override
    public Types.Result ProxyAddSharedKvstoreKeyValues(ExtismCurrentPlugin plugin, int kvstoreID, int keyData, int keySize, int valuesData, int valuesSize, int cas) {
        return null;
    }

    @Override
    public Types.Result ProxyRemoveSharedKvstoreKey(ExtismCurrentPlugin plugin, int kvstoreID, int keyData, int keySize, int cas) {
        return null;
    }

    @Override
    public Types.Result ProxyDeleteSharedKvstore(ExtismCurrentPlugin plugin, int kvstoreID) {
        return null;
    }

    @Override
    public Types.Result ProxyOpenSharedQueue(ExtismCurrentPlugin plugin, int queueNameData, int queueNameSize, int createIfNotExist, int returnQueueID) {
        return null;
    }

    @Override
    public Types.Result ProxyDequeueSharedQueueItem(ExtismCurrentPlugin plugin, int queueID, int returnPayloadData, int returnPayloadSize) {
        return null;
    }

    @Override
    public Types.Result ProxyEnqueueSharedQueueItem(ExtismCurrentPlugin plugin, int queueID, int payloadData, int payloadSize) {
        return null;
    }

    @Override
    public Types.Result ProxyDeleteSharedQueue(ExtismCurrentPlugin plugin, int queueID) {
        return null;
    }

    @Override
    public Types.Result ProxyCreateTimer(ExtismCurrentPlugin plugin, int period, int oneTime, int returnTimerID) {
        return null;
    }

    @Override
    public Types.Result ProxyDeleteTimer(ExtismCurrentPlugin plugin, int timerID) {
        return null;
    }

    @Override
    public Types.MetricType ProxyCreateMetric(ExtismCurrentPlugin plugin, Types.MetricType metricType, int metricNameData, int metricNameSize, int returnMetricID) {
        return null;
    }

    @Override
    public Types.Result ProxyDefineMetric(ExtismCurrentPlugin plugin, int metricType, int namePtr, int nameSize, int returnMetricId) {
        return null;
    }

    @Override
    public Types.Result ProxyGetMetricValue(ExtismCurrentPlugin plugin, int metricID, int returnValue) {
        return null;
    }

    @Override
    public Types.Result ProxySetMetricValue(ExtismCurrentPlugin plugin, int metricID, int value) {
        return null;
    }

    @Override
    public Types.Result ProxyIncrementMetricValue(ExtismCurrentPlugin plugin, VMData data, int metricID, long offset) {
        return null;
    }

    @Override
    public Types.Result ProxyDeleteMetric(ExtismCurrentPlugin plugin, int metricID) {
        return null;
    }

    @Override
    public Types.Result ProxyDispatchHttpCall(ExtismCurrentPlugin plugin, int upstreamNameData, int upstreamNameSize, int headersMapData, int headersMapSize, int bodyData, int bodySize, int trailersMapData, int trailersMapSize, int timeoutMilliseconds, int returnCalloutID) {
        return null;
    }

    @Override
    public Types.Result ProxyDispatchGrpcCall(ExtismCurrentPlugin plugin, int upstreamNameData, int upstreamNameSize, int serviceNameData, int serviceNameSize, int serviceMethodData, int serviceMethodSize, int initialMetadataMapData, int initialMetadataMapSize, int grpcMessageData, int grpcMessageSize, int timeoutMilliseconds, int returnCalloutID) {
        return null;
    }

    @Override
    public Types.Result ProxyOpenGrpcStream(ExtismCurrentPlugin plugin, int upstreamNameData, int upstreamNameSize, int serviceNameData, int serviceNameSize, int serviceMethodData, int serviceMethodSize, int initialMetadataMapData, int initialMetadataMapSize, int returnCalloutID) {
        return null;
    }

    @Override
    public Types.Result ProxySendGrpcStreamMessage(ExtismCurrentPlugin plugin, int calloutID, int grpcMessageData, int grpcMessageSize) {
        return null;
    }

    @Override
    public Types.Result ProxyCancelGrpcCall(ExtismCurrentPlugin plugin, int calloutID) {
        return null;
    }

    @Override
    public Types.Result ProxyCloseGrpcCall(ExtismCurrentPlugin plugin, int calloutID) {
        return null;
    }

    @Override
    public Types.Result ProxyCallCustomFunction(ExtismCurrentPlugin plugin, int customFunctionID, int parametersData, int parametersSize, int returnResultsData, int returnResultsSize) {
        return null;
    }

    @Override
    public Types.Result copyIntoInstance(ExtismCurrentPlugin plugin, Pointer memory, IoBuffer value, int retPtr, int retSize) {
        int addr = plugin.customMemoryAlloc(value.length());

        memory.write(addr, value.bytes(), 0, value.length());

        memory.setInt(retPtr, addr);
        memory.setInt(retSize, value.length());

        return ResultOk;
    }

    @Override
    public Types.Status ProxyRegisterSharedQueue(byte[] nameData, int nameSize, Integer returnID) {
//        String name = rawBytePtrToString(nameData, nameSize);
//        if id, ok = queueNameID[name]; ok {
//		*returnID = id
//            return internal.StatusOK
//        }
//
//        id = uint32(len(r.queues))
//        r.queues[id] = [][]byte{}
//        r.queueNameID[name] = id
//                *returnID = id
//
        return StatusOK;
    }

    @Override
    public Types.Status ProxySetTickPeriodMilliseconds(VMData data, int period) {
        data.setTickPeriod(period);
        return StatusOK;
    }

    @Override
    public Types.Status ProxyResolveSharedQueue(byte[] vmData, int vmIDSize, byte[] nameData, int nameSize, Integer returnID) {
        System.out.println("ProxyResolveSharedQueue not implemented in the host emulator yet");
        return StatusOK;
    }

    @Override
    public Types.Status ProxyEnqueueSharedQueue(int queueID, Byte[] valueData, int valueSize) {
//        queue, ok = r.queues[queueID]
//        if !ok {
//            log.Printf("queue %d is not found", queueID)
//            return internal.StatusNotFound
//        }
//
//        r.queues[queueID] = append(queue, internal.RawBytePtrToByteSlice(valueData, valueSize))
//        internal.ProxyOnQueueReady(PluginContextID, queueID)
        return StatusOK;
    }

    @Override
    public Types.Status ProxyDequeueSharedQueue(int queueID, Byte[] returnValueData /***byte returnValueData*/, Integer returnValueSize) {
//        queue, ok = r.queues[queueID]
//        if !ok {
//            log.Printf("queue %d is not found", queueID)
//            return internal.StatusNotFound
//        } else if len(queue) == 0 {
//            log.Printf("queue %d is empty", queueID)
//            return internal.StatusEmpty
//        }
//
//        data = queue[0]
//                *returnValueData = &data[0]
//                *returnValueSize = len(data)
//        r.queues[queueID] = queue[1:]
        return StatusOK;
    }

    @Override
    public Types.Status ProxyDone() {
        return StatusOK;
    }

    @Override
    public Types.Status ProxySetEffectiveContext(ExtismCurrentPlugin plugin, int contextID) {
        this.contextId = contextID;
        return StatusOK;
    }

    @Override
    public Types.Result ProxyGetProperty(ExtismCurrentPlugin plugin, VMData data, int pathPtr, int pathSize, int returnValueData, int returnValueSize) {
        traceVmHost("proxy_get_property");
        Either<Types.Error, Map.Entry<Pointer, byte[]>> mem = GetMemory(plugin, data, pathPtr, pathSize);

        if (mem.isLeft() || mem.getRight().getValue().length == 0) {
            return ResultBadArgument;
        } else {
            String path = new String(mem.getRight().getValue(), StandardCharsets.UTF_8)
                .replace(Character.toString((char)0), ".");

            byte[] value = data.properties.get(path);

            try {
                DEBUG("proxy_get_property", path + " : " + new String(value, StandardCharsets.UTF_8));
            } catch (Exception e) {
                DEBUG("proxy_get_property", path + " : " + value);
            }

            if (value == null) {
                return ResultNotFound;
            }

            return copyIntoInstance(plugin, mem.getRight().getKey(), new IoBuffer(value), returnValueData, returnValueSize);
        }
    }

    @Override
    public IoBuffer GetPluginConfig(ExtismCurrentPlugin plugin, VMData data) {
        return new IoBuffer(data.configuration.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public IoBuffer GetHttpRequestBody(ExtismCurrentPlugin plugin, VMData data) {
        // TODO
        return new IoBuffer(new byte[0]);
    }

    @Override
    public IoBuffer GetHttpResponseBody(ExtismCurrentPlugin plugin, VMData data) {
        // TODO
        return new IoBuffer(new byte[0]);
    }

    @Override
    public IoBuffer GetDownStreamData(ExtismCurrentPlugin plugin, VMData data) {
        // TODO
        return new IoBuffer(new byte[0]);
    }

    @Override
    public IoBuffer GetUpstreamData(ExtismCurrentPlugin plugin, VMData data) {
        // TODO
        return new IoBuffer(new byte[0]);
    }

    @Override
    public IoBuffer GetHttpCalloutResponseBody(ExtismCurrentPlugin plugin, VMData data) {
        // TODO
        return new IoBuffer(new byte[0]);
    }

    @Override
    public IoBuffer GetVmConfig(ExtismCurrentPlugin plugin, VMData data) {
        // TODO
        return new IoBuffer(new byte[0]);
    }

    @Override
    public IoBuffer GetCustomBuffer(Types.BufferType bufferType) {
        return null;
    }

    @Override
    public Map<String, byte[]> GetHttpRequestHeader(ExtismCurrentPlugin plugin, VMData data) {
        System.out.println("CALL GetHttpRequestHeader");

        return data.properties.entrySet()
                .stream()
                .filter(entry -> entry.getKey().startsWith("request.") || entry.getKey().startsWith(":"))
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
    }

    @Override
    public Map<String, byte[]> GetHttpRequestTrailer(ExtismCurrentPlugin plugin, VMData data) {
        return new HashMap<>();
    }

    @Override
    public Map<String, byte[]> GetHttpRequestMetadata(ExtismCurrentPlugin plugin, VMData data) {
        return new HashMap<>();
    }

    @Override
    public Map<String, byte[]> GetHttpResponseHeader(ExtismCurrentPlugin plugin, VMData data) {
        return new HashMap<>();
    }

    @Override
    public Map<String, byte[]> GetHttpResponseTrailer(ExtismCurrentPlugin plugin, VMData data) {
        return new HashMap<>();
    }

    @Override
    public Map<String, byte[]> GetHttpResponseMetadata(ExtismCurrentPlugin plugin, VMData data) {
        return new HashMap<>();
    }

    @Override
    public Map<String, byte[]> GetHttpCallResponseHeaders(ExtismCurrentPlugin plugin, VMData data) {
        return new HashMap<>();
    }

    @Override
    public Map<String, byte[]> GetHttpCallResponseTrailer(ExtismCurrentPlugin plugin, VMData data) {
        return new HashMap<>();
    }

    @Override
    public Map<String, byte[]> GetHttpCallResponseMetadata(ExtismCurrentPlugin plugin, VMData data) {
        return new HashMap<>();
    }

    @Override
    public Map<String, byte[]> GetCustomMap(ExtismCurrentPlugin plugin, VMData data, Types.MapType mapType) {
        return new HashMap<>();
    }

    @Override
    public Either<Types.Error, Map.Entry<Pointer, byte[]>> GetMemory(ExtismCurrentPlugin plugin, VMData vmData, int addr, int size) {
        Pointer memory = plugin.customMemoryGet();
        if (memory == null) {
            return Either.left(Types.Error.ErrorExportsNotFound);
        }

        // TODO - get memory size from RUST
//        long memoryLength = 1024 * 64 * 50;// plugin.memoryLength(0);
//        if (addr > memoryLength || (addr+size) > memoryLength) {
//            return Either.left(Types.Error.ErrAddrOverflow);
//        }

        return Either.right(new AbstractMap.SimpleImmutableEntry<>(memory, memory.share(addr).getByteArray(0, size)));
    }

    @Override
    public Either<Types.Error, Pointer> GetMemory(ExtismCurrentPlugin plugin, VMData vmData) {
        Pointer memory = plugin.customMemoryGet();
        if (memory == null) {
            return Either.left(Types.Error.ErrorExportsNotFound);
        }

        return Either.right(memory);
    }
}
