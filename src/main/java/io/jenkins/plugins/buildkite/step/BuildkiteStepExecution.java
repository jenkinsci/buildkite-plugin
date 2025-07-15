package io.jenkins.plugins.buildkite.step;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.model.Result;
import hudson.model.TaskListener;
import hudson.security.ACL;
import io.jenkins.plugins.buildkite.api_client.BuildkiteApiClient;
import io.jenkins.plugins.buildkite.api_client.BuildkiteBuild;
import io.jenkins.plugins.buildkite.api_client.CreateBuildRequest;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import org.jenkinsci.plugins.workflow.steps.FlowInterruptedException;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.SynchronousNonBlockingStepExecution;

import java.io.PrintStream;

public class BuildkiteStepExecution extends SynchronousNonBlockingStepExecution<Void> {
    private static final long serialVersionUID = 1L; // Required for Serializable interface
    private transient final BuildkiteStep step;

    public BuildkiteStepExecution(@NonNull BuildkiteStep step, @NonNull StepContext context) {
        super(context);
        this.step = step;
    }

    @Override
    protected Void run() throws Exception {
        TaskListener listener = getContext().get(TaskListener.class);
        PrintStream console = listener.getLogger();

        printCreatingBuild(console);

        // TODO: Tighten up this lookup
        StringCredentials credentials = CredentialsMatchers.firstOrNull(
                CredentialsProvider.lookupCredentialsInItem(
                        StringCredentials.class,
                        null,
                        ACL.SYSTEM2
                ),
                CredentialsMatchers.withId(this.step.getCredentialsId())
        );

        if (credentials == null) {
            var errorMessage = String.format("Could not find Credentials with id: %s", this.step.getCredentialsId());
            console.println(errorMessage);

            this.getContext().onFailure(new FlowInterruptedException(Result.FAILURE));

            return null;
        }

        var client = new BuildkiteApiClient(credentials.getSecret());
        var createBuildRequest = new CreateBuildRequest();

        createBuildRequest.setBranch(this.step.getBranch());
        createBuildRequest.setCommit(this.step.getCommit());
        createBuildRequest.setMessage(this.step.getMessage());

        BuildkiteBuild build = client.createBuild(
                this.step.getOrganization(),
                this.step.getPipeline(),
                createBuildRequest
        );

        printBuildCreated(build, console);

        if (this.step.isAsync()) {
            this.getContext().onSuccess(build);

            return null;
        }

        console.println("Waiting for build to finish");

        Thread.sleep(2000);

        BuildkiteBuild pollingBuild = null;
        while (pollingBuild == null || !pollingBuild.buildFinished()) {
            pollingBuild = client.getBuild(
                    this.step.getOrganization(),
                    this.step.getPipeline(),
                    build.getNumber()
            );
            console.println(String.format("  %s", pollingBuild.getState()));

            Thread.sleep(7000);
        }

        printBuildFinished(pollingBuild, console);

        if (pollingBuild.buildPassed()) {
            this.getContext().onSuccess(build);
        } else {
            this.getContext().onFailure(new FlowInterruptedException(Result.FAILURE));
        }

        return null;
    }

    private void printCreatingBuild(PrintStream console) {
        var message = String.format("Creating build for %s/%s on %s (%s)",
                this.step.getOrganization(),
                this.step.getPipeline(),
                this.step.getBranch(),
                this.step.getCommit()
        );
        console.println(message);
    }

    private void printBuildCreated(BuildkiteBuild build, PrintStream console) {
        console.println(
                String.format("%s/%s#%s created: %s",
                        this.step.getOrganization(),
                        this.step.getPipeline(),
                        build.getNumber(),
                        build.getWebUrl()
                )
        );
    }

    private void printBuildFinished(BuildkiteBuild build, PrintStream console) {
        var message = String.format(
                "%s/%s#%s finished with state: %s",
                this.step.getOrganization(),
                this.step.getPipeline(),
                build.getNumber(),
                build.getState()
        );
        console.println(message);
    }
}
