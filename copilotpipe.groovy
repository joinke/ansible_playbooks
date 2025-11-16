// Active Choices parameters
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

        // Reactive HTML checkbox list for ENV
        [$class: 'DynamicReferenceParameter',
            name: 'ENV',
            description: 'Select environments (only shown for Start AMH)',
            referencedParameters: 'OPERATION',
            script: [$class: 'GroovyScript',
                script: [script: '''
                    if (OPERATION == "Start AMH (example.py)") {
                        return """
                            <b>Please select environments:</b><br>
                            <input type='checkbox' name='value' value='UAT01'> UAT01<br>
                            <input type='checkbox' name='value' value='UAT02'> UAT02<br>
                            <input type='checkbox' name='value' value='UAT03'> UAT03<br>
                        """
                    } else {
                        return "<i>No environments required for this operation</i>"
                    }
                """, sandbox: true],
                fallbackScript: [script: 'return "<i>No environments available</i>"', sandbox: true]
            ]
        ]
    ])
])

// Declarative pipeline body
pipeline {
  agent any
  options {
    ansiColor('xterm')
  }
  environment {
    OPERATION = "${params.OPERATION}"
    ENVS      = "${params['ENV'] ?: ''}"
  }
  stages {
    stage('Print Params') {
      steps {
        script {
          echo "Selected OPERATION: ${env.OPERATION}"
          echo "Selected ENV: ${env.ENVS}"
        }
      }
    }
  }
}
