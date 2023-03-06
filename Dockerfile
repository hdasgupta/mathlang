#

FROM ubuntu:java

COPY ./target/arithmetic-0.0.1-SNAPSHOT.jar .

EXPOSE 80

ENTRYPOINT java -jar arithmetic-0.0.1-SNAPSHOT.jar

