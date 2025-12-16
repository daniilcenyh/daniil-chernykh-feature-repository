### Архитектура:
- `src/main/java/edu/booking/hotel_booking/` - вся бизнес-логика
- `src/main/resources/liquibase/` - миграции БД
- `src/test/` - тесты

### Основные компоненты:
1. **Репозитории** (`repository/`) - работа с БД через JdbcTemplate
2. **Сервисы** (`service/`) - бизнес-логика
3. **Контроллеры** (`controller/`) - REST API
4. **Обработка ошибок** (`global/GlobalExceptionHandler.java`)

### Swagger UI:
```
    http://localhost:8080/swagger-ui.html
```