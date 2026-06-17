FROM ghcr.io/graalvm/native-image-community:21 AS builder
WORKDIR /app
COPY .mvn .mvn
COPY mvnw pom.xml ./
ARG MAVEN_PROFILE=beta
RUN ./mvnw dependency:go-offline -q -Pnative,${MAVEN_PROFILE}
COPY src src
RUN ./mvnw native:compile -DskipTests -q -Pnative,${MAVEN_PROFILE}

FROM debian:bookworm-slim
WORKDIR /app
COPY --from=builder /app/target/grade-ops-agents ./app
EXPOSE 8081
ENTRYPOINT ["./app"]
