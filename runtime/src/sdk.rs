#![allow(clippy::missing_safety_doc)]

use std::os::raw::c_char;

use crate::*;

/// A union type for host function argument/return values
#[repr(C)]
#[derive(Copy, Clone)]
pub union ValUnion {
    i32: i32,
    i64: i64,
    f32: f32,
    f64: f64,
    // TODO: v128, ExternRef, FuncRef
}

/// `ExtismVal` holds the type and value of a function argument/return
#[repr(C)]
#[derive(Clone)]
pub struct ExtismVal {
    t: ValType,
    v: ValUnion,
}

/// Wraps host functions
pub struct ExtismFunction(Function);

pub struct ExtismMemory(WasmMemory);

impl From<Function> for ExtismFunction {
    fn from(x: Function) -> Self {
        ExtismFunction(x)
    }
}

/// Host function signature
pub type ExtismFunctionType = extern "C" fn(
    plugin: *mut Internal,
    inputs: *const ExtismVal,
    n_inputs: Size,
    outputs: *mut ExtismVal,
    n_outputs: Size,
    data: *mut std::ffi::c_void,
);

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
            // t => todo!("{}", t),
            t => ExtismVal {
                t: ValType::I32,
                v: ValUnion { i32: 1 },
            },
        }
    }
}

#[no_mangle]
pub unsafe extern "C" fn create_template_new(
    engine_ptr: *mut Engine,
    wasm: *const u8,
    wasm_size: Size,
) -> *mut PluginTemplate {
    let engine: &Engine = unsafe { &*engine_ptr };

    let data = std::slice::from_raw_parts(wasm, wasm_size as usize);
    Box::into_raw(Box::new(PluginTemplate::new(engine, data).unwrap()))
}

#[no_mangle]
pub unsafe extern "C" fn instantiate(
    engine_ptr: *mut Engine,
    template_ptr: *mut PluginTemplate,
    functions: *mut *const ExtismFunction,
    n_functions: Size,
    memories: *mut *const ExtismMemory,
    n_memories: i8,
    with_wasi: bool,
) -> *mut Plugin {
    let engine: &Engine = unsafe { &*engine_ptr };

    let template: &PluginTemplate = unsafe { &*template_ptr };

    let mut funcs = vec![];

    if !functions.is_null() {
        for i in 0..n_functions {
            unsafe {
                let f = *functions.add(i as usize);
                if f.is_null() {
                    continue;
                }
                let f = &*f;
                funcs.push(&f.0);
            }
        }
    }

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

    Box::into_raw(Box::new(
        template
            .instantiate(engine, funcs, mems, with_wasi)
            .unwrap(),
    ))
}

#[no_mangle]
pub extern "C" fn create_wasmtime_engine() -> *mut Engine {
    let engine = Engine::new(
        &Config::new(), // .debug_info(true)
    )
    .unwrap();
    Box::into_raw(Box::new(engine))
}

#[no_mangle]
pub extern "C" fn free_engine(engine: *mut Engine) {
    unsafe {
        drop(Box::from_raw(engine));
    }
}

#[no_mangle]
pub unsafe extern "C" fn extism_function_new(
    name: *const std::ffi::c_char,
    inputs: *const ValType,
    n_inputs: Size,
    outputs: *const ValType,
    n_outputs: Size,
    func: ExtismFunctionType,
    user_data: *mut std::ffi::c_void,
    free_user_data: Option<extern "C" fn(_: *mut std::ffi::c_void)>,
) -> *mut ExtismFunction {
    let name = match std::ffi::CStr::from_ptr(name).to_str() {
        Ok(x) => x.to_string(),
        Err(_) => {
            return std::ptr::null_mut();
        }
    };

    let inputs = if inputs.is_null() || n_inputs == 0 {
        &[]
    } else {
        std::slice::from_raw_parts(inputs, n_inputs as usize)
    }
    .to_vec();

    let output_types = if outputs.is_null() || n_outputs == 0 {
        &[]
    } else {
        std::slice::from_raw_parts(outputs, n_outputs as usize)
    }
    .to_vec();

    let user_data = UserData::new_pointer(user_data, free_user_data);
    let f = Function::new(
        name,
        inputs,
        output_types.clone(),
        Some(user_data),
        move |plugin, inputs, outputs, user_data| {
            let inputs: Vec<_> = inputs.iter().map(ExtismVal::from).collect();
            let mut output_tmp: Vec<_> = output_types
                .iter()
                .map(|t| ExtismVal {
                    t: t.clone(),
                    v: ValUnion { i64: 0 },
                })
                .collect();

            func(
                plugin,
                inputs.as_ptr(),
                inputs.len() as Size,
                output_tmp.as_mut_ptr(),
                output_tmp.len() as Size,
                user_data.as_ptr(),
            );

            for (tmp, out) in output_tmp.iter().zip(outputs.iter_mut()) {
                match tmp.t {
                    ValType::I32 => *out = Val::I32(tmp.v.i32),
                    ValType::I64 => *out = Val::I64(tmp.v.i64),
                    ValType::F32 => *out = Val::F32(tmp.v.f32 as u32),
                    ValType::F64 => *out = Val::F64(tmp.v.f64 as u64),
                    _ => todo!(),
                }
            }
            Ok(())
        },
    );
    Box::into_raw(Box::new(ExtismFunction(f)))
}

#[no_mangle]
pub unsafe extern "C" fn extism_function_set_namespace(
    ptr: *mut ExtismFunction,
    namespace: *const std::ffi::c_char,
) {
    let namespace = std::ffi::CStr::from_ptr(namespace);
    let f = &mut *ptr;
    f.0.set_namespace(namespace.to_string_lossy().to_string());
}

/// Free an `ExtismFunction`
#[no_mangle]
pub unsafe extern "C" fn free_function(ptr: *mut ExtismFunction) {
    drop(Box::from_raw(ptr))
}

/// Call a function
///
/// `func_name`: is the function to call
/// `data`: is the input data
/// `data_len`: is the length of `data`
#[no_mangle]
pub unsafe extern "C" fn extism_plugin_call(
    instance_ptr: *mut Plugin,
    func_name: *const c_char,
    data: *const u8,
    data_len: Size,
) -> i32 {
    let plugin = &mut *instance_ptr;

    plugin.set_input(data, data_len as usize);

    // Find function
    let name = std::ffi::CStr::from_ptr(func_name).to_str().unwrap();

    let func = plugin.get_func(name).unwrap();

    // Call the function
    let mut results = vec![wasmtime::Val::null(); 1];
    let res = func.call(&mut plugin.memory.store, &[], results.as_mut_slice());

    match res {
        Ok(()) => (),
        Err(e) => {
            let plugin = plugin;
            if let Some(exit) = e.downcast_ref::<wasmtime_wasi::I32Exit>() {
                trace!("WASI return code: {}", exit.0);
                if exit.0 != 0 {
                    return plugin.error(&e, exit.0);
                }
                return exit.0;
            }

            if e.root_cause().to_string() == "timeout" {
                return plugin.error("timeout", -1);
            }

            error!("Call: {e:?}");
            return plugin.error(e.context("Call failed"), -1);
        }
    };

    // If `results` is empty and the return value wasn't a WASI exit code then
    // the call succeeded
    if results.is_empty() {
        return 0;
    }

    // Return result to caller
    results[0].unwrap_i32()
}

#[no_mangle]
pub unsafe fn extism_plugin_call_native(
    instance_ptr: *mut Plugin,
    func_name: *const c_char,
    params: Vec<Val>,
) -> Option<Vec<Val>> {
    let name = std::ffi::CStr::from_ptr(func_name).to_str().unwrap();

    let instance = &mut *instance_ptr;

    let func = instance.get_func(name).unwrap();

    let n_results = func.ty(&instance.memory.store).results().len();

    let mut results = vec![wasmtime::Val::null(); n_results];

    func.call(
        &mut instance.memory.store,
        &params[..],
        results.as_mut_slice(),
    );

    Some(results)
}

#[no_mangle]
pub unsafe extern "C" fn deallocate_results(ptr: *mut ExtismVal, len: usize) {
    let len = len as usize;
    drop(Vec::from_raw_parts(ptr, len, len));
}

#[no_mangle]
pub unsafe extern "C" fn free_plugin(ptr: *mut Plugin) {
    unsafe {
        drop(Box::from_raw(ptr));
    }
}

#[no_mangle]
pub unsafe extern "C" fn call(
    instance_ptr: *mut Plugin,
    func_name: *const c_char,
    params: *const ExtismVal,
    n_params: Size,
) -> *mut ExtismVal {
    let prs = std::slice::from_raw_parts(params, n_params as usize).to_vec();

    let p: Vec<Val> = prs
        .iter()
        .map(|x| {
            let t = match x.t {
                ValType::I32 => Val::I32(x.v.i32),
                ValType::I64 => Val::I64(x.v.i64),
                ValType::F32 => Val::F32(x.v.f32 as u32),
                ValType::F64 => Val::F64(x.v.f64 as u64),
                _ => todo!(),
            };
            t
        })
        .collect();

    match extism_plugin_call_native(instance_ptr, func_name, p) {
        None => std::ptr::null_mut(),
        Some(t) => {
            // std::ptr::null_mut()
            let mut v = t
                .iter()
                .map(|x| {
                    let t = ExtismVal::from(x);
                    t
                })
                .collect::<Vec<ExtismVal>>();

            let ptr = v.as_mut_ptr() as *mut _;
            std::mem::forget(v);
            ptr
        }
    }
}

#[no_mangle]
pub unsafe extern "C" fn wasm_plugin_call_without_params(
    plugin_ptr: *mut Plugin,
    func_name: *const c_char,
) -> *mut ExtismVal {
    match extism_plugin_call_native(plugin_ptr, func_name, Vec::new()) {
        None => std::ptr::null_mut(),
        Some(t) => {
            // std::ptr::null_mut()
            let mut v = t
                .iter()
                .map(|x| {
                    let t = ExtismVal::from(x);
                    t
                })
                .collect::<Vec<ExtismVal>>();

            let ptr = v.as_mut_ptr() as *mut _;
            std::mem::forget(v);
            ptr
        }
    }
}

#[no_mangle]
pub unsafe extern "C" fn wasm_plugin_call_without_results(
    plugin_ptr: *mut Plugin,
    func_name: *const c_char,
    params: *const ExtismVal,
    n_params: Size,
) {
    let prs = std::slice::from_raw_parts(params, n_params as usize);

    let p: Vec<Val> = prs
        .iter()
        .map(|x| {
            let t = match x.t {
                ValType::I32 => Val::I32(x.v.i32),
                ValType::I64 => Val::I64(x.v.i64),
                ValType::F32 => Val::F32(x.v.f32 as u32),
                ValType::F64 => Val::F64(x.v.f64 as u64),
                _ => todo!(),
            };
            t
        })
        .collect();

    extism_plugin_call_native(plugin_ptr, func_name, p);
}

#[no_mangle]
pub unsafe extern "C" fn create_wasmtime_memory(
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

#[no_mangle]
pub extern "C" fn free_memory(mem: *mut ExtismMemory) {
    unsafe {
        drop(Box::from_raw(mem));
    }
}

#[no_mangle]
pub unsafe extern "C" fn extism_get_lineary_memory_from_host_functions(
    plugin: *mut Plugin,
    name: *const std::ffi::c_char,
) -> *mut u8 {
    let plugin = &mut *plugin;

    let name = match std::ffi::CStr::from_ptr(name).to_str() {
        Ok(x) => x.to_string(),
        Err(e) => return plugin.error(e, std::ptr::null_mut()),
    };

    let memory = match plugin.get_memory(name) {
        Some(x) => x,
        None => return plugin.error(format!("Memory not found: memory"), std::ptr::null_mut()),
    };

    memory.data_mut(&mut plugin.memory.store).as_mut_ptr()
}

/// Get the length of a plugin's output data
#[no_mangle]
pub unsafe extern "C" fn extism_plugin_output_length(instance_ptr: *mut Plugin) -> Size {
    let plugin = &mut *instance_ptr;

    plugin.memory.store.data().output_length as Size
}

/// Get the length of a plugin's output data
#[no_mangle]
pub unsafe extern "C" fn extism_plugin_output_data(instance_ptr: *mut Plugin) -> *const u8 {
    let plugin = &mut *instance_ptr;

    let data = plugin.memory.store.data();

    plugin
        .memory
        .ptr(MemoryBlock::new(data.output_offset, data.output_length))
        .map(|x| x as *const _)
        .unwrap_or(std::ptr::null())
}

/// Returns a pointer to the memory of the currently running plugin
/// NOTE: this should only be called from host functions.
#[no_mangle]
pub unsafe extern "C" fn extism_current_plugin_memory(plugin: *mut Internal) -> *mut u8 {
    if plugin.is_null() {
        return std::ptr::null_mut();
    }

    let plugin = &mut *plugin;
    plugin.memory_mut().data_mut().as_mut_ptr()
}

/// Allocate a memory block in the currently running plugin
/// NOTE: this should only be called from host functions.
#[no_mangle]
pub unsafe extern "C" fn extism_current_plugin_memory_alloc(plugin: *mut Internal, n: Size) -> u64 {
    if plugin.is_null() {
        return 0;
    }

    let plugin = &mut *plugin;

    let mem = match plugin.memory_mut().alloc(n as usize) {
        Ok(x) => x,
        Err(e) => {
            plugin.set_error(e);
            return 0;
        }
    };

    mem.offset as u64
}

/// Get the length of an allocated block
/// NOTE: this should only be called from host functions.
#[no_mangle]
pub unsafe extern "C" fn extism_current_plugin_memory_length(
    plugin: *mut Internal,
    n: Size,
) -> Size {
    if plugin.is_null() {
        return 0;
    }

    let plugin = &mut *plugin;

    match plugin.memory().block_length(n as usize) {
        Some(x) => x as Size,
        None => 0,
    }
}

/// Free an allocated memory block
/// NOTE: this should only be called from host functions.
#[no_mangle]
pub unsafe extern "C" fn extism_current_plugin_memory_free(plugin: *mut Internal, ptr: u64) {
    if plugin.is_null() {
        return;
    }

    let plugin = &mut *plugin;
    plugin.memory_mut().free(ptr as usize);
}
