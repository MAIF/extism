#pragma once

#include <stdint.h>
#include <stdbool.h>

#define EXTISM_FUNCTION(N) extern void N(ExtismCurrentPlugin*, const ExtismVal*, ExtismSize, ExtismVal*, ExtismSize, void*)
#define EXTISM_GO_FUNCTION(N) extern void N(void*, ExtismVal*, ExtismSize, ExtismVal*, ExtismSize, uintptr_t)

/** The return code from extism_plugin_call used to signal a successful call with no errors */
#define EXTISM_SUCCESS 0

/** An alias for I64 to signify an Extism pointer */
#define EXTISM_PTR ExtismValType_I64


/**
 * An enumeration of all possible value types in WebAssembly.
 */
typedef enum {
  /**
   * Signed 32 bit integer.
   */
  ExtismValType_I32,
  /**
   * Signed 64 bit integer.
   */
  ExtismValType_I64,
  /**
   * Floating point 32 bit integer.
   */
  ExtismValType_F32,
  /**
   * Floating point 64 bit integer.
   */
  ExtismValType_F64,
  /**
   * A 128 bit number.
   */
  ExtismValType_V128,
  /**
   * A reference to a Wasm function.
   */
  ExtismValType_FuncRef,
  /**
   * A reference to opaque data in the Wasm instance.
   */
  ExtismValType_ExternRef,
} ExtismValType;

/**
 * A `CancelHandle` can be used to cancel a running plugin from another thread
 */
typedef struct ExtismCancelHandle ExtismCancelHandle;

typedef struct ExtismCompiledPlugin ExtismCompiledPlugin;

/**
 * CurrentPlugin stores data that is available to the caller in PDK functions, this should
 * only be accessed from inside a host function
 */
typedef struct ExtismCurrentPlugin ExtismCurrentPlugin;

typedef struct ExtismFunction ExtismFunction;

typedef struct ExtismMemory ExtismMemory;

/**
 * Plugin contains everything needed to execute a WASM function
 */
typedef struct ExtismPlugin ExtismPlugin;

/**
 * A union type for host function argument/return values
 */
typedef union {
  int32_t i32;
  int64_t i64;
  float f32;
  double f64;
} ExtismValUnion;

/**
 * `ExtismVal` holds the type and value of a function argument/return
 * TODO - ADDED
 */
typedef struct {
  ExtismValType t;
  ExtismValUnion v;
} ExtismVal;

typedef uint64_t ExtismSize;

typedef uint64_t ExtismMemoryHandle;

/**
 * Host function signature
 */
typedef void (*ExtismFunctionType)(ExtismCurrentPlugin *plugin,
                                   const ExtismVal *inputs,
                                   ExtismSize n_inputs,
                                   ExtismVal *outputs,
                                   ExtismSize n_outputs,
                                   void *data);

/**
 * Log drain callback
 */
typedef void (*ExtismLogDrainFunctionType)(const char *data, ExtismSize size);



#ifdef __cplusplus
extern "C" {
#endif // __cplusplus

void extension_deallocate_results(ExtismVal *ptr, uintptr_t len);

ExtismVal *extension_call(ExtismPlugin *plugin,
                          const char *func_name,
                          const ExtismVal *params,
                          ExtismSize n_params);

void initialize_coraza(ExtismPlugin *plugin, const uint8_t *data, ExtismSize data_size);

uint8_t *coraza_new_transaction(ExtismPlugin *plugin, const uint8_t *data, ExtismSize data_size);

uint8_t *process_response_transaction(ExtismPlugin *plugin,
                                      const uint8_t *data,
                                      ExtismSize data_size);

uint8_t *coraza_errors(ExtismPlugin *plugin);

uint8_t *coraza_flow(ExtismPlugin *plugin);

ExtismVal *wasm_plugin_call_without_params(ExtismPlugin *plugin_ptr, const char *func_name);

void wasm_plugin_call_without_results(ExtismPlugin *plugin_ptr,
                                      const char *func_name,
                                      const ExtismVal *params,
                                      ExtismSize n_params);

ExtismMemory *extension_create_wasmtime_memory(const char *name,
                                               const char *namespace_,
                                               uint32_t min_pages,
                                               uint32_t max_pages);

/**
 * Remove all plugins from the registry
 */
void custom_memory_reset_from_plugin(ExtismPlugin *plugin);

int8_t extension_extism_memory_write_bytes(ExtismPlugin *instance_ptr,
                                           const uint8_t *data,
                                           ExtismSize data_size,
                                           uint32_t offset,
                                           const char *namespace_,
                                           const char *name);

uint8_t *linear_memory_get(ExtismCurrentPlugin *plugin, const char *namespace_, const char *name);

uint8_t *linear_memory_get_from_plugin(ExtismPlugin *plugin,
                                       const char *namespace_,
                                       const char *name);

const char *extension_plugin_error(ExtismPlugin *plugin);

uintptr_t linear_memory_size(ExtismPlugin *instance_ptr, const char *namespace_, const char *name);

void linear_memory_reset_from_plugin(ExtismPlugin *instance_ptr,
                                     const char *namespace_,
                                     const char *name);

uint8_t *custom_memory_get(ExtismCurrentPlugin *plugin);

uintptr_t custom_memory_size_from_plugin(ExtismPlugin *plugin);

uintptr_t linear_memory_size_from_plugin(ExtismPlugin *plugin,
                                         const char *namespace_,
                                         const char *name);

ExtismSize custom_memory_length(ExtismCurrentPlugin *plugin, ExtismSize n);

void custom_memory_free(ExtismCurrentPlugin *plugin, uint64_t ptr);

uint64_t custom_memory_alloc(ExtismCurrentPlugin *plugin, ExtismSize n);

ExtismPlugin *extension_extism_plugin_new_with_memories(const uint8_t *wasm,
                                                        ExtismSize wasm_size,
                                                        const ExtismFunction **functions,
                                                        ExtismSize n_functions,
                                                        const ExtismMemory **memories,
                                                        int8_t n_memories,
                                                        bool with_wasi,
                                                        char **errmsg);

/**
 * Get a plugin's ID, the returned bytes are a 16 byte buffer that represent a UUIDv4
 */
const uint8_t *extism_plugin_id(ExtismPlugin *plugin);

/**
 * Get the current plugin's associated host context data. Returns null if call was made without
 * host context.
 */
void *extism_current_plugin_host_context(ExtismCurrentPlugin *plugin);

/**
 * Returns a pointer to the memory of the currently running plugin
 * NOTE: this should only be called from host functions.
 */
uint8_t *extism_current_plugin_memory(ExtismCurrentPlugin *plugin);

/**
 * Allocate a memory block in the currently running plugin
 * NOTE: this should only be called from host functions.
 */
ExtismMemoryHandle extism_current_plugin_memory_alloc(ExtismCurrentPlugin *plugin, ExtismSize n);

/**
 * Get the length of an allocated block
 * NOTE: this should only be called from host functions.
 */
ExtismSize extism_current_plugin_memory_length(ExtismCurrentPlugin *plugin, ExtismMemoryHandle n);

/**
 * Free an allocated memory block
 * NOTE: this should only be called from host functions.
 */
void extism_current_plugin_memory_free(ExtismCurrentPlugin *plugin, ExtismMemoryHandle ptr);

/**
 * Create a new host function
 *
 * Arguments
 * - `name`: function name, this should be valid UTF-8
 * - `inputs`: argument types
 * - `n_inputs`: number of argument types
 * - `outputs`: return types
 * - `n_outputs`: number of return types
 * - `func`: the function to call
 * - `user_data`: a pointer that will be passed to the function when it's called
 *    this value should live as long as the function exists
 * - `free_user_data`: a callback to release the `user_data` value when the resulting
 *   `ExtismFunction` is freed.
 *
 * Returns a new `ExtismFunction` or `null` if the `name` argument is invalid.
 */
ExtismFunction *extism_function_new(const char *name,
                                    const ExtismValType *inputs,
                                    ExtismSize n_inputs,
                                    const ExtismValType *outputs,
                                    ExtismSize n_outputs,
                                    ExtismFunctionType func,
                                    void *user_data,
                                    void (*free_user_data)(void *_));

/**
 * Free `ExtismFunction`
 */
void extism_function_free(ExtismFunction *f);

/**
 * Set the namespace of an `ExtismFunction`
 */
void extism_function_set_namespace(ExtismFunction *ptr, const char *namespace_);

/**
 * Pre-compile an Extism plugin
 */
ExtismCompiledPlugin *extism_compiled_plugin_new(const uint8_t *wasm,
                                                 ExtismSize wasm_size,
                                                 const ExtismFunction **functions,
                                                 ExtismSize n_functions,
                                                 bool with_wasi,
                                                 char **errmsg);

/**
 * Free `ExtismCompiledPlugin`
 */
void extism_compiled_plugin_free(ExtismCompiledPlugin *plugin);

/**
 * Create a new plugin with host functions, the functions passed to this function no longer need to be manually freed using
 *
 * `wasm`: is a WASM module (wat or wasm) or a JSON encoded manifest
 * `wasm_size`: the length of the `wasm` parameter
 * `functions`: an array of `ExtismFunction*`
 * `n_functions`: the number of functions provided
 * `with_wasi`: enables/disables WASI
 */
ExtismPlugin *extism_plugin_new(const uint8_t *wasm,
                                ExtismSize wasm_size,
                                const ExtismFunction **functions,
                                ExtismSize n_functions,
                                bool with_wasi,
                                char **errmsg);

/**
 * Create a new plugin from an `ExtismCompiledPlugin`
 */
ExtismPlugin *extism_plugin_new_from_compiled(const ExtismCompiledPlugin *compiled, char **errmsg);

/**
 * Create a new plugin and set the number of instructions a plugin is allowed to execute
 */
ExtismPlugin *extism_plugin_new_with_fuel_limit(const uint8_t *wasm,
                                                ExtismSize wasm_size,
                                                const ExtismFunction **functions,
                                                ExtismSize n_functions,
                                                bool with_wasi,
                                                uint64_t fuel_limit,
                                                char **errmsg);

/**
 * Enable HTTP response headers in plugins using `extism:host/env::http_request`
 */
void extism_plugin_allow_http_response_headers(ExtismPlugin *plugin);

/**
 * Free the error returned by `extism_plugin_new`, errors returned from `extism_plugin_error` don't need to be freed
 */
void extism_plugin_new_error_free(char *err);

/**
 * Free `ExtismPlugin`
 */
void extism_plugin_free(ExtismPlugin *plugin);

/**
 * Get handle for plugin cancellation
 */
const ExtismCancelHandle *extism_plugin_cancel_handle(const ExtismPlugin *plugin);

/**
 * Cancel a running plugin
 */
bool extism_plugin_cancel(const ExtismCancelHandle *handle);

/**
 * Update plugin config values.
 */
bool extism_plugin_config(ExtismPlugin *plugin, const uint8_t *json, ExtismSize json_size);

/**
 * Returns true if `func_name` exists
 */
bool extism_plugin_function_exists(ExtismPlugin *plugin, const char *func_name);

/**
 * Call a function
 *
 * `func_name`: is the function to call
 * `data`: is the input data
 * `data_len`: is the length of `data`
 */
int32_t extism_plugin_call(ExtismPlugin *plugin,
                           const char *func_name,
                           const uint8_t *data,
                           ExtismSize data_len);

/**
 * Call a function with host context.
 *
 * `func_name`: is the function to call
 * `data`: is the input data
 * `data_len`: is the length of `data`
 * `host_context`: a pointer to context data that will be available in host functions
 */
int32_t extism_plugin_call_with_host_context(ExtismPlugin *plugin,
                                             const char *func_name,
                                             const uint8_t *data,
                                             ExtismSize data_len,
                                             void *host_context,
                                             bool retry);

void extism_reset_store(ExtismPlugin *plugin);

/**
 * Get the error associated with a `Plugin`
 */
const char *extism_error(ExtismPlugin *plugin);

/**
 * Get the error associated with a `Plugin`
 */
const char *extism_plugin_error(ExtismPlugin *plugin);

/**
 * Get the length of a plugin's output data
 */
ExtismSize extism_plugin_output_length(ExtismPlugin *plugin);

/**
 * Get a pointer to the output data
 */
const uint8_t *extism_plugin_output_data(ExtismPlugin *plugin);

/**
 * Set log file and level.
 * The log level can be either one of: info, error, trace, debug, warn or a more
 * complex filter like `extism=trace,cranelift=debug`
 * The file will be created if it doesn't exist.
 */
bool extism_log_file(const char *filename, const char *log_level);

/**
 * Enable a custom log handler, this will buffer logs until `extism_log_drain` is called
 * Log level should be one of: info, error, trace, debug, warn
 */
bool extism_log_custom(const char *log_level);

/**
 * Calls the provided callback function for each buffered log line.
 * This is only needed when `extism_log_custom` is used.
 */
void extism_log_drain(ExtismLogDrainFunctionType handler);

/**
 * Reset the Extism runtime, this will invalidate all allocated memory
 */
bool extism_plugin_reset(ExtismPlugin *raw_plugin);

/**
 * Get the Extism version string
 */
const char *extism_version(void);

#ifdef __cplusplus
}  // extern "C"
#endif  // __cplusplus
