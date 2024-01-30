use std::collections::BTreeMap;

use crate::{extension::*, CurrentPlugin};

/// Handles memory for plugins
pub struct PluginMemory {
    /// wasmtime Store
    pub store: *mut Store<CurrentPlugin>,

    /// WASM memory
    pub memory: Memory,

    /// Tracks allocated blocks
    pub live_blocks: BTreeMap<usize, usize>,

    /// Tracks free blocks
    pub free: Vec<MemoryBlock>,

    /// Tracks current offset in memory
    pub position: usize,
}

/// `ToMemoryBlock` is used to convert from Rust values to blocks of WASM memory
pub trait ToMemoryBlock {
    fn to_memory_block(&self, mem: &PluginMemory) -> Result<MemoryBlock, Error>;
}

impl ToMemoryBlock for MemoryBlock {
    fn to_memory_block(&self, _mem: &PluginMemory) -> Result<MemoryBlock, Error> {
        Ok(*self)
    }
}

impl ToMemoryBlock for (usize, usize) {
    fn to_memory_block(&self, _mem: &PluginMemory) -> Result<MemoryBlock, Error> {
        Ok(MemoryBlock {
            offset: self.0,
            length: self.1,
        })
    }
}

impl ToMemoryBlock for usize {
    fn to_memory_block(&self, mem: &PluginMemory) -> Result<MemoryBlock, Error> {
        match mem.at_offset(*self) {
            Some(x) => Ok(x),
            None => Err(Error::msg(format!("Invalid memory offset: {}", self))),
        }
    }
}

const PAGE_SIZE: u32 = 65536;

// BLOCK_SIZE_THRESHOLD exists to ensure that free blocks are never split up any
// smaller than this value
const BLOCK_SIZE_THRESHOLD: usize = 32;

impl PluginMemory {
    /// Create memory for a plugin
    pub fn new(store: *mut Store<CurrentPlugin>, memory: Memory) -> Self {
        PluginMemory {
            free: Vec::new(),
            live_blocks: BTreeMap::new(),
            store,
            memory,
            position: 1,
        }
    }

    fn store(&self) -> &Store<CurrentPlugin> {
        unsafe { &*self.store }
    }

    /// Size of memory in bytes
    pub fn size(&self) -> usize {
        self.memory.data_size(&self.store())
    }

    /// Reserve `n` bytes of memory
    pub fn alloc(&mut self, n: usize) -> Result<MemoryBlock, Error> {
        for (i, block) in self.free.iter_mut().enumerate() {
            if block.length == n {
                let block = self.free.swap_remove(i);
                self.live_blocks.insert(block.offset, block.length);

                return Ok(block);
            } else if block.length.saturating_sub(n) >= BLOCK_SIZE_THRESHOLD {
                let handle = MemoryBlock {
                    offset: block.offset,
                    length: n,
                };

                block.offset += n;
                block.length -= n;
                self.live_blocks.insert(handle.offset, handle.length);
                return Ok(handle);
            }
        }

        let new_offset = self.position.saturating_add(n);

        // If there aren't enough bytes, try to grow the memory size
        if new_offset >= self.size() {
            let bytes_needed = (new_offset as f64 - self.size() as f64) / PAGE_SIZE as f64;
            let mut pages_needed = bytes_needed.ceil() as u64;
            if pages_needed == 0 {
                pages_needed = 1
            }

            // This will fail if we've already allocated the maximum amount of memory allowed
            //let st = self.as_store_mut();
            unsafe {
                self.memory.grow(&mut *self.store, pages_needed)?;
            }
        }

        let mem = MemoryBlock {
            offset: self.position,
            length: n,
        };

        self.live_blocks.insert(mem.offset, mem.length);
        self.position += n;
        Ok(mem)
    }

    /// Free the block allocated at `offset`
    pub fn free(&mut self, offset: usize) {
        if let Some(length) = self.live_blocks.remove(&offset) {
            self.free.push(MemoryBlock { offset, length });
        } else {
            return;
        }

        let free_size: usize = self.free.iter().map(|x| x.length).sum();

        // Perform compaction if there is at least 1kb of free memory available
        if free_size >= 1024 {
            let mut last: Option<MemoryBlock> = None;
            let mut free = Vec::new();
            for block in self.free.iter() {
                match last {
                    None => {
                        free.push(*block);
                    }
                    Some(last) => {
                        if last.offset + last.length == block.offset {
                            free.push(MemoryBlock {
                                offset: last.offset,
                                length: last.length + block.length,
                            });
                        }
                    }
                }
                last = Some(*block);
            }
            self.free = free;
        }
    }

    /// Reset memory - clears free-list and live blocks and resets position
    pub fn reset(&mut self) {
        self.free.clear();
        self.live_blocks.clear();
        self.position = 1;
    }

    /// Get the length of the block starting at `offs`
    pub fn block_length(&self, offs: usize) -> Option<usize> {
        self.live_blocks.get(&offs).cloned()
    }

    /// Get the block at the specified offset
    pub fn at_offset(&self, offset: usize) -> Option<MemoryBlock> {
        let block_length = self.block_length(offset);
        block_length.map(|length| MemoryBlock { offset, length })
    }
}

#[derive(Clone, Copy)]
pub struct MemoryBlock {
    pub offset: usize,
    pub length: usize,
}

impl From<(usize, usize)> for MemoryBlock {
    fn from(x: (usize, usize)) -> Self {
        MemoryBlock {
            offset: x.0,
            length: x.1,
        }
    }
}
