package io.jenkins.plugins.buildkite.api_client;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateBuildRequest {
    private String commit;
    private String branch;
    private String message;
}
