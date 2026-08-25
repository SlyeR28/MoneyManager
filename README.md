# Money Management — Backend API

A personal finance REST API built with **Spring Boot 3.5**, **Java 21**, and **MySQL**. Tracks income, expenses, categories, and provides dashboard analytics — all secured with stateless **JWT authentication**.

Backed by a production-grade CI/CD pipeline running across **four dedicated LXC Jenkins agents**, with automated quality gates (SonarQube + JaCoCo), container security scanning (Trivy + OWASP), and continuous delivery onto **AWS infrastructure provisioned via CloudFormation (IaC)**.

---

## Table of Contents

- [Tech Stack](#tech-stack)
- [Architecture Overview](#architecture-overview)
- [Features](#features)
- [Project Structure](#project-structure)
- [API Endpoints](#api-endpoints)
- [Environment Profiles](#environment-profiles)
- [Local Development Setup](#local-development-setup)
- [Running with Docker Compose](#running-with-docker-compose)
- [Running Tests](#running-tests)
- [CI Pipeline — Jenkins Multi-Node](#ci-pipeline--jenkins-multi-node)
- [CD Pipeline — AWS CloudFormation IaC](#cd-pipeline--aws-cloudformation-iac)
- [LXC Agent Infrastructure](#lxc-agent-infrastructure)
- [Seeded Demo Data](#seeded-demo-data)
- [Security](#security)
- [Quick API Test](#quick-api-test)
- [Docker Image](#docker-image)

---

## Tech Stack

| Layer | Technology |
| :--- | :--- |
| Language | Java 21 (preview features enabled) |
| Framework | Spring Boot 3.5 |
| Security | Spring Security 6 — Stateless JWT (JJWT 0.12) |
| Persistence | Spring Data JPA / Hibernate + MySQL 8.0 |
| Mapping | MapStruct 1.6 |
| Validation | Jakarta Validation |
| Email | Brevo (Sendinblue) SMTP |
| Scheduling | Spring `@EnableScheduling` |
| Testing | JUnit 5, Spring Boot Test, H2 In-Memory DB |
| Coverage | JaCoCo — ≥ 80% new-code gate enforced |
| Code Quality | SonarQube (self-hosted) |
| Containerization | Docker + Docker Compose |
| Container Runtime | Eclipse Temurin 21 JRE |
| CI | Jenkins — declarative multi-node pipeline |
| Agent Infrastructure | LXC Linux Containers (4 dedicated nodes) |
| CD / IaC | AWS CloudFormation — VPC, EC2, RDS, Security Groups |
| Container Registry | Docker Hub (`rishu2801/money-management`) |

---

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────────────┐
│                        DEVELOPER MACHINE                            │
│   git push → GitHub → Webhook trigger                               │
└───────────────────────────────┬─────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────────┐
│                    JENKINS CONTROLLER (Ubuntu VM)                   │
│                     localhost:8080 — Orchestration                  │
└──────┬──────────────────┬─────────────────┬────────────────┬────────┘
       │                  │                 │                │
       ▼                  ▼                 ▼                ▼
┌────────────┐  ┌──────────────────┐  ┌──────────┐  ┌──────────────┐
│Build Agent │  │   Test Agent     │  │ Security │  │Docker Agent  │
│(LXC Node)  │  │  (LXC Node)      │  │  Agent   │  │ (LXC Node)   │
│            │  │                  │  │(LXC Node)│  │              │
│ Java 21    │  │ Java 21 + Maven  │  │ Java 21  │  │ Docker CE    │
│ Maven, Git │  │ SonarQube Scanner│  │ OWASP DC │  │ Trivy        │
│            │  │ JaCoCo Report    │  │          │  │ Docker Compose│
│ mvn clean  │  │ mvn verify       │  │ NVD CVE  │  │              │
│ package    │  │ Quality Gate     │  │ Scan     │  │ Build Image  │
│ -DskipTests│  │ ≥80% coverage    │  │          │  │ Trivy Scan   │
│            │  │                  │  │          │  │ Push to Hub  │
│ Stash:     │  │ Unstash sources  │  │ Unstash  │  │ Smoke Test   │
│ JAR+Sources│  │ pom.xml          │  │ full     │  │ /actuator    │
└────────────┘  └──────────────────┘  └──────────┘  └──────┬───────┘
                                                            │
                                                            │  trigger
                                                            ▼
┌─────────────────────────────────────────────────────────────────────┐
│               money-app-IaC  — Jenkins CD Pipeline                  │
│                  CloudFormation Stack Deployment                    │
└───────────────────────────────┬─────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────────┐
│                        AWS — ap-south-1                             │
│                                                                     │
│  ┌─────────────────────────────────────────────────────────────┐   │
│  │  VPC (Custom)                                               │   │
│  │                                                             │   │
│  │  ┌──────────────────┐        ┌──────────────────────────┐  │   │
│  │  │  Public Subnet   │        │    Private Subnet        │  │   │
│  │  │                  │        │                          │  │   │
│  │  │  ┌────────────┐  │        │  ┌────────────────────┐  │  │   │
│  │  │  │  EC2 (App) │  │◄──────►│  │  RDS MySQL 8.0     │  │  │   │
│  │  │  │  Docker    │  │        │  │  (Private, no      │  │  │   │
│  │  │  │  Container │  │        │  │   public access)   │  │  │   │
│  │  │  └────────────┘  │        │  └────────────────────┘  │  │   │
│  │  │  Internet Gateway│        │                          │  │   │
│  │  └──────────────────┘        └──────────────────────────┘  │   │
│  └─────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────┘
```

---

## Features

**Authentication**
- User registration with email-based activation (UUID token)
- JWT login — all protected endpoints require `Authorization: Bearer <token>`
- BCrypt password hashing (cost factor 14)
- Inactive account guard — blocked at login until email is confirmed

**Expense Management**
- Add, delete, and fetch expenses linked to a category
- Current-month view, latest 5, and running total

**Income Management**
- Add, delete, and fetch income records linked to a category
- Current-month view, latest 5, and running total

**Categories**
- Create and update custom categories per user
- Filter by type (`income` / `expense`)

**Transaction Filtering**
- Filter income or expenses by date range, keyword, sort field, and sort direction

**Dashboard**
- Aggregated summary — totals, recent transactions, and category breakdowns

---

## Project Structure

```
MoneyManagement/
├── Jenkinsfile                          # CI pipeline — 4-stage multi-node
├── Dockerfile                           # Eclipse Temurin 21 JRE, port 8009
├── docker-compose.yaml                  # MySQL + App container stack
├── pom.xml                              # Maven build — JaCoCo, MapStruct, JJWT
├── .env.example                         # Environment variable template
│
└── src/
    ├── main/
    │   ├── java/org/moneymanagement/
    │   │   ├── config/              # DatabaseSeeder (docker/prod demo data)
    │   │   ├── controller/          # REST controllers (7 controllers)
    │   │   ├── entity/              # JPA entities
    │   │   ├── exception/           # ResourceNotFoundException, UnauthorizedException, etc.
    │   │   ├── mappers/             # MapStruct DTO ↔ Entity mappers
    │   │   ├── payload/
    │   │   │   ├── request/         # AuthRequest, ExpenseRequest, FilterDto, etc.
    │   │   │   └── response/        # AuthResponse, ExpenseResponse, etc.
    │   │   ├── repository/          # Spring Data JPA repositories
    │   │   ├── security/            # JWT filter, JwtUtils, UserDetails, SecurityConfig
    │   │   ├── service/             # Service interfaces + implementations
    │   │   └── MoneyManagementApplication.java
    │   │
    │   └── resources/
    │       ├── application.yml                # Base config — ${placeholder} defaults
    │       ├── application-development.yml    # Local — MySQL localhost, debug logging
    │       ├── application-test.yml           # CI — H2 in-memory, mock mail
    │       ├── application-docker.yml         # Docker — MySQL container, port 8009
    │       └── application-prod.yml           # Prod — ddl validate, no SQL logging
    │
    └── test/
        ├── java/org/moneymanagement/
        │   ├── controller/          # Integration tests (MockMvc, full context)
        │   ├── repository/          # Repository layer tests
        │   ├── security/            # JWT filter, JwtUtils, UserDetails tests
        │   └── service/             # Service unit tests (Mockito)
        └── resources/
            └── data.sql             # H2 seed: 5 users, 30 categories, 30 expenses, 30 incomes
```

---

## API Endpoints

All protected endpoints require: `Authorization: Bearer <jwt_token>`

### Auth — `/api/v1`

| Method | Endpoint | Auth | Description |
| :--- | :--- | :---: | :--- |
| `POST` | `/api/v1/register` | — | Register a new account |
| `GET` | `/api/v1/activation?token={token}` | — | Activate account via email link |
| `POST` | `/api/v1/login` | — | Login — returns JWT token + email |

### Expenses — `/api/expenses`

| Method | Endpoint | Auth | Description |
| :--- | :--- | :---: | :--- |
| `POST` | `/api/expenses/add` | ✓ | Add a new expense |
| `GET` | `/api/expenses/get` | ✓ | Current month expenses |
| `GET` | `/api/expenses/top5` | ✓ | Latest 5 expenses |
| `GET` | `/api/expenses/total` | ✓ | Total expense amount |
| `DELETE` | `/api/expenses/{id}` | ✓ | Delete expense by ID |

### Incomes — `/api/incomes`

| Method | Endpoint | Auth | Description |
| :--- | :--- | :---: | :--- |
| `POST` | `/api/incomes/add` | ✓ | Add a new income record |
| `GET` | `/api/incomes/get` | ✓ | Current month incomes |
| `GET` | `/api/incomes/top5` | ✓ | Latest 5 incomes |
| `GET` | `/api/incomes/total` | ✓ | Total income amount |
| `DELETE` | `/api/incomes/{id}` | ✓ | Delete income by ID |

### Categories — `/api/category`

| Method | Endpoint | Auth | Description |
| :--- | :--- | :---: | :--- |
| `POST` | `/api/category/create` | ✓ | Create a custom category |
| `GET` | `/api/category/` | ✓ | Get all categories for current user |
| `GET` | `/api/category/{type}` | ✓ | Filter by type: `income` or `expense` |
| `PUT` | `/api/category/{id}` | ✓ | Update an existing category |

### Filter — `/api/filter`

| Method | Endpoint | Auth | Description |
| :--- | :--- | :---: | :--- |
| `POST` | `/api/filter/` | ✓ | Filter by type, date range, keyword, and sort |

**Filter request body:**
```json
{
  "type": "expense",
  "startDate": "2026-08-01",
  "endDate": "2026-08-31",
  "keyword": "groceries",
  "sortField": "date",
  "sortOrder": "desc"
}
```

### Dashboard — `/dashboard`

| Method | Endpoint | Auth | Description |
| :--- | :--- | :---: | :--- |
| `GET` | `/dashboard/` | ✓ | Full analytics summary |

### Utility

| Method | Endpoint | Auth | Description |
| :--- | :--- | :---: | :--- |
| `GET` | `/api/home` | — | Health check string |
| `GET` | `/actuator/**` | — | Spring Boot Actuator |

---

## Environment Profiles

| Property | `development` | `test` | `docker` | `prod` |
| :--- | :--- | :--- | :--- | :--- |
| Database | MySQL localhost:3306 | H2 In-Memory | MySQL container (mysql:3306) | AWS RDS MySQL 8.0 |
| DDL Mode | `update` | `create-drop` | `update` | `validate` |
| SQL Logging | `true` | `false` | `true` | `false` |
| Mail | Brevo SMTP (dev key) | Mock localhost:25 | Brevo SMTP | Brevo SMTP (prod key) |
| Port | `8080` | `8080` | `8009` | `8080` |

---

## Local Development Setup

**Prerequisites:** Java 21, Maven 3.6+, MySQL 8.0

**1. Clone**
```bash
git clone https://github.com/rishu2801/MoneyManagement.git
cd MoneyManagement
```

**2. Create local env file**
```bash
cp .env.example .env.local
```
Fill in your MySQL credentials and Brevo SMTP details:
```env
SPRING_PROFILES_ACTIVE=development
DB_HOST=localhost
DB_PORT=3306
DB_NAME=MoneyManagement
DB_USERNAME=root
DB_PASSWORD=your_password
BREVO_LOGIN=your_brevo_smtp_login
BREVO_PASSWORD=your_brevo_smtp_key
BREVO_MAIL=noreply@yourdomain.com
FRONTEND_URL=http://localhost:5173
MONEY_MANAGER_BACKEND_URL=http://localhost:8080
```

**3. Create the database**
```sql
CREATE DATABASE MoneyManagement;
```

**4. Run**
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=development
```
API available at `http://localhost:8080`

---

## Running with Docker Compose

No local MySQL needed — everything runs in containers.

**1. Create Docker env file**
```bash
cp .env.example .env.docker
```
```env
DB_ROOT_PASSWORD=Admin
DB_NAME=MoneyManagement
DB_USERNAME=admin
DB_PASSWORD=Admin
JPA_DDL_AUTO=update
BREVO_LOGIN=your_brevo_login
BREVO_PASSWORD=your_brevo_key
BREVO_MAIL=noreply@yourdomain.com
```

**2. Start**
```bash
docker compose --env-file .env.docker up --build -d
```
API available at `http://localhost:8009`

**3. Stop**
```bash
docker compose down -v
```

---

## Running Tests

Tests run against an **H2 in-memory database** automatically seeded by `data.sql`. No external services needed.

```bash
# Run all tests + generate JaCoCo coverage report
mvn verify
```

Coverage report: `target/site/jacoco/index.html`

The SonarQube Quality Gate enforces **≥ 80% coverage on new code**. The pipeline blocks the Docker build if this gate fails.

---

## CI Pipeline — Jenkins Multi-Node

The CI pipeline (`Jenkinsfile`) runs across four isolated **LXC-based Jenkins agents**, each carrying only the tools its stage needs.

### Agent Responsibilities

| Agent Label | Tools Installed | Stage |
| :--- | :--- | :--- |
| `build-agent` | Java 21 JDK, Maven, Git | Compile + package |
| `test-agent` | Java 21 JDK, Maven, SonarQube Scanner | Test + coverage + quality gate |
| `security-agent` | Java 21 JRE, OWASP Dependency-Check CLI | CVE dependency scan |
| `docker-agent` | Docker CE, Docker Compose, Trivy | Image build, scan, push, smoke test |

### Stage Flow

```
git push
    │
    ▼
[Build Agent] ─── mvn clean package -DskipTests
                   stash: jar-artifact, test-artifacts, dockerfile,
                          docker-compose, full-project-for-owasp
    │
    ▼
[Test Agent] ────── cleanWs()
                    unstash: test-artifacts (src + pom.xml only)
                    mvn verify
                      └─ JUnit tests against H2
                      └─ JaCoCo XML report → target/site/jacoco/jacoco.xml
                    mvn sonar:sonar
                      └─ -Dsonar.coverage.jacoco.xmlReportPaths=...
                    waitForQualityGate() ── BLOCKS if coverage < 80%
    │
    ├──────────────────────────────────────────────────────┐
    │                                                      │
    ▼                                                      ▼
[Test Agent continues]                          [Security Agent]
                                                unstash: full-project-for-owasp
                                                retry(3) OWASP Dependency-Check
                                                  └─ NVD CVE database scan
                                                publishHTML: dependency report
    │
    ▼
[Docker Agent] ─── unstash: jar-artifact, dockerfile, docker-compose
                   docker build -t rishu2801/money-management:${BUILD_NUMBER}
                   trivy image --severity CRITICAL --exit-code 1 ── BLOCKS on CRITICAL CVE
                   docker compose up -d (smoke test)
                     └─ curl /actuator/health → waits for "status":"UP"
                     └─ curl /api/home
                   docker push rishu2801/money-management:${BUILD_NUMBER}
                   docker compose down -v (cleanup)
    │
    ▼
[Jenkins Controller] ── build job: 'money-app-IaC'
                         params: ENVIRONMENT=prod, AWS_REGION=ap-south-1,
                                 ACTION=Deploy, DOCKER_TAG=${BUILD_NUMBER}
```

### Quality Gates — Pipeline Blockers

| Gate | Tool | Threshold | Effect |
| :--- | :--- | :--- | :--- |
| Unit test pass | JUnit 5 | All tests must pass | Blocks Sonar step |
| Code coverage | JaCoCo + SonarQube | ≥ 80% on new lines | Blocks Docker build |
| Known CVEs | OWASP Dependency-Check | Configurable | Report published |
| Container vulnerabilities | Trivy | CRITICAL severity | Blocks Docker push |

---

## CD Pipeline — AWS CloudFormation IaC

After the CI pipeline pushes the Docker image, it triggers the separate **`money-app-IaC`** Jenkins job, which deploys the application to AWS using CloudFormation stacks.

### AWS Region

`ap-south-1` (Mumbai)

### Infrastructure Provisioned by CloudFormation

```
AWS Account
└── VPC (Custom — dedicated CIDR block)
    ├── Internet Gateway
    │
    ├── Public Subnet
    │   ├── Route Table → Internet Gateway
    │   ├── EC2 Instance (Application Server)
    │   │   ├── Docker CE installed
    │   │   ├── Pulls rishu2801/money-management:${DOCKER_TAG} from Docker Hub
    │   │   ├── Runs container with prod environment variables
    │   │   └── Security Group: inbound 8080 (API), 22 (SSH)
    │   └── Elastic IP (stable public endpoint)
    │
    └── Private Subnet
        ├── RDS MySQL 8.0 Instance
        │   ├── No public accessibility
        │   └── Security Group: inbound 3306 from EC2 Security Group only
        └── DB Subnet Group (spans AZs for RDS placement)
```

### CD Trigger Parameters

The CI pipeline passes these parameters to the IaC job:

| Parameter | Value |
| :--- | :--- |
| `ENVIRONMENT` | `prod` |
| `AWS_REGION` | `ap-south-1` |
| `ACTION` | `Deploy` |
| `DOCKER_TAG` | Jenkins `${BUILD_NUMBER}` — exact image tag from CI |

### Deployment Flow

```
CI pipeline completes (image pushed to Docker Hub)
    │
    ▼
money-app-IaC job triggered (wait: false — non-blocking)
    │
    ▼
CloudFormation: Create or Update stack
    │
    ├── VPC + Subnets + IGW + Route Tables (if new)
    ├── Security Groups (EC2 ↔ RDS rules)
    ├── RDS MySQL — private subnet, no public access
    └── EC2 — public subnet
          └── UserData script:
                docker pull rishu2801/money-management:${DOCKER_TAG}
                docker run -d \
                  -p 8080:8080 \
                  -e SPRING_PROFILES_ACTIVE=prod \
                  -e DB_HOST=<rds-endpoint> \
                  -e DB_NAME=MoneyManagement \
                  -e DB_USERNAME=<from-secrets> \
                  -e DB_PASSWORD=<from-secrets> \
                  -e BREVO_LOGIN=<from-secrets> \
                  -e BREVO_PASSWORD=<from-secrets> \
                  --name moneymanagement-prod \
                  rishu2801/money-management:${DOCKER_TAG}
```

### Secret Management in Production

Secrets are never stored in the CloudFormation templates or committed to Git. They are injected at deploy time via:
- **Jenkins Credentials Store** — bound to pipeline environment variables using `withCredentials`
- **EC2 UserData** — environment variables passed to `docker run` at instance launch
- **AWS Secrets Manager** (recommended upgrade path) — retrieve via AWS CLI in UserData

---

## LXC Agent Infrastructure

Each Jenkins agent is a dedicated LXC Linux container running on the same Ubuntu host as the Jenkins controller. The containers share the host kernel but have isolated filesystems, network identities, and process spaces.

### Network Topology

| Component | Detail |
| :--- | :--- |
| Bridge name | `jenkins-agent` / `jenkins-net` |
| Subnet | `10.100.0.1/24` |
| Host gateway IP | `10.100.0.1` |
| NAT + DHCP | Enabled — containers reach the internet via the host |

### Agent Node Details

| Container Hostname | Jenkins Label | Key Tools | Purpose |
| :--- | :--- | :--- | :--- |
| `jenkins-agent-build` | `build-agent` | Java 21 JDK, Maven, Git | Source compile and JAR packaging |
| `jenkins-agent-test` | `test-agent` | Java 21 JDK, Maven, SonarQube Scanner CLI | Test execution, JaCoCo, SonarQube |
| `jenkins-agent-security` | `security-agent` | Java 21 JRE, OWASP Dependency-Check | Software composition analysis |
| `jenkins-agent-docker` | `docker-agent` | Java 21 JRE, Docker CE, Docker Compose, Trivy | Image build, scan, push, smoke test |

### SSH Agent Connection

Jenkins connects to each agent over SSH using the SSH Build Agents plugin. The verified connection sequence:

```
Host-key verification
    → SSH authentication (Jenkins credential)
    → SFTP: copy remoting.jar to agent home
    → java -jar remoting.jar -workDir /home/jenkins
    → Channel established
    → "Agent successfully connected and online"
```

### Agent Account

```
User:      jenkins
Home:      /home/jenkins
Shell:     /bin/bash
```

### Validation Commands (run on each agent as jenkins user)

```bash
java -version       # Must show openjdk 21
git --version       # build-agent only
mvn -version        # build-agent and test-agent
docker --version    # docker-agent only
```

---

## Seeded Demo Data

### Docker / Prod Profile (`DatabaseSeeder.java`)

| Name | Email | Password |
| :--- | :--- | :--- |
| Alex Morgan | `alex@moneymanager.com` | `Password@123` |
| Sarah Jenkins | `sarah@moneymanager.com` | `Password@123` |
| David Miller | `david@moneymanager.com` | `Password@123` |
| Emma Watson | `emma@moneymanager.com` | `Password@123` |
| Admin User | `admin@moneymanager.com` | `Password@123` |

### Test Profile (`data.sql` — H2)

- 5 user profiles (1 active, 1 inactive/pending activation, 3 active)
- 30 categories (15 income types, 15 expense types)
- 30 income records
- 30 expense records
- Auto-increment sequences reset at 1000 to avoid collision with runtime inserts

---

## Security

| Concern | Implementation |
| :--- | :--- |
| Authentication | Stateless JWT — `Authorization: Bearer <token>` header |
| Password storage | BCrypt, cost factor 14 |
| Session | None — `SessionCreationPolicy.STATELESS` |
| CSRF | Disabled (not needed for stateless JWT APIs) |
| Public routes | `/api/home`, `/api/v1/register`, `/api/v1/login`, `/api/v1/activation`, `/h2-console/**`, `/actuator/**` |
| All other routes | Require valid JWT |
| CORS | Configured — tighten `allowedOriginPatterns` for production |
| Secrets in CI | Jenkins Credentials store — never committed to Git |
| Container security | Trivy scans every image build — blocks on CRITICAL CVEs |
| Dependency security | OWASP Dependency-Check — NVD CVE database scan on every build |
| DB in production | RDS in private subnet — no public accessibility, EC2-only inbound on port 3306 |

---

## Quick API Test

**Base URLs:**
- Docker Compose: `http://localhost:8009`
- Local dev: `http://localhost:8080`

```powershell
# Step 1 — Login and capture token
$response = Invoke-RestMethod -Uri "http://localhost:8009/api/v1/login" `
  -Method POST -ContentType "application/json" `
  -Body '{"email":"alex@moneymanager.com","password":"Password@123"}'
$TOKEN = $response.token

# Step 2 — Set auth headers
$headers = @{ "Authorization" = "Bearer $TOKEN"; "Content-Type" = "application/json" }

# Step 3 — Dashboard summary
Invoke-RestMethod -Uri "http://localhost:8009/dashboard/" -Method GET -Headers $headers

# Step 4 — Add expense
$body = '{"name":"Supermarket Run","amount":75.50,"categoryId":16,"date":"2026-08-25"}'
Invoke-RestMethod -Uri "http://localhost:8009/api/expenses/add" -Method POST -Headers $headers -Body $body

# Step 5 — Add income
$body = '{"name":"Freelance Project","amount":1200.00,"categoryId":2,"date":"2026-08-25"}'
Invoke-RestMethod -Uri "http://localhost:8009/api/incomes/add" -Method POST -Headers $headers -Body $body

# Step 6 — Filter transactions
$body = '{"type":"expense","startDate":"2026-08-01","endDate":"2026-08-31","keyword":"","sortField":"date","sortOrder":"desc"}'
Invoke-RestMethod -Uri "http://localhost:8009/api/filter/" -Method POST -Headers $headers -Body $body

# Step 7 — Get categories by type
Invoke-RestMethod -Uri "http://localhost:8009/api/category/expense" -Method GET -Headers $headers
```

---

## Docker Image

```bash
# Pull latest
docker pull rishu2801/money-management:latest

# Run standalone (production mode)
docker run -d \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DB_HOST=your-rds-endpoint \
  -e DB_NAME=MoneyManagement \
  -e DB_USERNAME=prod_user \
  -e DB_PASSWORD=prod_pass \
  -e BREVO_LOGIN=your_brevo_login \
  -e BREVO_PASSWORD=your_brevo_key \
  -e BREVO_MAIL=noreply@yourdomain.com \
  --name moneymanagement \
  rishu2801/money-management:latest
```

The container exposes port `8009` internally (docker profile) or `8080` (prod profile). Map accordingly with `-p`.
