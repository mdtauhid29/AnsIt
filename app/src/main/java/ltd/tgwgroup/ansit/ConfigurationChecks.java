package ltd.tgwgroup.ansit;

import java.io.File;
import org.json.JSONObject;

/** Real requests are checked before a caller may publish new settings. */
final class ConfigurationChecks {
    interface Probes { VisionProbe create(); }
    interface Publisher { void write(File file, JSONObject value) throws Exception; }
    private final Probes probes;
    private final String searchEndpoint;
    private final Publisher publisher;
    ConfigurationChecks() { this(VisionProbe::create, SearchConfig.ENDPOINT); }
    ConfigurationChecks(Probes probes, String searchEndpoint) { this(probes, searchEndpoint, PrivateSettings::write); }
    ConfigurationChecks(Probes probes, String searchEndpoint, Publisher publisher) {
        this.probes = probes; this.searchEndpoint = searchEndpoint; this.publisher = publisher;
    }

    String saveModel(LlmCall call, ApiConfig config, boolean tools, File target) throws Exception {
        call.limitSeconds(120);
        String result = model(call, config, tools);
        call.check();
        publisher.write(target, config.json());
        return result;
    }
    String saveSearch(LlmCall call, SearchConfig search, ApiConfig savedModel, File target) throws Exception {
        call.limitSeconds(120);
        String result = search(call, search, savedModel);
        call.check();
        publisher.write(target, search.json());
        return result;
    }

    String model(LlmCall call, ApiConfig config, boolean tools) throws Exception {
        call.verifyModel(config, probes.create(), tools);
        return tools ? "Image input and tool calling verified" : "Image input verified";
    }
    String search(LlmCall call, SearchConfig search, ApiConfig savedModel) throws Exception {
        if (!search.enabled) return "Search disabled (no network check performed)";
        int count = new DeepSeekSearch(call, search.key, searchEndpoint).search("DeepSeek official API documentation", 1).getJSONArray("results").length();
        String result = count > 0 ? "Search endpoint verified" : "Search endpoint responded normally (the test query had no matches)";
        if (search.enabled && savedModel != null) {
            call.verifyModel(savedModel, probes.create(), true);
            result += "; main model image input and tool calling verified";
        } else if (search.enabled) result += "; save and verify the model configuration again to confirm tool support";
        return result;
    }
}
