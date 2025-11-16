// Define a single Active Choices checkbox parameter
properties([
    parameters([
        [$class: 'CascadeChoiceParameter',
            choiceType: 'PT_CHECKBOX',   // ✅ must use PT_CHECKBOX
            description: 'Enable feature?',
            name: 'FEATURE_FLAG',
            script: [$class: 'GroovyScript',
                script: [script: 'return ["UAT01","UAT02"]', sandbox: true],
                fallbackScript: [script: 'return ["false"]', sandbox: true]
            ]
        ]
    ])
])

node {
    stage('Show Param') {
        echo "FEATURE_FLAG selected: ${params.FEATURE_FLAG}"
    }
}
