package org.extism.sdk;

import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import org.extism.sdk.manifest.Manifest;
import org.extism.sdk.support.JsonSerde;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
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
    public Plugin(Context context, byte[] manifestBytes, boolean withWASI, HostFunction[] functions) {

        Objects.requireNonNull(context, "context");
        Objects.requireNonNull(manifestBytes, "manifestBytes");

        Pointer[] ptrArr = new Pointer[functions == null ? 0 : functions.length];

        if (functions != null)
            for (int i = 0; i < functions.length; i++) {
               ptrArr[i] = functions[i].pointer;
            }

        Pointer contextPointer = context.getPointer();

        int index = LibExtism.INSTANCE.extism_plugin_new(contextPointer, manifestBytes, manifestBytes.length,
                ptrArr,
                functions == null ? 0 : functions.length,
                withWASI);
        if (index == -1) {
            String error = context.error(this);
            throw new ExtismException(error);
        }

        this.index= index;
        this.context = context;
    }

    public Plugin(Context context, Manifest manifest, boolean withWASI, HostFunction[] functions) {
        this(context, serialize(manifest), withWASI, functions);
    }

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
    public Pointer call(String functionName, LibExtism.ExtismVal.ByReference params, int nParams) {

        Objects.requireNonNull(functionName, "functionName");

        Pointer contextPointer = context.getPointer();
        // int exitCode = LibExtism.INSTANCE.extism_plugin_call(contextPointer, index, functionName, inputData, inputDataLength);

        return LibExtism.INSTANCE.extism_plugin_call_native(
                contextPointer,
                index,
                functionName,
                params,
                nParams);

//        if (results == -1) {
//            String error = context.error(this);
//            throw new ExtismException(error);
//        }

//        int length = LibExtism.INSTANCE.extism_plugin_output_length(contextPointer, index);
//        Pointer output = LibExtism.INSTANCE.extism_plugin_output_data(contextPointer, index);
//        return output.getByteArray(0, length);
    }

    public Pointer nativeCall(String functionName, LibExtism.ExtismVal.ByReference params, int nParams) {
        return call(functionName, params, nParams);
    }

    public int callWithIntResult(String functionName, LibExtism.ExtismVal.ByReference params, int nParams) {
        return LibExtism.INSTANCE.extism_plugin_call_native_int(
                getPointer(),
                index,
                functionName,
                params,
                nParams,
                new byte[0],
                0);
    }

    public LibExtism.ExtismVal.ByReference intToParams(int value) {
        LibExtism.ExtismVal.ByReference ptr = new LibExtism.ExtismVal.ByReference();
        LibExtism.ExtismVal[] params = (LibExtism.ExtismVal []) ptr.toArray(1);
        params[0].t = 0;
        params[0].v.setType(Integer.TYPE);
        params[0].v.i32 = value;

        ptr.write();

        return ptr;
    }

    public Pointer nativeCallPointer(String functionName, LibExtism.ExtismVal.ByReference params, int nParams) {
        Pointer contextPointer = context.getPointer();

        return LibExtism.INSTANCE.extism_plugin_call_native(
                contextPointer,
                index,
                functionName,
                params,
                nParams);
    }

    /**
     * Invoke a function with the given name and input.
     *
     * @param functionName The name of the exported function to invoke
     * @param input        The string representing the input data
     * @return A string representing the output data
     */
    public String call(String functionName, String input, LibExtism.ExtismVal.ByReference params, int nParams) {

        Objects.requireNonNull(functionName, "functionName");

        var inputBytes = input == null ? null : input.getBytes(StandardCharsets.UTF_8);
        //var outputBytes = call(functionName, inputBytes, params, nParams);
        //return new String(outputBytes, StandardCharsets.UTF_8);


        return "";
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
        Objects.requireNonNull(manifestBytes, "manifestBytes");
        Pointer[] ptrArr = new Pointer[functions == null ? 0 : functions.length];

        if (functions != null)
            for (int i = 0; i < functions.length; i++) {
                ptrArr[i] = functions[i].pointer;
            }

        return LibExtism.INSTANCE.extism_plugin_update(context.getPointer(), index, manifestBytes, manifestBytes.length,
                ptrArr,
                functions == null ? 0 : functions.length,
                withWASI);
    }

    /**
     * Frees a plugin from memory. Plugins will be automatically cleaned up
     * if you free their parent Context using {@link org.extism.sdk.Context#free() free()} or {@link org.extism.sdk.Context#reset() reset()}
     */
    public void free() {
        LibExtism.INSTANCE.extism_plugin_free(context.getPointer(), index);
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
        return LibExtism.INSTANCE.extism_plugin_config(context.getPointer(), index, jsonBytes, jsonBytes.length);
    }

    /**
     * Calls {@link #free()} if used in the context of a TWR block.
     */
    @Override
    public void close() {
        free();
    }


    public int callFunctionWithTwoInts(String name, int v1, int v2) {
        System.out.println("callFunctionWithTwoInts : " + name);

        LibExtism.ExtismVal.ByReference p1 = new LibExtism.ExtismVal.ByReference();
        LibExtism.ExtismVal[] p1s = (LibExtism.ExtismVal[]) p1.toArray(2);
        p1s[0].t = 0;
        p1s[0].v.setType(Integer.TYPE);
        p1s[0].v.i32 = v1;

        p1s[1].t = 0;
        p1s[1].v.setType(Integer.TYPE);
        p1s[1].v.i32 = v2;

        p1.write();

        return callWithIntResult(name, p1, 2);
    }

    public Pointer getPointer() {
        return context.getPointer();
    }
}
