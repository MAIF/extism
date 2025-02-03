#![allow(clippy::missing_safety_doc)]

use std::os::raw::c_char;

use crate::{
    extension::*,
    function,
    sdk::{ExtismFunction, ExtismVal, Size},
    CurrentPlugin, Internal, Plugin, EXTISM_ENV_MODULE,
};

use super::wasm_memory::ExtismMemory;

#[no_mangle]
unsafe fn extension_plugin_call_native(
    plugin: *mut Plugin,
    func_name: *const c_char,
    params: Option<Vec<Val>>,
) -> Option<*mut ExtismVal> {
    if plugin as usize == 0 {
        return None;
    }

    if let Some(plugin) = plugin.as_mut() {
        let _lock = plugin.instance.clone();
        let mut acquired_lock = _lock.lock().unwrap();

        let name = std::ffi::CStr::from_ptr(func_name);
        let name = match name.to_str() {
            Ok(name) => name,
            Err(e) => {
                plugin.current_plugin_mut().extension_error = Some(Error::new(e));
                return None;
            }
        };

        let mut results = vec![wasmtime::Val::I32(0); 0];

        let res = plugin.raw_call(
            &mut acquired_lock,
            name,
            [0; 0],
            None::<()>,
            false,
            params,
            Some(&mut results),
        );

        return match res {
            Err((e, _rc)) => {
                plugin.current_plugin_mut().extension_error = Some(e);
                None
            }
            Ok(_x) => {
                let mut v = results
                    .iter()
                    .map(|x| ExtismVal::from_val(x, &plugin.store).ok().unwrap()) //  TODO - check if ok().wrap() is legit
                    .collect::<Vec<ExtismVal>>();

                let ptr = v.as_mut_ptr() as *mut _;
                std::mem::forget(v);
                Some(ptr)
            }
        };
    }

    None
}

#[no_mangle]
pub(crate) unsafe extern "C" fn extension_deallocate_results(ptr: *mut ExtismVal, len: usize) {
    let len = len as usize;
    drop(Vec::from_raw_parts(ptr, len, len));
}

#[no_mangle]
pub(crate) unsafe extern "C" fn extension_call(
    plugin: *mut Plugin,
    func_name: *const c_char,
    params: *const ExtismVal,
    n_params: Size,
) -> *mut ExtismVal {
    let params = ptr_as_val(params, n_params);

    match extension_plugin_call_native(plugin, func_name, params) {
        None => std::ptr::null_mut(),
        Some(values) => values,
    }
}

#[no_mangle]
pub(crate) unsafe extern "C" fn wasm_plugin_call_without_params(
    plugin_ptr: *mut Plugin,
    func_name: *const c_char,
) -> *mut ExtismVal {
    match extension_plugin_call_native(plugin_ptr, func_name, None) {
        None => std::ptr::null_mut(),
        Some(values) => values,
    }
}

unsafe fn ptr_as_val(params: *const ExtismVal, n_params: Size) -> Option<Vec<Val>> {
    let prs = std::slice::from_raw_parts(params, n_params as usize);

    let p: Vec<Val> = prs
        .iter()
        .map(|x| {
            let t = match x.t {
                function::ValType::I32 => Val::I32(x.v.i32),
                function::ValType::I64 => Val::I64(x.v.i64),
                function::ValType::F32 => Val::F32(x.v.f32 as u32),
                function::ValType::F64 => Val::F64(x.v.f64 as u64),
                _ => Val::I32(-1),
                // _ => todo!(),
            };
            t
        })
        .collect();

    if params.is_null() || n_params == 0 {
        None
    } else {
        Some(p)
    }
}

#[no_mangle]
pub(crate) unsafe extern "C" fn wasm_plugin_call_without_results(
    plugin_ptr: *mut Plugin,
    func_name: *const c_char,
    params: *const ExtismVal,
    n_params: Size,
) {
    let params = ptr_as_val(params, n_params);

    extension_plugin_call_native(plugin_ptr, func_name, params);
}

#[no_mangle]
pub(crate) unsafe extern "C" fn extension_create_wasmtime_memory(
    name: *const std::ffi::c_char,
    namespace: *const std::ffi::c_char,
    min_pages: u32,
    max_pages: u32,
) -> *mut ExtismMemory {
    let name = match std::ffi::CStr::from_ptr(name).to_str() {
        Ok(x) => x.to_string(),
        Err(_) => {
            return std::ptr::null_mut();
        }
    };

    let namespace = match std::ffi::CStr::from_ptr(namespace).to_str() {
        Ok(x) => x.to_string(),
        Err(_) => {
            return std::ptr::null_mut();
        }
    };

    let mem = WasmMemory::new(name, namespace, min_pages, max_pages);

    Box::into_raw(Box::new(ExtismMemory(mem)))
}

/// Remove all plugins from the registry
#[no_mangle]
pub unsafe extern "C" fn custom_memory_reset_from_plugin(plugin: *mut Plugin) {
    if plugin.is_null() {
        return;
    }

    let plugin = &mut *plugin;
    let plugin_memory = &mut *plugin.current_plugin().memory_export;
    plugin_memory.reset();
}

#[no_mangle]
pub unsafe extern "C" fn extension_extism_memory_write_bytes(
    instance_ptr: *mut Plugin,
    data: *const u8,
    data_size: Size,
    offset: u32,
    namespace: *const c_char,
    name: *const c_char,
) -> i8 {
    let plugin = &mut *instance_ptr;

    let (linker, store) = plugin.linker_and_store();

    let data = std::slice::from_raw_parts(data, data_size as usize);

    let ns = if namespace.is_null() {
        ""
    } else {
        match std::ffi::CStr::from_ptr(namespace).to_str() {
            Ok(name) => name,
            Err(_e) => EXTISM_ENV_MODULE,
        }
    };

    let name = match std::ffi::CStr::from_ptr(name).to_str() {
        Ok(name) => name,
        Err(_e) => "memory",
    };

    match (&mut *linker).get(&mut *store, ns, name) {
        None => -1,
        Some(external) => match external.into_memory() {
            None => -1,
            Some(memory) => match memory.write(&mut *store, offset as usize, data) {
                Ok(()) => 0,
                Err(_) => -1,
            },
        },
    }
}

#[no_mangle]
pub unsafe extern "C" fn linear_memory_get(
    plugin: *mut CurrentPlugin,
    namespace: *const std::ffi::c_char,
    name: *const std::ffi::c_char,
) -> *mut u8 {
    let plugin = &mut *plugin;
    let (linker, store) = plugin.linker_and_store();

    internal_linear_memory_get(linker, store, namespace, name)
}

#[no_mangle]
pub unsafe extern "C" fn linear_memory_get_from_plugin(
    plugin: *mut Plugin,
    namespace: *const std::ffi::c_char,
    name: *const std::ffi::c_char,
) -> *mut u8 {
    let plugin = &mut *plugin;
    let (linker, store) = plugin.linker_and_store();

    internal_linear_memory_get(linker, store, namespace, name)
}

#[no_mangle]
pub unsafe extern "C" fn extension_plugin_error(plugin: *mut Plugin) -> *const c_char {
    if plugin.is_null() {
        return std::ptr::null();
    }
    let plugin = &mut *plugin;
    let _lock = plugin.instance.clone();
    let _lock = _lock.lock().unwrap();

    // panic!("{:?}", plugin.current_plugin_mut().extension_error);
    let err = plugin
        .current_plugin_mut()
        .extension_error
        .as_mut()
        .unwrap();
    let backtrace_string = format!("{:?}", err);
    let c_string = std::ffi::CString::new(backtrace_string).unwrap();
    c_string.into_raw()
    // .backtrace()
    // .as_ptr() as *const _
}

unsafe fn internal_linear_memory_get(
    linker: &mut Linker<CurrentPlugin>,
    store: &mut Store<CurrentPlugin>,
    namespace: *const std::ffi::c_char,
    name: *const std::ffi::c_char,
) -> *mut u8 {
    let name = match std::ffi::CStr::from_ptr(name).to_str() {
        Ok(x) => x.to_string(),
        Err(_e) => return std::ptr::null_mut(),
    };

    let namespace = match std::ffi::CStr::from_ptr(namespace).to_str() {
        Ok(x) => x.to_string(),
        Err(_e) => EXTISM_ENV_MODULE.to_string(),
    };

    match (&mut *linker).get(&mut *store, &namespace, &name) {
        None => std::ptr::null_mut(),
        Some(external) => match external.into_memory() {
            None => std::ptr::null_mut(),
            Some(memory) => memory.data_mut(&mut *store).as_mut_ptr(),
        },
    }
}

#[no_mangle]
pub unsafe extern "C" fn linear_memory_size(
    instance_ptr: *mut Plugin,
    namespace: *const c_char,
    name: *const c_char,
) -> usize {
    let plugin = &mut *instance_ptr;

    let (linker, store) = plugin.linker_and_store();

    let name = match std::ffi::CStr::from_ptr(name).to_str() {
        Ok(name) => name,
        Err(_e) => "",
    };

    let namespace = match std::ffi::CStr::from_ptr(namespace).to_str() {
        Ok(namespace) => namespace,
        Err(_e) => "",
    };

    match (&mut *linker).get(&mut *store, &namespace, &name) {
        None => 0 as usize,
        Some(external) => match external.into_memory() {
            None => 0 as usize,
            Some(memory) => memory
                .data(&mut *store)
                .iter()
                .position(|x| x.to_owned() == 0)
                .unwrap(),
        },
    }
}

#[no_mangle]
pub unsafe extern "C" fn linear_memory_reset_from_plugin(
    instance_ptr: *mut Plugin,
    namespace: *const c_char,
    name: *const c_char,
) {
    let plugin = &mut *instance_ptr;

    let (linker, store) = plugin.linker_and_store();

    let name = match std::ffi::CStr::from_ptr(name).to_str() {
        Ok(name) => name,
        Err(_e) => "",
    };

    let namespace = match std::ffi::CStr::from_ptr(namespace).to_str() {
        Ok(namespace) => namespace,
        Err(_e) => "",
    };

    match (&mut *linker).get(&mut *store, namespace, &name) {
        None => (),
        Some(external) => match external.into_memory() {
            None => (),
            Some(memory) => {
                // let memory_type = memory.ty(&store);
                // let mem = wasmtime::Memory::new(&mut *store, memory_type).unwrap();
                // let res = linker.define(&mut *store, namespace, name, mem);

                let size = memory.data_size(&store);
                let buffer = vec![0; size];
                let _ = memory.write(store, 0, &buffer);

                // panic!("{:#?}", res.er());
            }
        },
    };

    ()
}

#[no_mangle]
pub unsafe extern "C" fn custom_memory_get(plugin: *mut CurrentPlugin) -> *mut u8 {
    let plugin = &mut *plugin;

    let plugin = &mut *plugin;
    let plugin_memory = &mut *plugin.memory_export;

    plugin_memory
        .memory
        .data_mut(&mut *plugin_memory.store)
        .as_mut_ptr()
}

#[no_mangle]
pub unsafe extern "C" fn custom_memory_size_from_plugin(plugin: *mut Plugin) -> usize {
    let plugin = &mut *plugin;

    if plugin.linker_and_store().1.data().memory_export.is_null() {
        0 as usize
    } else {
        let plugin_memory = &mut *plugin.linker_and_store().1.data().memory_export;

        plugin_memory
            .memory
            .data(&mut *plugin_memory.store)
            .iter()
            .position(|x| x.to_owned() == 0)
            .unwrap_or_default()
    }
}

#[no_mangle]
pub unsafe extern "C" fn linear_memory_size_from_plugin(
    plugin: *mut Plugin,
    namespace: *const c_char,
    name: *const c_char,
) -> usize {
    let plugin = &mut *plugin;

    let (linker, store) = plugin.linker_and_store();

    let name = match std::ffi::CStr::from_ptr(name).to_str() {
        Ok(name) => name,
        Err(_e) => "",
    };

    let namespace = match std::ffi::CStr::from_ptr(namespace).to_str() {
        Ok(namespace) => namespace,
        Err(_e) => "",
    };

    match (&mut *linker).get(&mut *store, namespace, &name) {
        None => 0 as usize,
        Some(external) => match external.into_memory() {
            None => 0 as usize,
            Some(memory) => memory
                .data(&mut *store)
                .iter()
                .position(|x| x.to_owned() == 0)
                .unwrap(),
        },
    }
}

#[no_mangle]
pub unsafe extern "C" fn custom_memory_length(plugin: *mut CurrentPlugin, n: Size) -> Size {
    if plugin.is_null() {
        return 0;
    }

    let plugin = &mut *plugin;
    let plugin_memory = &mut *plugin.memory_export;

    match plugin_memory.block_length(n as usize) {
        Some(x) => x as Size,
        None => 0,
    }
}

#[no_mangle]
pub unsafe extern "C" fn custom_memory_free(plugin: *mut CurrentPlugin, ptr: u64) {
    if plugin.is_null() {
        return;
    }

    let plugin = &mut *plugin;
    let plugin_memory = &mut *plugin.memory_export;
    plugin_memory.free(ptr as usize);
}

#[no_mangle]
pub unsafe extern "C" fn custom_memory_alloc(plugin: *mut CurrentPlugin, n: Size) -> u64 {
    if plugin.is_null() {
        return 0;
    }

    let plugin = &mut *plugin;
    let plugin_memory = &mut *plugin.memory_export;
    let mem = match plugin_memory.alloc(n as usize) {
        Ok(x) => x,
        Err(_e) => return 0,
    };

    mem.offset as u64
}

#[no_mangle]
pub(crate) unsafe extern "C" fn extension_extism_plugin_new_with_memories(
    wasm: *const u8,
    wasm_size: Size,
    functions: *mut *const ExtismFunction,
    n_functions: Size,
    memories: *mut *const ExtismMemory,
    n_memories: i8,
    with_wasi: bool,
    errmsg: *mut *mut std::ffi::c_char,
) -> *mut Plugin {
    let mut mems: Vec<&WasmMemory> = vec![];

    if !memories.is_null() {
        for i in 0..n_memories {
            unsafe {
                let f = *memories.add(i as usize);
                if f.is_null() {
                    continue;
                }
                let f = &*f;
                mems.push(&f.0);
            }
        }
    }

    // extism_plugin_new(wasm, wasm_size, functions, n_functions, mems, with_wasi, errmsg)
    let data = std::slice::from_raw_parts(wasm, wasm_size as usize);
    let mut funcs = vec![];

    if !functions.is_null() {
        for i in 0..n_functions {
            unsafe {
                let f = *functions.add(i as usize);
                if f.is_null() {
                    continue;
                }
                if let Some(f) = (*f).0.take() {
                    funcs.push(f);
                } else {
                    let e = std::ffi::CString::new(
                        "Function cannot be registered with multiple different Plugins",
                    )
                    .unwrap();
                    *errmsg = e.into_raw();
                }
            }
        }
    }

    let memories: Vec<WasmMemory> = mems.iter().map(|mem| WasmMemory::new(
        mem.name.clone(),
        mem.namespace.clone(),
        mem.ty.minimum() as u32,
        mem.ty.maximum().map(|r| r as u32).unwrap_or(0)
    )).collect::<Vec<WasmMemory>>();

    let plugin = Plugin::new_with_memories(data, funcs, memories, with_wasi);
    match plugin {
        Err(e) => {
            if !errmsg.is_null() {
                let e = std::ffi::CString::new(format!("Unable to create Extism plugin: {}", e))
                    .unwrap();
                *errmsg = e.into_raw();
            }
            std::ptr::null_mut()
        }
        Ok(p) => Box::into_raw(Box::new(p)),
    }
}
