# The Virtual Machine

## Memory Sections
The virtual machine contains multiple memory sections.

### The stack
todo

#### Return Address Stack
A second stack, with memory seperated from the main stack, used only to store return adresses of procedure call.

### The Programm

#### Instructions
See the **[instructions](instructions.md)** documentation.

#### static data
Static data that is included at the end of the `*.li` file.

### Memory
todo

### Flags
#### debug flag
`--debug` will print every instruction executed, with the content of the stack.