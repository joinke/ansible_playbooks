node {

    // Optional: checkout source code
    checkout scm

    // Read choices from workspace
    def choices = []
    def optionsFile = "${env.WORKSPACE}/options.txt"

    if (fileExists(optionsFile)) {
        choices = readFile(optionsFile).readLines()
    } else {
        error "options.txt not found in workspace: ${optionsFile}"
    }

    def operations = []
    def operationsFile = "${env.WORKSPACE}/operations.txt"
    
    if (fileExists(operationsFile)) {
        operations = readFile(operationsFile)
                      .split('\n')
                      .findAll { it.trim() } // remove blank lines
    } else {
        error "operations.txt not found in workspace: ${operationsFile}"
    }
    
    // Convert each operation line into an HTML <option>
    def operationsHtml = operations.collect { line ->
        def (value, label) = line.split(/\|/, 2)
        return "<option value='${value}'>${label}</option>"
    }.join('\n')

    echo "DEBUG: operations loaded = ${operations.size()}"
    echo "DEBUG: Generated HTML:\n${operationsHtml}"
    // Dynamically create parameters
    properties([
        parameters([
            choice(
                name: 'MY_PARAM',
                choices: choices,
                description: 'Pick one'
            ),
            [
                $class: 'DynamicReferenceParameter',
                name: 'OPERATION',
                choiceType: 'ET_FORMATTED_HTML',
                description: 'Select the operation',
                script: [
                    $class: 'GroovyScript',
                    script: [
                        script: '''
                            def html="<b>Choose an operation:</b><br>"
                    
                            html+="<select name='value'>"
                            html+="${operationsHtml}"
                            html+="</select>"
                            return html
                        ''',
                        sandbox: false  // must be false when using Groovy variables
                    ],
                    fallbackScript: [
                        script: 'return "<i>No operations available</i>"',
                        sandbox: true
                    ]
                ]
            ]
        ])
    ])

    // Your pipeline body
    ansiColor('xterm') {
        stage('Hello') {
            echo 'Hello World'

            withCredentials([
                sshUserPrivateKey(
                    credentialsId: '00b69538-5290-4373-a385-c2e59e5a4d9f',
                    keyFileVariable: 'SSH_KEY',
                    usernameVariable: 'SSH_USER'
                )
            ]) {
                sh """
                    echo "🧩 Using SSH key from Jenkins: \$SSH_KEY for user \$SSH_USER and \$OPERATION and environments \$SELECTEDENVS and component \$SELECTEDCOMP and site \$SELECTEDSITE"

                    # Run the Python wrapper
                    python3 -u /opt/test.py
                """
            }
        }
    }

    // Post actions
    // Note: Scripted pipeline uses try/finally, but Jenkins supports post{} if wrapped inside pipeline{}.
    // We'll do it the Scripted way:
    try {
        // stages already ran
    } finally {
        archiveArtifacts artifacts: 'fetched/**/*'
    }
}
