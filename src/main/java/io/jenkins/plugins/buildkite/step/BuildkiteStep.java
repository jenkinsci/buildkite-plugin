package io.jenkins.plugins.buildkite.step;

import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import hudson.Extension;
import hudson.model.Item;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.security.ACL;
import hudson.util.ListBoxModel;
import lombok.Getter;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import java.util.Set;
import java.util.logging.Logger;

public class BuildkiteStep extends Step {
    private static final Logger LOGGER = Logger.getLogger(BuildkiteStep.class.getName());

    @Getter private final String organization;
    @Getter private final String pipeline;
    @Getter private final String credentialsId;
    @Getter private String branch;
    @Getter private String commit;
    @Getter private String message;
    @Getter private boolean async;

    @DataBoundConstructor
    public BuildkiteStep(String organization, String pipeline, String credentialsId) {
        // Required fields
        this.organization = organization;
        this.pipeline = pipeline;
        this.credentialsId = credentialsId;

        // Required fields with defaults
        // If specified in the `buildkite(…)`, they are overridden
        // in the @DataBoundSetter set* methods below.
        this.branch = "main";
        this.commit = "HEAD";
        this.async = false;
    }

    @Override
    public StepExecution start(StepContext context) {
        if (this.message == null) {
            String fullDisplayName;

            try {
                fullDisplayName = context.get(Run.class).getFullDisplayName();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            this.message = String.format("Triggered by Jenkins build \"%s\"", fullDisplayName);
        }

        return new BuildkiteStepExecution(this, context);
    }

    @DataBoundSetter
    public void setBranch(String branch) {
        if (branch == null || branch.trim().isEmpty()) return;

        this.branch = branch;
    }

    @DataBoundSetter
    public void setCommit(String commit) {
        if (commit == null || commit.trim().isEmpty()) return;

        this.commit = commit;
    }

    @DataBoundSetter
    public void setMessage(String message) {
        if (message == null || message.trim().isEmpty()) return;

        this.message = message;
    }

    @DataBoundSetter
    public void setAsync(boolean async) {
        this.async = async;
    }

    @Extension
    public static class DescriptorImpl extends StepDescriptor {
        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            return Set.of(
                    TaskListener.class, // Used in BuildkiteStepExecution.run
                    Run.class // Used in BuildkiteStep.start
            );
        }

        @Override
        public String getFunctionName() {
            return "buildkite";
        }

        @Override
        public String getDisplayName() {
            return "Trigger a Buildkite Build";
        }

        public ListBoxModel doFillCredentialsIdItems(
                @AncestorInPath Item item,
                @QueryParameter String credentialsId
        ) {
            item.checkPermission(Item.BUILD);

            return (new StandardListBoxModel())
                    .includeEmptyValue()
                    .includeAs(ACL.SYSTEM2, item, StringCredentials.class);
        }
    }
}
