# LIVM File Format

```C 
struct LIVM_File {
  u2 magic_bytes = 0xFA_B4;
  u4 instructions_size;
  u1 instructions[instructions_size];
  u4 data_section_size;
  u1 data_section[data_section_size];
}
```

## Instructions array
The `instrutctions` array contains the opcode and optionally operand of the instructions. 
Each instruction is either 1 byte (for instruction without an operand) or 5 bytes
(for instructions with an operand). The VM must therefore know which opcode must be followed by an operand,
or not.

## Data Section array
The `data_section` array contains static data that can be referenced from certain instructions. It is divided into
individual sections that can be accessed individually. Each section can be represented by the following structure:
```C
struct Data_Section {
    u4 data_size;
    u1 data[data_size];
}
```
