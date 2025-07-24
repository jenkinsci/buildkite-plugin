package io.jenkins.plugins.buildkite.step;

import hudson.util.FormValidation;
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

    @Test
    void doCheckOrganization_validValue_returnsOk() {
        FormValidation result = descriptor.doCheckOrganization("valid-org");
        assertEquals(FormValidation.Kind.OK, result.kind);
    }

    @Test
    void doCheckOrganization_emptyValue_returnsError() {
        FormValidation result = descriptor.doCheckOrganization("");
        assertEquals(FormValidation.Kind.ERROR, result.kind);
        assertEquals("Organization is required", result.getMessage());
    }

    @Test
    void doCheckOrganization_nullValue_returnsError() {
        FormValidation result = descriptor.doCheckOrganization(null);
        assertEquals(FormValidation.Kind.ERROR, result.kind);
        assertEquals("Organization is required", result.getMessage());
    }

    @Test
    void doCheckPipeline_validValue_returnsOk() {
        FormValidation result = descriptor.doCheckPipeline("valid-pipeline");
        assertEquals(FormValidation.Kind.OK, result.kind);
    }

    @Test
    void doCheckPipeline_emptyValue_returnsError() {
        FormValidation result = descriptor.doCheckPipeline("");
        assertEquals(FormValidation.Kind.ERROR, result.kind);
        assertEquals("Pipeline is required", result.getMessage());
    }

    @Test
    void doCheckPipeline_nullValue_returnsError() {
        FormValidation result = descriptor.doCheckPipeline(null);
        assertEquals(FormValidation.Kind.ERROR, result.kind);
        assertEquals("Pipeline is required", result.getMessage());
    }
}
