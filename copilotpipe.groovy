def notifyStage(jobid,stage, status, message) {
    def payload = """{
        "job_id": "${jobid}",
        "stage": "${stage}",
        "status": "${status}",
        "message": "${message}",
        "build_number": "${env.BUILD_NUMBER}",
        "job_name": "${env.JOB_NAME}"      
    }"""

    sh """
    curl -X POST -H 'Content-Type: application/json' \
         -d '${payload}' \
         http://localhost:5000/update-status
    """
}

pipeline {
    agent any
    environment {
        RAW_JSON = "${raw_content}"  // raw JSON from webhook
    }
    stages {
        stage('Stage1:Parse JSON') {
            steps {
                script {
                    def data = readJSON text: env.RAW_JSON
                    def command = data.command
                    def options = data.options
                    def envs = data.environments
                    def job_id = data.job_id
                    env.jobid = job_id
                    
                    echo "Command: ${command}"
                    echo "Options: ${options}"
                    echo "Environments: ${envs}"
                    echo "ID: ${job_id}"

                    if (command == 'deploy') {
                        echo "Deploying to: ${options}"
                    } else if (command == 'test') {
                        echo "Testing types: ${options}"
                    }
                    notifyStage(env.jobid,"Parse JSON","success","Stage completed successfully")
                }
            }
        }
    }
    post {
    failure {
        script {
            notifyStage(env.jobid,"Pipeline", "failed", "Pipeline failed")
        }
    }
    success {
        script {
            notifyStage(env.jobid,"Pipeline", "success", "Pipeline completed successfully")
        }
    }
}
}
