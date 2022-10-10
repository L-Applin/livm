# LIVM
The small but powerfull Virtual machince.

LIVM stands for `LISP Virtual Machine`, as this VM is first intended to interpret a LISP langage.

DISCLAMER: This is a project for fun, don't expect anything from it.

ASM examples can be found in the `src/test/resources` folder.

### quickstart
```bash
make livm
make run-asm-native
```

## Build
```bash
mvn clean package
```
Will build an executable jar.

Or using make:
```bash
make target/livm-0.0-SNAPSHOT.jar
```


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

## Dependecies
- Java 17
- Graalvm 22.2.0 (native-image)

The easiest way to get all dependecies required is to install GraalVM 22.2.0 JDK using [sdkman](https://sdkman.io/):
```
sdk install java 22.2.r17-grl
```
**Make sure your `CLASSPATH` environment variable does not contains any `.`character**