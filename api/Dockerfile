FROM eclipse-temurin:21-jdk-jammy AS builder
WORKDIR /app
COPY .mvn .mvn
COPY mvnw pom.xml ./
ARG MAVEN_PROFILE=beta
RUN ./mvnw dependency:go-offline -q -P${MAVEN_PROFILE}
COPY src src
RUN ./mvnw package -DskipTests -q -P${MAVEN_PROFILE}

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", \
  "-XX:TieredStopAtLevel=1", \
  "-Dspring.jmx.enabled=false", \
  "-Dspring.backgroundpreinitializer.ignore=true", \
  "-jar", "app.jar"]
