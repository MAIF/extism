package org.extism.sdk;

import com.sun.jna.Pointer;
import org.extism.sdk.manifest.Manifest;

import java.util.ArrayList;


public class JniWrapper {

    static {
        System.loadLibrary("jni_wrapper");
    }

    public static native int extismPluginNew(long contextPointer, String wasm, int wasmSize, ArrayList<HostFunction> functions, int nFunctions, boolean withWASI);

    public static native long extismContextNew();

    public static native void extismPluginFree(long contextPointer, int pluginIndex);

    public static native String extismPluginResult(long contextPointer, int pluginIndex);

    public static native int extismPluginCall(long contextPointer, int pluginIndex, String function_name, String data, int dataLength);

    public static native String extismError(long contextPointer, int pluginId);

//    public static native int extism_plugin_output_length(long contextPointer, int pluginIndex);
//
//    public static native long extism_plugin_output_data(long contextPointer, int pluginIndex);
}
