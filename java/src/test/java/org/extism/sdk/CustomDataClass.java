package org.extism.sdk;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;

@Structure.FieldOrder({"a"})
public class CustomDataClass extends Structure {
    public static class ByReference extends CustomDataClass implements Structure.ByReference {
        public ByReference(Pointer ptr) {
            super(ptr);
        }

        public ByReference() {}
    }
    public CustomDataClass() {
    }
    public CustomDataClass(Pointer p) {
        super(p);
    }
    public int a;
}
