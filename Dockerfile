#syntax=docker/dockerfile:1.2
FROM openjdk:17
WORKDIR /app
COPY ./target/E-CommerceShop-0.0.1-SNAPSHOT.jar ./E-CommerceShop-0.0.1.jar

ENTRYPOINT ["java","-Dspring.profiles.active=prod","-jar","./E-CommerceShop-0.0.1.jar"]