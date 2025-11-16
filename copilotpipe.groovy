// Define Active Choices parameters
properties([
    parameters([
        [$class: 'CascadeChoiceParameter',
            choiceType: 'SINGLE_SELECT',
            description: 'Select the operation',
            name: 'OPERATION',
            script: [$class: 'GroovyScript',
                script: [script: '''
                    def commandMap = [
                        'example1.py': 'Stop AMH',
                        'example.py' : 'Start AMH',
                        'example3.py': 'Restart AMH'
                    ]
                    return commandMap.collect { k,v -> "${v} (${k})" }
                ''', sandbox: true],
                fallbackScript: [script: 'return ["Start AMH (example.py)"]', sandbox: true]
            ]
        ],

        [$class: 'CascadeChoiceParameter',
            choiceType: 'CHECKBOX',
            description: 'Select individual hosts',
            name: 'INDIVIDUAL',
            script: [$class: 'GroovyScript',
                script: [script: 'return ["false","true"]', sandbox: true],
                fallbackScript: [script: 'return ["false"]', sandbox: true]
            ]
        ],

        [$class: 'CascadeChoiceParameter',
            choiceType: 'CHECKBOX',
            description: 'Select environments',
            name: 'ENVS',
            script: [$class: 'GroovyScript',
                script: [script: '''
                    return [
                        [name: "Environment A", value: "UAT01", selected: true],
                        [name: "Environment B", value: "UAT02"],
                        [name: "Environment C", value: "UAT03"]
                    ]
                ''', sandbox: true],
                fallbackScript: [script: 'return ["UAT01"]', sandbox: true]
            ]
        ],

        [$class: 'CascadeChoiceParameter',
            choiceType: 'SINGLE_SELECT',
            description: 'Select component',
            name: 'COMP',
            script: [$class: 'GroovyScript',
                script: [script: '''
                    def compMap = ['STP':'STP','WB':'WB','ALL':'BOTH']
                    return compMap.collect { k,v -> "${v} (${k})" }
                ''', sandbox: true],
                fallbackScript: [script: 'return ["BOTH (ALL)"]', sandbox: true]
            ]
        ],

        [$class: 'CascadeChoiceParameter',
            choiceType: 'SINGLE_SELECT',
            description: 'Select site',
            name: 'SITE',
            script: [$class: 'GroovyScript',
                script: [script: '''
                    def siteMap = ['RCC':'RCC','WSDC':'WSDC','ALL':'BOTH']
                    return siteMap.collect { k,v -> "${v} (${k})" }
                ''', sandbox: true],
                fallbackScript: [script: 'return ["BOTH (ALL)"]', sandbox: true]
            ]
        ]
    ])
])

node {
    stage('Verify Params') {
        echo "Selected environments: ${params.ENVS}"
        echo "Selected component: ${params.COMP}"
        echo "Selected site: ${params.SITE}"
        echo "Individual selection: ${params.INDIVIDUAL}"

        if (params.INDIVIDUAL.toBoolean() && (params.ENVS == null || params.ENVS.trim().isEmpty())) {
            error("No environments selected. You must select at least one when 'INDIVIDUAL' is enabled.")
        }
    }

    stage('Run
