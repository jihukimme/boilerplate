# --- 1단계: 빌드 환경 (소스 코드를 JAR 파일로 만듦) ---
FROM gradle:jdk21 AS builder
WORKDIR /app

# 모든 프로젝트 파일을 컨테이너 내부로 복사
COPY . .

# 테스트를 제외하고 실행 가능한 JAR 파일 생성
RUN gradle bootJar -x test

# --- 2단계: 실행 환경 (실제 서버 운영을 위한 작은 이미지) ---
FROM eclipse-temurin:21-jre
WORKDIR /app

# 1단계(builder)에서 생성된 JAR 파일만 쏙 뽑아서 현재 단계로 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# 컨테이너 외부로 8080 포트 공개
EXPOSE 8080

# 서버 실행 명령
ENTRYPOINT ["java", "-jar", "app.jar"]