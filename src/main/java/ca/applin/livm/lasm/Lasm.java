package ca.applin.livm.lasm;

import ca.applin.livm.core.Program;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

public class Lasm {

    private static void usage() {
        System.out.println(
                """
                usage:
                lasm <INPUT FILE> [-o <OUTPUT FILE>]
                """
        );
    }

    public static String file;
    public static String outputFile;

    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("ERROR: Missing input file");
            usage();
            System.exit(-1);
        }
        initArgs(args);
        Program program = Program.fromAsmFile(file);
        try (PrintStream ps = outputFile == null
                ? System.out
                : new PrintStream(new FileOutputStream(outputFile))) {
            program.serialize(ps);
        } catch (IOException ioe) {
            System.err.printf("ERROR: could not save file %s. Cause: %s\n", file, ioe.getMessage());
            ioe.printStackTrace();
            System.exit(-1);
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


}
