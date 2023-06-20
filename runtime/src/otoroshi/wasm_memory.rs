#[derive(Clone)]
pub struct WasmMemory {
    pub(crate) name: String,
    pub(crate) namespace: String,
    pub(crate) ty: wasmtime::MemoryType,
}

impl WasmMemory {
    pub fn new(
        name: impl Into<String>,
        namespace: impl Into<String>,
        min_pages: u32,
        max_pages: u32
    ) -> WasmMemory {
        let maximum = if max_pages > min_pages {
            Some(max_pages)
        } else {
            None
        };

        WasmMemory {
            name: name.into(),
            namespace: namespace.into(),
            ty: wasmtime::MemoryType::new(min_pages, maximum),
        }
    }

    pub fn name(&self) -> &str {
        &self.name
    }

    pub fn namespace(&self) -> &str {
        &self.namespace
    }

    pub fn set_namespace(&mut self, namespace: impl Into<String>) {
        self.namespace = namespace.into()
    }

    pub fn with_namespace(mut self, namespace: impl Into<String>) -> Self {
        self.set_namespace(namespace);
        self
    }

    pub fn ty(&self) -> &wasmtime::MemoryType {
        &self.ty
    }
}
