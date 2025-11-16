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

node {
    stage('Show Params') {
        echo "OPERATION: ${params.OPERATION}"
        echo "ENV: ${params.ENV}"
    }
}
