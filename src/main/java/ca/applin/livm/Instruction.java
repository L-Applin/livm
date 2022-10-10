package ca.applin.livm;

public record Instruction(Instruction.Type type, Word operand) {
    public enum Type {
        NOP,
        PUSH_INT,
        PLUS,
        MINUS,
        MULT,
        DIV,
        DUP,
        EQ,
        JMP,
        JNZ,
        DUMP,
        PRINT;

        public static Type fromByte(byte b) {
            for (Type type: Type.values()) {
                if (((byte) type.ordinal()) == b) {
                    return type;
                }
            }
            throw new RuntimeException(b + " is not a valid Instruction byte.");
        }

        public byte asByte() {
            return (byte) this.ordinal();
        }

        public int operandCount() {
            return switch (this) {
                case PUSH_INT, DUP, JMP, JNZ -> 1;
                default -> 0;
            };
        }
    }

    public Instruction(Type type) {
        this(type, null);
    }

    @Override
    public String toString() {
        if (operand == null) {
            return type.name();
        }
        return type().name() + "[" + operand().toString() + "]";
    }

    public static Instruction INSTR_NOP    = new Instruction(Type.NOP);
    public static Instruction INSTR_PRINT = new Instruction(Type.PRINT);
    public static Instruction INSTR_DUMP  = new Instruction(Type.DUMP);
    public static Instruction INSTR_PLUS  = new Instruction(Type.PLUS);
    public static Instruction INSTR_MINUS = new Instruction(Type.MINUS);
    public static Instruction INSTR_MULT  = new Instruction(Type.MULT);
    public static Instruction INSTR_DIV   = new Instruction(Type.DIV);
    public static Instruction INSTR_DUP   = new Instruction(Type.DUP);
    public static Instruction INSTR_EQ    = new Instruction(Type.EQ);

    public static Instruction INSTR_JNZ(Word addr) {
        return new Instruction(Type.JNZ, addr);
    }

    public static Instruction INSTR_JMP(Word addr) {
        return new Instruction(Type.JMP, addr);
    }

    public static Instruction INSTR_PUSH_INT(Word operand) {
        return new Instruction(Type.PUSH_INT, operand);
    }

    public static Instruction INSTR_DUP(Word operand) {
        return new Instruction(Type.DUP, operand);
    }

}
