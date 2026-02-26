pipeline{
    agent any
     
    environment{
        DOCKER_IMAGE= "shejulv088/jenkins_api_gateway"
        ENVIRONMENT= "dev"
        ENV_HELM_REPO = "https://github.com/VaibhavShejul09/rankx-environments.git"
        KUBECONFIG_FILE = ''  // Will be set via withCredentials
    }
    stages{
        stage('Checkout the code'){
            steps{
                git branch: 'master', url: 'https://github.com/VaibhavShejul09/api-gateway.git'
            }
        }
        stage('Clean & Compile'){
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
                archiveArtifacts artifacts: 'target/*.jar', fingerprint: true   //this tells jenkins store the artifacts in build hostory & fingerprint: true - track which build create which artifact
            }
        }
        stage('build docker image'){
            steps{
                script{
                    def tag= "${env.BUILD_NUMBER}-${env.GIT_COMMIT.take(7)}"
                    env.IMAGE_TAG= tag
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
        stage('Push to dockerhub'){
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

        stage('Upadte the Helm Values'){
            steps{
                withCredentials([string(
                    credentialsId: 'git-token',
                    variable: 'GIT_TOKEN'
                )]){
                sh """
                        git clone ${ENV_HELM_REPO}
                        cd rankx-environments/${ENVIRONMENT}/api-gateway

                        sed -i "s/tag:.*/tag: \\"${IMAGE_TAG}\\"/" values.yaml

                        git config user.email "ci@jenkins.com"
                        git config user.name "jenkins"

                        git add values.yaml
                        git commit -m "Update image tag to ${IMAGE_TAG}"
                        git push

                  """
                }  
            }
        }

    }
}
