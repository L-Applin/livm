package ca.applin.livm;

import java.util.List;
import java.util.function.BiConsumer;

public class Args {

    private static final String HELP_HEADER = "LIVM: the small but powerfull virtual machine!";
    private static final String USAGE =
            """
            Example usage:
            Run from a asm file
                livm -asm -f <LASM FILE>

            Run from bytecode (livm) file
                livm -f <LIVM FILE>

            Compile an asm file to lism bytecode
                livm -asm -f <INPUT LASM FILE> -o <OUTPUT LISM FILE>
            """;

    public record Argument<T>(String name, String shortStr, String longStr, int argSize, String desc, Class<T> type, T defaultValue,
                              BiConsumer<Args, T> valueSetter) { };

    public static Args instance;

    public static final Argument<Boolean> ARG_HELP = new Argument<>("Help", "-h", "--help", 0,
                "Prints this help information.", Boolean.class, false,
                (args, value) -> args.help = value);
    public static final Argument<Boolean> ARG_DEBUG = new Argument<>("Debug", "-d", "--debug", 0,
                "Runs the vm in debug mode.", Boolean.class, false,
                (args, value) -> args.debug = value);
    public static final Argument<Boolean> ARG_ASM = new Argument<>("Asm", "-a", "--asm", 0,
                "Runs from a 'human readable' asm format file (*.lasm). If not specified, the program expect a binary file format (*.lism) instead.",
                Boolean.class, false, (args, value) -> args.asm = value);
    public static final Argument<String> ARG_FILE = new Argument<>("Input file", "-f", "--file", 1,
                "File to load in the vm.",
                String.class, null, (args, value) -> args.file = value);
    public static Argument<String> ARG_OUTPUT = new Argument<>("Output file", "-o", "--output", 1,
                "If running from ASM, will create a binary output file of the instructions in the *.lism format.",
                String.class, null, (args, value) -> args.outputFile = value);

    @SuppressWarnings("rawtypes")
    public static final List<Argument> ARGS = List.of(
            ARG_HELP,
            ARG_DEBUG, ARG_ASM, ARG_FILE, ARG_OUTPUT
    );

    public static void init(String[] args) {
        instance = parse(args);
        if (instance.isHelp()) {
            System.out.println(HELP_HEADER);
            System.out.println("Arguments:");
            ARGS.forEach(arg -> {
                String name = String.format("%-16s", arg.name);
                String nameAndArgs = String.format("%-32s", name + String.format("(%s, %s)", arg.shortStr, arg.longStr));
                System.out.println(nameAndArgs + arg.desc);
            });
            System.out.println();
            System.out.println(USAGE);
            System.exit(0);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static Args parse(String[] args) {
        if (args.length == 0) {
            Args usage = new Args();
            usage.help = true;
            return usage;
        }
        Args parsedArgs = new Args();
        for(Argument arg: ARGS) {
            arg.valueSetter.accept(parsedArgs, arg.defaultValue);
            for (int i = 0; i < args.length; i++) {
                String current = args[i];
                // set default value
                if (current.equals(arg.shortStr) || current.equals(arg.longStr)) {
                    if (arg.argSize == 0 && arg.type.equals(Boolean.class)) {
                        arg.valueSetter.accept(parsedArgs, true);
                    } else if (arg.argSize == 1 && arg.type.equals(String.class)) {
                        String argsValue = args[i + 1];
                        arg.valueSetter.accept(parsedArgs, argsValue);
                    }
                }
            }
        }
        // check required args
        if (parsedArgs.getFile() == null && !parsedArgs.isHelp()) {
            System.err.println("Argument -f <filenam> or --file <filename> is required");
            System.exit(-1);
        }
        return parsedArgs;
    }

    private boolean help;
    private boolean debug;
    private boolean asm;
    private String file;
    private String outputFile;

    public boolean isHelp() {
        return help;
    }

    public boolean isDebug() {
        return debug;
    }

    public boolean isAsm() {
        return asm;
    }

    public String getFile() {
        return file;
    }

    public String getOutputFile() {
        return this.outputFile;
    }
}
