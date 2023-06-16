use std::collections::BTreeMap;

use crate::*;

pub struct PluginTemplate {
    pub module: Module,
    pub main_module_name: String,
    pub modules: BTreeMap<String, Module>,
    pub manifest: Manifest,
}

impl PluginTemplate {
    pub fn new<'a>(
        engine: &Engine,
        wasm: impl AsRef<[u8]>,
    ) -> Result<PluginTemplate, Error> {
        let (manifest, modules) = Manifest::new(&engine, wasm.as_ref())?;

        let (main_module_name, main_module) =
            modules.get("main").map(|x| ("main", x)).unwrap_or_else(|| {
                let entry = modules.iter().last().unwrap();
                (entry.0.as_str(), entry.1)
            });

        Ok(PluginTemplate {
            module: main_module.clone(),
            modules: modules.clone(),
            main_module_name: main_module_name.to_owned(),
            manifest,
        })
    }

    pub fn instantiate<'a>(
        &self,
        engine: &Engine,
        imports: impl IntoIterator<Item = &'a Function>,
        memories_imports: impl IntoIterator<Item = &'a WasmMemory>,
        with_wasi: bool,
    ) -> Result<Plugin, Error> {
        let mut imports = imports.into_iter();
        let mut memories_imports = memories_imports.into_iter();
        
        let mut store = Store::new(&engine, Internal::new(&self.manifest, with_wasi)?);

        let memory = Memory::new(
            &mut store,
            MemoryType::new(4, self.manifest.as_ref().memory.max_pages),
        )?;

        let mut linker = Linker::new(&engine);
        // linker.allow_shadowing(true);

        if with_wasi {
            wasmtime_wasi::add_to_linker(&mut linker, |x: &mut Internal| {
                &mut x.wasi.as_mut().unwrap().ctx
            })?;
        }

        macro_rules! define_funcs {
            ($m:expr, { $($name:ident($($args:expr),*) $(-> $($r:expr),*)?);* $(;)?}) => {
                match $m {
                $(
                    concat!("extism_", stringify!($name)) => {
                        let t = FuncType::new([$($args),*], [$($($r),*)?]);
                        linker.func_new(EXPORT_MODULE_NAME, concat!("extism_", stringify!($name)), t, pdk::$name)?;
                        continue
                    }
                )*
                    _ => ()
                }
            };
        }

        // Add builtins
        for (_name, module) in self.modules.clone().iter() {
            for import in module.imports() {
                let module_name = import.module();
                let name = import.name();
                use wasmtime::ValType::*;

                if module_name == EXPORT_MODULE_NAME {
                    define_funcs!(name,  {
                        alloc(I64) -> I64;
                        free(I64);
                        load_u8(I64) -> I32;
                        load_u64(I64) -> I64;
                        store_u8(I64, I32);
                        store_u64(I64, I64);
                        input_length() -> I64;
                        input_load_u8(I64) -> I32;
                        input_load_u64(I64) -> I64;
                        output_set(I64, I64);
                        error_set(I64);
                        config_get(I64) -> I64;
                        var_get(I64) -> I64;
                        var_set(I64, I64);
                        http_request(I64, I64) -> I64;
                        http_status_code() -> I32;
                        length(I64) -> I64;
                        log_warn(I64);
                        log_info(I64);
                        log_debug(I64);
                        log_error(I64);
                    });

                    for f in &mut imports {
                        let name = f.name().to_string();
                        let ns = f.namespace().unwrap_or(EXPORT_MODULE_NAME);
                        linker.func_new(ns, &name, f.ty().clone(), unsafe {
                            &*std::sync::Arc::as_ptr(&f.f)
                        })?;
                    }

                    // for m in &mut memories_imports {
                    //     let name = m.name.to_string();
                    //     let ns = m.namespace.to_string();
                    //     let mem = wasmtime::Memory::new(&mut store, (&m.ty).clone())?;
                    //     linker.define(store, &ns, &name, mem)?;
                    // }
                }
            }
        }

        // Add modules to linker
        for (name, module) in self.modules.iter() {
            if name != self.main_module_name.as_str() {
                linker.module(&mut store, name, module)?;
                linker.alias_module(name, "env")?;
            }
        }
        
        let instance = linker.instantiate(&mut store, &self.module)?;

        let manifest = self.manifest.as_ref();
        let plugin = Plugin {
            memory: PluginMemory::new(store, memory, manifest),
            instance,
            last_error: std::cell::RefCell::new(None),
            vars: BTreeMap::new(),
        };

        Ok(plugin)
    }
}