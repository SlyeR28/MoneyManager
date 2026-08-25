# MoneyManagement - Profiles & Environment Configuration Guide

This guide explains how configuration, environment variables, and Spring Boot profiles work across all **4 phases of the CI/CD and deployment lifecycle**:
1. **`development`** (Local machine development)
2. **`test`** (Automated Unit / Integration tests & CI pipeline)
3. **`docker`** (Containerized development via `docker-compose`)
4. **`prod`** (Production deployment)

---

## 1. Profile Comparison Matrix

| Property / Feature | `development` | `test` | `docker` | `prod` |
| :--- | :--- | :--- | :--- | :--- |
| **Purpose** | Local coding & debugging | Automated JUnit/CI tests | Multi-container testing | Live production server |
| **Database** | MySQL (Local) | H2 In-Memory (`testdb`) | MySQL (Docker container) | Managed MySQL / Cloud RDS |
| **DB Host / URL** | `localhost:3306` | `jdbc:h2:mem:moneymanagement_test` | `mysql:3306` (Docker DNS) | e.g. `rds.amazonaws.com:3306` |
| **DDL Mode (`hibernate.ddl-auto`)** | `update` | `create-drop` | `update` | `validate` (or `none`) |
| **SQL Query Logging (`show-sql`)** | `true` (with formatted SQL) | `false` | `true` | `false` |
| **Mail / SMTP** | Brevo SMTP (Dev key) or Mock | Mock / Dummy (`localhost:25`) | Brevo SMTP | Brevo Production SMTP |
| **Default Server Port** | `8080` | `8080` | `8009` | `8080` (behind reverse proxy) |
| **Frontend URL** | `http://localhost:5173` | `http://localhost:5173` | `http://localhost:5173` | `https://app.yourdomain.com` |
| **Activation URL** | `http://localhost:8080` | `http://localhost:8080` | `http://localhost:8009` | `https://api.yourdomain.com` |

---

## 2. How Spring Boot Resolves Configuration

Spring Boot evaluates configurations using a hierarchy (highest precedence wins):

```
1. Command-line arguments: --spring.profiles.active=prod --server.port=8080
2. OS Environment variables: SPRING_PROFILES_ACTIVE, DB_PASSWORD, etc.
3. Profile-specific configuration: application-{profile}.yml (e.g. application-prod.yml)
4. Base configuration: application.yml
5. Spring Boot default fallback values (e.g. ${DB_HOST:localhost})
```

### Property Placeholder Mechanism
When Spring sees:
```yaml
url: jdbc:mysql://${DB_HOST:localhost}:${DB_PORT:3306}/${DB_NAME:MoneyManagement}
```
1. It checks if the OS environment variable `DB_HOST` is present.
2. If `DB_HOST` is defined (e.g., `DB_HOST=mysql`), it uses `mysql`.
3. If `DB_HOST` is NOT defined, it falls back to the default `localhost`.

---

## 3. Spring Boot YAML Configurations

Place these YAML files inside `src/main/resources/`:

### A. Base File: `application.yml`
Common fallback properties across all profiles:

```yaml
spring:
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:development}
  application:
    name: MoneyManagement

  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://${DB_HOST:localhost}:${DB_PORT:3306}/${DB_NAME:MoneyManagement}?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:}

  jpa:
    hibernate:
      ddl-auto: ${JPA_DDL_AUTO:update}
    show-sql: ${JPA_SHOW_SQL:false}
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect

  mail:
    host: ${MAIL_HOST:smtp-relay.brevo.com}
    port: ${MAIL_PORT:587}
    username: ${BREVO_LOGIN:}
    password: ${BREVO_PASSWORD:}
    protocol: smtp
    properties:
      mail:
        smtp:
          auth: ${MAIL_SMTP_AUTH:true}
          starttls:
            enable: ${MAIL_SMTP_STARTTLS_ENABLE:true}
          from: ${BREVO_MAIL:}

server:
  port: ${SERVER_PORT:8080}

money:
  manager:
    frontend:
      url: ${FRONTEND_URL:http://localhost:5173}

app:
  activation:
    url: ${MONEY_MANAGER_BACKEND_URL:http://localhost:8080}
```

---

### B. Development Profile: `application-development.yml`
```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        format_sql: true

logging:
  level:
    org.moneymanagement: DEBUG
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql: TRACE
```

---

### C. Test Profile: `application-test.yml` (and `src/test/resources/application.yml`)
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:moneymanagement_test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;MODE=MySQL
    driver-class-name: org.h2.Driver
    username: sa
    password: ""
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: false
    defer-datasource-initialization: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.H2Dialect
  sql:
    init:
      mode: always
      data-locations: classpath:data.sql
  mail:
    host: localhost
    port: 25
    username: test
    password: test
    properties:
      mail:
        smtp:
          auth: false
          starttls:
            enable: false
          from: test@example.com

server:
  port: 8080

money:
  manager:
    frontend:
      url: http://localhost:5173

app:
  activation:
    url: http://localhost:8080
```

---

### D. Docker Profile: `application-docker.yml`
```yaml
spring:
  datasource:
    url: jdbc:mysql://${DB_HOST:mysql}:${DB_PORT:3306}/${DB_NAME:MoneyManagement}?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: ${JPA_DDL_AUTO:update}
    show-sql: true

server:
  port: ${SERVER_PORT:8009}
```

---

### E. Production Profile: `application-prod.yml`
```yaml
spring:
  datasource:
    url: jdbc:mysql://${DB_HOST}:${DB_PORT:3306}/${DB_NAME}?useSSL=true&requireSSL=true&serverTimezone=UTC
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
  jpa:
    hibernate:
      # In production, never use create-drop or update to protect live data
      ddl-auto: validate
    show-sql: false

logging:
  level:
    root: INFO
    org.moneymanagement: INFO
    org.hibernate.SQL: WARN
```

---

## 4. Environment (`.env`) Files For Each Phase

### 1. Template: `.env.example` (Commit to Git)
```env
# Profile selector (development | test | docker | prod)
SPRING_PROFILES_ACTIVE=development

# Database Configuration
DB_HOST=localhost
DB_PORT=3306
DB_NAME=MoneyManagement
DB_USERNAME=root
DB_PASSWORD=your_database_password

# Server Port
SERVER_PORT=8080

# Brevo Mail Credentials
MAIL_HOST=smtp-relay.brevo.com
MAIL_PORT=587
BREVO_LOGIN=your_brevo_smtp_login
BREVO_PASSWORD=your_brevo_smtp_key
BREVO_MAIL=noreply@yourdomain.com

# Service Endpoints
FRONTEND_URL=http://localhost:5173
MONEY_MANAGER_BACKEND_URL=http://localhost:8080
```

---

### 2. Local Dev: `.env.dev` (Do NOT commit to Git)
```env
SPRING_PROFILES_ACTIVE=development
DB_HOST=localhost
DB_PORT=3306
DB_NAME=MoneyManagement
DB_USERNAME=root
DB_PASSWORD=Rishabh@28
SERVER_PORT=8080
BREVO_LOGIN=dev_brevo_login
BREVO_PASSWORD=dev_brevo_key
BREVO_MAIL=dev@moneymanager.local
FRONTEND_URL=http://localhost:5173
MONEY_MANAGER_BACKEND_URL=http://localhost:8080
```

---

### 3. Docker Compose: `.env.docker` (Do NOT commit to Git)
```env
SPRING_PROFILES_ACTIVE=docker
DB_HOST=mysql
DB_PORT=3306
DB_NAME=MoneyManagement
DB_USERNAME=money_user
DB_PASSWORD=docker_secret_password
SERVER_PORT=8009
BREVO_LOGIN=your_brevo_login
BREVO_PASSWORD=your_brevo_key
BREVO_MAIL=no-reply@moneymanager.com
FRONTEND_URL=http://localhost:5173
MONEY_MANAGER_BACKEND_URL=http://localhost:8009
```

---

### 4. Production: `.env.prod` (Managed via CI/CD Secrets / Vault)
```env
SPRING_PROFILES_ACTIVE=prod
DB_HOST=rds-mysql.cz87192.ap-south-1.rds.amazonaws.com
DB_PORT=3306
DB_NAME=MoneyManagementProd
DB_USERNAME=prod_db_admin
DB_PASSWORD=SuperSecureProdPassword#2026
SERVER_PORT=8080
BREVO_LOGIN=prod_smtp_login
BREVO_PASSWORD=prod_smtp_key
BREVO_MAIL=support@moneymanager.com
FRONTEND_URL=https://app.moneymanager.com
MONEY_MANAGER_BACKEND_URL=https://api.moneymanager.com
```

---

## 5. How to Run Each Phase

### Phase 1: Local Development
```bash
# Run with active profile via Maven
mvn spring-boot:run -Dspring-boot.run.profiles=development

# Or run the built jar
java -jar -Dspring.profiles.active=development target/MoneyManagement-0.0.1-SNAPSHOT.jar
```

### Phase 2: Automated Tests in CI/CD (Jenkinsfile)
During `mvn test`, Maven automatically uses `src/test/resources/application.yml` and activates the `test` profile:
```bash
mvn test -Dspring.profiles.active=test
```

In `Jenkinsfile`:
```groovy
stage('Test + SonarQube') {
    environment {
        SPRING_PROFILES_ACTIVE = 'test'
    }
    steps {
        sh 'mvn test'
    }
}
```

### Phase 3: Docker-Compose (Multi-Container)
```bash
# Run docker compose using the .env.docker file
docker compose --env-file .env.docker up --build -d
```

### Phase 4: Production Deployment
```bash
# Option A: Standalone JAR
java -jar -Dspring.profiles.active=prod target/MoneyManagement-0.0.1-SNAPSHOT.jar

# Option B: Docker Container with environment variables
docker run -d \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DB_HOST=rds-mysql.amazonaws.com \
  -e DB_USERNAME=prod_user \
  -e DB_PASSWORD=prod_pass \
  -e BREVO_LOGIN=prod_login \
  -e BREVO_PASSWORD=prod_pass \
  --name moneymanagement-prod \
  rishu2801/money-management:latest
```
