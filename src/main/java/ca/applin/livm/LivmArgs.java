package ca.applin.livm;

import java.io.FilterOutputStream;
import java.util.List;
import java.util.function.BiConsumer;

class LivmArgs {

    private static final String HELP_HEADER = "LIVM: the small but powerfull virtual machine!";
    private static final String USAGE =
            """
            usage
                livm <INPUT FILE> [options]
            """;

    public record Argument<T>(String name, String shortStr, String longStr, int argSize, String desc, Class<T> type, T defaultValue,
                              BiConsumer<LivmArgs, T> valueSetter) { };

    public static LivmArgs instance;

    public static final Argument<Boolean> ARG_HELP = new Argument<>("Help", "-h", "--help", 0,
                "Prints this help information.", Boolean.class, false,
                (args, value) -> args.help = value);
    public static final Argument<Boolean> ARG_DEBUG = new Argument<>("Debug", "-d", "--debug", 0,
                "Runs the vm in debug mode.", Boolean.class, false,
                (args, value) -> args.debug = value);

    @SuppressWarnings("rawtypes")
    public static final List<Argument> ARGS = List.of(
            ARG_HELP, ARG_DEBUG
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
    private static LivmArgs parse(String[] args) {
        if (args.length == 0) {
            LivmArgs usage = new LivmArgs();
            usage.help = true;
            return usage;
        }
        LivmArgs parsedArgs = new LivmArgs();
        parsedArgs.file = args[1];
        for(Argument arg: ARGS) {
            arg.valueSetter.accept(parsedArgs, arg.defaultValue);
            for (int i = 0; i < args.length; i++) {
                String current = args[i];
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
        if (parsedArgs.getFile() == null) {
            System.err.println(USAGE);
            System.exit(-1);
        }
        return parsedArgs;
    }

    private boolean help;
    private boolean debug;
    private String file;

    public boolean isHelp() {
        return help;
    }

    public boolean isDebug() {
        return debug;
    }

    public String getFile() {
        return file;
    }

}
