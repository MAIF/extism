package org.extism.sdk;

import com.sun.jna.Pointer;
import com.sun.jna.PointerType;
import org.extism.sdk.framework.NewFramework;

import java.nio.charset.StandardCharsets;

public class Internal extends PointerType {

    public Pointer memory() {
        return NewFramework.INSTANCE.extism_current_plugin_memory(this);
    }

    public int alloc(int n) {
        return NewFramework.INSTANCE.extism_current_plugin_memory_alloc(this, n);
    }

    public Pointer getLinearMemory(int instanceIndex, String memoryName) {
        return NewFramework.INSTANCE.extism_get_lineary_memory_from_host_functions(this, instanceIndex, memoryName);
    }

    public void free(long offset) {
        NewFramework.INSTANCE.extism_current_plugin_memory_free(this, offset);
    }

    public long memoryLength(long offset) {
        return NewFramework.INSTANCE.extism_current_plugin_memory_length(this, offset);
    }

    // Return a string from a host function
    public void returnString(NewFramework.ExtismVal output, String s) {
        returnBytes(output, s.getBytes(StandardCharsets.UTF_8));
    }

    // Return bytes from a host function
    public void returnBytes(NewFramework.ExtismVal output, byte[] b) {
        int offs = this.alloc(b.length);
        Pointer ptr = this.memory();
        ptr.write(offs, b, 0, b.length);
        output.v.i64 = offs;
    }

    // Return int from a host function
    public void returnInt(NewFramework.ExtismVal output, int v) {
        output.v.i32 = v;
    }

    // Get bytes from host function parameter
    public byte[] inputBytes(NewFramework.ExtismVal input) throws Exception {
        switch (input.t) {
            case 0:
                return this.memory()
                        .getByteArray(input.v.i32,
                                NewFramework.INSTANCE.extism_current_plugin_memory_length(this, input.v.i32));
            case 1:
                return this.memory()
                        .getByteArray(input.v.i64,
                                NewFramework.INSTANCE.extism_current_plugin_memory_length(this, input.v.i64));
            default:
                throw new Exception("inputBytes error: ExtismValType " + NewFramework.ExtismValType.values()[input.t] + " not implemtented");
        }
    }

    // Get string from host function parameter
    public String inputString(NewFramework.ExtismVal input) throws Exception {
        return new String(this.inputBytes(input));
    }
}