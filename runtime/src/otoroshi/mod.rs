use anyhow::Error;
use wasmtime::*;

pub mod manifest;
mod memory;
pub(crate) mod pdk;
mod plugin;
mod types;
mod plugin_template;
pub mod function;
pub mod sdk;
pub mod wasm_memory;

pub use types::{ExtismMemory, ExtismFunction, ExtismVal, OtoroshiFunctionType, ValUnion};
pub use function::{Function, UserData, Val, ValType};
pub use manifest::Manifest;
pub use memory::{MemoryBlock, PluginMemory, ToMemoryBlock};
pub use plugin::{Internal, WasmPlugin, Wasi};
pub use plugin_template::PluginTemplate;
pub use wasm_memory::WasmMemory;


const EXPORT_MODULE_NAME: &str = "env";
type Size = u64;

use log::{debug, error, trace};

// /// Converts any type implementing `std::fmt::Debug` into a suitable CString to use
// /// as an error message
fn error_string(e: impl std::fmt::Debug) -> std::ffi::CString {
    let x = format!("{:?}", e).into_bytes();
    let x = if x[0] == b'"' && x[x.len() - 1] == b'"' {
        x[1..x.len() - 1].to_vec()
    } else {
        x
    };
    unsafe { std::ffi::CString::from_vec_unchecked(x) }
}
