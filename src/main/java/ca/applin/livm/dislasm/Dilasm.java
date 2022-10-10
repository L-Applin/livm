package ca.applin.livm.dislasm;

import ca.applin.livm.Program;

import java.io.*;

public class Dilasm {

    public static boolean help;
    public static String file;
    public static String outputFile;

    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("ERROR: Missing input file");
            usage();
            System.exit(-1);
        }
        initArgs(args);
        Program program = Program.deserialize(file);
        try (PrintStream ps = outputFile == null
                ? System.out
                : new PrintStream(new FileOutputStream(outputFile))) {
            program.iterator().forEachRemaining(inst ->ps.println(inst.toAsm()));
        } catch (IOException ioe) {
            System.err.println("ERROR: Encountered a problem during dissasembly: " + ioe.getMessage());
            ioe.printStackTrace();
        }
    }

    private static void initArgs(String[] args) {
        file = args[0];
        if (args.length == 1) {
            return;
        }
        if (args.length != 3 || !"-o".equals(args[1])) {
            System.err.println("ERROR: Incorrect argument");
            usage();
            return;
        }
        outputFile = args[2];
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
