package ca.applin.livm;

import java.io.*;
import java.util.*;

import static ca.applin.livm.Instruction.*;

public class Program implements Iterable<Instruction>, Serializable {
    @Serial
    private static final long serialVersionUID = 1234567L;

    private List<Instruction> instructions;

    public Program(List<Instruction> instructions) {
        this.instructions = instructions;
    }

    public int size() {
        return instructions.size();
    }

    @Override
    public Iterator<Instruction> iterator() {
        return instructions.iterator();
    }

    public Instruction getInstruction(int i) {
        return instructions.get(i);
    }

    public void serialize(String out) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(out))) {
            for (Instruction instr: instructions) {
                byte b = instr.type().asByte();
                oos.writeByte(b);
                Word operand = instr.operand();
                if (operand != null) {
                    oos.writeInt(operand.word());
                }
            }
        } catch (IOException ioe) {
            System.err.printf("ERROR: could not save file %s. Cause: %s\n", out, ioe.getMessage());
            ioe.printStackTrace();
            System.exit(-1);
        }
    }

    public static Program deserialize(String in) {
        List<Instruction> instructions = new ArrayList<>();
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(in))) {
            while (ois.available() > 0) {
            byte b = ois.readByte();
                Instruction.Type type = Instruction.Type.fromByte(b);
                if (type.operandCount() == 0) {
                    instructions.add(new Instruction(type));
                } else {
                    int operand = ois.readInt();
                    instructions.add(new Instruction(type, new Word(operand)));
                }
            }
        } catch (IOException ioe) {
            System.err.printf("ERROR: could not load *.li file %s. Cause: %s\n", in, ioe.getMessage());
            ioe.printStackTrace();
            System.exit(-1);
            throw new RuntimeException(ioe);
        }
        return new Program(instructions);
    }

    // todo real lexer/parser
    public static Program fromAsmFile(String filename) {
        Map<String, Integer> labels = new HashMap<>();
        List<String> lines = new ArrayList<>();
        try (FileReader reader = new FileReader(filename); Scanner scanner = new Scanner(reader))
        {
            while (scanner.hasNextLine()) {
                lines.add(scanner.nextLine());
            }
        } catch (IOException ioe) {
            System.err.println("Cannot open file " + filename);
            ioe.printStackTrace();
        }
        fromAsmFile(lines, filename, labels); // @hack, just to get all the labels, especially forward labels
        return fromAsmFile(lines, filename, labels);
    }

    private static Program fromAsmFile(List<String> lines, String fileName, Map<String, Integer> labels) {
        final String COMMENTS = ";";
        int lineNum = 1;
        List<Instruction> instr = new ArrayList<>();
        for (String line : lines) {
            if (line.startsWith(COMMENTS)) {
                lineNum++;
                continue;
            }
            line = line.trim();
            String[] splits = line.split(" +");
            for (int i = 0; i < splits.length; i++) {
                if (splits[i].trim().startsWith(";")) {
                    String[] newSplits = new String[i];
                    System.arraycopy(splits, 0, newSplits, 0, i);
                    splits = newSplits;
                    break;
                }
            }
            if (splits.length == 1 && splits[0].startsWith(".")) {
                String label = splits[0];
                if (label.endsWith(":")) {
                    label = label.substring(0, label.length() - 1);
                }
                labels.putIfAbsent(label, instr.size());
            }
            switch (splits[0].toUpperCase()) {
                case "" -> {}
                case "NOP" -> {
                    assertArgSize("NOP", 0, splits, fileName, lineNum);
                    instr.add(INSTR_NOP);
                }
                case "PUSH" -> {
                    assertArgSize("PUSH", 1, splits, fileName, lineNum);
                    String litteral = splits[1].replace("_", "");
                    int value = litteral.startsWith("0x")
                            ? Integer.parseInt(litteral.substring(2), 16)
                            : Integer.parseInt(litteral);
                    instr.add(Instruction.INSTR_PUSH_INT(new Word(value)));
                }

                case "ADD" -> {
                    assertArgSize("ADD", 0, splits, fileName, lineNum);
                    instr.add(INSTR_ADD);
                }

                case "SUB" -> {
                    assertArgSize("SUB", 0, splits, fileName, lineNum);
                    instr.add(INSTR_SUB);
                }

                case "DIV" -> {
                    assertArgSize("DIV", 0, splits, fileName, lineNum);
                    instr.add(INSTR_DIV);
                }

                case "MUL" -> {
                    assertArgSize("MULT", 0, splits, fileName, lineNum);
                    instr.add(INSTR_ADD);
                }

                case "EQ" -> {
                    assertArgSize("EQ", 0, splits, fileName, lineNum);
                    instr.add(INSTR_EQ);
                }


                case "DUP" -> {
                    if (splits.length == 1) {
                        instr.add(INSTR_DUP(Word.WORD_0));
                    } else {
                        assertArgSize("DUP", 1, splits, fileName, lineNum);
                        instr.add(INSTR_DUP(new Word(Integer.parseInt(splits[1]))));
                    }
                }

                case "JMP" -> {
                    assertArgSize("JMP", 1, splits, fileName, lineNum);
                    if (!splits[1].startsWith(".")) {
                        instr.add(INSTR_JMP(new Word(Integer.parseInt(splits[1]))));
                        break;
                    }
                    Integer label = labels.get(splits[1]);
                    if (label != null) {
                        label = label - instr.size();
                    }
                    instr.add(INSTR_JMP(new Word(label)));
                }

                case "JNZ" -> {
                    assertArgSize("JNZ", 1, splits, fileName, lineNum);
                    if (!splits[1].startsWith(".")) {
                        instr.add(INSTR_JNZ(new Word(Integer.parseInt(splits[1]))));
                        break;
                    }
                    Integer label = labels.get(splits[1]);
                    if (label != null) {
                        label = label - instr.size();
                    }
                    instr.add(INSTR_JNZ(new Word(label)));
                }

                case "PRINT" -> {
                    assertArgSize("PRINT", 0, splits, fileName, lineNum);
                    instr.add(INSTR_PRINT);
                }

                case "DUMP" -> {
                    assertArgSize("DUMP", 0, splits, fileName, lineNum);
                    instr.add(INSTR_DUMP);
                }

                default -> {
                    if (!splits[0].startsWith(".")) {
                        throw new RuntimeException(splits[0].toUpperCase() + " not supported (" + line + ")");
                    }
                }

            }
            lineNum++;
        }
        return new Program(instr);
    }

    private static void assertArgSize(String instr, int required, String[] actuall, String filename, int line) {
        if (required != actuall.length - 1) {
            System.err.printf("[ERROR] %s:%d - '%s' requires %d arguments but got %d ()\n",
                    filename, line, instr, required, actuall.length - 1);
            System.exit(-1);
        }
    }
}
