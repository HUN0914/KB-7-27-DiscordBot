# 토르 디스코드 공부봇

`토르`는 스프링 부트 기반의 디스코드 공부봇입니다. 지정한 음성채널 `공부방1`에 머문 시간을 공부시간으로 기록하고, 디스코드 채팅 명령어와 자동 알림으로 일간/주간/월간 공부 현황을 보여줍니다.

## 주요 기능

- `!` 또는 `!명령어`로 사용 가능한 명령어 안내
- `!출석`으로 하루 1회 출석 체크
- `!일간 공부 시간`, `!주간 공부 시간`, `!월간 공부 시간`
- `!닉네임 일간 공부 시간`, `!닉네임 주간 공부 시간`, `!닉네임 월간 공부 시간`
- 음성채널 `공부방1` 체류 시간 자동 추적
- 매주 일요일 `23:59` 주간 공부 리포트 자동 전송
- 매일 `19:00` 랜덤 쓴소리 메시지 자동 전송
- MySQL 기반 공부시간 영구 저장
- 봇 재시작 시 현재 `공부방1` 접속자 세션 자동 복구

## 기술 스택

- Java 21
- Spring Boot 3
- JDA 5
- Spring Data JPA
- MySQL 8

## 명령어

| 명령어 | 설명 |
| --- | --- |
| `!` | 명령어 목록 표시 |
| `!명령어` | 명령어 목록 표시 |
| `!출석` | 오늘 출석 체크 |
| `!일간 공부 시간` | 서버 멤버 전체의 오늘 공부시간 조회 |
| `!주간 공부 시간` | 서버 멤버 전체의 이번 주 공부시간 조회 |
| `!월간 공부 시간` | 서버 멤버 전체의 이번 달 공부시간 조회 |
| `!닉네임 일간 공부 시간` | 특정 멤버의 오늘 공부시간 조회 |
| `!닉네임 주간 공부 시간` | 특정 멤버의 이번 주 공부시간 조회 |
| `!닉네임 월간 공부 시간` | 특정 멤버의 이번 달 공부시간 조회 |

사용자 조회는 닉네임, 유저명, 멘션(`<@USER_ID>`)을 지원합니다.

## 동작 기준

- 공부시간은 설정된 음성채널 이름과 정확히 일치하는 채널에 머문 시간만 기록합니다.
- 일간 기준은 매일 `00:00`부터 현재 시각까지입니다.
- 주간 기준은 매주 월요일 `00:00`부터 현재 시각까지입니다.
- 월간 기준은 매월 1일 `00:00`부터 현재 시각까지입니다.
- 기본 시간대는 `Asia/Seoul`입니다.

## 디스코드 봇 설정

1. [Discord Developer Portal](https://discord.com/developers/applications)에서 애플리케이션과 봇을 생성합니다.
2. Bot 메뉴에서 아래 Privileged Gateway Intents를 활성화합니다.
   - `MESSAGE CONTENT INTENT`
   - `SERVER MEMBERS INTENT`
   - `GUILD VOICE STATES INTENT`
3. 아래 권한으로 서버에 봇을 초대합니다.
   - `View Channels`
   - `Send Messages`
   - `Read Message History`
   - `Use Slash Commands`(필수는 아니지만 기본 권장)
   - `Connect`(음성채널 감지용)

## 프로젝트 실행 전 준비

### 1. MySQL 생성

```sql
CREATE DATABASE tor_study_bot
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

CREATE USER 'tor_user'@'%' IDENTIFIED BY 'tor_password';
GRANT ALL PRIVILEGES ON tor_study_bot.* TO 'tor_user'@'%';
FLUSH PRIVILEGES;
```

### 2. 환경 변수 설정

`.env.example`를 참고해서 환경 변수를 준비합니다.

```bash
export DISCORD_BOT_TOKEN=YOUR_DISCORD_BOT_TOKEN
export DISCORD_REPORT_CHANNEL_ID=123456789012345678
export DISCORD_ADMONITION_CHANNEL_ID=123456789012345678
export DISCORD_STUDY_VOICE_CHANNEL_NAME=공부방1
export DISCORD_TIMEZONE=Asia/Seoul

export SPRING_DATASOURCE_URL='jdbc:mysql://localhost:3306/tor_study_bot?serverTimezone=Asia/Seoul&characterEncoding=UTF-8'
export SPRING_DATASOURCE_USERNAME=tor_user
export SPRING_DATASOURCE_PASSWORD=tor_password
```

`DISCORD_ADMONITION_CHANNEL_ID`를 비워두면 주간 리포트 채널과 같은 채널을 사용합니다.

## 로컬 실행

```bash
mvn spring-boot:run
```

## 테스트

```bash
mvn test
```

## Docker로 MySQL만 빠르게 띄우기

```bash
docker compose up -d
```

기본 포트는 `3306`이며, `compose.yaml`에 예시 설정을 넣어두었습니다.

## 배포 체크리스트

- 디스코드 봇 토큰을 `.env`, GitHub Secrets, 배포 서버 환경 변수로만 관리하기
- `report-channel-id`, `admonition-channel-id`는 실제 텍스트 채널 ID로 설정하기
- `study-voice-channel-name`은 디스코드 서버 음성채널 이름과 정확히 맞추기
- 서버 시간대와 `DISCORD_TIMEZONE`을 일치시키기

## 레포지토리 구성

```text
tor-study-bot
├── src/main/java/com/hun/torbot
├── src/main/resources/application.yml
├── src/main/resources/admonitions.txt
├── src/test/java/com/hun/torbot
├── .env.example
├── .gitignore
├── compose.yaml
└── README.md
```

## 참고 사항

- 랜덤 쓴소리 문구는 `src/main/resources/admonitions.txt`에서 관리합니다.
- 공부시간 기록은 세션 단위로 저장되며, 일간/주간/월간 통계는 조회 시점에 집계합니다.
- 동일 유저가 `공부방1`에 들어왔다가 나가면 하나의 세션이 종료됩니다.
