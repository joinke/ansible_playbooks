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
        script: """
            import org.apache.commons.text.StringEscapeUtils

            def escape = { s -> StringEscapeUtils.escapeHtml4(s ?: '') }

            def html = "<b>Choose an operation:</b><br>"

            def file = new File('/var/jenkins_home/operations.txt')
            if (!file.exists()) {
                return "<div style='color:red'><b>Error:</b> operations.txt not found on controller at /var/jenkins_home/operations.txt</div>"
            }

            def options = []
            def lineNo = 0
            file.eachLine { line ->
                lineNo++
                line = line.trim()
                if (!line) return                      // skip blank lines

                def parts = line.split("\\\\|", 2)

                if (parts.size() < 1 || parts[0].trim() == "") {
                    // invalid format
                    html += "<div style='color:red'>Invalid line at ${lineNo}: '${escape(line)}'</div>"
                    return
                }

                def value = escape(parts[0].trim())
                def label = escape(parts.size() > 1 ? parts[1].trim() : parts[0].trim())

                options << [value: value, label: label]
            }

            if (options.isEmpty()) {
                return "<div style='color:red'><b>Error:</b> No valid operations found in operations.txt</div>"
            }

            // Autoselect first option
            html += "<select name='value'>"
            options.eachWithIndex { opt, idx ->
                if (idx == 0)
                    html += "<option value='${opt.value}' selected>${opt.label}</option>"
                else
                    html += "<option value='${opt.value}'>${opt.label}</option>"
            }
            html += "</select>"

            return html
        """,
        sandbox: false
    ],
    fallbackScript: [
        $class: 'GroovyScript',
        script: """
            return "<i>No operations available</i>"
        """,
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
