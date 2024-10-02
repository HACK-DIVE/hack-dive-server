FROM openjdk:17-jdk-slim

# Gradle 빌드를 실행하여 JAR 파일 생성
CMD ["./gradlew", "clean", "build", "-x", "test"]

# JAR 파일 경로와 이름 설정
ARG JAR_FILE_PATH=build/libs/hackdive-0.0.1-SNAPSHOT.jar

# JAR 파일을 컨테이너에 복사
COPY ${JAR_FILE_PATH} app.jar

# Java 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar"]
