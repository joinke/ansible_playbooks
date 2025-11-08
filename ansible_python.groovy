pipeline {
  agent any
  options {
    ansiColor('xterm')  // enable colored Ansible output
  }

  stages {
    stage('Run Ansible via Python') {
      steps {
        // Inject Jenkins SSH credentials for Ansible to connect to remote hosts
        sshagent(['00b69538-5290-4373-a385-c2e59e5a4d9f']) {
          sh '''
            echo "🧩 SSH Agent available at: $SSH_AUTH_SOCK"
            export ANSIBLE_HOST_KEY_CHECKING=False

            # Optionally confirm key works:
            ssh-add -l

            # Run your Python wrapper (which calls ansible-playbook)
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

