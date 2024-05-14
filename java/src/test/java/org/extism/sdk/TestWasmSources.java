package org.extism.sdk;

import org.extism.sdk.wasm.ByteArrayWasmSource;
import org.extism.sdk.wasm.PathWasmSource;
import org.extism.sdk.wasm.WasmSourceResolver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public enum TestWasmSources {

    CODE {
        public Path getWasmFilePath() {
            return Paths.get(WASM_LOCATION, "code.wasm");
        }

        public Path getWasmFunctionsFilePath() {
            return Paths.get(WASM_LOCATION, "code-functions.wasm");
        }

        public Path getWasmWebAssemblyFunctionFilePath() {
            return Paths.get(WASM_LOCATION, "functions.wasm");
        }

        public Path getRawAdditionFilePath() {
            return Paths.get(WASM_LOCATION, "addition.wasm");
        }

        public Path getMajorReleasePath() { return Paths.get(WASM_LOCATION, "test-1.0.0-dev.wasm"); }

        public Path getOPAPath() { return Paths.get(WASM_LOCATION, "OPA_POLICY-1.0.0-dev.wasm"); }

        public Path getWasmWaf() {
            return Paths.get(WASM_LOCATION, "coraza-proxy-wasm-v0.5.0.wasm");
        }

        public Path getGoLogginPath() {
            return Paths.get(WASM_LOCATION, "logging/go-logging-1.0.0-dev.wasm");
        }

        public Path getJsLogginPath() {
            return Paths.get(WASM_LOCATION, "logging/js-logging-1.0.0-dev.wasm");
        }

        public Path getRustLogginPath() {
            return Paths.get(WASM_LOCATION, "logging/rust-logging-1.0.0-dev.wasm");
        }

        public Path getLogPath() {
            return Paths.get(WASM_LOCATION, "log.wasm");
        }
    };

    public static final String WASM_LOCATION = "src/test/resources";

    public abstract Path getWasmFilePath();

    public abstract Path getWasmFunctionsFilePath();

    public abstract Path getRawAdditionFilePath();

    public abstract Path getMajorReleasePath();

    public abstract Path getOPAPath();

    public abstract Path getGoLogginPath();

    public abstract Path getJsLogginPath();

    public abstract Path getRustLogginPath();

    public abstract Path getLogPath();

    public abstract Path getWasmWebAssemblyFunctionFilePath();

    public PathWasmSource pathWasmSource() {
        return resolvePathWasmSource(getWasmFilePath());
    }

    public PathWasmSource pathWasmFunctionsSource() {
        return resolvePathWasmSource(getWasmFunctionsFilePath());
    }

    public PathWasmSource getRawAdditionPath() {
        return resolvePathWasmSource(getRawAdditionFilePath());
    }

    public PathWasmSource getMajorRelease() {
        return resolvePathWasmSource(getMajorReleasePath());
    }

    public PathWasmSource getOPA() {
        return resolvePathWasmSource(getOPAPath());
    }

    public PathWasmSource getGoLogging() {
        return resolvePathWasmSource(getGoLogginPath());
    }

    public PathWasmSource getJsLogging() {
        return resolvePathWasmSource(getJsLogginPath());
    }

    public PathWasmSource getRustLogging() {
        return resolvePathWasmSource(getRustLogginPath());
    }

    public PathWasmSource getLog() {
        return resolvePathWasmSource(getLogPath());
    }

    public abstract Path getWasmWaf();

    public PathWasmSource pathWasmWebAssemblyFunctionSource() {
        return resolvePathWasmSource(getWasmWebAssemblyFunctionFilePath());
    }

    public ByteArrayWasmSource byteArrayWasmSource() {
        try {
            byte[] wasmBytes = Files.readAllBytes(getWasmFilePath());
            return new WasmSourceResolver().resolve("wasm@" + Arrays.hashCode(wasmBytes), wasmBytes);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    public static PathWasmSource resolvePathWasmSource(Path path) {
        return new WasmSourceResolver().resolve(path);
    }

    public PathWasmSource pathWasmWaf() {
        return resolvePathWasmSource(getWasmWaf());
    }

}
