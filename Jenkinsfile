node {

    // Trigger a build when a push is made on the branch on GitHub gitRepositoryURL
    // Note: pollSCM is required to actually trigger the build but will not do any actuall polling (cf empty cron string)
    properties([pipelineTriggers([[$class: 'GitHubPushTrigger'], pollSCM('')])])

    // URL of the GitHub repository
    def gitRepositoryURL = scm.userRemoteConfigs[0].url
    
    // Cloning the repository
    // TODO: might be moved to generic-bonita-example-build-deploy-test job
    git url: gitRepositoryURL

    // Steps are actually defined in a job shared by multiple projects
    build job: 'generic-bonita-example-build-deploy-test', parameters: [[$class: 'StringParameterValue', name: 'LIVINGAPP_GIT_URL', value: gitRepositoryURL], [$class: 'StringParameterValue', name: 'LIVINGAPP_GIT_BRANCH', value: BRANCH_NAME]]
}

