# LIVM
The small but powerfull Virtual machince.

LIVM stands for `LISP Virtual Machine`, as this VM is first intended to interpret a LISP langage.

**DISCLAMER** This is a project for fun, don't expect anything from it.

ASM examples can be found in the `src/test/resources` folder.

### Quickstart
```bash
make
make run-asm-native
```

This will create the `livm` native executable (see [dependencies](#Dependecies) section) and run the 
[loop.lasm](src/test/resources/loop.lasm) file. This show that the VM can execute the assembly langage.

```bash
make lasm-to-li
make run-li
```
This will compile the [loop.lasm](src/test/resources/loop.lasm) to `src/test/resources/loop.li` file and execute it. 
This shows that the VM can execute binary `li` bytecode file.

## Components

### `livm`
The Virtual machine. It can ecexute `li` bytecode or `lasm` assembly.

### `li`
Bytecode representation of a programm for the livm. Can be executed by the virtual machine.

### `lasm`
Assembly langage in a 'human-readable' format. Can be interpreted or compile to li by the virtual machine. 
File extension: `*.lasm`




## Build

```bash
mvn clean package
```
Will build an executable jar.

Or using make:
```bash
make target/livm-0.0-SNAPSHOT.jar
```
### dilasm


## Run
```
java -jar target/livm-0.0-SNAPSHOT.jar --asm -f src/test/resources/loop.lasm
```
Will compile to a jar file and run it.

Or using make:
```bash
make run-asm [file=FILENAME]
```

## Native image
```
mvn clean package -P native
```
Will compile to a native image (quite slow). It can then be run just by calling the `livm` executable created

Or using make:
```bash
make livm
```

You can then run the `livm` executable
```bash
./livm --help
```

## dilasm

## build
```bash
make dilasm
```

## usage
first compile from lasm to li, then back from li top lasm
```bash
./livm --asm -f src/test/resources/loop.lasm -o src/test/resources/loop.li
./dilasm src/test/resources/loop.li
```

## Dependecies
- Java 17
- Graalvm 22.2.0 (native-image)

The easiest way to get all dependecies required is to install GraalVM 22.2.0 JDK using [sdkman](https://sdkman.io/):
```
sdk install java 22.2.r17-grl
```
**Make sure your `CLASSPATH` environment variable does not contains any `.`character**