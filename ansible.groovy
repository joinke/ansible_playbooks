pipeline {
    agent any
    options {
        ansiColor('xterm')
    }
    stages {
        stage('Hello') {
            steps {
                echo 'Hello World'
                sh 'cat playbooks/list.yml'
                sh 'cat inventories/ansible_hosts'
                ansiblePlaybook becomeUser: 'wanpen', 
                                colorized: true, 
                                credentialsId: '00b69538-5290-4373-a385-c2e59e5a4d9f', 
                                disableHostKeyChecking: true, 
                                installation: 'Ansible', 
                                inventory: 'inventories/ansible_hosts', 
                                playbook: 'playbooks/list.yml',
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
