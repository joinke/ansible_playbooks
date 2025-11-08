pipeline {
  agent any
  options {
    ansiColor('xterm')  // enable colored Ansible output
  }

  stages {
    stage('Run Ansible via Python') {
      steps {
        withCredentials([sshUserPrivateKey(credentialsId: '00b69538-5290-4373-a385-c2e59e5a4d9f',
                                           keyFileVariable: 'SSH_KEY',
                                           usernameVariable: 'SSH_USER')]) {
          sh '''
            echo "🧩 Using SSH key from Jenkins: $SSH_KEY for user $SSH_USER"
            export ANSIBLE_HOST_KEY_CHECKING=False

            # Run the Python wrapper (Ansible will use the key directly)
            python3 -u ansible_python.py
          '''
        }
      }
    }
  }

  post {
    always {
      echo '📦 Archiving fetched files...'
      archiveArtifacts artifacts: 'fetched/**/*', fingerprint: true
    }
  }
}

