package ca.applin.livm;

public class Instruction {

    public enum Type {
        NOP("NOP"),        // byte 0x00
        HALT("HALT"),
        PUSH("PUSH"),
        MEM("MEM"),
        STR("STR"),
        LOAD("LOAD"),
        ADD("ADD"),
        SUB("SUB"),
        MUL("MUL"),
        DIV("DIV"),
        DUP("DUP"),
        EQ("EQ"),
        JMP("JMP"),
        JNZ("JNZ"),
        CALL("CALL"),
        RET("RET"),
        DUMP("DUMP"),
        PRINT("PRINT")
        ;

        public final String asm;

        Type(String asm) {
            this.asm = asm;
        }

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
                case PUSH, DUP, JMP, JNZ -> 1;
                default -> 0;
            };
        }
    }

    public Instruction.Type type;
    public Word operand;

    public Instruction(Type type, Word word) {
        this.type = type;
        this.operand = word;
    }

    public Instruction(Type type) {
        this(type, null);
    }

    @Override
    public String toString() {
        if (operand == null) {
            return type.name();
        }
        return type.name() + "[" + operand.toString() + "]";
    }

    public String toAsm() {
        return String.format("%s%s", type.asm, operand == null ? "" : " " + operand.word());
    }

    public static Instruction INSTR_NOP   = new Instruction(Type.NOP);
    public static Instruction INSTR_PRINT = new Instruction(Type.PRINT);
    public static Instruction INSTR_DUMP  = new Instruction(Type.DUMP);
    public static Instruction INSTR_ADD   = new Instruction(Type.ADD);
    public static Instruction INSTR_SUB   = new Instruction(Type.SUB);
    public static Instruction INSTR_MUL   = new Instruction(Type.MUL);
    public static Instruction INSTR_DIV   = new Instruction(Type.DIV);
    public static Instruction INSTR_DUP   = new Instruction(Type.DUP);
    public static Instruction INSTR_EQ    = new Instruction(Type.EQ);
    public static Instruction INSTR_RET   = new Instruction(Type.RET);
    public static Instruction INSTR_HALT  = new Instruction(Type.HALT);

    public static Instruction INSTR_CALL(Word addr) {
        return new Instruction(Type.CALL, addr);
    }

    public static Instruction INSTR_JNZ(Word addr) {
        return new Instruction(Type.JNZ, addr);
    }

    public static Instruction INSTR_JMP(Word addr) {
        return new Instruction(Type.JMP, addr);
    }

    public static Instruction INSTR_PUSH_INT(Word operand) {
        return new Instruction(Type.PUSH, operand);
    }

    public static Instruction INSTR_DUP(Word operand) {
        return new Instruction(Type.DUP, operand);
    }

}
