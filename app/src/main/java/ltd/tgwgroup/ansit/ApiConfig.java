package ltd.tgwgroup.ansit;

import java.io.File;
import java.net.URI;
import org.json.JSONObject;

final class ApiConfig {
    static final String FILE_NAME = "api-config.json";
    final String address, key, model, endpoint;
    static final class Invalid extends Exception { Invalid(String message) { super(message); } }
    ApiConfig(String address, String key, String model) throws Invalid {
        this.address = address.trim(); this.key = key.trim(); this.model = model.trim();
        if (this.address.isEmpty()) throw new Invalid("Enter an API endpoint");
        if (this.key.isEmpty() || this.key.chars().anyMatch(Character::isISOControl)) throw new Invalid("Enter a valid API key");
        if (this.model.isEmpty() || this.model.length() > 200 || this.model.chars().anyMatch(Character::isISOControl)) throw new Invalid("Enter a model name");
        try {
            URI uri = new URI(this.address);
            if (!("https".equalsIgnoreCase(uri.getScheme()) || "http".equalsIgnoreCase(uri.getScheme())) || uri.getHost() == null
                || uri.getUserInfo() != null || uri.getQuery() != null || uri.getFragment() != null) throw new Exception();
            String base = this.address.replaceAll("/+$", "");
            if (base.endsWith("/chat/completions")) endpoint = base;
            else endpoint = base + (uri.getPath() == null || uri.getPath().isEmpty() || uri.getPath().equals("/") ? "/v1/chat/completions" : "/chat/completions");
        } catch (Exception e) { throw new Invalid("The address must be an HTTP(S) base URL or a complete /chat/completions URL, without query parameters"); }
    }
    JSONObject json() throws Exception { return new JSONObject().put("address", address).put("key", key).put("model", model); }
    static ApiConfig read(File file) throws Exception {
        if (!file.isFile()) throw new Invalid("Save the endpoint, API key, and model name in AnsIt first");
        JSONObject json = PrivateSettings.read(file, 16_384);
        return new ApiConfig(json.getString("address"), json.getString("key"), json.getString("model"));
    }
}
