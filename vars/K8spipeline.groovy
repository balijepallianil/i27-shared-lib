import com.i27academy.k8s.K8s

def call(Map pipelineParams) {
    K8s k8s = new K8s(this)
    pipeline {
        agent {
            label 'k8s-slave'
        }
    parameters {
        choice (name: 'buildOnly',
               choices: 'no\nyes',
               description: "Build the Application Only"
        )
        choice (name: 'dockerPush',
               choices: 'no\nyes',
               description: "Docker Build and push to registry"
        )
    }    

    tools{
        maven 'Maven-3.8.8'
        jdk 'JDK-17'
    }
    environment {
        APPLICATION_NAME = "${pipelineParams.appName}"
        POM_VERSION = readMavenPom().getVersion()
        POM_PACKAGING = readMavenPom().getPackaging()
        DOCKER_HUB = "docker.io/i27anilb3"
        DOCKER_CREDS = credentials('docker_creds')
    }
    stages{
       stage ('Build') {
            when {
                anyOf {
                    expression {
                        params.buildOnly == 'yes'
                    }
                }
            }
            steps {
                script {
                    k8s.buildApp("${env.APPLICATION_NAME}")
                }

            }
        }
        stage ('Docker Build and push') {
            when {
                anyOf {
                    expression {
                        params.dockerPush == 'yes'
                    }
                }
            }            
            steps {
                script  {
                    k8s.dockerBuildandPush()
                }

            }
        }
    }
}
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
