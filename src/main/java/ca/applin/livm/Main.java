package ca.applin.livm;

public class Main {
    public static void main(String[] args) {
        Args.init(args);
        String file = Args.instance.getFile();

        System.out.println("Running file " + file);
        Program programm = Args.instance.isAsm()
                ? Program.fromAsmFile(file)
                : Program.deserialize(file);
        VirtualMachine machineFromFile = new VirtualMachine(programm);
        machineFromFile.runOrFail();
        if (Args.instance.isAsm() && Args.instance.getOutputFile() != null) {
            programm.serialize(Args.instance.getOutputFile());
        }
    }

}
