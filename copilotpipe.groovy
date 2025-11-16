// Define Active Choices parameters
properties([
    parameters([
        // Dropdown for OPERATION
        [$class: 'CascadeChoiceParameter',
            choiceType: 'PT_SINGLE_SELECT',   // ✅ dropdown
            description: 'Select the operation',
            name: 'OPERATION',
            script: [$class: 'GroovyScript',
                script: [script: '''
                    return ["Start AMH (example.py)", "Stop AMH (example1.py)", "Restart AMH (example3.py)"]
                ''', sandbox: true],
                fallbackScript: [script: 'return ["Start AMH (example.py)"]', sandbox: true]
            ]
        ],

        // Checkbox list for ENV
        [$class: 'CascadeChoiceParameter',
            choiceType: 'PT_CHECKBOX',        // ✅ checkbox list
            description: 'Select environments',
            name: 'ENV',
            script: [$class: 'GroovyScript',
                script: [script: 'return ["UAT01","UAT02","UAT03"]', sandbox: true],
                fallbackScript: [script: 'return ["UAT01"]', sandbox: true]
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
