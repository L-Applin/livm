package ca.applin.livm.core;

import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.function.Function;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ca.applin.livm.core.Instruction.*;
import static ca.applin.livm.core.Instruction.INSTR_HALT;

public class LasmParser {

    public record ResolvableLabel(Instruction instruction, String label, int line) { }
    record Pair<A, B>(A fst, B snd) { }
    public static final String COMMENTS = ";";

    private List<Instruction> instrs = new ArrayList<>();
    private ByteBuffer dataSection = ByteBuffer.allocate(Program.DEFAULT_DATA_SECTION_SIZE);

    private final List<ResolvableLabel> instrWithUnknownLabels = new ArrayList<>();
    private final Map<String, Integer> labels = new HashMap<>();
    private final List<String> lines = new ArrayList<>();
    private int lineNum = 1;

    public Program fromAsmFile(String filename) {
        try (FileReader reader = new FileReader(filename); Scanner scanner = new Scanner(reader)) {
            while (scanner.hasNextLine()) {
                lines.add(scanner.nextLine());
            }
        } catch (IOException ioe) {
            System.err.println("ERROR: Cannot open file " + filename);
            ioe.printStackTrace();
            System.exit(-1);
        }
        Program program = fromAsmFile(lines, filename);
        resolveUnknownLabels();
        return program;
    }

    private void resolveUnknownLabels() {
        for (ResolvableLabel toResolve: instrWithUnknownLabels) {
            Instruction instruction = toResolve.instruction;
            instruction.operand = new Word(labels.get(toResolve.label));
        }
        // check if some labels are still unknown
        for (ResolvableLabel resolved: instrWithUnknownLabels) {
            if (resolved.instruction.operand.word() == null) {
                throw new RuntimeException("ERROR: unknown label " + resolved.label + " at libne " + resolved.line);
            }
        }
    }

    private Program fromAsmFile(List<String> lines, String fileName) {
        for (String line : lines) {
            if (line.startsWith(COMMENTS)) {
                lineNum++;
                continue;
            }
            line = line.trim();

            // split by space, but not in string litterals
            // @Improvement there might be a better way than regexp...
            Pattern pattern = Pattern.compile("[^\\s\"']+|\"([^\"]*)\"|'([^']*)'");
            Matcher matcher = pattern.matcher(line);
            String[] splits = matcher.results().map(MatchResult::group).toList().toArray(new String[0]);
            if (splits.length == 0) {
                continue;
            }

            // comments
            // @Improvement wont work if ; is not at the start of the word
            for (int i = 0; i < splits.length; i++) {
                if (splits[i].trim().startsWith(";")) {
                    String[] newSplits = new String[i];
                    System.arraycopy(splits, 0, newSplits, 0, i);
                    splits = newSplits;
                    break;
                }
            }
            if (splits.length == 0) {
                continue;
            }

            // labels with nothing ewlse on the line, ie:
            //    .code:
            if (splits.length == 1 && splits[0].startsWith(".")) {
                String label = splits[0];
                if (label.endsWith(":")) {
                    label = label.substring(0, label.length() - 1);
                }
                labels.putIfAbsent(label, instrs.size());
            }

            // labels withinstruction on the line, ie:
            //    .data: mem 0xCA 0xFE
            if (splits.length > 1 && splits[0].startsWith(".")) {
                String label = splits[0];
                if (label.endsWith(":")) {
                    label = label.substring(0, label.length() - 1);
                }
                String instr = splits[1];
                switch (instr.toUpperCase()) {
                    case "STR" -> {
                        String data = splits[2];
                        if (!data.startsWith("\"") && !data.endsWith("\"")) {
                            System.err.println("ERROR: String litterals must be surrounded with double quotes: [" + line + "]");
                        }
                        data = data.substring(1, data.length() - 1);
                        int addr = putDataSection(data.getBytes());
                        labels.put(label, addr);
                    }
                    case "MEM" -> {
                        throw new RuntimeException("TODO");
                    }
                    default ->
                            throw new RuntimeException("ERROR: " +
                                    instr.toUpperCase() + " not supported after label " + label + " [" + line + "]");
                }
            }
            switch (splits[0].toUpperCase()) {
                case "" -> {}
                case "NOP" -> {
                    assertArgSize("NOP", 0, splits, fileName, lineNum);
                    instrs.add(INSTR_NOP);
                }
                case "PUSH" -> {
                    assertArgSize("PUSH", 1, splits, fileName, lineNum);
                    if (splits[1].startsWith(".")) {
                        instWithtLabeledOperand(splits[1], Instruction::INSTR_PUSH_INT);
                    } else {
                        String litteral = splits[1].replace("_", "");
                        int value = litteral.startsWith("0x")
                                ? Integer.parseInt(litteral.substring(2), 16)
                                : Integer.parseInt(litteral);
                        instrs.add(INSTR_PUSH_INT(new Word(value)));
                    }
                }

                case "ADD" -> {
                    assertArgSize("ADD", 0, splits, fileName, lineNum);
                    instrs.add(INSTR_ADD);
                }

                case "SUB" -> {
                    assertArgSize("SUB", 0, splits, fileName, lineNum);
                    instrs.add(INSTR_SUB);
                }

                case "DIV" -> {
                    assertArgSize("DIV", 0, splits, fileName, lineNum);
                    instrs.add(INSTR_DIV);
                }

                case "MUL" -> {
                    assertArgSize("MULT", 0, splits, fileName, lineNum);
                    instrs.add(INSTR_ADD);
                }

                case "EQ" -> {
                    assertArgSize("EQ", 0, splits, fileName, lineNum);
                    instrs.add(INSTR_EQ);
                }

                case "DUP" -> {
                    if (splits.length == 1) {
                        instrs.add(INSTR_DUP(Word.WORD_0));
                    } else {
                        assertArgSize("DUP", 1, splits, fileName, lineNum);
                        instrs.add(INSTR_DUP(new Word(Integer.parseInt(splits[1]))));
                    }
                }

                case "JMP" -> {
                    assertArgSize("JMP", 1, splits, fileName, lineNum);
                    if (splits[1].startsWith(".")) {
                        instWithtLabeledOperand(splits[1], Instruction::INSTR_JMP);
                    } else {
                        instrs.add(INSTR_JMP(new Word(Integer.parseInt(splits[1]))));
                    }
                }

                case "JNZ" -> {
                    assertArgSize("JNZ", 1, splits, fileName, lineNum);
                    if (splits[1].startsWith(".")) {
                        instWithtLabeledOperand(splits[1], Instruction::INSTR_JNZ);
                    } else {
                        instrs.add(INSTR_JNZ(new Word(Integer.parseInt(splits[1]))));
                    }
                }

                case "CALL" -> {
                    assertArgSize("CALL", 1, splits, fileName, lineNum);
                    if (splits[1].startsWith(".")) {
                        instWithtLabeledOperand(splits[1], Instruction::INSTR_CALL);
                    } else {
                        instrs.add(INSTR_CALL(new Word(Integer.parseInt(splits[1]))));
                    }
                }

                case "RET" -> {
                    assertArgSize("RET", 0, splits, fileName, lineNum);
                    instrs.add(INSTR_RET);
                }

                case "PRINT" -> {
                    assertArgSize("PRINT", 0, splits, fileName, lineNum);
                    instrs.add(INSTR_PRINT);
                }

                case "DUMP" -> {
                    assertArgSize("DUMP", 0, splits, fileName, lineNum);
                    instrs.add(INSTR_DUMP);
                }

                case "HALT" -> {
                    assertArgSize("HALT", 0, splits, fileName, lineNum);
                    instrs.add(INSTR_HALT);
                }

                case "LOAD" -> {
                    if (splits[1].startsWith(".")) {
                        instWithtLabeledOperand(splits[1], Instruction::INSTR_LOAD);
                    } else {
                        instrs.add(INSTR_LOAD(new Word(Integer.parseInt(splits[1]))));
                    }
                }

                case "MOV" -> {
                    if (splits[1].startsWith(".")) {
                        throw new RuntimeException("TODO: MOV with label operand");
                    }
                    assertArgSize("MOV", 1, splits, fileName, lineNum);
                    instrs.add(INSTR_MOV(new Word(Integer.parseInt(splits[1]))));
                }

                case "STR" -> {
                    throw new RuntimeException("TODO: STR without label");
                }

                case "MEM" -> {
                    assertArgSize("MEM", 1, splits, fileName, lineNum);
                    instrs.add(INSTR_MEM(new Word(Integer.parseInt(splits[1]))));
                }

                case "READ" -> {
                    assertArgSize("READ", 0, splits, fileName, lineNum);
                    instrs.add(INSTR_READ);
                }

                default -> {
                    if (!splits[0].startsWith(".")) {
                        throw new RuntimeException("ERROR: " +
                                splits[0].toUpperCase() + " not supported (" + line + ")");
                    }
                }

            }
            lineNum++;
        }
        // trim data section
        ByteBuffer trimmed = ByteBuffer.allocate(dataSection.position());
        trimmed.put(0, dataSection, 0, dataSection.position());
        trimmed.position(trimmed.capacity());
        return new Program(instrs, trimmed);
    }

    private void instWithtLabeledOperand(String label, Function<Word, Instruction> instrFun) {
        Integer foundLabel = labels.get(label);
        Instruction instr = instrFun.apply(new Word(foundLabel));
        if (foundLabel == null) {
            instrWithUnknownLabels.add(new ResolvableLabel(instr, label, lineNum));
        }
        instrs.add(instr);
    }

    private int putDataSection(byte[] bytes) {
        // get the soze of the memory to add
        int addr = dataSection.position();
        if (dataSection.remaining() < bytes.length + 4) {
            growDataSectionSize();
            return putDataSection(bytes);
        }
        int byteOffset = bytes.length % 4;
        int bytesToAdd = byteOffset == 0 ? 0 : 4 - byteOffset;
        // allign by padding 0x00
        dataSection.putInt(bytes.length + bytesToAdd);
        dataSection.put(bytes);
        for (int i = 0; i < bytesToAdd; i++) {
            dataSection.put((byte)0x00);
        }
        return addr;
    }

    private void growDataSectionSize() {
        int capacity = dataSection.capacity();
        // todo better grow algorithm
        ByteBuffer newBb = ByteBuffer.allocate((int)(capacity*1.5));
        dataSection.position(0);
        newBb.put(dataSection);
    }

    private static void assertArgSize(String instr, int required, String[] actuall, String filename, int line) {
        if (required != actuall.length - 1) {
            System.err.printf("ERROR: %s:%d - '%s' requires %d arguments but got %d ()\n",
                    filename, line, instr, required, actuall.length - 1);
            System.exit(-1);
        }
    }


}
