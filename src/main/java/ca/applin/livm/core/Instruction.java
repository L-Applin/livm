package ca.applin.livm.core;

public class Instruction {

    public enum Type {
        NOP("NOP", 0),        // byte 0x00
        HALT("HALT", 0),
        PUSH("PUSH", 1),
        MEM("MEM", 1),
        STR("STR", 1),
        LOAD("LOAD", 1),
        MOV("MOV", 1),
        READ("READ", 0),
        ADD("ADD", 0),
        SUB("SUB", 0),
        MUL("MUL", 0),
        DIV("DIV", 0),
        DUP("DUP", 1),
        EQ("EQ", 0),
        JMP("JMP", 1),
        JNZ("JNZ", 1),
        CALL("CALL", 1),
        RET("RET", 0),
        DUMP("DUMP", 0),
        PRINT("PRINT", 0)
        ;

        public final String asm;
        public final int operandCount;

        Type(String asm, int operandCount) {
            this.asm = asm;
            this.operandCount = operandCount;
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
            return operandCount;
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
    public static Instruction INSTR_EQ    = new Instruction(Type.EQ);
    public static Instruction INSTR_RET   = new Instruction(Type.RET);
    public static Instruction INSTR_HALT  = new Instruction(Type.HALT);
    public static Instruction INSTR_READ  = new Instruction(Type.READ);
    public static Instruction INSTR_DUP   = new Instruction(Type.DUP, new Word(0));

    public static Instruction INSTR_MEM(Word value) {
        return new Instruction(Type.MEM, value);
    }

    public static Instruction INSTR_MOV(Word value) {
        return new Instruction(Type.MOV, value);
    }

    public static Instruction INSTR_LOAD(Word addr) {
        return new Instruction(Type.LOAD, addr);
    }

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
