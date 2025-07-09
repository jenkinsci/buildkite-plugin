package io.jenkins.plugins.buildkite;

import hudson.Extension;
import hudson.ExtensionList;
import hudson.util.FormValidation;
import hudson.util.Secret;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest2;

import java.net.HttpURLConnection;
import java.net.URL;

@Extension
public class GlobalConfiguration extends jenkins.model.GlobalConfiguration {
    private String baseUrl = "https://api.buildkite.com/";
    private Secret apiKey;

    public static GlobalConfiguration get() {
        return ExtensionList.lookupSingleton(GlobalConfiguration.class);
    }

    public GlobalConfiguration() {
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

    public Secret getApiKey() {
        return apiKey;
    }

    @DataBoundSetter
    public void setApiKey(Secret apiKey) {
        this.apiKey = apiKey;
    }

    public FormValidation doCheckBaseUrl(@QueryParameter String value) {
        if (StringUtils.isBlank(value)) {
            return FormValidation.error("Base URL cannot be empty");
        }
        if (!value.startsWith("http")) {
            return FormValidation.warning("URL should start with http or https");
        }
        return FormValidation.ok();
    }

    public FormValidation doCheckApiKey(@QueryParameter String value) {
        if (StringUtils.isBlank(value)) {
            return FormValidation.error("API key is required");
        }
        return FormValidation.ok();
    }

    public FormValidation doTestConnection(@QueryParameter("baseUrl") String baseUrl,
                                           @QueryParameter("apiKey") Secret apiKey) {
        try {
            URL url = new URL(baseUrl.replaceAll("/$", "") + "/v2/organizations");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Bearer " + Secret.toString(apiKey));
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            int code = conn.getResponseCode();
            if (code == 200) {
                return FormValidation.ok("Connection successful");
            } else {
                return FormValidation.error("Connection failed (HTTP " + code + ")");
            }
        } catch (Exception ex) {
            return FormValidation.error("Connection failed: " + ex.getMessage());
        }
    }
}
