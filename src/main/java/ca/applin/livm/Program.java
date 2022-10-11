package ca.applin.livm;

import java.io.*;
import java.util.*;

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
                byte b = instr.type.asByte();
                oos.writeByte(b);
                Word operand = instr.operand;
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

    public static Program fromAsmFile(String filename) {
        LasmParser parser = new LasmParser();
        return parser.fromAsmFile(filename);
    }
}
