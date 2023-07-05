
use crate::otoroshi::*;

pub struct ExtismMemory(pub WasmMemory);

/// A union type for host function argument/return values
#[repr(C)]
#[derive(Copy, Clone)]
pub union ValUnion {
    pub i32: i32,
    pub i64: i64,
    pub f32: f32,
    pub f64: f64,
    // TODO: v128, ExternRef, FuncRef
}

/// `ExtismVal` holds the type and value of a function argument/return
#[repr(C)]
#[derive(Clone)]
pub struct ExtismVal {
    pub t: ValType,
    pub v: ValUnion,
}

/// Wraps host functions
pub struct ExtismFunction(pub Function);

impl From<Function> for ExtismFunction {
    fn from(x: Function) -> Self {
        ExtismFunction(x)
    }
}

impl From<&wasmtime::Val> for ExtismVal {
    fn from(value: &wasmtime::Val) -> Self {
        match value.ty() {
            wasmtime::ValType::I32 => ExtismVal {
                t: ValType::I32,
                v: ValUnion {
                    i32: value.unwrap_i32(),
                },
            },
            wasmtime::ValType::I64 => ExtismVal {
                t: ValType::I64,
                v: ValUnion {
                    i64: value.unwrap_i64(),
                },
            },
            wasmtime::ValType::F32 => ExtismVal {
                t: ValType::F32,
                v: ValUnion {
                    f32: value.unwrap_f32(),
                },
            },
            wasmtime::ValType::F64 => ExtismVal {
                t: ValType::F64,
                v: ValUnion {
                    f64: value.unwrap_f64(),
                },
            },
            t => ExtismVal {
                t: ValType::I32,
                v: ValUnion {
                    i32: -1,
                },
            },
        }
    }
}

/// Host function signature
pub type OtoroshiFunctionType = extern "C" fn(
    plugin: *mut WasmPlugin,
    inputs: *const ExtismVal,
    n_inputs: Size,
    outputs: *mut ExtismVal,
    n_outputs: Size,
    data: *mut std::ffi::c_void,
);
