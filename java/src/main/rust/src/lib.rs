pub use anyhow::Error;
pub(crate) use wasmtime::*;

mod context;
mod function;
pub mod manifest;
mod memory;
pub(crate) mod pdk;
mod plugin;
mod plugin_ref;
pub mod sdk;
mod timer;

pub use context::Context;
pub use function::{Function, UserData, Val, ValType};
pub use manifest::Manifest;
pub use memory::{MemoryBlock, PluginMemory, ToMemoryBlock};
pub use plugin::{Internal, Plugin, Wasi};
pub use plugin_ref::PluginRef;
pub(crate) use timer::{Timer, TimerAction};

pub type Size = u64;
pub type PluginIndex = i32;

pub(crate) use log::{debug, error, trace};

/// Converts any type implementing `std::fmt::Debug` into a suitable CString to use
/// as an error message
pub(crate) fn error_string(e: impl std::fmt::Debug) -> std::ffi::CString {
    let x = format!("{:?}", e).into_bytes();
    let x = if x[0] == b'"' && x[x.len() - 1] == b'"' {
        x[1..x.len() - 1].to_vec()
    } else {
        x
    };
    unsafe { std::ffi::CString::from_vec_unchecked(x) }
}

use robusta_jni::bridge;

#[bridge]
mod jni {
    use robusta_jni::convert::{
        Field, IntoJavaValue, Signature, TryFromJavaValue, TryIntoJavaValue,
    };
    use robusta_jni::jni::errors::Result as JniResult;
    use robusta_jni::jni::objects::AutoLocal;
    use robusta_jni::jni::JNIEnv;
    // use sdk::extism_error;
    use std::os::raw::c_char;

    use crate::sdk::{
        extism_context_new, extism_plugin_call, extism_plugin_free, extism_plugin_new,
        ExtismFunction,
    };
    // use robusta_jni::jni::errors::Error as JniError;

    #[derive(Signature, TryIntoJavaValue, IntoJavaValue, TryFromJavaValue)]
    #[package(org.extism.sdk.manifest)]
    pub struct Manifest<'env: 'borrow, 'borrow> {
        #[instance]
        raw: AutoLocal<'env, 'borrow>,
        #[field]
        sources: Field<'env, 'borrow, Vec<ByteArrayWasmSource<'env, 'borrow>>>,
    }

    #[derive(Signature, TryIntoJavaValue, IntoJavaValue, TryFromJavaValue)]
    #[package(org.extism.sdk.wasm)]
    pub struct ByteArrayWasmSource<'env: 'borrow, 'borrow> {
        #[instance]
        raw: AutoLocal<'env, 'borrow>,
        #[field]
        name: Field<'env, 'borrow, String>,
        #[field]
        hash: Field<'env, 'borrow, String>,
        #[field]
        data: Field<'env, 'borrow, String>,
    }

    #[derive(Signature, TryIntoJavaValue, IntoJavaValue, TryFromJavaValue)]
    #[package(org.extism.sdk)]
    pub struct JniWrapper<'env: 'borrow, 'borrow> {
        #[instance]
        raw: AutoLocal<'env, 'borrow>,
    }

    #[derive(Signature, TryIntoJavaValue, IntoJavaValue, TryFromJavaValue)]
    #[package(org.extism.sdk)]
    pub struct HostFunction<'env: 'borrow, 'borrow> {
        #[instance]
        raw: AutoLocal<'env, 'borrow>,
    }

    // #[derive(Signature, TryIntoJavaValue, IntoJavaValue, TryFromJavaValue)]
    // #[package(org.extism.sdk)]
    // pub struct CustomContext<'env: 'borrow, 'borrow,> {
    //     #[instance]
    //     raw: AutoLocal<'env, 'borrow>,
    //     #[field] contextPointer: Field<'env, 'borrow, i64>
    // }

    impl<'env: 'borrow, 'borrow> JniWrapper<'env, 'borrow> {
        #[constructor]
        pub extern "java" fn new(env: &'borrow JNIEnv<'env>) -> JniResult<Self> {}

        pub unsafe extern "jni" fn extismPluginNew(
            ctxPointer: i64,
            // manifest: Manifest<'env, 'borrow>,
            manifest: String,
            manifestSize: i32,
            mut functions: Vec<HostFunction<'env, 'borrow>>,
            n_functions: i32,
            with_wasi: bool,
        ) -> i32 {
            let ctx = unsafe { ctxPointer as *mut crate::Context };
            extism_plugin_new(
                ctx,
                manifest.as_ptr() as *const u8,
                manifestSize as u64,
                functions.as_mut_ptr() as *mut *const ExtismFunction,
                n_functions as crate::Size,
                with_wasi,
            )
        }

        pub unsafe extern "jni" fn extismContextNew() -> i64 {
            extism_context_new() as i64
        }

        pub unsafe extern "jni" fn extismPluginFree(ctxPointer: i64, pluginIndex: i32) {
            let ctx = unsafe { ctxPointer as *mut crate::Context };
            extism_plugin_free(ctx, pluginIndex)
        }

        pub unsafe extern "jni" fn extismPluginCall(
            ctxPointer: i64,
            pluginIndex: i32,
            functionName: String,
            data: String,
            data_len: i32,
        ) -> i32 {
            let ctx = unsafe { ctxPointer as *mut crate::Context };
            extism_plugin_call(
                ctx,
                pluginIndex,
                functionName.as_ptr() as *const c_char,
                data.as_ptr() as *const u8,
                data_len as crate::Size,
            )
        }

        pub unsafe extern "jni" fn extismError(ctxPointer: i64, pluginIndex: i32) -> String {
            let ctx = unsafe { ctxPointer as *mut crate::Context };
            core::ffi::CStr::from_ptr(crate::sdk::extism_error(ctx, pluginIndex))
                .to_str()
                .unwrap()
                .to_owned()
        }

        pub unsafe extern "jni" fn extismPluginResult(ctxPointer: i64, pluginIndex: i32) -> String {
            let ctx = unsafe { ctxPointer as *mut crate::Context };
            let out_len = crate::sdk::extism_plugin_output_length(ctx, pluginIndex);

            let ptr = crate::sdk::extism_plugin_output_data(ctx, pluginIndex);
            let t = std::slice::from_raw_parts(ptr, out_len as usize);
            std::str::from_utf8_unchecked(t).to_owned()
        }
    }
}
