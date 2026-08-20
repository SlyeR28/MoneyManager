pipeline {
    agent none

    environment {
        // Docker image repository (change to your Docker Hub username/repo)
        DOCKER_IMAGE = 'your-dockerhub-username/money-management'
        DOCKER_TAG = "${env.BUILD_NUMBER}"

        // SonarCloud configuration (replace with your actual values)
        SONAR_HOST_URL = 'https://sonarcloud.io'
        SONAR_ORGANIZATION = 'your-org-key'          // e.g., 'slyer28'
        SONAR_PROJECT_KEY = 'your-project-key'       // e.g., 'SlyeR28_MoneyManagement'
    }

    stages {
        stage('Build') {
            agent { label 'build-agent' }
            environment {
                SPRING_PROFILES_ACTIVE = 'test'
                MAVEN_OPTS = '-Xmx1024m'
            }
            steps {
                // Checkout source code
                checkout scm

                // Compile and package JAR (skip tests to avoid duplication)
                sh 'mvn clean package -DskipTests'

                // Stash artifacts for downstream stages
                stash includes: 'target/*.jar', name: 'jar-artifact'
                stash includes: 'src/**, pom.xml, target/classes/**, target/test-classes/**', name: 'test-artifacts'
                stash includes: 'pom.xml', name: 'pom-for-owasp'
            }
        }

        stage('Test & Security') {
            parallel {
                // =====================================================
                // Stage A: Test + SonarQube (Test Agent)
                // =====================================================
                stage('Test + SonarQube') {
                    agent { label 'test-agent' }
                    environment {
                        SPRING_PROFILES_ACTIVE = 'test'
                    }
                    steps {
                        // Get source and compiled classes from Build
                        unstash 'test-artifacts'

                        // Run all tests
                        sh 'mvn test'

                        // SonarQube analysis (SonarCloud)
                        withCredentials([string(credentialsId: 'sonar-token', variable: 'SONAR_TOKEN')]) {
                            sh '''
                                sonar-scanner \
                                  -Dsonar.projectKey=${SONAR_PROJECT_KEY} \
                                  -Dsonar.organization=${SONAR_ORGANIZATION} \
                                  -Dsonar.host.url=${SONAR_HOST_URL} \
                                  -Dsonar.login=${SONAR_TOKEN}
                            '''
                        }
                    }
                }

                // =====================================================
                // Stage B: Dependency Scan (Security Agent)
                // =====================================================
                stage('Dependency Scan (OWASP)') {
                    agent { label 'security-agent' }
                    steps {
                        // Get pom.xml for dependency check
                        unstash 'pom-for-owasp'

                        // Run OWASP Dependency-Check
                        sh '''
                            dependency-check \
                              --project MoneyManagement \
                              --scan pom.xml \
                              --format HTML \
                              --out /tmp/dependency-report

                            # Fail if any CRITICAL vulnerability is found (simple check)
                            if grep -q "CRITICAL" /tmp/dependency-report/dependency-check-report.html; then
                                echo "Critical vulnerabilities found!"
                                exit 1
                            fi
                        '''
                    }
                }
            }
        }

        stage('Docker Build, Scan, Push') {
            agent { label 'docker-agent' }
            steps {
                // Retrieve JAR from Build
                unstash 'jar-artifact'

                // Build Docker image
                sh "docker build -t ${DOCKER_IMAGE}:${DOCKER_TAG} ."

                // Scan image with Trivy (fail on HIGH or CRITICAL)
                sh "trivy image --severity HIGH,CRITICAL --exit-code 1 ${DOCKER_IMAGE}:${DOCKER_TAG}"

                // Push to Docker Hub only if previous steps pass
                withCredentials([usernamePassword(credentialsId: 'docker-registry-credentials', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                    sh "echo $DOCKER_PASS | docker login -u $DOCKER_USER --password-stdin"
                    sh "docker push ${DOCKER_IMAGE}:${DOCKER_TAG}"
                }
            }
        }
    }

    post {
        success {
            echo 'Pipeline completed successfully!'
        }
        failure {
            echo 'Pipeline failed. Check logs for details.'
        }
    }
}
