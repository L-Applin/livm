# Instructions
All operand are of the `int` format, ie 4 bytes.

Doc format:
## mnemonic
### Operation
Short description of the instruction

### Format
```
mnemonic [operand]
```
### Forms
mnemonic: opcode

### Stack
stack before and after the execution of the instruction:
```
value1, value2 
-> 
value3
```
### Notes
optional, additional notes.

## nop 
### Operation
It does nothing.

### Format
```
nop
```
### Forms
nop: 0x00

### Stack
```
...
-> 
...
```

## halt 
### Operation
Stops the execution of the virtual machine at the current instruction. Any values left on the stack are discarded.

### Format
```
halt
```
### Forms
halt: 0x01

### Stack
```
...
-> 
...
```

## push 
### Operation
Push operand value on the stack.

### Format
```
push operand 
```
### Forms
push: 0x02

### Stack
```
...
-> 
..., value1
```
