use std::collections::BTreeMap;

use crate::*;

/// Plugin contains everything needed to execute a WASM function
pub struct Plugin {
    pub instance: Instance,
    pub last_error: std::cell::RefCell<Option<std::ffi::CString>>,
    pub memory: PluginMemory,
    pub vars: BTreeMap<String, Vec<u8>>
}

pub struct Internal {
    pub input_length: usize,
    pub input: *const u8,
    pub output_offset: usize,
    pub output_length: usize,
    pub plugin: *mut Plugin,
    pub wasi: Option<Wasi>,
    pub http_status: u16,
}

pub struct Wasi {
    pub ctx: wasmtime_wasi::WasiCtx,
    #[cfg(feature = "nn")]
    pub nn: wasmtime_wasi_nn::WasiNnCtx,
    #[cfg(not(feature = "nn"))]
    pub nn: (),
}

impl Internal {
    pub fn new(manifest: &Manifest, wasi: bool) -> Result<Self, Error> {
        let wasi = if wasi {
            let auth = wasmtime_wasi::ambient_authority();
            let mut ctx = wasmtime_wasi::WasiCtxBuilder::new();
            for (k, v) in manifest.as_ref().config.iter() {
                ctx = ctx.env(k, v)?;
            }

            if let Some(a) = &manifest.as_ref().allowed_paths {
                for (k, v) in a.iter() {
                    let d = wasmtime_wasi::Dir::open_ambient_dir(k, auth)?;
                    ctx = ctx.preopened_dir(d, v)?;
                }
            }

            #[cfg(feature = "nn")]
            let nn = wasmtime_wasi_nn::WasiNnCtx::new()?;

            #[cfg(not(feature = "nn"))]
            #[allow(clippy::let_unit_value)]
            let nn = ();

            Some(Wasi {
                ctx: ctx.build(),
                nn,
            })
        } else {
            None
        };

        Ok(Internal {
            input_length: 0,
            output_offset: 0,
            output_length: 0,
            input: std::ptr::null(),
            wasi,
            plugin: std::ptr::null_mut(),
            http_status: 0,
        })
    }

    pub fn plugin(&self) -> &Plugin {
        unsafe { &*self.plugin }
    }

    pub fn plugin_mut(&mut self) -> &mut Plugin {
        unsafe { &mut *self.plugin }
    }

    pub fn memory(&self) -> &PluginMemory {
        &self.plugin().memory
    }

    pub fn memory_mut(&mut self) -> &mut PluginMemory {
        &mut self.plugin_mut().memory
    }
}


impl Plugin {
    /// Get a function by name
    pub fn get_func(&mut self, function: impl AsRef<str>) -> Option<Func> {
        self.instance
            .get_func(&mut self.memory.store, function.as_ref())
    }

    /// Get a memory by name
    pub fn get_memory(&mut self, memory: impl AsRef<str>) -> Option<Memory> {
        self.instance
            .get_memory(&mut self.memory.store, memory.as_ref())
    }

    /// Set `last_error` field
    pub fn set_error(&self, e: impl std::fmt::Debug) {
        debug!("Set error: {:?}", e);
        *self.last_error.borrow_mut() = Some(error_string(e));
    }

    pub fn error<E>(&self, e: impl std::fmt::Debug, x: E) -> E {
        self.set_error(e);
        x
    }

    /// Unset `last_error` field
    pub fn clear_error(&self) {
        *self.last_error.borrow_mut() = None;
    }

    /// Store input in memory and initialize `Internal` pointer
    pub fn set_input(&mut self, input: *const u8, mut len: usize) {
        if input.is_null() {
            len = 0;
        }
        let ptr = self as *mut _;
        let internal = self.memory.store.data_mut();
        internal.input = input;
        internal.input_length = len;
        internal.plugin = ptr;
    }

    pub fn dump_memory(&self) {
        self.memory.dump();
    }

    pub fn has_wasi(&self) -> bool {
        self.memory.store.data().wasi.is_some()
    }
}