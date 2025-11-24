def getChoices() {
    def file = new File("env.WORKSPACE/options.txt")
    return file.readLines()
}

pipeline {
    agent any
    options {
        ansiColor('xterm')
    } 
    parameters {
    choice(name: 'MY_PARAM', choices: getChoices(), description: 'Pick one')
  }
    stages {
        stage('Hello') {
            steps {
                echo 'Hello World'
                //sh 'cat /opt/concurrent.yml'
                //sh 'cat /opt/ansible_hosts'
                ansiColor('xterm') {
                    withCredentials([sshUserPrivateKey(credentialsId: '00b69538-5290-4373-a385-c2e59e5a4d9f',
                                               keyFileVariable: 'SSH_KEY',
                                               usernameVariable: 'SSH_USER')]) {
                      sh '''
                        echo "🧩 Using SSH key from Jenkins: $SSH_KEY for user $SSH_USER and $OPERATION and envirionments $SELECTEDENVS and component $SELECTEDCOMP and site $SELECTEDSITE "
            
                        # Run the Python wrapper (Ansible will use the key directly)
                        python3 -u /opt/test.py
                      '''
                    }
                }
            }
        }
    }
    post {
        always {
            archiveArtifacts artifacts: 'fetched/**/*'
        }
    }
}
