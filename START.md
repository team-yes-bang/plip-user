# 서비스 템플릿 설정 및 초기화 가이드 (Quick Start Guide)

이 문서는 `template` 레포지토리를 복사한 직후 **이름·패키지·YML·env·Swagger 타이틀·로컬 기동**만 다룹니다.

| 문서 | 역할 |
| --- | --- |
| **START.md** (본 문서) | 템플릿 적용 직후 초기 설정 |
| [DOCKER_SETUP.md](DOCKER_SETUP.md) | 로컬 **앱+MySQL Docker Compose** 기동 |
| [DEVELOPMENT_GUIDE.md](DEVELOPMENT_GUIDE.md) | 팀원용 전체 개발 규칙 (헥사고날, DB, OpenAPI, 샘플 참고) |
| [AI_CODING_GUIDELINES.md](AI_CODING_GUIDELINES.md) | AI 에이전트용 코드 생성 제약 |
| [GIT_CONVENTION.md](GIT_CONVENTION.md) | Issue / 브랜치 / 커밋 / PR / CI |

초기 설정이 끝나면 이후 개발은 DEVELOPMENT_GUIDE를 따릅니다. (`src/test/java/com/sample`은 빌드되지 않는 구조 참고용입니다. 명명은 샘플의 `adaptor`/`vo`가 아니라 DEVELOPMENT_GUIDE 표준 `adapter`/`dto`/`persistence`를 사용하세요.)

## 📌 Check List

- [ ] 1. 프로젝트 이름 및 기본 패키지명 변경 (`com.plip.template` -> `com.plip.{service}`)
- [ ] 2. `settings.gradle` 및 `build.gradle` 설정 변경
- [ ] 3. `application.yaml`(공통) / `application-local.yml`·`application-docker.yml` 및 DB env 설정
- [ ] 4. OpenAPI / Swagger 문서 설정 수정
- [ ] 5. 로컬 실행 확인 ([DOCKER_SETUP.md](DOCKER_SETUP.md) 권장: `docker compose up -d --build`)

---

## ⚠️ 샘플 코드 (`com.sample`) — 참고용 (빌드 제외)

`src/test/java/com/sample/**` 아래 코드(예: `auth_service`, `chatting` Hexagonal 구조)는 **실제 서비스에 포함되지 않는 참고용 샘플**입니다.

- `./gradlew build` / `bootRun` / 테스트 시 **컴파일·실행 대상에서 제외**됩니다.
- `build.gradle`의 `sourceSets` 설정으로 자동 제외됩니다.

```groovy
sourceSets {
    test {
        java {
            // test 아래 샘플 패키지 전체를 컴파일·테스트 실행 대상에서 제외
            exclude '**/com/sample/**'
        }
    }
}
```

- 패키지·레이어 배치만 **구조 참고**하고, 본인 서비스 코드는 `com.plip.{service}` 아래에 작성하세요.
- 샘플은 `adaptor` / `vo` / `mysql` 등 레거시 표기를 쓸 수 있습니다. **신규 코드 명명은 [DEVELOPMENT_GUIDE.md](DEVELOPMENT_GUIDE.md) 표준**을 따릅니다.
- 실제 단위/통합 테스트는 `src/test/java/com/plip/{service}/**`에 작성합니다. (`TemplateApplicationTests`, `OpenApiGeneratorTest` 등)

---



## 1단계: 프로젝트 기본 정보 변경

복사된 프로젝트의 기본 이름과 최상위 패키지를 본인의 마이크로서비스에 맞게 수정합니다.

### 1) `settings.gradle` 수정

```groovy
// ❌ 변경 전
rootProject.name = 'template'

// ⭕ 변경 후 (예시: agit-service / user-service / media-service)
rootProject.name = 'agit-service'

```



### 2) `build.gradle` 수정

```groovy
group = 'com.plip'
version = '0.0.1-SNAPSHOT'

```

- `group` / `version`은 보통 그대로 둡니다.
- `sourceSets`의 `com/sample` exclude는 유지하세요. (샘플이 테스트 실행에 섞이지 않도록)



### 3) 최상위 패키지명 리팩토링 (IDE에서 실행)

- IntelliJ의 `src/main/java/com/plip/template` 폴더로 이동합니다.
- `template` 패키지 우클릭 $\rightarrow$ **[Refactor]** $\rightarrow$ **[Rename (Shift + F6)]** 클릭
- 본인의 서비스 이름으로 변경 (예: `com.plip.agit`, `com.plip.user`, `com.plip.media`)
- `src/test/java/com/plip/template` 폴더도 동일하게 패키지명을 변경해 줍니다.
- 메인 클래스 이름도 `{ServiceName}Application.java`로 변경합니다. (예: `AgitServiceApplication.java`)

---



## 2단계: 환경별 Application YML · Env 설정

설정은 **공통 / 프로필 / 시크릿**으로 나눕니다.


| 파일 | 역할 |
| --- | --- |
| `application.yaml` | 모든 환경 공통 (앱 이름, driver, DB 계정 env 키, Eureka instance 등) |
| `application-local.yml` | `local` 프로필 — 호스트 IDE/`bootRun` (포트, localhost DB, Eureka) |
| `application-docker.yml` | `docker` 프로필 — Compose 앱 컨테이너 (DB 호스트=`mysql`) |
| `.env` (또는 IDE/OS 환경변수) | `DB_USERNAME`, `DB_PASSWORD`, (충돌 시) `SERVER_PORT` — **Git 커밋 금지** |


> 값이 환경마다 같으면 `application.yaml`에만 두고, `-local` 등에 다시 적지 않습니다.  
> 프로필 파일은 **달라지는 설정만** 작성합니다.



### 설정 우선순위 (포트 등)

같은 키를 여러 곳에 두면 아래 순서로 **위가 이깁니다.**

1. OS / IDE **환경변수** (`SERVER_PORT` 등) · 커맨드라인
2. `application-local.yml` (활성 프로필)
3. `application.yaml` (공통)

그래서 공통에 `server.port`를 두고 local에도 두면 → **local 값이 적용**되고,  
`SERVER_PORT` env가 있으면 → **env가 YAML(공통·local)보다 우선**합니다.  
포트는 서비스마다 기본값이 다르므로 **공통이 아니라** `application-local.yml`**에만** 둡니다.

### 1) 팀 내 서비스별 로컬 포트 할당 표 (고정)

로컬에서 여러 서비스를 동시에 띄울 때 충돌을 막기 위해 **아래 포트를 기본값으로 사용**합니다.


| 서비스명                | 담당자 | 로컬 포트 (`server.port`) |
| ------------------- | --- | --------------------- |
| **Gateway Service** | 공통  | `8000`                |


**포트가 이미 사용 중이면 (충돌 시)**

1. `application-local.yml`의 `${SERVER_PORT:할당포트}` 기본값을 바꾸거나
2. 로컬에서만 `SERVER_PORT` 환경변수로 오버라이드 (권장 — 팀 기본값은 파일에 유지)

```bash
# 예: 8082가 점유된 경우
export SERVER_PORT=9082
```



### 2) `src/main/resources/application.yaml` 수정 (공통)

```yaml
spring:
  application:
    name: agit-service # 👈 Eureka 등록명 — 소문자·하이픈
  profiles:
    active: local
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
  jpa:
    properties:
      hibernate:
        format_sql: true

eureka:
  instance:
    prefer-ip-address: true
    lease-renewal-interval-in-seconds: 5
    lease-expiration-duration-in-seconds: 10
  client:
    enabled: true # 공통(dev 등) 기본 연동
    register-with-eureka: true
    fetch-registry: true
```

> Eureka는 **공통 기본값 `true`**입니다. `local` / `docker` 프로필에서는 각각 `application-local.yml` / `application-docker.yml`이 `false`로 덮어씁니다.  
> **로컬 전부 Docker** 기동은 [DOCKER_SETUP.md](DOCKER_SETUP.md)를 따르세요.



### 3) `src/main/resources/application-local.yml` 수정 (로컬 전용)

```yaml
server:
  port: ${SERVER_PORT} # 포트는 env에서 관리, 혹은 env를 사용하지 않을 때 ${SERVER_PORT:8081} 등으로 

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/plip_agit?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true

eureka:
  client:
    enabled: false # 로컬 기본 비활성. Eureka 연동 시 true로 변경
    service-url:
      defaultZone: http://localhost:8761/eureka/ # 로컬 Eureka (공용 Dev 사용 시 해당 URL로 변경)
```



### 4) DB 계정 · 포트 env 설정

비밀번호·계정은 YAML에 평문으로 두지 않습니다. 프로젝트 루트의 `.env.example`을 참고해 환경변수를 설정합니다.

```bash
# .env.example 참고
DB_USERNAME=root
DB_PASSWORD=your_password
# SERVER_PORT=9082              # 포트 충돌 시에만
```

- `.env.example`을 복사해 `.env`를 만들어도 되지만, **Spring Boot는** `.env`**를 기본으로 읽지 않습니다.**
- 로컬에서는 아래 중 하나로 `DB_USERNAME`, `DB_PASSWORD`(필요 시 `SERVER_PORT`)를 주입하세요.
  - IntelliJ Run Configuration → Environment variables
  - 터미널: `export DB_USERNAME=...` / `export DB_PASSWORD=...` 후 실행
- `.env`는 `.gitignore`에 포함되어 있으므로 **커밋하지 마세요.**

---



## 3단계: Swagger (OpenAPI) 명세 설정 변경

Swagger UI 메인 화면에 표시될 서비스 이름과 설명을 수정합니다.

- `src/main/java/com/plip/{service}/global/config/SwaggerConfig.java` 이동:

```java
package com.plip.agit.global.config; // 변경된 본인 패키지 경로

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("PLIP Agit Service API")         // 👈 본인 서비스 타이틀
                        .description("아지트 생성 및 관리 API 명세서") // 👈 서비스 설명
                        .version("v1.0.0"));
    }
}

```

---



## 4단계: OpenAPI YAML 파일 자동 생성 테스트 실행

`src/test/java/com/plip/template/OpenApiGeneratorTest.java`가 springdoc 스펙을 읽어 프로젝트 루트 `docs/openapi.yaml`을 갱신합니다. (패키지 rename 후에는 `com.plip.{service}` 경로의 동일 클래스)

1. IntelliJ에서 `OpenApiGeneratorTest` 실행, 또는 터미널:

```bash
./gradlew test
```

2. **결과 확인:** `docs/openapi.yaml`이 정상 생성/갱신되었는지 확인 후 Git에 커밋합니다.

---



## 5단계: 로컬 실행 및 헬스 체크

모든 설정이 끝났으면 서버를 구동하여 정상 동작을 확인합니다. **권장: Docker 전부 기동** → 상세는 [DOCKER_SETUP.md](DOCKER_SETUP.md)

### A) Docker (앱 + MySQL)

1. `.env.example`을 복사해 `.env` 작성 (`DB_PASSWORD` 필수)
2. `docker compose up -d --build`
3. Swagger: `http://localhost:{SERVER_PORT}/swagger-ui/index.html`
4. API 호출로 `200 OK` 확인

### B) 호스트 직접 실행 (IDE / bootRun)

1. **환경변수 확인:** `DB_USERNAME`, `DB_PASSWORD` 주입
2. **로컬 MySQL:** `application-local.yml`의 DB(`plip_{service}` 등) 생성
3. **메인 애플리케이션 실행:** `{ServiceName}Application.java` (프로필: `local`)
4. **Swagger / API** 확인
5. **(Eureka 연동 시에만)** `application-local.yml`에서 `eureka.client.enabled=true` 후 대시보드 확인

---



## 6단계: 개발 중 필요 시 의존성 추가

템플릿에는 공통으로 쓰는 의존성만 포함되어 있습니다. 기능 구현 중 Security, Redis, Kafka, Validation 등이 필요하면 **그때** `build.gradle`**의** `dependencies`**에 추가**하세요.

### 예시

```groovy
dependencies {
    // 기존 템플릿 의존성 ...

    // 예: Validation
    implementation 'org.springframework.boot:spring-boot-starter-validation'

    // 예: Redis
    // implementation 'org.springframework.boot:spring-boot-starter-data-redis'

    // 예: Security (샘플 auth_service 참고 시 필요할 수 있음)
    // implementation 'org.springframework.boot:spring-boot-starter-security'
}
```



### 주의

- Spring Cloud 관련 라이브러리는 이미 `dependencyManagement`에 BOM이 있으므로, 버전을 직접 적지 말고 starter 이름만 추가하는 것을 권장합니다.
- 의존성을 추가한 뒤 IDE에서 Gradle 리로드(`Reload Gradle Project`)하고 `./gradlew build`로 확인하세요.
- `com.sample` 샘플이 쓰는 라이브러리라고 해서 템플릿에 미리 넣을 필요는 없습니다. **본인 서비스에서 실제로 쓸 때만** 추가합니다.
- Git hooks: 커밋 시 **메시지 검증만** (`scripts/commit-msg`), **테스트는 push 시** (`pre-push` → `./gradlew test`). 클론 후 `./gradlew compileJava` 등으로 한 번 실행해 훅을 설치하세요. 자세한 내용은 [GIT_CONVENTION.md](GIT_CONVENTION.md)를 참고하세요.

---



## ❓ FAQ & 주의사항

- **Q.** `Package name`**을 수정했더니 컴파일 에러가 나요.**
- `src/main/java/com/plip/{service}` 아래에 있는 모든 자바 파일의 최상단 `package com.plip...` 선언문이 제대로 변경되었는지 확인해 주세요. (IntelliJ의 `Shift + F6` Refactor 기능 사용 권장)
- **Q.** `application.yaml`**과** `application-local.yml`**에 같은 값을 또 적어야 하나요?**
- 아니요. 공통 값은 `application.yaml`에만 두고, 환경마다 다른 값만 `application-local.yml`(또는 이후 `application-dev.yml` 등)에 작성합니다.
- **Q.** `Could not resolve placeholder 'DB_PASSWORD'` **오류가 나요.**
- `DB_USERNAME`, `DB_PASSWORD` 환경변수가 프로세스에 전달되지 않은 상태입니다. IDE Run Configuration 또는 터미널 export로 설정한 뒤 다시 실행하세요.
- **Q. 포트가 이미 사용 중이라고 나와요.**
- 팀 할당 포트를 다른 프로세스가 쓰고 있는 경우입니다. `SERVER_PORT` env로 임시 포트를 지정하거나, `application-local.yml`의 `${SERVER_PORT:…}` 기본값을 변경하세요. env가 있으면 YAML 값보다 env가 우선합니다.
- **Q. Eureka에 연결하지 않고 로컬만 띄울 수 있나요?**
- 네. `application-local.yml`에서 `eureka.client.enabled: false`(기본)라 Eureka Server 없이도 기동됩니다. 로컬에서 연동할 때만 같은 파일의 값을 `true`로 바꾸세요. (공통/`dev` 등은 기본 `true`)
- **Q.** `src/test/java/com/sample` **코드가 IDE에는 보이는데 실행/빌드가 안 돼요.**
- 정상입니다. `build.gradle`의 `sourceSets`에서 `**/com/sample/**`를 컴파일·테스트 대상에서 제외했기 때문입니다. 구조 참고용이며, 서비스 코드·테스트는 `com.plip.{service}`에 작성하세요.
- **Q. OpenAPI YAML 파일은 언제 커밋하나요?**
- Controller나 Request/Response DTO 명세가 변경되어 `./gradlew test`를 수행할 때마다 `docs/openapi.yaml` 파일이 자동으로 업데이트되므로, 이 파일도 함께 Git에 Commit & Push해 주셔야 합니다.

