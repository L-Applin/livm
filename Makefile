
all: livm lasm dilasm

livm:
	mvn clean package -P livm
	if [[ -f livm.build_artifacts.txt ]]; then rm livm.build_artifacts.txt; fi

lasm:
	mvn clean package -P lasm
	if [[ -f lasm.build_artifacts.txt ]]; then rm lasm.build_artifacts.txt; fi

dilasm:
	mvn clean package -P dilasm
	if [[ -f dilasm.build_artifacts.txt ]]; then rm dilasm.build_artifacts.txt; fi

target/livm-0.0-SNAPSHOT.jar:
	mvn clean package



# ######################################################
# helper runner targets
# ######################################################
.PHONY: run-li run-native lasm-to-li li-to-lasm clean

asm_location = src/test/resources
override DEFAULT_TEST_LASM_FILE = mem.lasm
override DEFAULT_TEST_LI_FILE   = mem.li

file    = $(DEFAULT_TEST_LASM_FILE)
asm     = $(DEFAULT_TEST_LI_FILE)

run-li : target/livm-0.0-SNAPSHOT.jar lasm
	java -jar target/livm-0.0-SNAPSHOT.jar asm -f $(asm_location)/$(li_file)

run-native: livm
	./livm -f $(asm_location)/$(li_file)

lasm-to-li: lasm
	./lasm $(asm_location)/$(file) -o $(asm_location)/$(asm:.lasm=.li)

li-to-lasm: dilasm
	./dilasm $(asm_location)/$(li_file) -o $(asm_location)/$(li_file:.li=.asm)

clean:
	mvn clean
	if [[ -f livm ]]; then rm livm; fi
	if [[ -f lasm ]]; then rm lasm; fi
	if [[ -f dilasm ]]; then rm dilasm; fi
	if [[ -f livm.build_artifacts.txt ]]; then rm livm.build_artifacts.txt; fi
	if [[ -f lasm.build_artifacts.txt ]]; then rm lasm.build_artifacts.txt; fi
	if [[ -f dilasm.build_artifacts.txt ]]; then rm dilasm.build_artifacts.txt; fi
