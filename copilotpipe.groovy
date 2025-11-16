// ===== Parameters Definition =====
properties([
    parameters([
        // Operation selection
        [
            $class: 'DynamicReferenceParameter',
            name: 'OPERATION',
            choiceType: 'ET_FORMATTED_HTML',
            omitValueField: true,
            script: [
                $class: 'GroovyScript',
                script: [
                    $class: 'SecureGroovyScript',
                    sandbox: true,
                    script: '''
                        def commandMap = [
                            'example1.py': 'Stop AMH',
                            'example.py': 'Start AMH',
                            'example3.py': 'Restart AMH',
                        ]
                        def defaultValue = commandMap.keySet().iterator().next()
                        def html = new StringBuilder("<select name='value'>")
                        commandMap.each { value, label ->
                            def selected = (value == defaultValue) ? 'selected' : ''
                            html.append("<option value='${value}' ${selected}>${label}</option>")
                        }
                        html.append("</select>")
                        return html.toString()
                    '''
                ]
            ]
        ],

        // Individual Hosts checkbox
        [
            $class: 'DynamicReferenceParameter',
            name: 'INDIVIDUAL',
            choiceType: 'ET_FORMATTED_HTML',
            omitValueField: true,
            referencedParameters: 'OPERATION',
            script: [
                $class: 'GroovyScript',
                script: [
                    $class: 'SecureGroovyScript',
                    sandbox: true,
                    script: '''
                        def op = OPERATION?.trim()
                        def individualMap = ['true': 'Individual Hosts']
                        def defaultSelected = ['false']
                        def html = new StringBuilder()
                        individualMap.each { value, label ->
                            def checked = (value in defaultSelected) ? 'checked' : ''
                            html.append("<label><input type='checkbox' name='value' value='${value}' ${checked}> ${label}</label><br>")
                        }
                        return html.toString()
                    '''
                ]
            ]
        ],

        // Environments ET_CHECKBOX
        [
            $class: 'DynamicReferenceParameter',
            name: 'ENVS',
            choiceType: 'ET_CHECKBOX',
            referencedParameters: 'OPERATION',
            script: [
                $class: 'GroovyScript',
                script: [
                    $class: 'SecureGroovyScript',
                    sandbox: true,
                    script: '''
                        if (OPERATION?.trim() != "example.py") return []
                        return [
                            [name: "Environment A", value: "UAT01"],
                            [name: "Environment B", value: "UAT02", selected: true],
                            [name: "Environment C", value: "UAT03"]
                        ]
                    '''
                ],
                fallbackScript: [
                    $class: 'SecureGroovyScript',
                    script: 'return []',
                    sandbox: true
                ]
            ]
        ],

        // Component dropdown
        [
            $class: 'DynamicReferenceParameter',
            name: 'COMPONENT',
            choiceType: 'ET_FORMATTED_HTML',
            referencedParameters: 'OPERATION',
            script: [
                $class: 'GroovyScript',
                script: [
                    $class: 'SecureGroovyScript',
                    sandbox: true,
                    script: '''
                        def compMap = ['STP':'STP', 'WB':'WB', 'ALL':'BOTH']
                        def defaultValue = 'ALL'
                        if (OPERATION?.trim() != "example.py") return ""
                        def html = new StringBuilder("<select name='value'>")
                        compMap.each { value, label ->
                            def selected = (value == defaultValue) ? 'selected' : ''
                            html.append("<option value='${value}' ${selected}>${label}</option>")
                        }
                        html.append("</select>")
                        return html.toString()
                    '''
                ]
            ]
        ],

        // Site dropdown
        [
            $class: 'DynamicReferenceParameter',
            name: 'SITE',
            choiceType: 'ET_FORMATTED_HTML',
            referencedParameters: 'OPERATION',
            script: [
                $class: 'GroovyScript',
                script: [
                    $class: 'SecureGroovyScript',
                    sandbox: true,
                    script: '''
                        def siteMap = ['RCC':'RCC', 'WSDC':'WSDC', 'ALL':'BOTH']
                        def defaultValue = 'ALL'
                        if (OPERATION?.trim() != "example.py") return ""
                        def html = new StringBuilder("<select name='value'>")
                        siteMap.each { value, label ->
                            def selected = (value == defaultValue) ? 'selected' : ''
                            html.append("<option value='${value}' ${selected}>${label}</option>")
                        }
                        html.append("</select>")
                        return html.toString()
                    '''
                ]
            ]
        ]
    ])
])

// ===== Pipeline Definition =====
pipeline {
    agent any
    options { ansiColor('xterm') }
    environment {
        OPERATION = "${params.OPERATION}"
        ENVS = "${params.ENVS ?: ''}"
        COMPONENT = "${params.COMPONENT ?: ''}"
        SITE = "${params.SITE ?: ''}"
        INDIVIDUAL = "${params.INDIVIDUAL}"
    }
    stages {
        stage('Verify Params') {
            steps {
                script {
                    echo "Operation: ${env.OPERATION}"
                    echo "Selected Envs: ${env.ENVS}"
                    echo "Component: ${env.COMPONENT}"
                    echo "Site: ${env.SITE}"
                    echo "Individual hosts: ${env.INDIVIDUAL}"

                    if (params.INDIVIDUAL?.toBoolean() && !env.ENVS?.trim()) {
                        error("No hosts selected! Individual is enabled but ENVS is empty.")
                    }
                }
            }
        }

        stage('Run Python') {
            steps {
                script {
                    sh """
                        echo "Running operation $OPERATION on environments $ENVS with component $COMPONENT and site $SITE"
                        python3 -u ssh_runner.py
                    """
                }
            }
        }
    }

    post {
        always {
            echo 'Archiving artifacts...'
            archiveArtifacts artifacts: 'fetched/**/*', fingerprint: true, allowEmptyArchive: true
        }
    }
}
