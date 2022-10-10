package ca.applin.livm;

public class Livm {
    public static void main(String[] args) {
        Args.init(args);
        String file = Args.instance.getFile();

        if (Args.instance.isDebug()) {
            System.out.println("Running file " + file);
        }

        Program programm = Args.instance.isAsm()
                ? Program.fromAsmFile(file)
                : Program.deserialize(file);
        if (Args.instance.isAsm() && Args.instance.getOutputFile() != null) {
            programm.serialize(Args.instance.getOutputFile());
            System.exit(0);
        }
        VirtualMachine machineFromFile = new VirtualMachine(programm);
        machineFromFile.runOrFail();
    }

}
