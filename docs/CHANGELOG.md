# Changelog

## v0.1.0 - Foundation

### Added

- Spring Boot 3.5
- Java 21
- PostgreSQL
- Redis
- RabbitMQ
- Docker Compose
- Flyway
- OpenAPI
- Actuator
- Multi-profile configuration
- Modular project structure

23/07/26:
    -Completely redesiging the reservation section
    -Buiseness rules:
        1. No cancellation for customer after pending notificaiton status
        2. Cancellation allowed only for merchants that too after desc why
        3. thinking to add that cancellation should also not be allowed till whole active    period like shut the cancellation window for merchant also before 3 min of expiry
        4. Bharosa Score is something i invented which calculates how much points to be given or taken based on the entry if cancelled without any reason and customer complained then big loss also if viewed and then not fulfilling the order again a huge loss
        5. Need to work on this before leaving i.e. 25th.
