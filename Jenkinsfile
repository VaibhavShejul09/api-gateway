pipeline {
    agent any

    environment{
        DOCKER_IMAGE= "shejulv088/jenkins_api_gateway"
    }
    stages{
        stage('checkout code'){
            steps{
                git 'https://github.com/VaibhavShejul09/api-gateway.git'
            }
        }
        stage('Build'){
            steps{
                sh 'mvn clean packge'
            }
        }
        stage('build image'){
            steps{
                sh 'docker build -t $DOCKER_IMAGE:v1 .' 
            }
        }
        stage('push to dockerhub'){
            steps{
                withCredentials([usernamePassword(
                    credentialsId: 'dockerhub'
                    usernameVariable: 'DOCKER_USER'
                    passwordVariable: 'DOCKE_PASS'
                )]){
                    sh '''
                        echo $DOCKER_PASS | docker login -u $DOCKER_USER --password-stdin
                        docker push $DOCKER_IMAGE:v1
                '''
                }
            }
        }
    }
}