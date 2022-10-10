package ca.applin.livm;

import java.util.LinkedList;
import java.util.function.BinaryOperator;

import static ca.applin.livm.Word.WORD_0;
import static ca.applin.livm.Word.WORD_1;

public class VirtualMachine {
    private static final Word FALSE = WORD_0,
                              TRUE  = WORD_1;

    public int ip;
    private final Program programm;
    private final LinkedList<Word> stack;

    // @Improvement used to implements procedure call quicly, there might be a better solution
    private final LinkedList<Word> returnStack;

    private boolean halt;

    public VirtualMachine(Program programm) {
        this.programm = programm;
        this.stack = new LinkedList<>();
        this.returnStack = new LinkedList<>();
        this.ip = 0;
        this.halt = false;
    }

    public void runOrFail() {
        Trap trap = this.run();
        if (trap != Trap.OK) {
            System.err.printf("Error: %s - %s ip=%d\n", trap.name(), programm.getInstruction(ip), ip);
            if (Args.instance.isDebug()) {
                dump();
            }
            return;
        }
        if (Args.instance.isDebug()) {
            System.out.printf("%s (ip=%d)\n", trap.name(), ip);
        }
    }

    public Trap run() {
        while (!halt) {
            Instruction instr = programm.getInstruction(ip);
            if (Args.instance.isDebug()) {
                System.out.println(instr.type().name()
                        + (instr.operand() == null ? "" : " " + instr.operand()));
            }
            switch (instr.type()) {

                case NOP -> { /* do nothing*/ }

                case PUSH -> stack.push(instr.operand());

                case DUP -> {
                    if (stack.isEmpty()) {
                        return Trap.STACK_UNDERFLOW;
                    }
                    if (instr.operand() == null) {
                        stack.push(stack.peek());
                    } else {
                        int addr = instr.operand().word();
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
                    final int addrRel = instr.operand().word();
                    final int addr = ip + addrRel;
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

                // intrisics
                case PRINT -> {
                    if (stack.isEmpty()) {
                        return Trap.STACK_UNDERFLOW;
                    }
                    System.out.println(">>>>> " + stack.pop().word());
                }

                case HALT -> {
                    halt = true;
                    continue;
                }

                case DUMP -> dump();
                default -> throw new RuntimeException(instr.type() + " not yet implemented");
            }
            if (Args.instance.isDebug()) {
                dump();
            }
            ip++;
            halt = ip >= programm.size();
        }
    return Trap.OK;
    }

    private Trap doJump(Instruction instr) {
        final int addrRel = instr.operand().word();
        final int addr = ip + addrRel;
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
