package io.jenkins.plugins.buildkite.api_client;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BuildkiteBuild {
    private String id;
    private int number;
    private String commit;
    private String branch;
    private String state;
    private String url;
    private String webUrl;
    private String message;

    public boolean buildFinished() {
        return "passed".equals(state) || "failed".equals(state) || "canceled".equals(state) || "blocked".equals(state);
    }

    public boolean buildPassed() {
        return "passed".equals(state);
    }
}
