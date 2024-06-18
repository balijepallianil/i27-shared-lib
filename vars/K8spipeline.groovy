import com.i27academy.builds.K8s

def call(Map pipelineParams) {
    K8s K8s = new K8s(this)
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
        APPLICATION_NAME = "eureka"
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
                    buildApp().call()
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
                    dockerBuildandPush().call()
                }

            }
        }
    }
}
}
