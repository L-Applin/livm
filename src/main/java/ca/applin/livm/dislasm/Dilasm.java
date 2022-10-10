package ca.applin.livm.dislasm;

import ca.applin.livm.Program;

import java.io.*;

public class Dilasm {

    public boolean help;
    public String file;
    public String outputFile;

    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Missing input file");
            usage();
            System.exit(-1);
        }
        Dilasm dilasm = new Dilasm();
        dilasm.initArgs(args);
        dilasm.doDisassemble();
    }

    private void doDisassemble() {
        Program program = Program.deserialize(file);
        try (PrintStream ps = outputFile == null
                ? System.out
                : new PrintStream(new FileOutputStream(outputFile))) {
            program.iterator().forEachRemaining(inst ->ps.println(inst.toAsm()));
        } catch (IOException ioe) {
            System.err.println("ERROR encountered during dissasembly: " + ioe.getMessage());
            ioe.printStackTrace();
        }
    }

    private void initArgs(String[] args) {
        this.file = args[0];
        if (args.length == 1) {
            return;
        }
        if (args.length != 3 || !"-o".equals(args[1])) {
            System.err.println("Incorrect argument");
            usage();
            return;
        }
        this.outputFile = args[2];
    }

    private static void usage() {
        System.out.println(
                """
                usage:
                dilasm <INPUT FILE> [-o <OUTPUT FILE>]
                """
        );
    }
}
