pipeline {
    agent any

    parameters {
        extendedChoice(
            name: 'OPERATIONS',
            type: 'PT_CHECKBOX',          // checkboxes
            description: 'Select one or more operations',
            multiSelectDelimiter: ',',    // delimiter for multiple selections
            groovyScript: '''
                import java.nio.file.Files
                import java.nio.file.Paths

                def path = '/var/jenkins_home/operations.txt'
                if (!Files.exists(Paths.get(path))) {
                    return ["ERROR: operations.txt not found"]
                }

                def lines = new File(path).readLines()
                // Extract first part before | and ignore empty lines
                return lines.collect { it.split("\\|")[0].trim() }.findAll { it }
            '''
        )
    }

    stages {
        stage('Show selected operations') {
            steps {
                script {
                    // params.OPERATIONS will be a comma-separated string
                    def selectedOps = params.OPERATIONS?.split(',') ?: []
                    echo "Selected operations: ${selectedOps}"
                }
            }
        }
    }
}
