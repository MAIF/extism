package org.extism.sdk.coraza.proxywasmhost.v2;

public class Utils {

    public static void traceVmHost(String message) {
        System.out.println("[vm->host]: " + message);
    }

    public static void traceHostVm(String message) {
        System.out.println("[host->vm]: " + message);
    }

    public static void DEBUG(String functionName, String str) {
        System.out.println("[DEBUG](" + functionName + "): " + str);
    }
}
