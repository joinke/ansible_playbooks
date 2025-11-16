def envMap = [
    'UAT01': 'UAT01',
    'UAT02': 'UAT02',
    'UAT03': 'UAT03'
]

def getSelectedKeys(mymap, boolString) {
    def bools = boolString.split(',').collect { it.trim().toBoolean() }
    def keys = mymap.keySet().toList()
    def selected = []
    for (int i = 0; i < Math.min(keys.size(), bools.size()); i++) {
        if (bools[i]) {
            selected << keys[i]
        }
    }
    return selected.join(',')
}

properties([
    parameters([
        [
            $class: 'DynamicReferenceParameter',
            name: 'OPERATION',
            choiceType: 'ET_FORMATTED_HTML',
            omitValueField: true,
            referencedParameters: '',
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
                        // Select the first option by default
                        def defaultValue = commandMap.keySet().iterator().next()
                        
                        // Build <select> dropdown
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
                        def individualmap = [
                            'true': 'Individual Hosts',
                        ]
                        // Pre-select some options if needed
                        def defaultSelected = ['false']
                        
                        // Build checkbox list
                        def html = new StringBuilder()
                        individualmap.each { value, label ->
                            def checked = (value in defaultSelected) ? 'checked' : ''
                            html.append("<label>")
                            html.append("<input type='checkbox' name='value' value='${value}' ${checked}> ${label}")
                            html.append("</label><br>")
                        }
                        return html.toString()                
                    '''
                ]
            ]
        ],
        [
          $class: 'CascadeChoiceParameter',
          choiceType: 'PT_MULTI_SELECT',
          name: '\u200B',
          referencedParameters: 'OPERATION',
          script: [
            $class: 'GroovyScript',
            script: [
              $class: 'SecureGroovyScript',
              sandbox: true,
              script: '''
                // Map of values → labels
                def envMap = [
                    'UAT01': 'Env UAT01',
                    'UAT02': 'Env UAT02',
                    'UAT03': 'Env UAT03'
                ]
        
                // Only show options if operation matches
                if (OPERATION?.trim() != "example.py") {
                    return []
                }
        
                def defaultSelected = ['UAT02']
        
                // Active Choices format: "label|value"
                return envMap.collect { value, label ->
                    def entry = "${label}|${value}"
                    return defaultSelected.contains(value) ? entry + "*": entry
                }
              '''
            ]
          ]
        ],
        [
            $class: 'DynamicReferenceParameter',
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
                        if (op == 'example.py') {
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
        ],
        [
            $class: 'DynamicReferenceParameter',
            name: '\u200D',
            choiceType: 'ET_FORMATTED_HTML',
            omitValueField: true,
            referencedParameters: 'OPERATION',
            script: [
                $class: 'GroovyScript',
                script: [
                    $class: 'SecureGroovyScript',
                    sandbox: true,
                    script: '''
                        def siteMap = [
                            'RCC': 'RCC',
                            'WSDC': 'WSDC',
                            'ALL': 'BOTH'
                        ]
                        def op = OPERATION?.trim()
                        // Pre-select some options if needed
                        def defaultValue = 'ALL'
                        if (op == 'example.py') {
                        // Build checkbox list
                        def html = new StringBuilder("<b>Site</b><br><select name='value'>")
                        siteMap.each { value, label ->
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
        ],
        [
            $class: 'DynamicReferenceParameter',
            name: '\u2060',
            choiceType: 'ET_FORMATTED_HTML',
            omitValueField: true,
            referencedParameters: '\u200B,\u200D,\u200C,INDIVIDUAL',
            script: [
                $class: 'GroovyScript',
                script: [
                    $class: 'SecureGroovyScript',
                    sandbox: true,
                    script: '''
                        def hostMap = [
                            'UAT01': [
                                'RCC': [
                                    'STP': ['192.168.70.175','192.168.70.193'],
                                    'WB' : ['hostc','hostd']
                                ],
                                'WSDC': [
                                    'STP': ['hoste','hostf'],
                                    'WB' : ['hostg','hosth']
                                ],
                            ],
                            'UAT02': [
                                'RCC': [
                                    'STP': ['hosti','hostj'],
                                    'WB' : ['hostk','hostl']
                                ],
                                'WSDC': [
                                    'STP': ['hostm','hostn'],
                                    'WB' : ['hosto','hostp']
                                ],
                                'ALL':[
                                    'STP': ['hosti','hostj','hostm','hostn'],
                                    'WB' : ['hostk','hostl','hosto','hostp']
                                ]
                            ]
                        ]
                        
                        def envs = \u200B.split(',')*.trim()
                        def site = \u200D
                        def comp = \u200C
                        // Collect hosts from all selected environments
                        def hosts = envs.collectMany { env ->
                            def envMap = hostMap[env] ?: [:]
                        
                            // If site == ALL, take all sites
                            def sitesToUse = (site == 'ALL') ? envMap.keySet() : [site]
                        
                            sitesToUse.collectMany { s ->
                                def compMap = envMap[s] ?: [:]
                        
                                // If comp == STPWB, take both STP and WB
                                def compsToUse = (comp == 'ALL') ? ['STP','WB'] : [comp]
                        
                                compsToUse.collectMany { c ->
                                    compMap[c] ?: []
                                }
                            }
                        }.unique()
                        if (INDIVIDUAL) {
                            def html = new StringBuilder("<label><b>Hosts</b></label><br><select multiple name='value' size='8'>")
                            hosts.each { h ->
                                html.append("<option value='${h}'>${h}</option>")
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
    stage('Run Python') {
      steps {
        script {
            def environments = getSelectedKeys(envMap, env.ENVS ?: '')
            env.SELECTEDENVS = "$environments"
            env.SELECTEDCOMP = "${env.COMPS ?: ''}"
            env.SELECTEDSITE = "${env.SITE ?: ''}"
            env.SELECTEDHOSTS = "${env.MYHOSTS ?: ''}"
            env.SELECTEDOPERATION = "${env.OPERATION ?: ''}"
            
            echo "Selected environments: ${environments}"
            echo "Selected components: ${env.COMPS}"
            echo "Selected site: ${env.SITE}"
            echo "Selected hosts: ${env.MYHOSTS}"
            echo "Selected individual : ${env.INDIVIDUAL}"
        }
        withCredentials([sshUserPrivateKey(credentialsId: '00b69538-5290-4373-a385-c2e59e5a4d9f',
                                           keyFileVariable: 'SSH_KEY',
                                           usernameVariable: 'SSH_USER')]) {
          sh '''
            echo "🧩 Using SSH key from Jenkins: $SSH_KEY for user $SSH_USER and $OPERATION and envirionments $SELECTEDENVS and component $SELECTEDCOMP and site $SELECTEDSITE "

            # Run the Python wrapper (Ansible will use the key directly)
            python3 -u ssh_runner.py
          '''
        }
      }
    }
  }

  post {
    always {
      echo '📦 Archiving fetched files...'
      archiveArtifacts artifacts: 'fetched/**/*', fingerprint: true , allowEmptyArchive: true
    }
  }
}

