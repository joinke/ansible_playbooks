pipeline {
    agent any

    // No parameters defined here yet; we'll define them dynamically in the first stage
    stages {
        stage('Setup Parameter') {
            steps {
                script {
                    // Ensure the file exists
                    def opsFile = "${env.WORKSPACE}/operations.txt"
                    if (!fileExists(opsFile)) {
                        error "File operations.txt not found in workspace!"
                    }

                    // Read operations.txt and extract the first part before '|' as the choice
                    def choices = readFile(opsFile)
                                    .readLines()
                                    .collect { line -> 
                                        def trimmed = line.trim()
                                        if (!trimmed) return null
                                        return trimmed.split("\\|")[0].trim()
                                    }
                                    .findAll { it } // remove nulls / empty strings

                    if (!choices) {
                        error "No valid operations found in operations.txt"
                    }

                    // Dynamically create the Choice Parameter
                    properties([
                        parameters([
                            choice(
                                name: 'OPERATION',
                                choices: choices.join('\n'),
                                description: 'Select an operation'
                            )
                        ])
                    ])

                    echo "Choices setup: ${choices}"
                }
            }
        }

        stage('Run') {
            steps {
                echo "Selected operation: ${params.OPERATION}"
                // Here you can run the actual logic based on the choice
            }
        }
    }
}
