package io.jenkins.plugins.buildkite.api_client;

public class CreateBuildRequest {
    private String commit;
    private String branch;
    private String message;

    public String getCommit() {
        return commit;
    }

    public CreateBuildRequest setCommit(String commit) {
        this.commit = commit;
        return this;
    }

    public String getBranch() {
        return branch;
    }

    public CreateBuildRequest setBranch(String branch) {
        this.branch = branch;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public CreateBuildRequest setMessage(String message) {
        this.message = message;
        return this;
    }
}
