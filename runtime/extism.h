#pragma once

#include <stdint.h>
#include <stdbool.h>

#define EXTISM_FUNCTION(N) extern void N(ExtismCurrentPlugin*, const ExtismVal*, ExtismSize, ExtismVal*, ExtismSize, void*)
#define EXTISM_GO_FUNCTION(N) extern void N(void*, ExtismVal*, ExtismSize, ExtismVal*, ExtismSize, uintptr_t)


/**
 * A list of all possible value types in WebAssembly.
 */
typedef enum {
  /**
   * Signed 32 bit integer.
   */
  I32,
  /**
   * Signed 64 bit integer.
   */
  I64,
  /**
   * Floating point 32 bit integer.
   */
  F32,
  /**
   * Floating point 64 bit integer.
   */
  F64,
  /**
   * A 128 bit number.
   */
  V128,
  /**
   * A reference to a Wasm function.
   */
  FuncRef,
  /**
   * A reference to opaque data in the Wasm instance.
   */
  ExternRef,
} ExtismValType;

/**
 * Wraps host functions
 */
typedef struct ExtismFunction ExtismFunction;

typedef struct ExtismMemory ExtismMemory;

typedef struct ExtismCurrentPlugin ExtismCurrentPlugin;

/**
 * Plugin contains everything needed to execute a WASM function
 */
typedef struct Plugin Plugin;

typedef struct PluginTemplate PluginTemplate;

typedef uint64_t ExtismSize;

typedef int32_t ExtismPlugin;

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
 */
typedef struct {
  ExtismValType t;
  ExtismValUnion v;
} ExtismVal;

/**
 * Host function signature
 */
typedef void (*ExtismFunctionType)(ExtismCurrentPlugin *plugin, const ExtismVal *inputs, ExtismSize n_inputs, ExtismVal *outputs, ExtismSize n_outputs, void *data);

PluginTemplate *create_template_new(Engine *engine_ptr, const uint8_t *wasm, ExtismSize wasm_size);

Plugin *instantiate(Engine *engine_ptr,
                    PluginTemplate *template_ptr,
                    const ExtismFunction **functions,
                    ExtismSize n_functions,
                    const ExtismMemory **memories,
                    int8_t n_memories,
                    bool with_wasi);

Engine *create_wasmtime_engine(void);

void free_engine(Engine *engine);

uint8_t *extism_get_memory(ExtismContext *ctx, ExtismPlugin plugin, const char *name);

uint8_t *extism_get_lineary_memory_from_host_functions(Plugin *plugin, const char *name);

ExtismVal *wasm_plugin_call_without_params(ExtismContext *ctx,
                                           ExtismPlugin plugin_id,
                                           const char *func_name,
                                           const uint8_t *data,
                                           ExtismSize data_len);

/**
 * Returns a pointer to the memory of the currently running plugin
 * NOTE: this should only be called from host functions.
 */
uint8_t *extism_current_plugin_memory(Plugin *plugin);

/**
 * Allocate a memory block in the currently running plugin
 * NOTE: this should only be called from host functions.
 */
uint64_t extism_current_plugin_memory_alloc(Plugin *plugin, ExtismSize n);

/**
 * Get the length of an allocated block
 * NOTE: this should only be called from host functions.
 */
ExtismSize extism_current_plugin_memory_length(Plugin *plugin, ExtismSize n);

/**
 * Free an allocated memory block
 * NOTE: this should only be called from host functions.
 */
void extism_current_plugin_memory_free(Plugin *plugin, uint64_t ptr);

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

ExtismMemory *extism_memory_new(const char *name,
                                const char *namespace_,
                                uint32_t min_pages,
                                uint32_t max_pages);

/**
 * Set the namespace of an `ExtismFunction`
 */
void extism_function_set_namespace(ExtismFunction *ptr, const char *namespace_);

/**
 * Free an `ExtismFunction`
 */
void free_function(ExtismFunction *ptr);

/**
 * Call a function
 *
 * `func_name`: is the function to call
 * `data`: is the input data
 * `data_len`: is the length of `data`
 */
int32_t extism_plugin_call(Plugin *instance_ptr,
                           const char *func_name,
                           const uint8_t *data,
                           ExtismSize data_len);

void deallocate_results(ExtismVal *ptr, uintptr_t len);

void free_plugin(Plugin *ptr);

ExtismVal *call(Plugin *instance_ptr,
                const char *func_name,
                const ExtismVal *params,
                ExtismSize n_params);

ExtismVal *wasm_plugin_call_without_params(Plugin *plugin_ptr, const char *func_name);

void wasm_plugin_call_without_results(Plugin *plugin_ptr,
                                      const char *func_name,
                                      const ExtismVal *params,
                                      ExtismSize n_params);

ExtismMemory *create_wasmtime_memory(const char *name,
                                     const char *namespace_,
                                     uint32_t min_pages,
                                     uint32_t max_pages);

void free_memory(ExtismMemory *mem);

void wasm_plugin_call_without_results(ExtismContext *ctx,
                                      ExtismPlugin plugin_id,
                                      const char *func_name,
                                      const ExtismVal *params,
                                      ExtismSize n_params,
                                      const uint8_t *data,
                                      ExtismSize data_len);

/**
 * Get the length of a plugin's output data
 */
uint8_t *extism_get_lineary_memory_from_host_functions(Plugin *plugin, const char *name);

/**
 * Get the length of a plugin's output data
 */
ExtismSize extism_plugin_output_length(Plugin *instance_ptr);

/**
 * Get the length of a plugin's output data
 */
const uint8_t *extism_plugin_output_data(Plugin *instance_ptr);

/**
 * Returns a pointer to the memory of the currently running plugin
 * NOTE: this should only be called from host functions.
 */
uint8_t *extism_current_plugin_memory(ExtismCurrentPlugin *plugin);

/**
 * Allocate a memory block in the currently running plugin
 * NOTE: this should only be called from host functions.
 */
uint64_t extism_current_plugin_memory_alloc(ExtismCurrentPlugin *plugin, ExtismSize n);

/**
 * Get the length of an allocated block
 * NOTE: this should only be called from host functions.
 */
ExtismSize extism_current_plugin_memory_length(ExtismCurrentPlugin *plugin, ExtismSize n);

/**
 * Free an allocated memory block
 * NOTE: this should only be called from host functions.
 */
void extism_current_plugin_memory_free(ExtismCurrentPlugin *plugin, uint64_t ptr);

void extism_reset(ExtismContext *ctx, ExtismPlugin plugin);
