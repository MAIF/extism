typedef signed char int8_t;

typedef short int16_t;

typedef int int32_t;

typedef long long int64_t;

typedef unsigned char uint8_t;

typedef unsigned short uint16_t;

typedef unsigned int uint32_t;

typedef unsigned long long uint64_t;

typedef int8_t int_least8_t;

typedef int16_t int_least16_t;

typedef int32_t int_least32_t;

typedef int64_t int_least64_t;

typedef uint8_t uint_least8_t;

typedef uint16_t uint_least16_t;

typedef uint32_t uint_least32_t;

typedef uint64_t uint_least64_t;

typedef int8_t int_fast8_t;

typedef int16_t int_fast16_t;

typedef int32_t int_fast32_t;

typedef int64_t int_fast64_t;

typedef uint8_t uint_fast8_t;

typedef uint16_t uint_fast16_t;

typedef uint32_t uint_fast32_t;

typedef uint64_t uint_fast64_t;

typedef signed char __int8_t;

typedef unsigned char __uint8_t;

typedef short __int16_t;

typedef unsigned short __uint16_t;

typedef int __int32_t;

typedef unsigned int __uint32_t;

typedef long long __int64_t;

typedef unsigned long long __uint64_t;

typedef long __darwin_intptr_t;

typedef unsigned int __darwin_natural_t;

typedef int __darwin_ct_rune_t;

typedef union {

char __mbstate8[128];

long long _mbstateL;

} __mbstate_t;

typedef __mbstate_t __darwin_mbstate_t;

typedef long int __darwin_ptrdiff_t;

typedef long unsigned int __darwin_size_t;

typedef int __darwin_wchar_t;

typedef __darwin_wchar_t __darwin_rune_t;

typedef int __darwin_wint_t;

typedef unsigned long __darwin_clock_t;

typedef __uint32_t __darwin_socklen_t;

typedef long __darwin_ssize_t;

typedef long __darwin_time_t;

typedef __int64_t __darwin_blkcnt_t;

typedef __int32_t __darwin_blksize_t;

typedef __int32_t __darwin_dev_t;

typedef unsigned int __darwin_fsblkcnt_t;

typedef unsigned int __darwin_fsfilcnt_t;

typedef __uint32_t __darwin_gid_t;

typedef __uint32_t __darwin_id_t;

typedef __uint64_t __darwin_ino64_t;

typedef __darwin_ino64_t __darwin_ino_t;

typedef __darwin_natural_t __darwin_mach_port_name_t;

typedef __darwin_mach_port_name_t __darwin_mach_port_t;

typedef __uint16_t __darwin_mode_t;

typedef __int64_t __darwin_off_t;

typedef __int32_t __darwin_pid_t;

typedef __uint32_t __darwin_sigset_t;

typedef __int32_t __darwin_suseconds_t;

typedef __uint32_t __darwin_uid_t;

typedef __uint32_t __darwin_useconds_t;

typedef unsigned char __darwin_uuid_t[16];

typedef char __darwin_uuid_string_t[37];

struct __darwin_pthread_handler_rec {

void (*__routine)(void *);

void *__arg;

struct __darwin_pthread_handler_rec *__next;

};

struct _opaque_pthread_attr_t {

long __sig;

char __opaque[56];

};

struct _opaque_pthread_cond_t {

long __sig;

char __opaque[40];

};

struct _opaque_pthread_condattr_t {

long __sig;

char __opaque[8];

};

struct _opaque_pthread_mutex_t {

long __sig;

char __opaque[56];

};

struct _opaque_pthread_mutexattr_t {

long __sig;

char __opaque[8];

};

struct _opaque_pthread_once_t {

long __sig;

char __opaque[8];

};

struct _opaque_pthread_rwlock_t {

long __sig;

char __opaque[192];

};

struct _opaque_pthread_rwlockattr_t {

long __sig;

char __opaque[16];

};

struct _opaque_pthread_t {

long __sig;

struct __darwin_pthread_handler_rec *__cleanup_stack;

char __opaque[8176];

};

typedef struct _opaque_pthread_attr_t __darwin_pthread_attr_t;

typedef struct _opaque_pthread_cond_t __darwin_pthread_cond_t;

typedef struct _opaque_pthread_condattr_t __darwin_pthread_condattr_t;

typedef unsigned long __darwin_pthread_key_t;

typedef struct _opaque_pthread_mutex_t __darwin_pthread_mutex_t;

typedef struct _opaque_pthread_mutexattr_t __darwin_pthread_mutexattr_t;

typedef struct _opaque_pthread_once_t __darwin_pthread_once_t;

typedef struct _opaque_pthread_rwlock_t __darwin_pthread_rwlock_t;

typedef struct _opaque_pthread_rwlockattr_t __darwin_pthread_rwlockattr_t;

typedef struct _opaque_pthread_t *__darwin_pthread_t;

typedef unsigned char u_int8_t;

typedef unsigned short u_int16_t;

typedef unsigned int u_int32_t;

typedef unsigned long long u_int64_t;

typedef int64_t register_t;

typedef unsigned long uintptr_t;

typedef u_int64_t user_addr_t;

typedef u_int64_t user_size_t;

typedef int64_t user_ssize_t;

typedef int64_t user_long_t;

typedef u_int64_t user_ulong_t;

typedef int64_t user_time_t;

typedef int64_t user_off_t;

typedef u_int64_t syscall_arg_t;

typedef __darwin_intptr_t intptr_t;

typedef long int intmax_t;

typedef long unsigned int uintmax_t;

typedef enum {

I32,

I64,

F32,

F64,

V128,

FuncRef,

ExternRef,

} ExtismValType;

typedef struct ExtismCancelHandle ExtismCancelHandle;

typedef struct ExtismCurrentPlugin ExtismCurrentPlugin;

typedef struct ExtismFunction ExtismFunction;

typedef struct ExtismMemory ExtismMemory;

typedef struct ExtismPlugin ExtismPlugin;

typedef struct PluginTemplate PluginTemplate;

typedef struct WasmPlugin WasmPlugin;

typedef uint64_t ExtismSize;

typedef union {

int32_t i32;

int64_t i64;

float f32;

double f64;

} ExtismValUnion;

typedef struct {

ExtismValType t;

ExtismValUnion v;

} ExtismVal;

typedef void (*OtoroshiFunctionType)(WasmPlugin *plugin,

const ExtismVal *inputs,

ExtismSize n_inputs,

ExtismVal *outputs,

ExtismSize n_outputs,

void *data);

typedef uint64_t ExtismMemoryHandle;

typedef void (*ExtismFunctionType)(ExtismCurrentPlugin *plugin,

const ExtismVal *inputs,

ExtismSize n_inputs,

ExtismVal *outputs,

ExtismSize n_outputs,

void *data);

PluginTemplate *wasm_otoroshi_create_template_new(Engine *engine_ptr,

const uint8_t *wasm,

ExtismSize wasm_size);

WasmPlugin *wasm_otoroshi_instantiate(Engine *engine_ptr,

PluginTemplate *template_ptr,

const ExtismFunction **functions,

ExtismSize n_functions,

const ExtismMemory **memories,

int8_t n_memories,

_Bool with_wasi);

uint8_t *get_custom_data(WasmPlugin *instance_ptr);

Engine *wasm_otoroshi_create_wasmtime_engine(void);

void wasm_otoroshi_free_engine(Engine *engine);

void wasm_otoroshi_free_template(PluginTemplate *template_);

void wasm_otoroshi_free_function(ExtismFunction *ptr);

int32_t wasm_otoroshi_bridge_extism_plugin_call(WasmPlugin *instance_ptr,

const char *func_name,

const uint8_t *data,

ExtismSize data_len);

ExtismSize wasm_otoroshi_bridge_extism_plugin_output_length(WasmPlugin *instance_ptr);

const uint8_t *wasm_otoroshi_bridge_extism_plugin_output_data(WasmPlugin *instance_ptr);

void wasm_otoroshi_deallocate_results(ExtismVal *ptr, uintptr_t len);

void wasm_otoroshi_free_plugin(WasmPlugin *ptr);

ExtismVal *wasm_otoroshi_call(WasmPlugin *instance_ptr,

const char *func_name,

const ExtismVal *params,

ExtismSize n_params);

ExtismVal *wasm_otoroshi_wasm_plugin_call_without_params(WasmPlugin *plugin_ptr,

const char *func_name);

void wasm_otoroshi_wasm_plugin_call_without_results(WasmPlugin *plugin_ptr,

const char *func_name,

const ExtismVal *params,

ExtismSize n_params);

ExtismMemory *wasm_otoroshi_create_wasmtime_memory(const char *name,

const char *namespace_,

uint32_t min_pages,

uint32_t max_pages);

void wasm_otoroshi_free_memory(ExtismMemory *mem);

uint8_t *wasm_otoroshi_extism_get_lineary_memory_from_host_functions(WasmPlugin *plugin,

const char *name);

int8_t *wasm_otoroshi_instance_error(WasmPlugin *instance_ptr);

void wasm_otoroshi_extism_reset(WasmPlugin *instance_ptr);

int8_t wasm_otoroshi_extism_memory_write_bytes(WasmPlugin *instance_ptr,

const uint8_t *data,

ExtismSize data_size,

uint32_t offset);

uint8_t *wasm_otoroshi_extism_get_memory(WasmPlugin *instance_ptr, const char *name);

ExtismFunction *wasm_otoroshi_extism_function_new(const char *name,

const ExtismValType *inputs,

ExtismSize n_inputs,

const ExtismValType *outputs,

ExtismSize n_outputs,

OtoroshiFunctionType func,

void *user_data,

void (*free_user_data)(void *_));

uint64_t wasm_otoroshi_extism_current_plugin_memory_alloc(WasmPlugin *instance_ptr, ExtismSize n);

ExtismSize wasm_otoroshi_extism_current_plugin_memory_length(WasmPlugin *instance_ptr,

ExtismSize n);

void wasm_otoroshi_extism_current_plugin_memory_free(WasmPlugin *instance_ptr, uint64_t ptr);

uintptr_t wasm_otoroshi_extism_memory_bytes(WasmPlugin *instance_ptr);

const uint8_t *extism_plugin_id(ExtismPlugin *plugin);

uint8_t *extism_current_plugin_memory(ExtismCurrentPlugin *plugin);

ExtismMemoryHandle extism_current_plugin_memory_alloc(ExtismCurrentPlugin *plugin, ExtismSize n);

ExtismSize extism_current_plugin_memory_length(ExtismCurrentPlugin *plugin, ExtismMemoryHandle n);

void extism_current_plugin_memory_free(ExtismCurrentPlugin *plugin, ExtismMemoryHandle ptr);

ExtismFunction *extism_function_new(const char *name,

const ExtismValType *inputs,

ExtismSize n_inputs,

const ExtismValType *outputs,

ExtismSize n_outputs,

ExtismFunctionType func,

void *user_data,

void (*free_user_data)(void *_));

void extism_function_free(ExtismFunction *f);

void extism_function_set_namespace(ExtismFunction *ptr, const char *namespace_);

ExtismPlugin *extism_plugin_new(const uint8_t *wasm,

ExtismSize wasm_size,

const ExtismFunction **functions,

ExtismSize n_functions,

_Bool with_wasi,

char **errmsg);

void extism_plugin_new_error_free(char *err);

void extism_plugin_free(ExtismPlugin *plugin);

const ExtismCancelHandle *extism_plugin_cancel_handle(const ExtismPlugin *plugin);

_Bool extism_plugin_cancel(const ExtismCancelHandle *handle);

_Bool extism_plugin_config(ExtismPlugin *plugin, const uint8_t *json, ExtismSize json_size);

_Bool extism_plugin_function_exists(ExtismPlugin *plugin, const char *func_name);

int32_t extism_plugin_call(ExtismPlugin *plugin,

const char *func_name,

const uint8_t *data,

ExtismSize data_len);

const char *extism_error(ExtismPlugin *plugin);

const char *extism_plugin_error(ExtismPlugin *plugin);

ExtismSize extism_plugin_output_length(ExtismPlugin *plugin);

const uint8_t *extism_plugin_output_data(ExtismPlugin *plugin);

_Bool extism_log_file(const char *filename, const char *log_level);

_Bool extism_log_custom(const char *log_level);

void extism_log_drain(void (*handler)(const char*, uintptr_t));

const char *extism_version(void);

void extism_reset(Context *ctx, PluginIndex plugin);