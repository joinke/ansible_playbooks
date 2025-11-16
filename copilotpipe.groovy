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

        // Dynamic HTML block for instructions
        [$class: 'DynamicReferenceParameter',
            name: '',
            description: '',
            choiceType: 'ET_FORMATTED_HTML',
            referencedParameters: 'OPERATION',
            script: [$class: 'GroovyScript',
                script: [script: '''
                    if (OPERATION == "Start AMH (example.py)") {
                        return "<b>Please select environments below:</b>"
                    } else {
                        return ""
                    }
                ''', sandbox: true],
                fallbackScript: [script: 'return "<i>No environments available</i>"', sandbox: true]
            ]
        ],

        // Checkbox list for ENV
        [$class: 'CascadeChoiceParameter',
            choiceType: 'PT_CHECKBOX',
            description: '',
            name: '\u200B',
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
          echo "OPERATION: ${params['OPERATION']}"
          echo "ENV: ${params['\u200B]}"
        }
      }
    }
  }
}
