package io.jenkins.plugins.buildkite.step;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BuildkiteStepDescriptorTest {

    private BuildkiteStep.DescriptorImpl descriptor;

    @BeforeEach
    void setUp() {
        descriptor = new BuildkiteStep.DescriptorImpl();
    }

    @Test
    void getFunctionName_returnsBuildkite() {
        assertEquals("buildkite", descriptor.getFunctionName());
    }

    @Test
    void getDisplayName_returnsCorrectName() {
        assertEquals("Trigger a Buildkite Build", descriptor.getDisplayName());
    }
}
