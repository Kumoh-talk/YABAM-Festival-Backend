---
description: 로컬 개발 환경에서 Config, Discovery, Infra 및 Yabam Application을 실행하는 절차
---

# 로컬 애플리케이션 실행 가이드 (Local Application Running)

이 가이드는 로컬 개발 환경에서 에코시스템(Config, Discovery)과 인프라(MySQL, Redis)를 포함한 Yabam 애플리케이션을 순차적으로 실행하는 방법을 설명합니다.

## 1. 사전 준비
- **Docker Desktop**이 실행 중인지 확인합니다.
- **Java 17** 환경인지 확인합니다.

## 2. 인프라 실행 (Infrastructure)
Docker Compose를 사용하여 MySQL과 Redis를 실행합니다.
```powershell
# 프로젝트 루트에서 실행
docker-compose up -d
```
> [!IMPORTANT]
> - MySQL 포트: `3315` (local_mydb)
> - Redis 포트: `6380`

## 3. 에코시스템 실행 (Config & Discovery)
애플리케이션 설정과 서비스 등록을 위해 순차적으로 실행합니다.

### 3.1 Config Server (Port: 8888)
```powershell
./gradlew.bat :application:config:bootRun
```
- 실행 후 `http://localhost:8888/yabam/local` 접속 시 설정 파일이 정상적으로 로드되는지 확인합니다.

### 3.2 Discovery Server (Eureka)
```powershell
./gradlew.bat :application:discovery:bootRun
```
- 실행 후 `http://localhost:8761` 포털이 뜨는지 확인합니다.

## 4. Yabam Core 실행 (Main App)
성능 테스트나 실제 동작을 위해 최적화된 설정으로 실행합니다.

```powershell
# 환경 변수 설정 (PowerShell 기준)
$env:SPRING_PROFILES_ACTIVE="local"
$env:SERVER_TOMCAT_THREADS_MAX="400"
$env:SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE="100"

# 애플리케이션 실행
./gradlew.bat :application:yabam:yabam-core:bootRun
```

## 5. 문제 해결 (Troubleshooting)
### 프로세스가 이미 실행 중인 경우 (Port Conflict)
포트 8011(Core)이나 8888(Config)이 이미 사용 중이라면 해당 프로세스를 종료해야 합니다.
```powershell
# 8011 포트 사용 프로세스 확인 및 종료
$pid = (netstat -ano | findstr :8011 | ForEach-Object { $_.Split(' ', [System.StringSplitOptions]::RemoveEmptyEntries)[-1] } | Select-Object -Unique)
if ($pid) { taskkill /F /PID $pid }
```

### 설정이 반영되지 않는 경우
Config Server를 먼저 재시작한 후 Yabam Core를 재시작하십시오.
