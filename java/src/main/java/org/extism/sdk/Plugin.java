package org.extism.sdk;

import com.sun.jna.Pointer;
import org.extism.sdk.manifest.Manifest;
import org.extism.sdk.support.JsonSerde;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Objects;

/**
 * Represents a Extism plugin.
 */
public class Plugin implements AutoCloseable {

    /**
     * Holds the Extism {@link Context} that the plugin belongs to.
     */
    private final Context context;

    /**
     * Holds the index of the plugin
     */
    private final int index;

    /**
     * Constructor for a Plugin. Only expose internally. Plugins should be created and
     * managed from {@link org.extism.sdk.Context}.
     *
     * @param context       The context to manage the plugin
     * @param manifestBytes The manifest for the plugin
     * @param functions     The Host functions for th eplugin
     * @param withWASI      Set to true to enable WASI
     */
    public Plugin(Context context, Manifest manifestBytes, boolean withWASI, HostFunction[] functions) {

        Objects.requireNonNull(context, "context");
        Objects.requireNonNull(manifestBytes, "manifestBytes");

        Pointer[] ptrArr = new Pointer[functions == null ? 0 : functions.length];

        if (functions != null)
            for (int i = 0; i < functions.length; i++) {
               ptrArr[i] = functions[i].pointer;
            }

        long contextPointer = context.getPointer();

//        int index = LibExtism.INSTANCE.extism_plugin_new(contextPointer, manifestBytes, manifestBytes.length,
//                ptrArr,
//                functions == null ? 0 : functions.length,
//                withWASI);
        byte[] test = serialize(manifestBytes);
        int index = JniWrapper.extismPluginNew(
                contextPointer,
                new String(test, StandardCharsets.UTF_8),
                test.length,
                // functions == null ? new HostFunction[0] : functions,
                new ArrayList<>(),
                0,//functions == null ? 0 : functions.length,
                withWASI);

        if (index == -1) {
            String error = context.error(this);
            throw new ExtismException(error);
        }

        this.index= index;
        this.context = context;
    }

//    public Plugin(Context context, Manifest manifest, boolean withWASI, HostFunction[] functions) {
////        this(context, serialize(manifest), withWASI, functions);
//        this(context, manifest, withWASI, functions);
//    }

    private static byte[] serialize(Manifest manifest) {
        Objects.requireNonNull(manifest, "manifest");
        return JsonSerde.toJson(manifest).getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Getter for the internal index pointer to this plugin.
     *
     * @return the plugin index
     */
    public int getIndex() {
        return index;
    }

    /**
     * Invoke a function with the given name and input.
     *
     * @param functionName The name of the exported function to invoke
     * @param inputData    The raw bytes representing any input data
     * @return A byte array representing the raw output data
     * @throws ExtismException if the call fails
     */
    public byte[] call(String functionName, byte[] inputData) {

        Objects.requireNonNull(functionName, "functionName");

        long contextPointer = context.getPointer();
        int inputDataLength = inputData == null ? 0 : inputData.length;
        int exitCode = JniWrapper.extismPluginCall(contextPointer, index, functionName, new String(inputData, StandardCharsets.UTF_8), inputDataLength);
        if (exitCode == -1) {
            String error = context.error(this);
            throw new ExtismException(error);
        }

        System.out.println(JniWrapper.extismPluginResult(contextPointer, index));

//        int length = JniWrapper.extism_plugin_output_length(contextPointer, index);
//        long output = JniWrapper.extism_plugin_output_data(contextPointer, index);

//        new Pointer(output)
//        return output.getByteArray(0, length);

        this.free();
        throw new ExtismException("NEED TO BE CHANGED");
    }

    /**
     * Invoke a function with the given name and input.
     *
     * @param functionName The name of the exported function to invoke
     * @param input        The string representing the input data
     * @return A string representing the output data
     */
    public String call(String functionName, String input) {

        Objects.requireNonNull(functionName, "functionName");

        var inputBytes = input == null ? null : input.getBytes(StandardCharsets.UTF_8);
        var outputBytes = call(functionName, inputBytes);
        return new String(outputBytes, StandardCharsets.UTF_8);
    }

    /**
     * Update the plugin code given manifest changes
     *
     * @param manifest The manifest for the plugin
     * @param withWASI Set to true to enable WASI
     * @return {@literal true} if update was successful
     */
    public boolean update(Manifest manifest, boolean withWASI, HostFunction[] functions) {
        return update(serialize(manifest), withWASI, functions);
    }

    /**
     * Update the plugin code given manifest changes
     *
     * @param manifestBytes The manifest for the plugin
     * @param withWASI      Set to true to enable WASI
     * @return {@literal true} if update was successful
     */
    public boolean update(byte[] manifestBytes, boolean withWASI, HostFunction[] functions) {
//        Objects.requireNonNull(manifestBytes, "manifestBytes");
//        Pointer[] ptrArr = new Pointer[functions == null ? 0 : functions.length];
//
//        if (functions != null)
//            for (int i = 0; i < functions.length; i++) {
//                ptrArr[i] = functions[i].pointer;
//            }
//
//        return LibExtism.INSTANCE.extism_plugin_update(context.getPointer(), index, manifestBytes, manifestBytes.length,
//                ptrArr,
//                functions == null ? 0 : functions.length,
//                withWASI);
        throw new ExtismException("NEED TO BE CHANGED");
    }

    /**
     * Frees a plugin from memory. Plugins will be automatically cleaned up
     * if you free their parent Context using {@link org.extism.sdk.Context#free() free()} or {@link org.extism.sdk.Context#reset() reset()}
     */
    public void free() {
        JniWrapper.extismPluginFree(context.getPointer(), index);
    }

    /**
     * Update plugin config values, this will merge with the existing values.
     *
     * @param json
     * @return
     */
    public boolean updateConfig(String json) {
        Objects.requireNonNull(json, "json");
        return updateConfig(json.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Update plugin config values, this will merge with the existing values.
     *
     * @param jsonBytes
     * @return {@literal true} if update was successful
     */
    public boolean updateConfig(byte[] jsonBytes) {
        Objects.requireNonNull(jsonBytes, "jsonBytes");
//        return LibExtism.INSTANCE.extism_plugin_config(context.getPointer(), index, jsonBytes, jsonBytes.length);
        throw new ExtismException("NEED TO BE CHANGED");
    }

    /**
     * Calls {@link #free()} if used in the context of a TWR block.
     */
    @Override
    public void close() {
        free();
    }
}
