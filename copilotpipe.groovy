pipeline {
    agent any

    // Parameters will be passed from your form
    parameters {
        string(name: 'COMMAND', defaultValue: '', description: 'Command selected from frontend')
        string(name: 'OPTIONS', defaultValue: '', description: 'Comma-separated options selected')
    }

    stages {
        stage('Print Inputs') {
            steps {
                script {
                    echo "Command received: ${params.COMMAND}"
                    echo "Options received: ${params.OPTIONS}"

                    // Convert comma-separated options to a list
                    def optionsList = params.OPTIONS.split(',').collect { it.trim() }
                    echo "Options as list: ${optionsList}"

                    // Example: conditional execution
                    if (params.COMMAND == 'deploy') {
                        echo "Triggering deploy pipeline..."
                        // sh "deploy_script.sh ${params.OPTIONS}"
                    } else if (params.COMMAND == 'test') {
                        echo "Running tests..."
                        // sh "test_script.sh ${params.OPTIONS}"
                    } else {
                        echo "Unknown command, skipping execution."
                    }
                }
            }
        }
    }

    post {
        always {
            echo "Pipeline finished"
        }
    }
}
