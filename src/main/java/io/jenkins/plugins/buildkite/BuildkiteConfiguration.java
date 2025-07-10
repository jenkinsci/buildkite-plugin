package io.jenkins.plugins.buildkite;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import hudson.Extension;
import hudson.ExtensionList;
import hudson.model.Item;
import hudson.security.ACL;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import hudson.util.Secret;
import jenkins.model.GlobalConfiguration;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest2;

import java.net.HttpURLConnection;
import java.net.URL;

@Extension
public class BuildkiteConfiguration extends GlobalConfiguration {
    private String baseUrl = "https://api.buildkite.com/";
    private String credentialsId;

    public static BuildkiteConfiguration get() {
        return ExtensionList.lookupSingleton(BuildkiteConfiguration.class);
    }

    public BuildkiteConfiguration() {
        load();
    }

    @Override
    public boolean configure(StaplerRequest2 req, JSONObject json) {
        req.bindJSON(this, json);
        save();
        return true;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    @DataBoundSetter
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getCredentialsId() {
        return credentialsId;
    }

    @DataBoundSetter
    public void setCredentialsId(String credentialsId) {
        this.credentialsId = credentialsId;
    }

    public FormValidation doCheckBaseUrl(@QueryParameter String baseUrl) {
        if (StringUtils.isBlank(baseUrl)) {
            return FormValidation.error("Base URL cannot be empty");
        }
        if (!baseUrl.startsWith("http")) {
            return FormValidation.warning("URL should start with http or https");
        }
        return FormValidation.ok();
    }

    public ListBoxModel doFillCredentialsIdItems(
            @AncestorInPath Item item,
            @QueryParameter String credentialsId
    ) {
        Jenkins.get().checkPermission(Jenkins.ADMINISTER);

        return (new StandardListBoxModel())
                .includeEmptyValue()
                // TODO: Maybe limit the credentials that can be selected here.
                //  I feel like we could use better values for `authentication` and `context` arguments.
                .includeAs(ACL.SYSTEM2, item, StringCredentials.class)
                .includeCurrentValue(credentialsId);
    }

    public FormValidation doTestConnection(
            @QueryParameter("baseUrl") String baseUrl,
            @QueryParameter("credentialsId") String credentialsId
    ) {
        Jenkins.get().checkPermission(Jenkins.ADMINISTER);

        Secret apiKey = lookupCredentialsSecret(credentialsId);

        if (apiKey == null) {
            return FormValidation.error("API key not selected");
        }

        try {
            var url = new URL(baseUrl.replaceAll("/$", "") + "/v2/organizations");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Bearer " + Secret.toString(apiKey));
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            var code = conn.getResponseCode();

            if (code == 200) {
                return FormValidation.ok("Connection successful");
            } else {
                return FormValidation.error("Connection failed (HTTP " + code + ")");
            }

        } catch (Exception ex) {
            return FormValidation.error("Connection failed: " + ex.getMessage());
        }
    }

    private Secret lookupCredentialsSecret(String credentialsId) {
        if (StringUtils.isBlank(credentialsId)) {
            return null;
        }

        StringCredentials credentials = CredentialsMatchers.firstOrNull(
                // TODO: Limit credentials that can be looked-up here too, perhaps?
                //  I feel like we could use better values for `item` and `authentication` arguments.
                CredentialsProvider.lookupCredentialsInItem(
                        StringCredentials.class,
                        (Item) null,
                        ACL.SYSTEM2
                ),
                CredentialsMatchers.withId(credentialsId)
        );

        return (credentials != null)
                ? credentials.getSecret()
                : null;
    }
}
