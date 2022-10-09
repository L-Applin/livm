package ca.applin.livm;

public record Instruction(Instruction.Type type, Word operand) {
    public enum Type {
        PUSH_INT((byte) 0),
        PLUS((byte) 1),
        MINUS((byte) 2),
        MULT((byte) 3),
        DIV((byte) 4),
        DUP((byte) 5),
        EQ((byte) 6),
        JMP((byte) 7),
        JNZ((byte) 8),
        DUMP((byte) 9),
        PRINT((byte) 10);
        public final byte b;

        Type(byte b) {
            this.b = b;
        }

        public static Type fromByte(byte b) {
            for (Type type: Type.values()) {
                if (type.b == b) {
                    return type;
                }
            }
            throw new RuntimeException(b + " is not a valid Instruction byte.");
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

    public static int operandCount(Instruction.Type type) {
        return switch (type) {
            case PUSH_INT, DUP, JMP, JNZ -> 1;
            default -> 0;
        };
    }
}
