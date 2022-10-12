package ca.applin.livm;

import ca.applin.livm.core.Parameters;
import ca.applin.livm.core.Program;
import ca.applin.livm.core.VirtualMachine;

public class Livm {
    public static void main(String[] args) {
        LivmArgs.init(args);
        String file = LivmArgs.instance.getFile();

        if (LivmArgs.instance.isDebug()) {
            System.out.println("Running file " + file);
        }

        Program programm = Program.deserialize(file);
        VirtualMachine machineFromFile = new VirtualMachine(programm, new Parameters(LivmArgs.instance.isDebug()));
        machineFromFile.runOrFail();
    }

}
