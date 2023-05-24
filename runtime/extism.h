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

/**
 * Plugin contains everything needed to execute a WASM function
 */
typedef struct ExtismCurrentPlugin ExtismCurrentPlugin;

typedef struct PluginTemplate PluginTemplate;

typedef uint64_t ExtismSize;

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

ExtismCurrentPlugin *instantiate(Engine *engine_ptr,
                                 PluginTemplate *template_ptr,
                                 const ExtismFunction **functions,
                                 ExtismSize n_functions,
                                 const ExtismMemory **memories,
                                 int8_t n_memories,
                                 bool with_wasi);

Engine *create_wasmtime_engine(void);

void free_engine(Engine *engine);

ExtismFunction *extism_function_new(const char *name,
                                    const ExtismValType *inputs,
                                    ExtismSize n_inputs,
                                    const ExtismValType *outputs,
                                    ExtismSize n_outputs,
                                    ExtismFunctionType func,
                                    void *user_data,
                                    void (*free_user_data)(void *_));

void extism_function_set_namespace(ExtismFunction *ptr, const char *namespace_);

/**
 * Free an `ExtismFunction`
 */
void extism_function_free(ExtismFunction *ptr);

void deallocate_results(ExtismVal *ptr, uintptr_t len);

void free_plugin(ExtismCurrentPlugin *ptr);

ExtismVal *call(ExtismCurrentPlugin *instance_ptr,
                const char *func_name,
                const ExtismVal *params,
                ExtismSize n_params);

ExtismVal *wasm_plugin_call_without_params(ExtismCurrentPlugin *plugin_ptr, const char *func_name);

void wasm_plugin_call_without_results(ExtismCurrentPlugin *plugin_ptr,
                                      const char *func_name,
                                      const ExtismVal *params,
                                      ExtismSize n_params);
