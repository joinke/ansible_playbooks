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
      $class: 'SecureGroovyScript',
      script: """
import org.apache.commons.text.StringEscapeUtils

def escape = { s -> StringEscapeUtils.escapeHtml4(s ?: '') }
def html = "<b>Choose an operation:</b><br>"

def file = new File('/var/jenkins_home/operations.txt')
if (!file.exists()) {
    return "<div style=\"color:red\"><b>Error:</b> operations.txt not found</div>"
}

def lines = file.readLines()
def options = []

lines.each { line ->
    def trimmedLine = line.trim()        // <- use 'def' here
    if (!trimmedLine) return

    def parts = trimmedLine.split("\\|", 2)
    if (!parts[0]?.trim()) {
        html += "<div style=\"color:red\">Invalid entry: '${escape(trimmedLine)}'</div>"
        return
    }

    def value = escape(parts[0].trim())  // <- use 'def' here
    def label = escape(parts.length > 1 ? parts[1].trim() : parts[0].trim())

    options << [value: value, label: label]
}

if (options.isEmpty()) {
    return "<div style=\"color:red\"><b>Error:</b> No valid operations found.</div>"
}

html += "<select name='value'>"
options.eachWithIndex { opt, idx ->
    html += "<option${idx == 0 ? ' selected' : ''} value='${opt.value}'>${opt.label}</option>"
}
html += "</select>"

return html

      """,
      sandbox: false
    ],
    /* UNO-CHOICE HAS NO fallbackScript → REMOVE IT */
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
