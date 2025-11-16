properties([
    parameters([
        // Dropdown for OPERATION
        [$class: 'CascadeChoiceParameter',
            choiceType: 'PT_SINGLE_SELECT',
            description: 'Select the operation',
            name: 'OPERATION',
            script: [$class: 'GroovyScript',
                script: [script: '''
                    return ["Start AMH (example.py)", "Stop AMH (example1.py)", "Restart AMH (example3.py)"]
                ''', sandbox: true],
                fallbackScript: [script: 'return ["Start AMH (example.py)"]', sandbox: true]
            ]
        ],

        // Checkbox list for ENV, reactive to OPERATION
        [$class: 'CascadeChoiceParameter',
            choiceType: 'PT_CHECKBOX',
            description: 'Select environments (only for Start AMH)',
            name: 'ENV',
            referencedParameters: 'OPERATION',   // ✅ makes it conditional
            script: [$class: 'GroovyScript',
                script: [script: '''
                    if (OPERATION == "Start AMH (example.py)") {
                        return ["UAT01","UAT02","UAT03"]
                    } else {
                        return []
                    }
                ''', sandbox: true],
                fallbackScript: [script: 'return []', sandbox: true]
            ]
        ]
    ])
])

pipeline {
  agent any
  options {
    ansiColor('xterm')  // enable colored Ansible output
  }
  environment {
    OPERATION = "${params.OPERATION}"
    ENVS = "${params['\u200B'] ?: ''}"
    COMPS = "${params['\u200C'] ?: ''}"
    SITE = "${params['\u200D'] ?: ''}"
    MYHOSTS = "${params['\u2060'] ?: ''}"
    INDIVIDUAL = "${params.INDIVIDUAL}"
  }
  stages {
    stage('Verify Params') {
      steps {
          script {
              echo "Selected env is $env.ENVS and myhosts is $env.MYHOSTS"
              if (params.INDIVIDUAL?.toBoolean() && (env.MYHOSTS == null || env.MYHOSTS.trim().isEmpty())) {
                error("No Hosts Selected. You must select hosts when 'INDIVIDUAL' is enabled.")
              }
         }
      }
    }
  }
}
