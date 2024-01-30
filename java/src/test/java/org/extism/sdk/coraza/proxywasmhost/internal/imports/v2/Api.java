package org.extism.sdk.coraza.proxywasmhost.internal.imports.v2;

import com.sun.jna.Pointer;
import org.extism.sdk.ExtismCurrentPlugin;

import org.extism.sdk.coraza.proxywasm.Either;
import org.extism.sdk.coraza.proxywasm.VMData;
import org.extism.sdk.coraza.proxywasmhost.common.IoBuffer;
import org.extism.sdk.coraza.proxywasmhost.v2.Types;
import org.extism.sdk.coraza.proxywasmhost.v2.Types.BufferType;
import org.extism.sdk.coraza.proxywasmhost.v2.Types.Result;
import org.extism.sdk.coraza.proxywasmhost.v2.Types.StreamType;
import org.extism.sdk.coraza.proxywasmhost.v2.Types.MapType;
import org.extism.sdk.coraza.proxywasmhost.v2.Types.MetricType;

import java.util.Map;

public interface Api {

    Result ProxyLog(ExtismCurrentPlugin plugin, VMData vmData, int logLevel, int messageData, int messageSize);

    Result ProxyResumeStream(ExtismCurrentPlugin plugin, StreamType streamType);

    Result ProxyCloseStream(ExtismCurrentPlugin plugin, StreamType streamType);

    Result ProxySendHttpResponse(ExtismCurrentPlugin plugin, int responseCode, int responseCodeDetailsData, int responseCodeDetailsSize,
                                 int responseBodyData, int responseBodySize, int additionalHeadersMapData, int additionalHeadersSize,
                                 int grpcStatus);

    Result ProxyResumeHttpStream(ExtismCurrentPlugin plugin, StreamType streamType);

    Result ProxyCloseHttpStream(ExtismCurrentPlugin plugin, StreamType streamType);

    IoBuffer GetBuffer(ExtismCurrentPlugin plugin, VMData data, BufferType bufferType);

    Result ProxyGetBuffer(ExtismCurrentPlugin plugin, VMData data, int bufferType, int offset, int maxSize,
                          int returnBufferData, int returnBufferSize);

    Result ProxySetBuffer(ExtismCurrentPlugin plugin, VMData vmData, int bufferType, int offset, int size,
                          int bufferData, int bufferSize) ;

    Map<String, byte[]> GetMap(ExtismCurrentPlugin plugin, VMData vmData, MapType mapType);

    void copyMapIntoInstance(Map<String, String> m, ExtismCurrentPlugin plugin, int returnMapData, int returnMapSize);

    int ProxyGetHeaderMapPairs(ExtismCurrentPlugin plugin, VMData vmData, int mapType, int returnDataPtr, int returnDataSize);

    Result ProxyGetHeaderMapValue(ExtismCurrentPlugin plugin, VMData vmData, int mapType, int keyData, int keySize, int valueData, int valueSize);

    Result ProxyReplaceHeaderMapValue(ExtismCurrentPlugin plugin, VMData vmData, int mapType, int  keyData, int keySize, int valueData, int valueSize);

    Result ProxyOpenSharedKvstore(ExtismCurrentPlugin plugin, int kvstoreNameData, int kvstoreNameSiz,  int createIfNotExist,
                                  int kvstoreID);

    Result ProxyGetSharedKvstoreKeyValues(ExtismCurrentPlugin plugin, int kvstoreID, int keyData, int keySize,
                                          int returnValuesData, int returnValuesSize, int returnCas);

    Result ProxySetSharedKvstoreKeyValues(ExtismCurrentPlugin plugin, int kvstoreID, int keyData, int keySize,
                                          int valuesData, int valuesSize, int cas);

    Result ProxyAddSharedKvstoreKeyValues(ExtismCurrentPlugin plugin, int kvstoreID, int keyData, int keySize,
                                          int valuesData, int valuesSize, int cas);

    Result ProxyRemoveSharedKvstoreKey(ExtismCurrentPlugin plugin, int kvstoreID, int keyData, int keySize, int cas) ;

    Result ProxyDeleteSharedKvstore(ExtismCurrentPlugin plugin, int kvstoreID);

    Result ProxyOpenSharedQueue(ExtismCurrentPlugin plugin, int queueNameData, int queueNameSize, int createIfNotExist,
                                int returnQueueID);

    Result ProxyDequeueSharedQueueItem(ExtismCurrentPlugin plugin, int queueID, int returnPayloadData, int returnPayloadSize) ;

    Result ProxyEnqueueSharedQueueItem(ExtismCurrentPlugin plugin, int queueID, int payloadData, int payloadSize) ;

    Result ProxyDeleteSharedQueue(ExtismCurrentPlugin plugin, int queueID);

    Result ProxyCreateTimer(ExtismCurrentPlugin plugin, int period, int oneTime, int returnTimerID) ;

    Result ProxyDeleteTimer(ExtismCurrentPlugin plugin, int timerID);

    MetricType ProxyCreateMetric(ExtismCurrentPlugin plugin, MetricType metricType,
                                 int metricNameData, int metricNameSize, int returnMetricID);

    Result ProxyGetMetricValue(ExtismCurrentPlugin plugin, int metricID, int returnValue);

    Result ProxySetMetricValue(ExtismCurrentPlugin plugin, int metricID, int value);

    Result ProxyIncrementMetricValue(ExtismCurrentPlugin plugin, VMData data, int metricID, long offset);

    Result ProxyDeleteMetric(ExtismCurrentPlugin plugin, int metricID);

    Result ProxyDefineMetric(ExtismCurrentPlugin plugin, int metricType, int namePtr, int nameSize, int returnMetricId);

    Result ProxyDispatchHttpCall(ExtismCurrentPlugin plugin, int upstreamNameData, int upstreamNameSize, int headersMapData, int headersMapSize,
                                    int bodyData, int bodySize, int trailersMapData, int trailersMapSize, int timeoutMilliseconds,
                                    int returnCalloutID);

    Result ProxyDispatchGrpcCall(ExtismCurrentPlugin plugin, int upstreamNameData, int upstreamNameSize, int serviceNameData, int serviceNameSize,
                                    int serviceMethodData, int serviceMethodSize, int initialMetadataMapData, int initialMetadataMapSize,
                                    int grpcMessageData, int grpcMessageSize, int timeoutMilliseconds, int returnCalloutID);

    Result ProxyOpenGrpcStream(ExtismCurrentPlugin plugin, int upstreamNameData, int upstreamNameSize, int serviceNameData, int serviceNameSize,
                               int serviceMethodData, int serviceMethodSize, int initialMetadataMapData, int initialMetadataMapSize,
                               int returnCalloutID);

    Result ProxySendGrpcStreamMessage(ExtismCurrentPlugin plugin, int calloutID, int grpcMessageData, int grpcMessageSize);

    Result ProxyCancelGrpcCall(ExtismCurrentPlugin plugin, int calloutID);

    Result ProxyCloseGrpcCall(ExtismCurrentPlugin plugin, int calloutID);

    Result ProxyCallCustomFunction(ExtismCurrentPlugin plugin, int customFunctionID, int parametersData, int parametersSize,
                                   int returnResultsData, int returnResultsSize);

    Result copyIntoInstance(ExtismCurrentPlugin plugin, Pointer memory, IoBuffer value, int retPtr, int retSize);

    Result ProxyGetProperty(ExtismCurrentPlugin plugin, VMData data, int keyPtr, int keySize, int returnValueData, int returnValueSize);

    Types.Status ProxyRegisterSharedQueue(byte[] nameData, int nameSize, Integer returnID);

    Types.Status ProxyResolveSharedQueue(byte[] vmIDData, int vmIDSize, byte[] nameData, int nameSize, Integer returnID);

    Types.Status ProxyEnqueueSharedQueue(int queueID, Byte[] valueData, int valueSize);

    Types.Status ProxyDequeueSharedQueue(int queueID, Byte[] returnValueData /***byte returnValueData*/, Integer returnValueSize);

    Types.Status ProxyDone();

    Types.Status ProxySetTickPeriodMilliseconds(VMData data, int period);

    Types.Status ProxySetEffectiveContext(ExtismCurrentPlugin plugin, int contextID);


    IoBuffer GetPluginConfig(ExtismCurrentPlugin plugin, VMData data);

    IoBuffer GetHttpRequestBody(ExtismCurrentPlugin plugin, VMData data);

    IoBuffer GetHttpResponseBody(ExtismCurrentPlugin plugin, VMData data);

    IoBuffer GetDownStreamData(ExtismCurrentPlugin plugin, VMData data);

    IoBuffer GetUpstreamData(ExtismCurrentPlugin plugin, VMData data);

    IoBuffer GetHttpCalloutResponseBody(ExtismCurrentPlugin plugin, VMData data);

    IoBuffer GetVmConfig(ExtismCurrentPlugin plugin, VMData data);

    IoBuffer GetCustomBuffer(BufferType bufferType);

    Map<String, byte[]> GetHttpRequestHeader(ExtismCurrentPlugin plugin, VMData data);

    Map<String, byte[]> GetHttpRequestTrailer(ExtismCurrentPlugin plugin, VMData data);

    Map<String, byte[]> GetHttpRequestMetadata(ExtismCurrentPlugin plugin, VMData data);

    Map<String, byte[]> GetHttpResponseHeader(ExtismCurrentPlugin plugin, VMData data);

    Map<String, byte[]> GetHttpResponseTrailer(ExtismCurrentPlugin plugin, VMData data);

    Map<String, byte[]> GetHttpResponseMetadata(ExtismCurrentPlugin plugin, VMData data);

    Map<String, byte[]> GetHttpCallResponseHeaders(ExtismCurrentPlugin plugin, VMData data);

    Map<String, byte[]> GetHttpCallResponseTrailer(ExtismCurrentPlugin plugin, VMData data);

    Map<String, byte[]> GetHttpCallResponseMetadata(ExtismCurrentPlugin plugin, VMData data);

    Map<String, byte[]> GetCustomMap(ExtismCurrentPlugin plugin, VMData data, MapType mapType);

    Either<Types.Error, Map.Entry<Pointer, byte[]>> GetMemory(ExtismCurrentPlugin plugin, VMData vmData, int addr, int size);

    Either<Types.Error, Pointer> GetMemory(ExtismCurrentPlugin plugin, VMData vmData);
}
