pipeline {
    agent none

    environment {
        DOCKER_IMAGE = 'rishu2801/money-management'
        DOCKER_TAG = "${env.BUILD_NUMBER}"
        SONAR_PROJECT_KEY = 'MoneyManagement'
        // SonarQube server URL is configured in Jenkins (withSonarQubeEnv)
    }

    stages {
        stage('Build') {
            agent { label 'build-agent' }
            environment {
                SPRING_PROFILES_ACTIVE = 'test'
                MAVEN_OPTS = '-Xmx1024m'
            }
            steps {
                checkout scm
                sh 'mvn clean package -DskipTests'

                // Stash for Docker
                stash includes: 'target/*.jar', excludes: 'target/*.jar.original', name: 'jar-artifact'
                // Stash for Test/SonarQube
                stash includes: 'src/**, pom.xml, target/classes/**, target/test-classes/**', name: 'test-artifacts'
                // Stash Dockerfile
                stash includes: 'Dockerfile', name: 'dockerfile'
                // Stash full project for OWASP Maven plugin
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
                sh 'mvn test'

                withSonarQubeEnv('SonarQube') {
                    sh """
                        sonar-scanner \
                          -Dsonar.projectKey=${SONAR_PROJECT_KEY} \
                          -Dsonar.sources=src/main/java \
                          -Dsonar.tests=src/test/java \
                          -Dsonar.java.binaries=target/classes
                    """
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

                // Optional: publish HTML report (requires HTML Publisher plugin)
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

        stage('Docker Build, Scan, Push') {
            agent { label 'docker-agent' }
            options { skipDefaultCheckout() }
            steps {
                unstash 'jar-artifact'
                unstash 'dockerfile'

                sh "docker build -t ${DOCKER_IMAGE}:${DOCKER_TAG} ."

                // Temporarily only fail on CRITICAL (fix later to HIGH,CRITICAL)
                sh "trivy image --severity CRITICAL --exit-code 1 ${DOCKER_IMAGE}:${DOCKER_TAG}"

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
            echo 'Pipeline completed successfully!'
        }
        failure {
            echo 'Pipeline failed. Check logs.'
        }
        always {
            echo "Build Number: ${BUILD_NUMBER}"
        }
    }
}
