pipeline {
    agent any

    environment {
        RAW_JSON = "${raw_content}"
    }

    stages {

        stage("Run Ansible Pipeline") {
            steps {
                sh """
                    python3 /opt/my_playbook.py
                """
            }
        }
    }
}
