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

        // Extra explanatory text block
        [$class: 'CascadeChoiceParameter',
            choiceType: 'PT_FORMATTED_HTML',   // ✅ formatted HTML
            name: 'ENV_HELP',
            description: 'Instructions',
            referencedParameters: 'OPERATION',
            script: [$class: 'GroovyScript',
                script: [script: '''
                    if (OPERATION == "Start AMH (example.py)") {
                        return ["<b>Please select environments below:</b><br>(Only applies when starting AMH)"]
                    } else {
                        return ["<i>No environments required for this operation</i>"]
                    }
                ''', sandbox: true],
                fallbackScript: [script: 'return ["<i>No environments available</i>"]', sandbox: true]
            ]
        ],

        // Checkbox list for ENV
        [$class: 'CascadeChoiceParameter',
            choiceType: 'PT_CHECKBOX',
            description: 'Select environments',
            name: 'ENV',
            referencedParameters: 'OPERATION',
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
  stages {
    stage('Print Params') {
      steps {
        script {
          echo "OPERATION: ${params.OPERATION}"
          echo "ENV: ${params.ENV}"
        }
      }
    }
  }
}
