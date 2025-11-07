pipeline {
    agent any
    options {
        ansiColor('xterm')
    }
    stages {
        stage('Hello') {
            steps {
                echo 'Hello World'
                sh 'cat /opt/ansible.yml'
                sh 'cat /opt/ansible_hosts'
                ansiblePlaybook becomeUser: 'wanpen', 
                                colorized: true, 
                                credentialsId: '00b69538-5290-4373-a385-c2e59e5a4d9f', 
                                disableHostKeyChecking: true, 
                                installation: 'Ansible', 
                                inventory: '/opt/ansible_hosts', 
                                playbook: '/opt/ansible.yml',
                                extraVars: [fetch_dest: "${WORKSPACE}/fetched/"]
            }
        }
    }
    post {
        always {
            archiveArtifacts artifacts: 'fetched/**/*'
        }
    }
}
