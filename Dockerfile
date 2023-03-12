FROM gradle:8.0.2-jdk11 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle shadowJar --no-daemon

FROM openjdk:11-jre-slim

RUN mkdir /app

COPY --from=build /home/gradle/src/git-clio-app/build/libs/ /app/

ENTRYPOINT ["java","-jar","/app/gitclio.jar"]