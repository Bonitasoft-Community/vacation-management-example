node {
    // URL of the GitHub repository
    def gitRepositoryURL = scm.userRemoteConfigs[0].url

    // Need to do a checkout due to https://issues.jenkins-ci.org/browse/JENKINS-46431
    checkout scm

    // Steps are actually defined in a job shared by multiple community projects
    build job: 'generic-bonita-example-build-deploy-test', parameters: [[$class: 'StringParameterValue', name: 'LIVINGAPP_GIT_URL', value: gitRepositoryURL], [$class: 'StringParameterValue', name: 'LIVINGAPP_GIT_BRANCH', value: BRANCH_NAME]]
}

