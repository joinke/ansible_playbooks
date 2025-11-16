def environments = '["UAT01","UAT02","UAT03"]'
def test = 'ikke'
properties([
    parameters([
        // Dropdown for OPERATION
        [$class: 'DynamicReferenceParameter',
            name: 'OPERATION',
            choiceType: 'ET_FORMATTED_HTML',
            description: 'Select the operation',
            script: [$class: 'GroovyScript',
                script: [script: '''
                    return """
                        <b>Choose an operation:</b><br>
                        <select name='value'>
                          <option value='amhstart'>Start AMH</option>
                          <option value='amhstop'>Stop AMH</option>
                          <option value='amhrestart'>Restart AMH</option>
                        </select>
                    """
                ''', sandbox: true],
                fallbackScript: [script: 'return "<i>No operations available</i>"', sandbox: true]
            ]
        ],

        // Dynamic HTML block for instructions
        [$class: 'DynamicReferenceParameter',
            name: '',
            description: '',
            choiceType: 'ET_FORMATTED_HTML',
            referencedParameters: 'OPERATION',
            script: [$class: 'GroovyScript',
                script: [script: """
                    if (OPERATION == "amhstart") {
                        return "<b>Please select environments below:</b>${test}"
                    } else {
                        return ""
                    }
                """, sandbox: true],
                fallbackScript: [script: 'return "<i>No environments available</i>"', sandbox: true]
            ]
        ],
        [$class: 'DynamicReferenceParameter',
            name: 'ENV',
            description: 'Select environments',
            referencedParameters: 'OPERATION',
            script: [$class: 'GroovyScript',
                script: [script: """
                    if (OPERATION == "amhstart") {
                        return '''
                            <b>Please select environments:</b><br>
                            <input type='checkbox' name='value' value='UAT01'> Environment A (UAT01)<br>
                            <input type='checkbox' name='value' value='UAT02'> Environment B (UAT02)<br>
                            <input type='checkbox' name='value' value='UAT03'> Environment C (UAT03)<br>
                        '''
                    } else {
                        return "<i>No environments required for this operation</i>"
                    }
                """, sandbox: true],
                fallbackScript: [script: 'return "<i>No environments available</i>"', sandbox: true]
            ]
           ],
            [$class: 'DynamicReferenceParameter',
            name: '\u200C',
            choiceType: 'ET_FORMATTED_HTML',
            omitValueField: true,
            referencedParameters: 'OPERATION',
            script: [
                $class: 'GroovyScript',
                script: [
                    $class: 'SecureGroovyScript',
                    sandbox: true,
                    script: '''
                        def compMap = [
                            'STP': 'STP',
                            'WB': 'WB',
                            'ALL': 'BOTH'
                        ]
                        def op = OPERATION?.trim()
                        // Pre-select some options if needed
                        def defaultValue = 'ALL'
                        if (op == 'amhstart') {
                        // Build checkbox list
                        def html = new StringBuilder("<b>Component</b><br><select name='value'>")
                        compMap.each { value, label ->
                            def selected = (value == defaultValue) ? 'selected' : ''
                            html.append("<option value='${value}' ${selected}>${label}</option>")
                        }
                        html.append("</select>")
                        return html.toString()
                        } else {
                          return ''
                        }
                    '''
                ]
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
          echo "ENV: ${params['\u200B']}"
          echo "COMP: ${params['\u200C']}"
        }
      }
    }
  }
}
