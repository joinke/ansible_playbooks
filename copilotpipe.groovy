pipeline {
    agent any
    options {
        ansiColor('xterm')  // enable colored output
    }

    parameters {
        // Operation dropdown
        activeChoiceParam('OPERATION') {
            description('Select the operation')
            choiceType('SINGLE_SELECT')
            groovyScript {
                script('''
                    def commandMap = [
                        'example1.py': 'Stop AMH',
                        'example.py': 'Start AMH',
                        'example3.py': 'Restart AMH'
                    ]
                    return commandMap.collect { k,v -> "${v} (${k})" }
                '''.stripIndent())
                fallbackScript('return ["example.py"]')
            }
        }

        // Individual checkbox
        activeChoiceParam('INDIVIDUAL') {
            description('Select individual hosts')
            choiceType('SINGLE_SELECT') // checkbox emulated
            groovyScript {
                script('return ["false","true"]')
                fallbackScript('return ["false"]')
            }
        }

        // Environment checkbox (ET_CHECKBOX)
        activeChoiceParam('ENVS') {
            description('Select environments')
            choiceType('CHECKBOX')
            groovyScript {
                script('''
                    return [
                        [name: "Environment A", value: "UAT01", selected: true],
                        [name: "Environment B", value: "UAT02"],
                        [name: "Environment C", value: "UAT03"]
                    ]
                '''.stripIndent())
                fallbackScript('return []')
            }
        }

        // Component dropdown
        activeChoiceParam('COMP') {
            description('Select component')
            choiceType('SINGLE_SELECT')
            groovyScript {
                script('''
                    def compMap = ['STP':'STP','WB':'WB','ALL':'BOTH']
                    return compMap.collect { k,v -> "${v} (${k})" }
                '''.stripIndent())
                fallbackScript('return ["ALL"]')
            }
        }

        // Site dropdown
        activeChoiceParam('SITE') {
            description('Select site')
            choiceType('SINGLE_SELECT')
            groovyScript {
                script('''
                    def siteMap = ['RCC':'RCC','WSDC':'WSDC','ALL':'BOTH']
                    return siteMap.collect { k,v -> "${v} (${k})" }
                '''.stripIndent())
                fallbackScript('return ["ALL"]')
            }
        }
    }

    environment {
        // Convert ENVS list of maps into comma-separated string
        ENVS = "${params.ENVS instanceof List ? params.ENVS*.value.join(',') : params.ENVS ?: ''}"
        OPERATION = "${params.OPERATION ?: ''}"
        COMPS = "${params.COMP ?: ''}"
        SITE = "${params.SITE ?: ''}"
        INDIVIDUAL = "${params.INDIVIDUAL ?: 'false'}"
    }

    stages {
        stage('Verify Params') {
            steps {
                script {
                    echo "Selected environments: ${env.ENVS}"
                    echo "Selected component: ${env.COMPS}"
                    echo "Selected site: ${env.SITE}"
                    echo "Individual selection: ${env.INDIVIDUAL}"
                    
                    if (env.INDIVIDUAL.toBoolean() && env.ENVS.trim().isEmpty()) {
                        error("No environments selected. You must select at least one when 'INDIVIDUAL' is enabled.")
                    }
                }
            }
        }

        stage('Run Python') {
            steps {
                script {
                    echo "Running operation: ${env.OPERATION}"
                    echo "On environments: ${env.ENVS}"
                    echo "Component: ${env.COMPS}, Site: ${env.SITE}"
                }

                withCredentials([sshUserPrivateKey(credentialsId: '00b69538-5290-4373-a385-c2e59e5a4d9f',
                                                   keyFileVariable: 'SSH_KEY',
                                                   usernameVariable: 'SSH_USER')]) {
                    sh '''
                        echo "🧩 Using SSH key from Jenkins: $SSH_KEY for user $SSH_USER"
                        python3 -u ssh_runner.py
                    '''
                }
            }
        }
    }

    post {
        always {
            echo '📦 Archiving fetched files...'
            archiveArtifacts artifacts: 'fetched/**/*', fingerprint: true , allowEmptyArchive: true
        }
    }
}
