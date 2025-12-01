def notifyStage(jobid, stage, status, message) {
    def payload = [
        job_id     : jobid,
        stage      : stage,
        status     : status,
        message    : message,      // safely handle newlines and quotes
        build_number: env.BUILD_NUMBER,
        job_name   : env.JOB_NAME
    ]
    writeFile file: 'payload.json', text: groovy.json.JsonOutput.toJson(payload)
    sh "curl -s -X POST -H 'Content-Type: application/json' -d @payload.json http://localhost:5000/update-status"
    sh "rm -f payload.json"
}
def sendLog() {
    def payload = [
        job_id: env.jobid,
        log   : env.result
    ]
    writeFile file: 'payload.json', text: groovy.json.JsonOutput.toJson(payload)
    sh "curl -s -X POST -H 'Content-Type: application/json' -d @payload.json http://localhost:5000/logs"
    sh "rm -f payload.json"
}

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
                    def job_id = data.job_id
                    env.jobid = job_id
                    env.result = ''
                    
                    env.result = sh(
                        script: """
                            echo "Command: ${command}"
                            echo "Options: ${options}"
                            echo "Environments: ${envs}"
                            echo "ID: ${job_id}"
                        """, 
                        returnStdout: true
                    ).trim()

                    if (command == 'deploy') {
                        echo "Deploying to: ${options}"
                    } else if (command == 'test') {
                        echo "Testing types: ${options}"
                    }
                    notifyStage(env.jobid,"Parse JSON","success","Command : ${command}, Options : ${options}, Environments : ${envs}, Job : ${job_id}")
                }
            }
        }
        stage('Get df') {
            steps {
                script {
                    env.result = sh(script: 'df -h', returnStdout: true).trim()
                    notifyStage(env.jobid,"Get Jobs","success","df output, check logs")
                }
            }
        }
        stage('Get free') {
            steps {
                script {
                    env.result += sh(script: 'free -m', returnStdout: true).trim()
                    notifyStage(env.jobid,"Free Mem","success","free output, check logs")
                }
            }
        }
        stage('Send Logs') {
            steps {
                script {
                    sendLog()
                    notifyStage(env.jobid,"Send Logs","success","Send Logs done")
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
            notifyStage(env.jobid,"Pipeline", "success", "Post completed successfully")
        }
    }
}
}
