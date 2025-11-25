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
        $class: 'SecureGroovyScript',
        script: '''
            import org.apache.commons.text.StringEscapeUtils

            def escape = { s -> StringEscapeUtils.escapeHtml4(s ?: '') }

            def html = "<b>Choose an operation:</b><br>"

            def file = new File('/var/jenkins_home/operations.txt')
            if (!file.exists()) {
                return "<div style=\\"color:red\\"><b>Error:</b> operations.txt not found</div>"
            }

            List lines = file.readLines()
            List options = []

            for (int i = 0; i < lines.size(); i++) {
                String raw = lines.get(i).trim()

                if (raw.length() == 0) continue

                String[] parts = raw.split("\\\\|", 2)

                if (parts.length == 0 || parts[0].trim().length() == 0) {
                    html += "<div style=\\"color:red\\">Invalid entry: '\${escape(raw)}'</div>"
                    continue
                }

                String value = escape(parts[0].trim())
                String label = escape(parts.length > 1 ? parts[1].trim() : parts[0].trim())

                options.add([value: value, label: label])
            }

            if (options.isEmpty()) {
                return "<div style=\\"color:red\\"><b>Error:</b> No valid operations found.</div>"
            }

            html += "<select name='value'>"
            for (int i = 0; i < options.size(); i++) {
                def opt = options.get(i)
                if (i == 0)
                    html += "<option selected value='\${opt.value}'>\${opt.label}</option>"
                else
                    html += "<option value='\${opt.value}'>\${opt.label}</option>"
            }
            html += "</select>"

            return html
        ''',
        sandbox: false
    ],
    fallbackScript: [
        $class: 'SecureGroovyScript',
        script: 'return "<i>No operations available</i>"',
        sandbox: true
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
