asm_location = src/test/resources
override DEFAULT_TEST_LASM_FILE = loop.lasm
override DEFAULT_TEST_LI_FILE   = loop.li

file    = $(DEFAULT_TEST_LASM_FILE)
li_file = $(DEFAULT_TEST_LI_FILE)

livm:
	mvn clean package -P livm

dilasm:
	mvn clean package -P dilasm

target/livm-0.0-SNAPSHOT.jar:
	mvn clean package

# helper runner targets
.PHONY: run-asm run-asm-native lasm-to-li run-livm clean

run-asm: target/livm-0.0-SNAPSHOT.jar
	java -jar target/livm-0.0-SNAPSHOT.jar --asm -f $(asm_location)/$(file)

run-asm-native: livm
	./livm --asm -f $(asm_location)/$(file)

lasm-to-li: livm
	./livm --asm -f $(asm_location)/$(file) -o $(asm_location)/$(file:.lasm=.li)

li-to-lasm: dilasm
	./dilasm $(li_file)

run-li: livm
	./livm -f $(asm_location)/$(li_file)

clean:
	mvn clean
	rm livm livm.build_artifacts.txt dilasm dilasm.build_artifacts.txt