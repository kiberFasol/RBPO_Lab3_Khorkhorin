# Лабораторная работа 2. Вариант 22 — Распределение мест в общежитии

REST API приложение на Java 17 + Spring Boot 3. Реализует учёт студентов, комнат, заявлений на заселение, договоров проживания и заявок на переселение.

## Стек

- Java 17
- Spring Boot 3.2.5 (Web, Data JPA)
- H2 (in-memory БД)
- Lombok
- Maven

## Структура

```
src/main/java/com/university/dormitory/
├── DormitoryApplication.java
├── entity/          # JPA сущности
├── repository/      # Spring Data репозитории
├── dto/             # DTO для запросов и ответов
├── service/         # бизнес-логика
├── controller/      # REST контроллеры
└── exception/       # обработка ошибок
```

Сущности: `User`, `Room`, `HousingApplication`, `ResidenceContract`, `RoomTransferRequest`.

## Запуск

Нужен JDK 17 и Maven.

```bash
mvn spring-boot:run
```

Приложение поднимается на `http://localhost:8080`.

Консоль H2 доступна по адресу `http://localhost:8080/h2-console`  
JDBC URL: `jdbc:h2:mem:dormitory`, логин: `sa`, пароль пустой.

## API

Каждая сущность поддерживает CRUD:

| Метод  | Путь                 | Описание             | Код ответа |
|--------|----------------------|----------------------|------------|
| GET    | /api/{resource}      | список всех          | 200        |
| GET    | /api/{resource}/{id} | по id                | 200 / 404  |
| POST   | /api/{resource}      | создать              | 201        |
| PATCH  | /api/{resource}/{id} | частичное обновление | 200        |
| DELETE | /api/{resource}/{id} | удалить              | 204        |

Базовые пути: `/api/users`, `/api/rooms`, `/api/housing-applications`, `/api/residence-contracts`, `/api/room-transfer-requests`.

### Примеры

Создать пользователя:
```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"fullName": "Иванов Иван", "email": "ivanov@mail.ru", "role": "STUDENT"}'
```

Подать заявление на заселение (дата проставляется сервером автоматически):
```bash
curl -X POST http://localhost:8080/api/housing-applications \
  -H "Content-Type: application/json" \
  -d '{"userId": 1}'
```

Одобрить заявление:
```bash
curl -X PATCH http://localhost:8080/api/housing-applications/1 \
  -H "Content-Type: application/json" \
  -d '{"status": "APPROVED"}'
```

Заключить договор:
```bash
curl -X POST http://localhost:8080/api/residence-contracts \
  -H "Content-Type: application/json" \
  -d '{"applicationId": 1, "roomId": 1, "checkInDate": "2026-09-01", "checkOutDate": "2027-06-30"}'
```

## Ошибки

404 — если ресурс не найден:
```json
{
  "status": 404,
  "error": "Not Found",
  "message": "User with id 999 not found"
}
```
