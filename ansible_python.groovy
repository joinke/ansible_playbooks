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
                            'main': 'Main Branch',
                            'dev': 'Development',
                            'feature-ui': 'UI Feature'
                        ]

                        // Select the first option by default
                        def defaultValue = branchMap.keySet().iterator().next()

                        // Build <select> dropdown
                        def html = new StringBuilder("<select name='value'>")
                        branchMap.each { value, label ->
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

pipeline {
  agent any
  options {
    ansiColor('xterm')  // enable colored Ansible output
  }
  environment {
    HOST_LIST = '192.168.70.175,192.168.70.193'
  }
  stages {
    stage('Run Ansible via Python') {
      steps {
        withCredentials([sshUserPrivateKey(credentialsId: '00b69538-5290-4373-a385-c2e59e5a4d9f',
                                           keyFileVariable: 'SSH_KEY',
                                           usernameVariable: 'SSH_USER')]) {
          sh '''
            echo "🧩 Using SSH key from Jenkins: $SSH_KEY for user $SSH_USER and operation ${params.OPERATION}"

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
      archiveArtifacts artifacts: 'fetched/**/*', fingerprint: true
    }
  }
}

