# Docker 초기 세팅 가이드

로컬에서 **앱 + MySQL을 Docker Compose로 전부** 띄우는 방법입니다.  
템플릿 rename·패키지 변경 등 기본 작업은 [START.md](START.md)를 먼저 진행하세요.

| 파일 | 역할 |
| --- | --- |
| [Dockerfile](Dockerfile) | Spring Boot 앱 이미지 빌드 |
| [docker-compose.yml](docker-compose.yml) | `app` + `mysql` 오케스트레이션 |
| [application-local.yml](src/main/resources/application-local.yml) | `local` 프로필 — Compose 앱 컨테이너 (DB 호스트 = `mysql`) |
| [.env.example](.env.example) | Compose/앱 공통 환경변수 예시 |

---

## 1. 사전 준비

1. [Docker Desktop](https://www.docker.com/products/docker-desktop/) (또는 Docker Engine + Compose v2) 설치
2. 프로젝트 루트에서:

```bash
cp .env.example .env
```

3. `.env`의 `DB_PASSWORD` 등을 본인 값으로 수정 (`.env`는 Git에 커밋하지 않음)

---

## 2. 구성 개요

```text
┌─────────────────┐     jdbc:mysql://mysql:3306/...     ┌─────────────────┐
│  app (Spring)   │ ──────────────────────────────────► │  mysql (8.4)    │
│  profile=local  │                                     │  volume: data   │
│  :8080          │                                     │  (내부 3306)    │
└─────────────────┘                                     └─────────────────┘
```

- 앱은 **호스트 JDK가 아니라** 컨테이너에서 실행됩니다.
- DB 주소는 `localhost`가 아니라 Compose **서비스명 `mysql`** 입니다. (`application-local.yml`)
- MySQL은 호스트 `3308:3306`으로 매핑됩니다. (PC의 3306과 충돌 방지)
- Eureka는 Docker 로컬에서 기본 **비활성**입니다. 필요 시 compose에 Eureka 서비스를 추가하고 `eureka.client.enabled=true`로 바꾸면 됩니다.

### 나중에 서비스 추가·삭제

`docker-compose.yml`에 Redis, 두 번째 DB 등을 **서비스 블록으로 추가/삭제**하면 됩니다.

```bash
docker compose up -d          # 이미지 재빌드 없음 — 코드 변경 반영 안 됨
docker compose up -d --build  # 소스 변경 후 이미지 재빌드 + 기동 (필수)
docker compose down           # 중지 (볼륨 유지)
docker compose down -v        # 중지 + DB 데이터 볼륨 삭제
```

---

## 3. 기동 / 종료

프로젝트 **루트**에서:

```bash
# 개발용 (권장) — src 변경 시 이미지 자동 재빌드·재기동
docker compose up --watch

# 백그라운드 1회 기동 (코드 변경 자동 반영 없음)
docker compose up -d --build

# 로그
docker compose logs -f app

# 중지 (데이터 볼륨 유지)
docker compose down
```

첫 빌드는 Gradle 의존성 때문에 시간이 걸릴 수 있습니다.

`docker-compose.yml`의 `develop.watch`가 `src/main`, `build.gradle` 변경을 감지해 앱 이미지를 재빌드합니다.  
**개발 중에는 `docker compose up --watch`만 실행하면** `down` / `up --build`를 매번 치지 않아도 됩니다.

> **코드 변경이 반영되지 않을 때**  
> `docker compose down` 후 `up`만 하면 **이전 JAR 이미지**가 그대로 실행됩니다.  
> Watch 없이 수동 기동할 때는 `docker compose up -d --build`로 재빌드하세요.

### 접속

| 대상 | URL |
| --- | --- |
| API / 앱 | http://localhost:8080 (또는 `.env`의 `SERVER_PORT`) |
| Swagger UI | http://localhost:8080/swagger-ui/index.html |
| MySQL (호스트 도구) | `localhost:3308` (`.env`의 `MYSQL_PORT`), user/password = `.env` |

---

## 4. 서비스별 커스터마이징 (템플릿 복사 후)

START.md에서 서비스명을 바꾼 뒤 Docker 쪽도 맞춥니다.

| 항목 | 파일 | 예시 |
| --- | --- | --- |
| DB 스키마명 | `.env` → `MYSQL_DATABASE` | `plip_agit` |
| 컨테이너 이름 | `docker-compose.yml` → `container_name` | `plip-agit-app` |
| 볼륨 이름 | `docker-compose.yml` → `volumes` | `plip_agit_mysql_data` |
| 앱 포트 | `.env` → `SERVER_PORT` | 팀 할당 포트 (예: 8082) |

`application-local.yml`의 DB URL은 `${MYSQL_DATABASE:...}`를 쓰므로, 보통 `.env`만 바꿔도 됩니다.

---

## 5. 자주 하는 작업

```bash
# 앱만 다시 빌드·기동
docker compose up -d --build app

# MySQL 셸
docker compose exec mysql mysql -uroot -p"$DB_PASSWORD" plip_template

# 상태 확인
docker compose ps
```

OpenAPI 파일(`docs/openapi.yaml`) 갱신은 컨테이너와 별개로, 호스트에서 `./gradlew test`로 수행합니다.

---

## 6. 호스트에서 앱만 띄우는 경우 (참고)

이 프로젝트는 **Docker Compose로 앱+MySQL을 함께 기동**하는 것을 기본 로컬 방식으로 합니다.  
`application-local.yml`은 Compose 앱 컨테이너용 설정이며, `application.yaml`의 `spring.profiles.active: local`과 함께 사용됩니다.

호스트에서 IDE/`bootRun`으로만 앱을 띄우려면 DB URL을 `localhost:${MYSQL_PORT}`로 바꾸고 MySQL 컨테이너만 별도 실행해야 합니다.

---

## 7. 문제 해결

| 증상 | 확인 |
| --- | --- |
| 앱이 DB 연결 실패 | `docker compose ps`에서 mysql이 healthy인지, `.env`의 `DB_PASSWORD`가 비어 있지 않은지 |
| 포트 충돌 | `.env`의 `SERVER_PORT` / `MYSQL_PORT` 변경 |
| 빌드 실패 | `docker compose build --no-cache app` 후 로그 확인 |
| 오래된 DB 스키마 | `docker compose down -v` 후 재기동 (데이터 삭제됨) |
