# CPU-SIM
The PEP/8 Virtual Machine is a classical 16-bit von Neumann computer with an accumulator (A), an index register (X), a program counter (PC), a stack pointer (SP), and an instruction register (IR). It has eight addressing modes: immediate, direct, indirect, stack-relative, stack-relative deferred, indexed, stack-indexed, and stack-indexed deferred. The instruction set is based on an expanding opcode yielding a total of 39 instructions, which come in two flavors – unary and nonunary. The unary instructions consist of a single 8-bit instruction specifier(InsSp), while the nonunary instructions have the instruction specifier followed by a 16-bit operand specifier (OpSp).