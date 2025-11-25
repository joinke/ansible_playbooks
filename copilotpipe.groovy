pipeline {
    agent any

    parameters {
        // Active Choices Parameter for multi-select checkboxes
        activeChoiceParam(name: 'OPERATIONS') {
            description('Select one or more operations')
            filterable(false)
            choiceType('CHECKBOX')  // can also be 'MULTI_SELECT'
            groovyScript {
                script("""
                    import java.nio.file.Files
                    import java.nio.file.Paths

                    def filePath = '/var/jenkins_home/operations.txt'
                    def operations = []

                    if (Files.exists(Paths.get(filePath))) {
                        operations = new File(filePath).readLines()
                            .collect { it.split("\\|")[0].trim() }  // take first column
                            .findAll { it }                          // remove empty lines
                    } else {
                        operations = ["ERROR: operations.txt not found"]
                    }

                    return operations
                """.stripIndent())
                fallbackScript('return ["ERROR reading file"]')
            }
        }
    }

    stages {
        stage('Show Selected') {
            steps {
                script {
                    echo "Selected operations: ${params.OPERATIONS}"
                }
            }
        }

        stage('Do Operations') {
            steps {
                script {
                    if (params.OPERATIONS) {
                        params.OPERATIONS.each { op ->
                            echo "Processing operation: ${op}"
                            // place your operation logic here
                        }
                    } else {
                        echo "No operations selected."
                    }
                }
            }
        }
    }
}
