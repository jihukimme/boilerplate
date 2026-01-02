# Boilerplate

개발 효율을 위해 설계된 Boilerplate입니다. 반복되는 인프라 설정(Docker, 환경 변수)과 공통 로직(예외 처리, 응답 포맷, 보안, 로깅) 구축에 소요되는 시간을 최소화해 비즈니스 로직에만 집중할 수 있도록 하고자 했습니다. 

코드의 응집도를 높이고 공통 관심사를 분리하기 위해, 계층형 아키텍처 대신 도메인형 아키텍처를 채택했습니다.


---

## 🛠 기술 스택

- **Language**: Java 21
- **Framework**: Spring Boot 4.0.1
- **Build Tool**: Gradle
- **Database**: MySQL, Redis
- **Logging**: Log4j2 (기존 Logback 제외 및 성능 최적화)
- **Template Engine**: Thymeleaf
- **Infrastructure**: Docker, Docker Compose
- **...**


---

## 🚀 실행 방법 및 도커 전략

이 프로젝트는 Docker 환경에서 빌드부터 실행까지 한 번에 처리되도록 구성되어 있습니다.

### 사전 요구 사항

* Docker 및 Docker Compose가 설치되어 있어야 합니다.

### 실행 명령어

프로젝트 루트 디렉토리에서 아래 명령어를 실행하면, Spring Boot 애플리케이션과 필요한 인프라(DB 등)가 컨테이너로 실행됩니다.

**기본 실행 시**

```bash
docker-compose up -d
```

**코드 변경 시**

```bash
docker-compose up -d --build
```

### 도커 빌드 및 운영 전략

- **멀티 스테이지 빌드 (Multi-stage Build)**:
    - **빌드 환경의 독립성:** 호스트 머신에 Java나 Gradle을 설치할 필요가 없습니다. Docker 내부에서 빌드부터 실행까지 전 과정을 처리합니다.
    - **자동화된 빌드 프로세스:** 기존처럼 로컬에서 `bootJar`를 직접 실행하여 JAR 파일을 생성한 뒤 Docker로 넘기는 번거로움이 사라졌습니다.  
    - **이미지 최적화:** 빌드 단계와 실행 단계를 분리하여, 최종 이미지에는 실행에 필요한 JRE와 JAR 파일만 남겨 용량을 최소화하고 보안성을 높였습니다.


- **Docker Compose 통합 관리**:
    - **원큐 실행 (One-Step):** `docker-compose` 명령어 하나로 애플리케이션 빌드, 이미지 생성, 컨테이너 실행, 네트워크 설정을 일괄 처리합니다.
    - **인프라 오케스트레이션:** 애플리케이션 컨테이너와 MySQL 데이터베이스 컨테이너 간의 의존성(`depends_on`) 및 네트워크 연결을 정의하여, 복잡한 인프라를 코드 한 줄로 관리합니다.


---

## 🔐 환경 변수 및 설정 파일 관리 전략

보안과 개발 편의성을 동시에 확보하고, 배포 환경에 유연하게 대응하기 위해 개발 환경과 운영 환경에 따라 환경 변수 파일(.env)과 설정 파일(yml)을 분리하여 관리합니다.

### 환경 변수 관리 

민감한 정보(DB 비밀번호, API Key, JWT Secret 등)는 아래 두 파일로 관리하며, `docker-compose.yml`을 통해 컨테이너에 주입됩니다.

- **`.env.dev` (개발용)**
    - **Git 포함 (Tracked)**: 개발 편의성을 위해 저장소에 포함됩니다.
    - 팀원들이 `git pull` 후 별도 설정 없이 바로 개발을 시작할 수 있습니다.


- **`.env.prod` (운영용)**
    - **Git 제외 (Ignored)**: 보안을 위해 절대 저장소에 올리지 않습니다. (`.gitignore` 등록)
    - 실제 운영 서버 배포 시 서버 루트 경로에 직접 생성해야 합니다.


**[주입 원리 및 우선순위]**
Docker Compose 설정에서 `.env.dev`를 기본으로 읽고, `.env.prod`가 존재할 경우 덮어씌우도록(Overriding) 구성되어 있습니다.

```yaml
env_file:
  - path: .env.dev
    required: true  # 필수 (개발 설정)
  - path: .env.prod
    required: false # 선택 (운영 설정 - 파일이 있으면 덮어씌움)
```

### 설정 파일 관리

환경별(`dev`, `prod`) 설정을 분리하여 관리합니다. `application.yml`의 공통 설정을 기반으로, 활성화된 프로필에 따라 특정 환경 설정이 **병합(Merge)**됩니다.

- `application.yml`: 모든 환경 공통 설정 (기본값)
- `application-dev.yml`: 개발 환경 전용 (Debug 로깅, 로컬 DB 연결 등)
- `application-prod.yml`: 운영 환경 전용 (Prod DB 연결, 성능 최적화 등)

**[작동 메커니즘]** 
Docker Compose가 `.env.dev` 또는 `.env.prod` 파일에 정의된 `SPRING_PROFILES_ACTIVE` 값을 컨테이너에 주입하고, Spring Boot가 이를 감지하여 설정을 로드합니다.

- **개발 환경 (`.env.dev` 감지 후 `SPRING_PROFILES_ACTIVE=dev` 주입 시):** application.yml ➡ application-dev.yml
- **운영 환경 (`.env.prod` 감지 후 `SPRING_PROFILES_ACTIVE=prod` 주입 시):** application.yml ➡ application-prod.yml


---

## 📂 프로젝트 구조 및 아키텍처

추후 다른 서버를 추가할 수 있는 점을 고려해, 루트 디렉토리 하위에 `application`이라는 Spring Boot 모듈을 두어 진행했습니다.
비즈니스 로직(domain)의 응집도를 높이고, 공통 관심사(global/common)를 분리하기 위해 계층형 아키텍처가 아닌 도메인형 아키텍처를 채택했습니다.

```text
[Root]
 └── application        # Spring Boot Application (Core Server)
     └── src/main/java/com/example
         ├── common     # (dto, entity, util, ...)
         ├── domain     # (auth, email, home, user, ...)
         └── global     # (config, security, exception, dto, ...)
```

### domain 패키지

도메인형 패키지 구조를 채택하여, 관련된 비즈니스 로직(Controller, Service, Repository, Entity, DTO)을 하나의 패키지 안에서 응집도 높게 관리합니다.

- **domain/auth**: 로그인, 회원가입, 토큰 재발급 등 인증 관련 비즈니스 로직
- **domain/email**: 이메일 인증 코드 발송 및 검증 로직
- **domain/home**: 메인 페이지 및 홈 화면 관련 로직
- **domain/user**: 사용자 정보 조회, 수정, 탈퇴 등 회원 관리 로직
- *(추후 확장 시 domain/order, domain/payment 등으로 추가)*

### common 패키지

모든 도메인에서 공통적으로 사용되는 로우 레벨의 유틸리티나 엔티티를 관리하여 코드 중복을 방지합니다.

- **common/dto**: 페이징 요청/응답(PageRequestDto, PageResponseDto)과 같이 여러 도메인에서 공통으로 재사용되는 DTO 관리
- **common/entity**: BaseEntity(생성일, 수정일)와 같이 모든 테이블에 공통으로 상속되는 JPA 엔티티 관리
- **common/util**: 날짜 계산, 문자열 처리 등 도메인 비즈니스 로직에 종속되지 않는 순수 유틸리티 클래스

### global 패키지

애플리케이션 전반에 영향을 미치는 공통 관심사를 비즈니스 로직과 분리하여 관리합니다.

- **global/security**: JWT + Interceptor 조합을 활용한 경량화된 보안 시스템 구현 (Spring Security 사용하지 않음)
    - **인증 메커니즘**:
        - **저장**: 클라이언트는 로그인 성공 시 발급받은 JWT(Access Token)를 브라우저의 Local Storage에 저장합니다.
        - **전송**: API 요청 시 HTTP Header의 `Authorization` 필드에 토큰을 담아 서버로 전송합니다.
        - **검증**: AuthInterceptor가 컨트롤러 도달 전 요청을 가로채(Intercept), 토큰을 추출하고 유효성을 검증하여 인증을 수행합니다.
     

- **global/config**: 애플리케이션 전반의 설정 클래스 관리 (WebMvcConfig 등)
    - **Interceptor 설정**: `WebMvcConfig`에서 AuthInterceptor를 등록하고, 로그인/회원가입 등 인증이 불필요한 경로는 `excludePathPatterns`에 추가하여 관리합니다.
 

- **global/exception**: 전역 예외 처리 핸들러 및 에러 코드 관리
    - **ErrorCode**: 애플리케이션 내 발생하는 모든 예외 메시지와 코드를 Enum으로 중앙 관리합니다.
    - **GlobalExceptionHandler**: `@RestControllerAdvice`를 통해 Controller 및 Service 계층에서 던져진 예외를 전역적으로 포착(Catch)하여 표준 포맷으로 변환합니다.
 

- **global/dto**: 응답 형태를 통일하기 위한 API 표준 응답 형태(ApiResponseDto) 정의
    - **Anti-Soft 200 전략**: 에러 발생 시 상태 코드가 200으로 고정되는 문제를 방지하기 위해, GlobalExceptionHandler에서는 `ResponseEntity`로 감싸 명확한 HTTP Status Code(400, 404, 500 등)를 반환합니다.
    - **Controller 전략**: Controller에서는 `try-catch` 블록을 지양하여 코드를 깔끔하게 유지하며, 성공 시 `ApiResponseDto`를 반환하여 데이터 형태의 일관성을 확보합니다.
    - **Service 전략**: 비즈니스 로직에서 예외 상황 발생 시 로깅한 후, 예외를 던집니다(`throw`).
    - **구조 및 응답 예시**:
        - **구조**:
            
            ```java
            // [Class Structure]
            public class ApiResponseDto<T> {
                private final boolean success; // 성공 여부
                private final String code;     // 비즈니스 코드 ("200", "ERR-404" 등)
                private final String message;  // 응답 메시지
                private final T data;          // 실제 데이터 Payload
            }
            ```
            
        - **응답**:
            
            ```json
            // [Response Example - Success (HTTP 200)]
            {
              "success": true,
              "code": "200",
              "message": "요청이 성공적으로 처리되었습니다.",
              "data": { "id": 1, "username": "user1" }
            }
            ```
            
            ```json
            // [Response Example - Failure (HTTP 400)]
            {
              "success": false,
              "code": "ERR-400",
              "message": "잘못된 요청 파라미터입니다.",
              "data": null
            }
            ```


---

## 🎨 개발 컨벤션 및 규칙

일관성 있고 유지보수하기 좋은 코드를 위해, 프로젝트 내에서 합의된 다음 규칙들을 준수합니다.

- **파라미터 네이밍**: 변수명에 타입을 불필요하게 반복하지 않고 의도를 명확히 합니다. (예: `LoginRequestDto loginRequestDto`  ➡ `LoginRequestDto request`)
- **계층 간 참조 제약**: Controller는 절대로 Repository에 직접 접근하지 않으며, 반드시 **Service** 계층을 거쳐 비즈니스 로직을 수행해야 합니다.
- **Setter 사용 지양**: Entity와 DTO의 무결성을 위해 무분별한 `@Setter` 사용을 금지합니다. Entity 상태 변경이 필요할 경우, 명확한 의도를 가진 **비즈니스 메소드**를 구현하여 사용합니다.
- **DTO 변환**: Entity ↔ DTO 변환 시에는 생성자보다 `from`, `of`와 같은 **정적 팩토리 메소드(Static Factory Method)** 사용을 권장합니다. (단, 로직에 따라 유연하게 적용)
- **예외 처리 및 로깅**:
    - **Controller**: `try-catch` 블록 사용을 지양하고, `GlobalExceptionHandler`에게 처리를 위임하여 코드를 깔끔하게 유지합니다.
    - **Service**: 비즈니스 로직에서 예외 상황 발생 시 로깅한 후 예외를 던집니다(`throw`).
- **단일 데이터 DTO 래핑**: 반환할 데이터가 단 하나(문자열, 숫자 등)인 경우에도, 확장성을 위해 반드시 **DTO 객체에 담아서 반환**합니다.


---

## 🗓 TODO

- [ ] **로깅 시스템 고도화**: 현재 기본적인 로깅 설정 외에, AOP를 활용한 로깅 전략이나 모니터링 툴 연동 등 로깅 시스템을 구체적으로 발전시킬 예정
- [ ] **common 패키지 설계**: paging, BaseEntity 등 공통 유틸 사용 및 적용
- [ ] **config 설계**: AsyncConfig, RedisConfig 등 다양한 config











