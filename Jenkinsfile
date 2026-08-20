pipeline {
    agent none

    environment {
        // Docker image repository (change to your Docker Hub username/repo)
        DOCKER_IMAGE = 'rishu2801/money-management'
        DOCKER_TAG = "${env.BUILD_NUMBER}"

        // Local SonarQube server URL (host IP from LXD bridge, likely 10.100.0.1)
        SONAR_HOST_URL = 'http://10.100.0.1:9000'
        SONAR_PROJECT_KEY = 'MoneyManagement'
    }

    stages {
        stage('Build') {
            agent { label 'build-agent' }
            environment {
                SPRING_PROFILES_ACTIVE = 'test'   // not strictly used, but set for safety
                MAVEN_OPTS = '-Xmx1024m'
            }
            steps {
                checkout scm
                sh 'mvn clean package -DskipTests'

                stash includes: 'target/*.jar', name: 'jar-artifact'
                stash includes: 'src/**, pom.xml, target/classes/**, target/test-classes/**', name: 'test-artifacts'
                stash includes: 'pom.xml', name: 'pom-for-owasp'
            }
        }

        stage('Test & Security') {
            parallel {
                stage('Test + SonarQube') {
                    agent { label 'test-agent' }
                    environment {
                        SPRING_PROFILES_ACTIVE = 'test'
                    }
                    steps {
                        unstash 'test-artifacts'
                        sh 'mvn test'

                        script {
                            withSonarQubeEnv('SonarQube') {
                                sh """
                                    sonar-scanner \
                                      -Dsonar.projectKey=${SONAR_PROJECT_KEY} \
                                      -Dsonar.host.url=${SONAR_HOST_URL} \
                                      -Dsonar.login=${SONAR_TOKEN}
                                """
                            }
                            timeout(time: 1, unit: 'HOURS') {
                                def qg = waitForQualityGate()
                                if (qg.status != 'OK') {
                                    error "SonarQube quality gate failed: ${qg.status}"
                                }
                            }
                        }
                    }
                }

                stage('Dependency Scan (OWASP)') {
                    agent { label 'security-agent' }
                    steps {
                        unstash 'pom-for-owasp'
                        sh '''
                            dependency-check \
                              --project MoneyManagement \
                              --scan pom.xml \
                              --format HTML \
                              --out /tmp/dependency-report

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
                unstash 'jar-artifact'
                sh "docker build -t ${DOCKER_IMAGE}:${DOCKER_TAG} ."
                sh "trivy image --severity HIGH,CRITICAL --exit-code 1 ${DOCKER_IMAGE}:${DOCKER_TAG}"

                withCredentials([usernamePassword(credentialsId: 'docker-registry-credentials', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                    sh "echo $DOCKER_PASS | docker login -u $DOCKER_USER --password-stdin"
                    sh "docker push ${DOCKER_IMAGE}:${DOCKER_TAG}"
                }
            }
        }
    }

    post {
        success {
            echo ' '
            echo 'Pipeline completed successfully!'
        }
        failure {
            echo 'Pipeline failed. Check logs.'
            echo 'i am good'
        }
    }
}
