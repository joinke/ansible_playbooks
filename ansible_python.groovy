def envMap = [
    'UAT01': 'UAT01',
    'UAT02': 'UAT02',
    'UAT03': 'UAT03'
]
def siteMap = [
    'RCC': 'RCC',
    'WSDC': 'WSDC',
    'ALL': 'BOTH'
]

def compMap = [
    'STP': 'STP',
    'WB': 'WB',
    'STPWB': 'ALL'
]

def hostMap = [
    'UAT01': [
        'RCC': [
            'STP': ['hosta','hostb'],
            'WB' : ['hostc','hostd'],
            'STPWB': ['hosta','hostb','hostc','hostd']
        ],
        'WSDC': [
            'STP': ['hoste','hostf'],
            'WB' : ['hostg','hosth'],
            'STPWB': ['hoste','hostf','hostg','hosth']
        ]
    ],
    'UAT02': [
        'RCC': [
            'STP': ['hosti','hostj'],
            'WB' : ['hostk','hostl'],
            'STPWB': ['hosti','hostj','hostk','hostl']
        ],
        'WSDC': [
            'STP': ['hostm','hostn'],
            'WB' : ['hosto','hostp'],
            'STPWB': ['hostm','hostn','hosto','hostp']
        ]
    ]
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
                        def branchMap = [
                            'ssh_runner1.py': 'Stop AMH',
                            'ssh_runner.py': 'Start AMH',
                            'ssh_runner3.py': 'Restart AMH',
                        ]

                        // Select the first option by default
                        def defaultValue = branchMap.keySet().iterator().next()

                        // Build <select> dropdown
                        def html = new StringBuilder("<select name='value'>")
                        branchMap.each { value, label ->
                            def  selected = (value == defaultValue) ? 'selected' : ''
                            html.append("<option name='value' value='${value}' ${selected}>${label}</option>")
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
                        def environmentmap = [
                            'true': 'Individual Hosts',
                        ]
                        // Pre-select some options if needed
                        def defaultSelected = ['UAT02']
                        
                        // Build checkbox list
                        def html = new StringBuilder()
                        environmentmap.each { value, label ->
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
            $class: 'DynamicReferenceParameter',
            name: '\u200B',
            choiceType: 'ET_FORMATTED_HTML',
            omitValueField: true,
            referencedParameters: 'OPERATION',
            script: [
                $class: 'GroovyScript',
                script: [
                    $class: 'SecureGroovyScript',
                    sandbox: true,
                    script: '''
                        def envMap = [
                            'UAT01': 'UAT01',
                            'UAT02': 'UAT02',
                            'UAT03': 'UAT03'
                        ]
                        def op = OPERATION?.trim()
                        def defaultSelected = ['UAT02']
                        
                        if (op == 'ssh_runner.py') {
                        // Build checkbox list
                        def html = new StringBuilder("<b>Environment</b><br>")
                        envMap.each { value, label ->
                            def checked = (value in defaultSelected) ? 'checked' : ''
                            html.append("<label>")
                            html.append("<input type='checkbox' name='value' value='${value}' ${checked}> ${label}")
                            html.append("</label><br>")
                        }

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
                            'STPWB': 'ALL'
                        ]
                        def op = OPERATION?.trim()
                        // Pre-select some options if needed
                        def defaultValue = 'STPWB'
                        if (op == 'ssh_runner.py') {
                        // Build checkbox list
                        def html = new StringBuilder("<b>Component</b><br><select name='value'>")
                        compMap.each { value, label ->
                            def selected = (value == defaultValue) ? 'selected' : ''
                            html.append("<option name='value' value='${value}' ${selected}>${label}</option>")
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
                        if (op == 'ssh_runner.py') {
                        // Build checkbox list
                        def html = new StringBuilder("<b>Site</b><br><select name='value'>")
                        siteMap.each { value, label ->
                            def selected = (value == defaultValue) ? 'selected' : ''
                            html.append("<option name='value' value='${value}' ${selected}>${label}</option>")
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
            name: 'HOSTS',
            choiceType: 'ET_FORMATTED_HTML',
            omitValueField: true,
            referencedParameters: '\u200B,\u200D,\u200C',
            script: [
                $class: 'GroovyScript',
                script: [
                    $class: 'SecureGroovyScript',
                    sandbox: true,
                    script: '''
                        def hostMap = [
                            'UAT01': [
                                'RCC': [
                                    'STP': ['hosta','hostb'],
                                    'WB' : ['hostc','hostd'],
                                    'STPWB': ['hosta','hostb','hostc','hostd']
                                ],
                                'WSDC': [
                                    'STP': ['hoste','hostf'],
                                    'WB' : ['hostg','hosth'],
                                    'STPWB': ['hoste','hostf','hostg','hosth']
                                ],
                                'ALL':[
                                    'STP' : ['hosta','hostb','hoste','hostf'],
                                    'WB' : ['hostc','hostd','hostg','hosth'],
                                    'STPWB':['hosta','hostb','hoste','hostf','hostc','hostd','hostg','hosth']
                                ],
                            ],
                            'UAT02': [
                                'RCC': [
                                    'STP': ['hosti','hostj'],
                                    'WB' : ['hostk','hostl'],
                                    'STPWB': ['hosti','hostj','hostk','hostl']
                                ],
                                'WSDC': [
                                    'STP': ['hostm','hostn'],
                                    'WB' : ['hosto','hostp'],
                                    'STPWB': ['hostm','hostn','hosto','hostp']
                                ],
                                'ALL':[
                                    'STP': ['hosti','hostj','hostm','hostn'],
                                    'WB' : ['hostk','hostl','hosto','hostp'],
                                    'STPWB':['hosti','hostj','hostm','hostn','hostk','hostl','hosto','hostp']
                                ]
                            ]
                        ]
                        def hosts = hostMap[\u200B][\u200D][\u200C]
                        def html = new StringBuilder("<select multiple name='value' size='8'>")
                        hosts.each { h ->
                            html.append("<option value='${h}'>${h} ${\u200B}</option>")
                        }
                        html.append("</select>")
                        return html.toString()
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
    HOST_LIST = '192.168.70.175,192.168.70.193'
    OPERATION = "${params.OPERATION}"
    ENVS = "${params['\u200B'] ?: ''}"
    COMPS = "${params['\u200C'] ?: ''}"
    SITE = "${params['\u200D'] ?: ''}"
  }
  stages {
    stage('Run Ansible via Python') {
      steps {
        script {
            def environments = getSelectedKeys(envMap, env.ENVS ?: '')
            env.SELECTEDENVS = "$environments"
            env.SELECTEDCOMP = "${env.COMPS ?: ''}"
            env.SELECTEDSITE = "${env.SITE ?: ''}"
            echo "Selected environments: ${environments}"
            echo "Selected components: ${env.SELECTEDCOMP}"
            echo "Selected components: ${env.SELECTEDSITE}"
        }
        withCredentials([sshUserPrivateKey(credentialsId: '00b69538-5290-4373-a385-c2e59e5a4d9f',
                                           keyFileVariable: 'SSH_KEY',
                                           usernameVariable: 'SSH_USER')]) {
          sh '''
            echo "🧩 Using SSH key from Jenkins: $SSH_KEY for user $SSH_USER and $OPERATION and envirionments $SELECTEDENVS and component $SELECTEDCOMP and site $SELECTEDSITE"

            # Run the Python wrapper (Ansible will use the key directly)
            python3 -u $OPERATION
          '''
        }
      }
    }
  }

  post {
    always {
      echo '📦 Archiving fetched files...'
      archiveArtifacts artifacts: 'fetched/**/*', fingerprint: true
    }
  }
}

