# Engineering Blog: Building an Enterprise-Grade Multi-Node CI/CD Pipeline & 4-Tier Environment Strategy for Spring Boot

> **Author:** Rishabh Kumar & DevOps Engineering Team  
> **Project:** MoneyManagement (Spring Boot 3, Java 21, MySQL, Docker, Jenkins)  
> **Date:** August 2026  
> **Architecture:** Distributed LXC Multi-Agent Nodes + Jenkins Pipeline + Automated Security & Code Quality Gates

---

## 1. Executive Summary & Architectural Vision

Modern enterprise applications require CI/CD pipelines that are **fast**, **isolated**, **secure**, and **cost-efficient**. Rather than running every build step sequentially on a single bloated monolithic Jenkins controller, we engineered a **distributed, multi-agent CI/CD infrastructure** backed by Linux Containers (LXC/LXD) combined with a **4-tier environment profile strategy** (`development`, `test`, `docker`, `prod`).

This engineering documentation details the entire technical journey: from containerized agent provisioning and security hardening to artifact-passing pipeline choreography and zero-secret production deployment.

```
                                 [ Developer Push ]
                                         │
                                         ▼
                                 [ GitHub Webhook ]
                                         │
                                         ▼
                         ┌───────────────────────────────┐
                         │   Jenkins Controller Master   │
                         │       (Orchestration)         │
                         └───────────────┬───────────────┘
                                         │
         ┌───────────────────────────────┼───────────────────────────────┐
         ▼                               ▼                               ▼
┌───────────────────┐           ┌───────────────────┐           ┌───────────────────┐
│   Build Agent     │           │    Test Agent     │           │  Security Agent   │
│ (LXC: Java21,Maven│           │(LXC: Java21, Sonar│           │ (LXC: Java21 JRE, │
│       Git)        │           │     Scanner)      │           │    OWASP Scan)    │
└────────┬──────────┘           └────────┬──────────┘           └────────┬──────────┘
         │                               │                               │
         │ Stash JAR & Classes           │ mvn test + SonarQube          │ Dependency Check
         └───────────────────────────────┼───────────────────────────────┘
                                         │ (Quality Gates Pass)
                                         ▼
                                ┌───────────────────┐
                                │   Docker Agent    │
                                │ (LXC: Docker CLI, │
                                │   Trivy Scanner)  │
                                └────────┬──────────┘
                                         │
                                         ├── Docker Build & Trivy Scan
                                         ├── Push to Docker Hub
                                         ▼
                             [ Production Deployment ]
```

---

## 2. Distributed LXC Multi-Node Infrastructure

### Why LXC Containers Over Heavy Virtual Machines?
- **Near-Zero Virtualization Overhead:** LXC shares the Linux host kernel while offering strict process, network, and mount isolation.
- **Fast Startup:** Nodes boot and connect via SSH remoting in less than 3 seconds.
- **Resource Control:** Granular CPU and RAM capping per node on an 8 GB host.

### Dedicated Network Topology
We created a custom virtual network bridge on the host:
- **Bridge Name:** `jenkins-agent` (or `jenkins-net`)
- **Subnet:** `10.100.0.1/24` (with NAT and DHCP enabled)
- **Host Gateway IP:** `10.100.0.1`

### Agent Node Matrix & Tool Specialization
To enforce strict separation of concerns, eliminate tool clutter, and reduce container image sizes, each agent was provisioned with **only** the binaries required for its specific role:

| Agent Name | Label | Base Packages | Specialized Tools | Removed Binaries |
| :--- | :--- | :--- | :--- | :--- |
| `jenkins-agent-build` | `build-agent` | OpenJDK 21 (Full JDK), Curl, Unzip, SSH | Git, Apache Maven 3.6+ | None (Root of compilation) |
| `jenkins-agent-test` | `test-agent` | OpenJDK 21 (Full JDK), Maven, SSH | SonarQube Scanner CLI (`/opt/sonar-scanner-...`) | Git (receives stashed classes) |
| `jenkins-agent-security` | `security-agent` | OpenJDK 21 (JRE Headless), Curl, Unzip, SSH | OWASP Dependency-Check CLI (`/opt/dependency-check`) | Git, Maven, SonarQube |
| `jenkins-agent-docker` | `docker-agent` | OpenJDK 21 (JRE Headless), Curl, SSH | Docker Engine (CE), Docker Compose, Trivy Scanner | Git, Maven, SonarQube |

---

## 3. The 4-Tier Environment & Profile Architecture

A major flaw in naive deployments is hardcoding credentials or having configurations coupled to a single environment. We implemented a **4-tier Spring Boot profile strategy**:

```
src/main/resources/
├── application.yml                 <-- Base configuration (Common defaults & dynamic ${...} placeholders)
├── application-development.yml     <-- Local Dev (MySQL localhost:3306, show-sql: true, DEBUG logs)
├── application-test.yml            <-- CI & Automated Tests (In-Memory H2 DB, ddl-auto: create-drop, mock mail)
├── application-docker.yml          <-- Docker Compose (DB Host: 'mysql', Container port 8009)
└── application-prod.yml            <-- Production (ddl-auto: validate, show-sql: false, Cloud RDS, SSL)
```

### Profile Matrix Comparison

| Configuration Key | `development` | `test` | `docker` | `prod` |
| :--- | :--- | :--- | :--- | :--- |
| **Database Engine** | MySQL 8.0 | H2 In-Memory (MySQL Mode) | MySQL 8.0 (Container) | Amazon RDS / Managed MySQL |
| **Database URL** | `jdbc:mysql://localhost:3306/...` | `jdbc:h2:mem:moneymanagement_test` | `jdbc:mysql://mysql:3306/...` | `jdbc:mysql://rds-endpoint:3306/...` |
| **Hibernate DDL** | `update` | `create-drop` | `update` | `validate` (Protects schema) |
| **SQL Logging** | `true` (Formatted) | `false` | `true` | `false` |
| **Mail Service** | Brevo SMTP (Dev credentials) | Mock / Dummy (`localhost:25`) | Brevo SMTP | Brevo SMTP (Production Key) |
| **Activation URL** | `http://localhost:8080` | `http://localhost:8080` | `http://localhost:8009` | `https://api.moneymanager.com` |
| **Frontend URL** | `http://localhost:5173` | `http://localhost:5173` | `http://localhost:5173` | `https://app.moneymanager.com` |

---

## 4. Zero-Trust Production Secret Management

### Core Principle
> **Never commit `.env` files into Git, and never bake credentials into Docker images.**

1. **Git Repository:** Contains only `application.yml` with placeholder syntax (e.g., `password: ${DB_PASSWORD:}`).
2. **Docker Artifacts:** Completely environment-agnostic; the same image tag is promoted from testing to production.
3. **Runtime Injection:**
   - **Local Dev:** `.env` loaded via IntelliJ EnvFile plugin or manual run configs.
   - **Docker Compose:** `.env.docker` passed via `docker compose --env-file .env.docker up`.
   - **Production:** Injected via Jenkins Credentials store (`withCredentials`), Cloud Secrets Manager (AWS Secrets Manager / Vault), or locked server env files (`sudo chmod 600 /etc/moneymanager/prod.env`).

---

## 5. End-to-End Jenkins Pipeline Workflow (`Jenkinsfile`)

### Pipeline Design Philosophy
1. **No Duplicate Compilation or Testing:** The `Build` stage compiles once with `-DskipTests` to produce the JAR. Unit and integration tests run once during the `Test` stage.
2. **Parallel Security & Quality Scanning:** Code testing (`Test + SonarQube`) runs in parallel with third-party dependency scanning (`OWASP Dependency-Check`).
3. **Artifact Stashing Across LXC Nodes:** Source files, compiled classes, and the JAR are passed seamlessly using Jenkins Remoting stashes.
4. **Strict Security Gates:** Trivy and SonarQube Quality Gates fail the build before anything reaches the container registry.

### Declarative Pipeline Implementation

```groovy
pipeline {
    agent none

    environment {
        DOCKER_IMAGE = 'rishu2801/money-management'
        DOCKER_TAG = "${env.BUILD_NUMBER}"
        SONAR_PROJECT_KEY = 'MoneyManagement'
    }

    stages {
        // =================================================================
        // STAGE 1: COMPILATION & ARTIFACT STASHING (Build Agent)
        // =================================================================
        stage('Build') {
            agent { label 'build-agent' }
            environment {
                SPRING_PROFILES_ACTIVE = 'test'
                MAVEN_OPTS = '-Xmx1024m'
            }
            steps {
                checkout scm
                
                // Compile & Package (skipping tests for downstream execution)
                sh 'mvn clean package -DskipTests'

                // Stash artifacts for downstream agents
                stash includes: 'target/*.jar', excludes: 'target/*.jar.original', name: 'jar-artifact'
                stash includes: 'src/**, pom.xml, target/classes/**, target/test-classes/**', name: 'test-artifacts'
                stash includes: 'Dockerfile', name: 'dockerfile'
                stash includes: '**', name: 'full-project-for-owasp'
            }
        }

        // =================================================================
        // STAGE 2: PARALLEL EXECUTION (Test + SonarQube & OWASP Dependency)
        // =================================================================
        stage('Test & Security Analysis') {
            parallel {
                // Track A: Unit/Integration Tests + SonarQube Quality Gate
                stage('Test + SonarQube') {
                    agent { label 'test-agent' }
                    options { skipDefaultCheckout() }
                    environment {
                        SPRING_PROFILES_ACTIVE = 'test'
                    }
                    steps {
                        unstash 'test-artifacts'
                        
                        // Execute tests against in-memory H2 database
                        sh 'mvn test'

                        // Static Code Analysis via SonarScanner
                        withSonarQubeEnv('SonarQube') {
                            sh """
                                sonar-scanner \
                                  -Dsonar.projectKey=${SONAR_PROJECT_KEY} \
                                  -Dsonar.sources=src/main/java \
                                  -Dsonar.tests=src/test/java \
                                  -Dsonar.java.binaries=target/classes
                            """
                        }

                        // Enforce SonarQube Quality Gate
                        script {
                            timeout(time: 1, unit: 'HOURS') {
                                def qg = waitForQualityGate()
                                if (qg.status != 'OK') {
                                    error "SonarQube Quality Gate failed: ${qg.status}"
                                }
                            }
                        }
                    }
                }

                // Track B: Software Composition Analysis (OWASP Dependency-Check)
                stage('Dependency Scan (OWASP)') {
                    agent { label 'security-agent' }
                    options { skipDefaultCheckout() }
                    steps {
                        unstash 'full-project-for-owasp'

                        retry(3) {
                            withCredentials([string(credentialsId: 'nvd-api-key', variable: 'NVD_API_KEY')]) {
                                sh '''
                                    mvn org.owasp:dependency-check-maven:13.0.0:check \
                                      -DnvdApiKey=$NVD_API_KEY \
                                      -DdataDirectory=/home/jenkins/dependency-check-data
                                '''
                            }
                        }

                        publishHTML([
                            target: [
                                allowMissing: true,
                                alwaysLinkToLastBuild: true,
                                keepAll: true,
                                reportDir: 'target',
                                reportFiles: 'dependency-check-report.html',
                                reportName: 'OWASP Dependency-Check Report'
                            ]
                        ])
                    }
                }
            }
        }

        // =================================================================
        // STAGE 3: CONTAINERIZATION, IMAGE SCANNING & REGISTRY PUSH
        // =================================================================
        stage('Docker Build, Scan, Push') {
            agent { label 'docker-agent' }
            options { skipDefaultCheckout() }
            steps {
                unstash 'jar-artifact'
                unstash 'dockerfile'

                // Build local Docker Image
                sh "docker build -t ${DOCKER_IMAGE}:${DOCKER_TAG} ."

                // Scan Docker Image with Trivy (Fail on CRITICAL vulnerabilities)
                sh "trivy image --severity CRITICAL --exit-code 1 ${DOCKER_IMAGE}:${DOCKER_TAG}"

                // Authenticate and push to Docker Registry
                withCredentials([
                    usernamePassword(
                        credentialsId: 'docker-registry-credentials',
                        usernameVariable: 'DOCKER_USER',
                        passwordVariable: 'DOCKER_PASS'
                    )
                ]) {
                    sh '''
                        echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin
                    '''
                    sh "docker push ${DOCKER_IMAGE}:${DOCKER_TAG}"
                }
            }
        }
    }

    post {
        success {
            echo '✅ Pipeline completed successfully! Image published and ready for deploy.'
        }
        failure {
            echo '❌ Pipeline failed. Review the logs, SonarQube quality gate, or Trivy report.'
        }
        always {
            echo "Build Number: ${BUILD_NUMBER}"
        }
    }
}
```

---

## 6. Key Engineering Challenges Solved

### 1. The Redundant Test Execution Dilemma
- **Issue:** Running unit and integration tests during the initial compilation and then re-running tests during SonarQube analysis doubled the pipeline runtime.
- **Solution:** Added `-DskipTests` during the initial `Build` phase, stashed `target/classes` and `target/test-classes`, and ran the full suite once on `test-agent`.

### 2. Broken Docker Repositories & Subshell Escaping
- **Issue:** Scripts injecting `$(lsb_release -cs)` inside nested double quotes corrupted `/etc/apt/sources.list.d/docker.list`.
- **Solution:** Switched to explicit distribution codenames (`jammy`) with gpg keyrings under `/usr/share/keyrings/docker-archive-keyring.gpg` and single-quoted heredocs.

### 3. SonarQube Service Daemonization under Systemd
- **Issue:** SonarQube’s `sonar.sh start` backgrounded itself, causing systemd (set to `Type=simple`) to register an unexpected exit and trigger restart loops (`start-limit-hit`).
- **Solution:** Updated systemd unit file to `Type=forking` and increased `vm.max_map_count=524288` for Elasticsearch.

### 4. Agent SSH Remoting Java Mismatch
- **Issue:** Agents running Java 11 could not execute Java 21 bytecode compiled by the project.
- **Solution:** Standardized OpenJDK 21 across all LXC agent containers and synchronized system alternatives.

---

## 7. Operational Runbook: Executing Each Phase

### 1. Running Locally (`development`)
```bash
# In IntelliJ: set active profile to development
# Or CLI:
mvn spring-boot:run -Dspring-boot.run.profiles=development
```

### 2. Triggering CI/CD Automated Tests (`test`)
```bash
# Automated via Jenkins on Git push:
mvn test -Dspring.profiles.active=test
```

### 3. Running Containerized Stack (`docker`)
```bash
# Spin up MySQL container and Application container together:
docker compose --env-file .env.docker up --build -d
```

### 4. Production Release (`prod`)
```bash
# Run production container with runtime credentials:
docker run -d \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DB_HOST=rds.yourdomain.internal \
  -e DB_USERNAME=prod_user \
  -e DB_PASSWORD=$PROD_DB_PASS \
  -e BREVO_LOGIN=$PROD_BREVO_LOGIN \
  -e BREVO_PASSWORD=$PROD_BREVO_PASS \
  --name moneymanagement-prod \
  rishu2801/money-management:latest
```

---

## 8. Conclusion

By combining **LXC container isolation**, **specialized lightweight build nodes**, **SonarQube & Trivy quality gates**, and a **4-tier Spring Boot profile strategy**, this project demonstrates a modern, battle-tested CI/CD and deployment pipeline capable of scaling seamlessly from developer laptops to enterprise production clusters.
