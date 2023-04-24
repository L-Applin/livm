package ca.applin.livm.core;


import ca.applin.livm.utils.ByteUtils;

import java.nio.ByteBuffer;
import java.util.HexFormat;
import java.util.LinkedList;
import java.util.function.BinaryOperator;

import static ca.applin.livm.core.Word.WORD_0;
import static ca.applin.livm.core.Word.WORD_1;

public class VirtualMachine {
    private static final int DEFAULT_MEMORY_SEGMENT_SIZE = 1024 * 1024; // 1 MB
    private static final HexFormat DATA_SECTION_HEX_FORMAT = HexFormat.ofDelimiter(" ");
    private static final Word FALSE = WORD_0,
                              TRUE  = WORD_1;

    public int ip;
    private final Program programm;
    private final LinkedList<Word> stack;

    // @Improvement used to implements procedure call quicly, there might be a better solution
    private final LinkedList<Word> returnStack;

    // Memery segement
    private ByteBuffer memory;

    // flags
    private boolean halt;
    private Parameters parameters;

    public VirtualMachine(Program programm, Parameters parameters) {
        this.programm = programm;
        this.stack = new LinkedList<>();
        this.returnStack = new LinkedList<>();
        this.ip = 0;
        this.halt = false;
        this.parameters = parameters;
        this.memory = ByteBuffer.allocate(DEFAULT_MEMORY_SEGMENT_SIZE);
    }

    public void runOrFail() {
        if (parameters.debug) {
            debugPrintDataSection();
        }
        Trap trap = this.run();
        if (trap != Trap.OK) {
            System.err.printf("Error: %s - %s ip=%d\n", trap.name(), programm.getInstruction(ip), ip);
            if (parameters.debug) {
                dump();
            }
            return;
        }
        if (parameters.debug) {
            System.out.printf("%s (ip=%d)\n", trap.name(), ip);
        }
    }

    public Trap run() {
        loop: while (!halt) {
            Instruction instr = programm.getInstruction(ip);
            if (parameters.debug) {
                System.out.println(instr.type.name()
                        + (instr.operand == null ? "" : " " + instr.operand));
            }
            switch (instr.type) {

                case NOP -> { /* do nothing*/ }

                case PUSH -> stack.push(instr.operand);

                // static programm memory access
                case LOAD -> {
                    int addr = instr.operand.word();
                    stack.push(new Word(programm.getDataSection().getInt(addr)));
                }

                // allocate VM memory
                case MEM -> {
                    int addr = memory.position();
                    int size = instr.operand.word();
                    int newPosition = addr + size;
                    if (newPosition > memory.limit()) {
                        // todo grow?
                        return Trap.OUT_OF_MEMORY;
                    }
                    memory.position(newPosition);
                    stack.push(new Word(addr));
                }

                // VM memory random access (write)
                case MOV -> {
                    if (stack.isEmpty()) {
                        return Trap.STACK_UNDERFLOW;
                    }
                    int addr = stack.pop().word();
                    if (addr < 0 || addr > memory.limit()) {
                        return Trap.ILLEGAL_INSTR_ACCESS;
                    }
                    int value = instr.operand.word();
                    memory.put(addr, ByteUtils.to_bytes_big(value));
                }

                // VM memory random access (read)
                case READ -> {
                    if (stack.isEmpty()) {
                        return Trap.STACK_UNDERFLOW;
                    }
                    int addr = stack.pop().word();
                    byte[] dataByte = new byte[4];
                    memory.get(addr, dataByte, 0, 4);
                    stack.push(new Word(ByteUtils.from_byte_int_big(dataByte)));
                }

                case DUP -> {
                    if (stack.isEmpty()) {
                        return Trap.STACK_UNDERFLOW;
                    }
                    if (instr.operand == null) {
                        stack.push(stack.peek());
                    } else {
                        int addr = instr.operand.word();
                        if (addr > stack.size() - 1) {
                            return Trap.STACK_UNDERFLOW;
                        }
                        stack.push(stack.get(addr));
                    }
                }

                case JMP -> {
                    Trap trap = doJump(instr);
                    if (trap != Trap.OK) {
                        return trap;
                    }
                    continue;
                }

                case JNZ -> {
                    final int addr = instr.operand.word();
                    if (addr < 0 || addr > programm.size()) {
                        return Trap.ILLEGAL_INSTR_ACCESS;
                    }
                    if (stack.pop().word() != 0) {
                        this.ip = addr;
                        continue;
                    }
                }

                case CALL -> {
                    int retAddr = ip;
                    returnStack.push(new Word(retAddr));
                    Trap trap = doJump(instr);
                    if (trap != Trap.OK) {
                        return trap;
                    }
                    continue;
                }

                case RET -> {
                    Word retAddr = returnStack.pop();
                    ip = retAddr.word();
                }

                case EQ -> {
                    Trap trap = wordBinop((w1, w2) -> w1.word().intValue() == w2.word().intValue() ? TRUE : FALSE);
                    if (trap != Trap.OK) {
                        return trap;
                    }
                }

                case ADD -> {
                    Trap trap = wordBinop((w1, w2) ->  new Word(Math.addExact(w1.word(), w2.word())));
                    if (trap != Trap.OK) {
                        return trap;
                    }
                }

                case SUB -> {
                    Trap trap = wordBinop((w1, w2) -> new Word(w1.word() - w2.word()));
                    if (trap != Trap.OK) {
                        return trap;
                    }
                }

                case MUL -> {
                    Trap trap = wordBinop((w1, w2) ->  new Word(w1.word() * w2.word()));
                    if (trap != Trap.OK) {
                        return trap;
                    }
                }

                case DIV -> {
                    if (stack.size() < 2) {
                        return Trap.STACK_UNDERFLOW;
                    }
                    Word fst = stack.pop();
                    Word snd = stack.pop();
                    if (snd.word() == 0) {
                        return Trap.DIVISION_BY_ZERO;
                    }
                    stack.push(new Word(fst.word() / snd.word()));
                }

                case PRINT -> {
                    if (stack.isEmpty()) {
                        return Trap.STACK_UNDERFLOW;
                    }
                    System.out.println(">>>>> " + stack.pop().word());
                }

                case HALT -> {
                    halt = true;
                    break loop;
                }

                case DUMP -> dump();
                default -> throw new RuntimeException(instr.type + " not yet implemented");
            }
            if (parameters.debug) {
                dump();
            }
            ip++;
            halt = ip >= programm.size();
        }
        return Trap.OK;
    }

    private void debugPrintDataSection() {
        System.out.println("===== DATA SECTION =====");
        ByteBuffer dataSection = programm.getDataSection();
        System.out.println(DATA_SECTION_HEX_FORMAT.formatHex(dataSection.array(), 0, dataSection.capacity()));
        if (dataSection.remaining() > 0) {
            System.out.printf("... (%d empty remaining bytes ommited)\n", dataSection.remaining());
        }
        System.out.println("===== END DATA SECTION ===== ");
    }

    private Trap doJump(Instruction instr) {
        final int addr = instr.operand.word();
        if (addr < 0 || addr > programm.size()) {
            return Trap.ILLEGAL_INSTR_ACCESS;
        }
        this.ip = addr;
        return Trap.OK;
    }

    private Trap wordBinop(BinaryOperator<Word> binop) {
        if (stack.size() < 2) {
            return Trap.STACK_UNDERFLOW;
        }
        Word fst = stack.pop();
        Word snd = stack.pop();
        try {
            stack.push(binop.apply(fst, snd));
        } catch (ArithmeticException ae) {
            return Trap.ARITHMETIC_OVERFLOW;
        }
        return Trap.OK;
    }


    public void dump() {
        System.out.println("Stack:");
        if (stack.isEmpty()) {
            System.out.println("    [EMPTY]");
        }
        stack.forEach(word -> {
            System.out.println("    " + word);
        });
    }
}
