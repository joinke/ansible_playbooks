pipeline {
    agent any
    environment {
        RAW_JSON = "${raw_content}"  // raw JSON from webhook
    }
    stages {
        stage('Parse JSON') {
            steps {
                script {
                    def data = readJSON text: env.RAW_JSON
                    def command = data.command
                    def options = data.options
                    def envs = data.environments

                    echo "Command: ${command}"
                    echo "Options: ${options}"
                    echo "Environments: ${envs}"

                    if (command == 'deploy') {
                        echo "Deploying to: ${options}"
                    } else if (command == 'test') {
                        echo "Testing types: ${options}"
                    }
                }
            }
        }
    }
}
