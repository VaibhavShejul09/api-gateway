pipeline {
    agent any

    environment{
        DOCKER_IMAGE= "shejulv088/jenkins_api_gateway"
        KUBECONFIG_FILE = ''  // Will be set via withCredentials
    }
    stages{
        stage('checkout code'){
            steps{
                git branch: 'master',
                    url: 'https://github.com/VaibhavShejul09/api-gateway.git'
            }
        }

        stage('Compile'){
            steps{
                sh 'mvn clean compile'
            }
        }
        stage('Unit Test'){
            steps{
                sh 'mvn test'
            }
        }

        stage('Package'){
            steps{
                sh 'mvn package -DskipTests'
                archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
            }
        }

        stage('build image'){
            steps{
                script{
                    def tag = "${env.BUILD_NUMBER}-${env.GIT_COMMIT.take(7)}"
                    env.IMAGE_TAG = tag
                    sh "docker build -t ${DOCKER_IMAGE}:${tag} ."
                }
            }
        }
/*        stage("Trivy Scan"){
            steps{
                sh "trivy image --exit-code 1 --severity HIGH,CRITICAL ${DOCKER_IMAGE}:${env.IMAGE_TAG}"
            }
        }
*/        
        stage('push to dockerhub'){
            steps{
                withCredentials([usernamePassword(
                    credentialsId: 'dockerhub',
                    usernameVariable: 'DOCKER_USER',
                    passwordVariable: 'DOCKER_PASS'
                )]){
                    sh """
                       echo $DOCKER_PASS | docker login -u $DOCKER_USER --password-stdin
                        docker push ${DOCKER_IMAGE}:${env.IMAGE_TAG}
                """
                }
            }
        }
        stage('Get Nodes') {
            steps {
                    sh 'kubectl get nodes'
            }
        }      
        stage('Checkout Helm Charts') {
            steps {
                dir('helm-chart'){
                    // Clone your Helm chart repo
                    git branch: 'master', url: 'https://github.com/VaibhavShejul09/Helm_Repo.git'
                }
            }
        }
        stage('Helm Deploy') {
            steps {
                withCredentials([file(credentialsId: 'kubeconfig', variable: 'KUBECONFIG_FILE')]) {
                    sh """
                    helm install apigateway helm-chart/Rankx/charts/microservice-base -f helm-chart/services/api-gateway/values.yaml \
                        --namespace my-app 
                    """
                }
            }
        }       
    }
}
