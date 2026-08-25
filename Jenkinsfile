pipeline {
    agent none

    environment {
        DOCKER_IMAGE = 'rishu2801/money-management'
        DOCKER_TAG = "${env.BUILD_NUMBER}"
        SONAR_PROJECT_KEY = 'MoneyManagement'
    }

    stages {
        stage('Build') {
            agent { label 'build-agent' }
            environment {
                MAVEN_OPTS = '-Xmx1024m'
            }
            steps {
                checkout scm
                sh 'mvn clean package -DskipTests'

                stash includes: 'target/*.jar', excludes: 'target/*.jar.original', name: 'jar-artifact'
                stash includes: 'src/**, pom.xml, target/classes/**, target/test-classes/**', name: 'test-artifacts'
                stash includes: 'Dockerfile', name: 'dockerfile'
                stash includes: 'docker-compose.yaml', name: 'docker-compose'
                stash includes: '**', name: 'full-project-for-owasp'
            }
        }

     stage('Test + SonarQube') {
    agent { label 'test-agent' }
    options { skipDefaultCheckout() }
    environment {
        SPRING_PROFILES_ACTIVE = 'test'
    }
    steps {
        unstash 'test-artifacts'
        sh 'mvn test'   // generate coverage and test reports

        withSonarQubeEnv('SonarQube') {
            sh '''
                mvn org.sonarsource.scanner.maven:sonar-maven-plugin:sonar \
                  -Dsonar.projectKey=$SONAR_PROJECT_KEY \
                  -Dsonar.host.url=$SONAR_HOST_URL \
                  -Dsonar.login=$SONAR_TOKEN \
                  -Dsonar.scm.disabled=true \
                  -Dsonar.java.libraries=target/classes
                  -Dsonar.test.exclusions=**/src/test/**
                  
            '''
        }

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

        stage('Docker Build, Scan, Test & Push') {
            agent { label 'docker-agent' }
            options { skipDefaultCheckout() }
            steps {
                unstash 'jar-artifact'
                unstash 'dockerfile'
                unstash 'docker-compose'

                // Build Docker image
                sh "docker build -t ${DOCKER_IMAGE}:${DOCKER_TAG} -t money-management-app:latest ."

                // Trivy security vulnerability scan
                sh "trivy image --severity CRITICAL --exit-code 1 ${DOCKER_IMAGE}:${DOCKER_TAG}"

                // Ephemeral in-container smoke test & health check
                script {
                    echo "🚀 Starting ephemeral containers to test Docker boot & SQL seeding..."
                    sh '''
                        DB_ROOT_PASSWORD=Admin \
                        DB_NAME=MoneyManagement \
                        DB_USERNAME=admin \
                        DB_PASSWORD=Admin \
                        JPA_DDL_AUTO=update \
                        BREVO_LOGIN=test \
                        BREVO_PASSWORD=test \
                        BREVO_MAIL=test@example.com \
                        docker compose up -d
                    '''

                    echo "⏳ Waiting for App to become healthy on /actuator/health..."
                    sh '''
                        HEALTHY=false
                        for i in $(seq 1 30); do
                            if curl -s http://localhost:8009/actuator/health | grep '"status":"UP"'; then
                                echo "✅ Spring Boot Application & MySQL are UP and HEALTHY!"
                                HEALTHY=true
                                break
                            fi
                            echo "Waiting for app to start... ($i/30)"
                            sleep 3
                        done

                        if [ "$HEALTHY" != "true" ]; then
                            echo "❌ Actuator Health check failed! Container logs:"
                            docker logs money-app-container
                            exit 1
                        fi

                        echo "🔍 Testing API response..."
                        curl -f -s http://localhost:8009/api/home || {
                            echo "❌ API check /api/home failed!"
                            docker logs money-app-container
                            exit 1
                        }

                        echo "✅ Database & Docker smoke tests passed!"
                    '''
                }

                // Push to Docker Hub after health & smoke tests succeed
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
            post {
                always {
                    // Clean up test containers and volumes from the agent
                    sh 'docker compose down -v || true'
                }
            }
        }
    }

    post {
        success {
            echo 'CI pipeline completed successfully. Triggering IaC deployment...'
            build job: 'money-app-IaC', parameters: [
                string(name: 'ENVIRONMENT', value: 'prod'),
                string(name: 'AWS_REGION', value: 'ap-south-1'),
                string(name: 'ACTION', value: 'Deploy'),
                string(name: 'DOCKER_TAG', value: env.BUILD_NUMBER)
            ], wait: false
        }
        failure {
            echo 'CI pipeline failed, skipping IaC.'
        }
        always {
            echo "Build Number: ${BUILD_NUMBER}"
        }
    }
}
