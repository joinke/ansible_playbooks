pipeline {
    agent any
    options {
        ansiColor('xterm')
    }
    stages {
      stage('Run Ansible via Python') {
        steps {
          sshagent(['00b69538-5290-4373-a385-c2e59e5a4d9f']) {
            sh 'python3 -u ansible_python.py'
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
