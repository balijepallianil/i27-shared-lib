package com.i27academy.k8s

class K8s {
    def jenkins
    K8s(jenkins) {
            this.jenkins = jenkins
    }
    def buildApp() {
        return {
            echo "Bulding ${env.APPLICATION_NAME} application"
            sh 'mvn clean package -DskipTests=true'

        }
    }
    def dockerBuildandPush() {
        return {
            echo "Starting Docker build stage"
            sh "cp ${WORKSPACE}/target/i27-${env.APPLICATION_NAME}-${env.POM_VERSION}.${env.POM_PACKAGING} ./.cicd/"
            echo "**************************** Building Docker Image ****************************"
            sh "docker build --force-rm --no-cache --build-arg JAR_SOURCE=i27-${env.APPLICATION_NAME}-${env.POM_VERSION}.${env.POM_PACKAGING} -t ${env.DOCKER_HUB}/${env.APPLICATION_NAME}:${GIT_COMMIT} ./.cicd"
            echo "********Docker login******"
            sh "docker login -u ${DOCKER_CREDS_USR} -p ${DOCKER_CREDS_PSW}"
            echo "********Docker Push******"
            sh "docker push ${env.DOCKER_HUB}/${env.APPLICATION_NAME}:${GIT_COMMIT}"
        }
    }

}