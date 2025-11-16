def envMap = [
    'UAT01': 'Environment A',
    'UAT02': 'Environment B',
    'UAT03': 'Environment C'
]

def getSelectedKeys(mymap, boolString) {
    def bools = boolString.split(',').collect { it.trim().toBoolean() }
    def keys = mymap.keySet().toList()
    def selected = []
    for (int i = 0; i < Math.min(keys.size(), bools.size()); i++) {
        if (bools[i]) selected << keys[i]
    }
    return selected.join(',')
}

pipeline {
    agent any
    options { ansiColor('xterm') }

    parameters {
        // Operation dropdown
        activeChoiceParam('OPERATION') {
            choiceType('SINGLE_SELECT')
            groovyScript {
                script('''
                    def commandMap = [
                        'example1.py': 'Stop AMH',
                        'example.py': 'Start AMH',
                        'example3.py': 'Restart AMH'
                    ]
                    return commandMap.keySet() as List
                ''')
                fallbackScript('return ["example.py"]')
            }
        }

        // Individual checkbox
        activeChoiceParam('INDIVIDUAL') {
            choiceType('CHECKBOX')
            groovyScript {
                script('return [[name:"Individual Hosts", value:"true"]]')
                fallbackScript('return []')
            }
        }

        // Environment checkboxes
            activeChoiceParam('ENVS') {
                choiceType('CHECKBOX')
                referencedParameter('OPERATION')
                groovyScript {
                    script('''
                        if (OPERATION?.trim() != 'example.py') return []
                        return ["UAT01", "UAT02", "UAT03"]
                    ''')
                    fallbackScript('return []')
                }
            }

        // Component dropdown
        activeChoiceParam('COMP') {
            choiceType('SINGLE_SELECT')
            referencedParameter('OPERATION')
            groovyScript {
                script('return ["STP","WB","ALL"]')
                fallbackScript('return ["ALL"]')
            }
        }

        // Site dropdown
        activeChoiceParam('SITE') {
            choiceType('SINGLE_SELECT')
            referencedParameter('OPERATION')
            groovyScript {
                script('return ["RCC","WSDC","ALL"]')
                fallbackScript('return ["ALL"]')
            }
        }
    }

    environment {
        OPERATION = "${params.OPERATION}"
        ENVS = "${params.ENVS ?: ''}"
        COMP = "${params.COMP ?: ''}"
        SITE = "${params.SITE ?: ''}"
        INDIVIDUAL = "${params.INDIVIDUAL ?: ''}"
    }

    stages {
        stage('Verify Params') {
            steps {
                script {
                    echo "Operation: ${env.OPERATION}"
                    echo "Environments: ${env.ENVS}"
                    echo "Component: ${env.COMP}"
                    echo "Site: ${env.SITE}"
                    echo "Individual: ${env.INDIVIDUAL}"

                    if (env.INDIVIDUAL.toBoolean() && !env.ENVS?.trim()) {
                        error("No environments selected while INDIVIDUAL is enabled")
                    }
                }
            }
        }

        stage('Run Python') {
            steps {
                script {
                    def selectedEnvs = getSelectedKeys(envMap, env.ENVS ?: '')
                    env.SELECTEDENVS = selectedEnvs
                    echo "Running on environments: ${selectedEnvs}"
                }

                withCredentials([sshUserPrivateKey(
                    credentialsId: '00b69538-5290-4373-a385-c2e59e5a4d9f',
                    keyFileVariable: 'SSH_KEY',
                    usernameVariable: 'SSH_USER')]) {
                    sh '''
                        echo "Using SSH key $SSH_KEY for $SSH_USER"
                        python3 -u ssh_runner.py
                    '''
                }
            }
        }
    }

    post {
        always {
            echo "Archiving artifacts..."
            archiveArtifacts artifacts: 'fetched/**/*', fingerprint: true, allowEmptyArchive: true
        }
    }
}
