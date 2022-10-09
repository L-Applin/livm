set -e

mvn clean package
clear
java -jar target/livm-0.0-SNAPSHOT.jar "$@"