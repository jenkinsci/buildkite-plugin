package io.jenkins.plugins.buildkite.api_client;

public class BuildkiteBuild {
    private String id;
    private int number;
    private String commit;
    private String branch;
    private String state;
    private String url;
    private String webUrl;
    private String message;

    public String getId() {
        return id;
    }

    public BuildkiteBuild setId(String id) {
        this.id = id;
        return this;
    }

    public int getNumber() {
        return number;
    }

    public BuildkiteBuild setNumber(int number) {
        this.number = number;
        return this;
    }

    public String getCommit() {
        return commit;
    }

    public BuildkiteBuild setCommit(String commit) {
        this.commit = commit;
        return this;
    }

    public String getBranch() {
        return branch;
    }

    public BuildkiteBuild setBranch(String branch) {
        this.branch = branch;
        return this;
    }

    public String getState() {
        return state;
    }

    public BuildkiteBuild setState(String state) {
        this.state = state;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public BuildkiteBuild setUrl(String url) {
        this.url = url;
        return this;
    }

    public String getWebUrl() {
        return webUrl;
    }

    public BuildkiteBuild setWebUrl(String webUrl) {
        this.webUrl = webUrl;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public BuildkiteBuild setMessage(String message) {
        this.message = message;
        return this;
    }

    public boolean buildFinished() {
        return "passed".equals(state) || "failed".equals(state) || "canceled".equals(state) || "blocked".equals(state);
    }

    public boolean buildPassed() {
        return "passed".equals(state);
    }
}
