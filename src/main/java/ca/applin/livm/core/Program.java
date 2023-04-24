package ca.applin.livm.core;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.*;

/**
 * LIVM File Format:
 * struct LIVM_File {
 *      u2   magic_bytes = OxFAB4;
 *      u4   instructions_size;
 *      u1[] instructions[instructions_size];
 *      u4   data_section_size;
 *      u1[] data_section[data_section_size];
 * }
 */
public class Program implements Iterable<Instruction>, Serializable {
    @Serial
    private static final long serialVersionUID = 1234567L;

    public static final short MAGIC_BYTES = (short) 0xFAB4;
    public static final int DEFAULT_DATA_SECTION_SIZE = 1024;

    private int totalInstructionSizeInBytes;
    private List<Instruction> instructions;
    private ByteBuffer dataSection;

    public Program(List<Instruction> instructions) {
        this.instructions = instructions;
    }

    public Program(List<Instruction> instructions, int totalInstructionSizeInBytes, ByteBuffer dataSection) {
        this.instructions = instructions;
        this.dataSection = dataSection;
        this.totalInstructionSizeInBytes = totalInstructionSizeInBytes;
    }

    public Program(List<Instruction> instructions, ByteBuffer dataSection) {
        this.instructions = instructions;
        this.dataSection = dataSection;
        for (Instruction instr : instructions) {
            totalInstructionSizeInBytes += 1;
            if (instr.operand != null) {
                totalInstructionSizeInBytes += 4;
            }
        }
    }

    public ByteBuffer getDataSection() {
        return this.dataSection;
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

    public void serialize(PrintStream ps) throws IOException {
        DataOutputStream oos = new DataOutputStream(ps);
        oos.writeShort(MAGIC_BYTES);
        oos.writeInt(totalInstructionSizeInBytes);
        for (Instruction instr: instructions) {
            byte b = instr.type.asByte();
            oos.writeByte(b);
            Word operand = instr.operand;
            if (operand != null) {
                oos.writeInt(operand.word());
            }
        }
        oos.writeInt(dataSection.capacity());
        oos.write(dataSection.array());
        oos.flush();
    }

    public static Program deserialize(String in) {
        List<Instruction> instructions = new ArrayList<>();
        try (DataInputStream dis = new DataInputStream(new FileInputStream(in))) {
            short magicBytes = dis.readShort();
            if (magicBytes != MAGIC_BYTES) {
                System.err.printf("ERROR: Cannot open file %s, invalid magic bytes", in);
                System.exit(-1);
            }
            int totalInstructionSize = dis.readInt();
            int totalBytes = 0;
            while(totalBytes < totalInstructionSize) {
                byte b = dis.readByte();
                Instruction.Type type = Instruction.Type.fromByte(b);
                if (type.operandCount() == 0) {
                    instructions.add(new Instruction(type));
                    totalBytes += 1;
                } else {
                    int operand = dis.readInt();
                    instructions.add(new Instruction(type, new Word(operand)));
                    totalBytes += 5;
                }
            }

            int dataSectionSize = dis.readInt();
            ByteBuffer dataSection = ByteBuffer.allocate(dataSectionSize);
            while (dis.available() > 0) {
                dataSection.put(dis.readByte());
            }
            return new Program(instructions, totalInstructionSize, dataSection);
        } catch (IOException ioe) {
            System.err.printf("ERROR: could not load *.li file %s. Cause: %s\n", in, ioe.getMessage());
            ioe.printStackTrace();
            System.exit(-1);
            throw new RuntimeException(ioe);
        }
    }

    public static Program fromAsmFile(String filename) {
        LasmParser parser = new LasmParser();
        return parser.fromAsmFile(filename);
    }
}
