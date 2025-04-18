package org.extism.sdk;

import com.sun.jna.Pointer;

import java.nio.charset.StandardCharsets;

public class ExtismCurrentPlugin {
    public Pointer pointer;

    public ExtismCurrentPlugin(Pointer pointer) {
        this.pointer = pointer;
    }

    public Pointer memory() {
        return LibExtism.INSTANCE.extism_current_plugin_memory(this.pointer);
    }

    public int alloc(int n) {
        return LibExtism.INSTANCE.extism_current_plugin_memory_alloc(this.pointer, n);
    }

    public void free(long offset) {
        LibExtism.INSTANCE.extism_current_plugin_memory_free(this.pointer, offset);
    }

    public long memoryLength(long offset) {
        return LibExtism.INSTANCE.extism_current_plugin_memory_length(this.pointer, offset);
    }

    /**
     * Return a string from a host function
     * @param output - The output to set
     * @param s - The string to return
     */
    public void returnString(LibExtism.ExtismVal output, String s) {
        returnBytes(output, s.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Return bytes from a host function
     * @param output - The output to set
     * @param b - The buffer to return
     */
    public void returnBytes(LibExtism.ExtismVal output, byte[] b) {
        int offs = this.alloc(b.length);
        Pointer ptr = this.memory();
        ptr.write(offs, b, 0, b.length);
        output.v.i64 = offs;
    }

    /**
     * Return int from a host function
     * @param output - The output to set
     * @param v - The int to return
     */
    public void returnInt(LibExtism.ExtismVal output, int v) {
        output.v.i32 = v;
    }

    /**
     * Get bytes from host function parameter
     * @param input - The input to read
     */
    public byte[] inputBytes(LibExtism.ExtismVal input) {
        switch (input.t) {
            case 0:
                return this.memory()
                        .getByteArray(input.v.i32,
                                LibExtism.INSTANCE.extism_current_plugin_memory_length(this.pointer, input.v.i32));
            case 1:
                return this.memory()
                        .getByteArray(input.v.i64,
                                LibExtism.INSTANCE.extism_current_plugin_memory_length(this.pointer, input.v.i64));
            default:
                throw new ExtismException("inputBytes error: ExtismValType " + LibExtism.ExtismValType.values()[input.t] + " not implemtented");
        }
    }

    /**
     * Get string from host function parameter
     * @param input - The input to read
     */
    public String inputString(LibExtism.ExtismVal input) {
        return new String(this.inputBytes(input));
    }

    public Pointer customMemoryGet() {
        return LibExtism.INSTANCE.custom_memory_get(this.pointer);
    }

    public int customMemoryAlloc(int n) {
        return LibExtism.INSTANCE.custom_memory_alloc(this.pointer, n);
    }

    public void customMemoryFree(long offset) {
        LibExtism.INSTANCE.custom_memory_free(this.pointer, offset);
    }

    public long customMemoryLength(long offset) {
        return LibExtism.INSTANCE.custom_memory_length(this.pointer, offset);
    }

    public Pointer linearMemoryGet(String namespace, String name) {
        return LibExtism.INSTANCE.linear_memory_get(this.pointer, namespace, name);
    }

    public int linearMemorySize(String namespace, String name, long n) {
        return LibExtism.INSTANCE.linear_memory_size(this.pointer, namespace, name, n);
    }

    /*public int linearMemoryAlloc(String namespace, String name, long n) {
        return LibExtism.INSTANCE.linear_memory_alloc(this.pointer, namespace, name, n);
    }*/

//    public void linearMemoryFree(String namespace, String name, long ptr) {
//        LibExtism.INSTANCE.linear_memory_free(this.pointer, namespace, name, ptr);
//    }
}