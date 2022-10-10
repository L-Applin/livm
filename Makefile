.PHONY: run-asm-native run-asm clean

asm_location = src/test/resources
file         = loop.lasm

target/livm-0.0-SNAPSHOT.jar:
	mvn clean package

run-asm: target/livm-0.0-SNAPSHOT.jar
	java -jar target/livm-0.0-SNAPSHOT.jar --asm -f $(asm_location)/$(file)

livm:
	mvn clean package -P native

run-asm-native: livm
	./livm --asm -f $(asm_location)/$(file)

clean:
	mvn clean
	rm livm livm.build_artifacts.txt