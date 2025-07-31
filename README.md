# Buildkite Plugin

A plugin that allows you to trigger a [Buildkite pipeline](https://buildkite.com/docs/pipelines) from
a [Jenkins pipeline](https://www.jenkins.io/doc/book/pipeline/).

## `buildkite` Pipeline Step

`buildkite(…)` triggers a new Buildkite pipeline build from a Jenkins pipeline and blocks pipeline execution until the
Buildkite
build is complete.

### Basic example

```groovy
buildkite(
        organization: "my-org",
        pipeline: "my-pipeline",
        credentialsId: "buildkite-api-token"
)
```

`buildkite(…)` accepts the following arguments:

#### Required

| Argument        | Type   | Description                                                                                                                 |
|-----------------|--------|-----------------------------------------------------------------------------------------------------------------------------|
| `organization`  | String | Your Buildkite organization name                                                                                            |
| `pipeline`      | String | Your Buildkite pipeline slug                                                                                                |
| `credentialsId` | String | ID of the Secret Text [credentials](https://www.jenkins.io/doc/book/using/using-credentials/) with your Buildkite API token |

#### Optional

| Argument  | Type    | Default        | Description                                                                                                            |
|-----------|---------|----------------|------------------------------------------------------------------------------------------------------------------------|
| `branch`  | String  | `"main"`       | Git branch to build                                                                                                    |
| `commit`  | String  | `"HEAD"`       | Git commit SHA to build                                                                                                |
| `message` | String  | Auto-generated | Build message (auto-generated from Jenkins build name if not specified)                                                |
| `async`   | Boolean | `false`        | `false` blocks execution until the triggered build has completed. <br>`true` triggers build and continues immediately. |

### Full example

```groovy
buildkite(
        organization: "my-org",
        pipeline: "my-pipeline",
        credentialsId: "buildkite-api-token",
        branch: "feature/new-feature",
        commit: "9eb03cc26",
        message: "Custom build message",
        async: true
)
```
