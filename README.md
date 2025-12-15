# Twinby-like Dating App (MVP)

Монорепо: **backend (FastAPI + Postgres)** и **android (Kotlin + Jetpack Compose)**.

## Что уже заложено в MVP
- **Регистрация/авторизация** (JWT)
- **Профиль**: фото (круглое на фронте), имя, пол, возраст, о себе, интересы
- **Лента**: карточки пользователей (эндпоинт под выдачу; логика ранжирования — через внешний ML/нейро сервис, подключение вынесено в конфиг)
- **Свайпы**:
  - влево: дизлайк (показываем следующего)
  - вправо: лайк → создаём чат (попадает во вкладку “Чаты”)
- **Техподдержка**: чат с ботом через **GigaChat-2-Pro**

## Interests (фиксированный список)
```
INTERESTS_LIST = [
  "music","sports","coding","movies",
  "travel","art","football","reading"
]
```

## Быстрый старт (Ubuntu сервер / локально через Docker)

1) Скопируйте env:
```bash
cp env.example .env
```

2) Заполните `.env` (важно: **не коммитьте** ключи).

3) Поднимите сервисы:
```bash
docker compose up --build -d
```

4) Проверка:
- API: `http://YOUR_SERVER:8080/docs`
- Статика (фото): `http://YOUR_SERVER:8080/static/...`

### Деплой на Ubuntu (кратко)
- Установите Docker + Compose (официальные пакеты Docker).
- Скопируйте проект на сервер.
- Создайте `.env` рядом с `docker-compose.yml` (из `env.example`) и вставьте **реальные** значения:
  - `JWT_SECRET`
  - `POSTGRES_PASSWORD`
  - `GIGACHAT_CREDENTIALS`
- Запуск: `docker compose up --build -d`

## Android
Откройте папку `android/` в Android Studio и запустите на эмуляторе/устройстве.

В `android/app/build.gradle.kts` задаётся `API_BASE_URL` (по умолчанию `http://10.0.2.2:8080` для эмулятора).

### Важно про Gradle wrapper
В этом снапшоте **нет** бинарника `gradle-wrapper.jar`. Если Android Studio попросит — сделайте:
- `File → Sync Project with Gradle Files` (IDE может сам докачать/восстановить wrapper)
- или установите Gradle и выполните в `android/`: `gradle wrapper --gradle-version 8.7`

## API маршруты (для фронта)
- **POST** `/auth/register` (multipart): `login,password,name,gender,age,about,interests(csv),photo`
- **POST** `/auth/login` (json): `{login,password}`
- **GET** `/me` (bearer)
- **PUT** `/me` (multipart, все поля опциональны)
- **GET** `/feed?limit=20` (bearer)
- **POST** `/swipe` (bearer): `{target_user_id, direction(left|right)}` → `{created_chat_id?}`
- **GET** `/chats` (bearer)
- **GET** `/chats/{chatId}/messages` (bearer)
- **POST** `/chats/{chatId}/messages` (bearer): `{text}`
- **GET** `/support/messages` (bearer)
- **POST** `/support/messages` (bearer): `{text}` → ответ техподдержки через GigaChat

## Важно про креды GigaChat
Ключи/токены **никогда не хардкодим в коде** — только через переменные окружения на сервере (`.env` / systemd / docker secrets).


