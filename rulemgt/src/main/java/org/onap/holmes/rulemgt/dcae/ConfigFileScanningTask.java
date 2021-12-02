/**
 * Copyright 2021 ZTE Corporation.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onap.holmes.rulemgt.dcae;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.lang.StringUtils;
import org.onap.holmes.common.ConfigFileScanner;
import org.onap.holmes.common.utils.FileUtils;
import org.onap.holmes.common.utils.JerseyClient;
import org.onap.holmes.rulemgt.bean.request.RuleCreateRequest;
import org.onap.holmes.rulemgt.bean.response.RuleQueryListResponse;
import org.onap.holmes.rulemgt.bean.response.RuleResult4API;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.nio.file.Paths;
import java.util.*;

public class ConfigFileScanningTask implements Runnable {
    final public static long POLLING_PERIOD = 30L;
    final private static Logger LOGGER = LoggerFactory.getLogger(ConfigFileScanningTask.class);
    final private static long FILE_SIZE_LMT = 1024 * 1024 * 10; // 10MB
    final private Map<String, String> configInEffect = new HashMap(); // Contents for configInEffect are <closedControlLoop>:<ruleContents> pairs.
    private String configFile = "/opt/hrmrules/index.json";
    private ConfigFileScanner configFileScanner;
    private String url = "https://127.0.0.1:9101/api/holmes-rule-mgmt/v1/rule";

    public ConfigFileScanningTask(ConfigFileScanner configFileScanner) {
        this.configFileScanner = configFileScanner;
    }

    @Override
    public void run() {
        if (null == configFileScanner) {
            configFileScanner = new ConfigFileScanner();
        }

        try {
            Map<String, String> newConfig = extractConfigItems(configFileScanner.scan(configFile));

            List<RuleResult4API> deployedRules = getExistingRules();

            // deal with newly added rules
            final Set<String> existingKeys = new HashSet(configInEffect.keySet());
            final Set<String> newKeys = new HashSet(newConfig.keySet());
            newKeys.stream()
                    .filter(key -> !existingKeys.contains(key))
                    .forEach(key -> {
                        if (deployRule(key, newConfig.get(key))) {
                            configInEffect.put(key, newConfig.get(key));
                            LOGGER.info("Rule '{}' has been deployed.", key);
                        }
                    });

            // deal with removed rules
            existingKeys.stream().filter(key -> !newKeys.contains(key)).forEach(key -> {
                if (deleteRule(find(deployedRules, key))) {
                    configInEffect.remove(key);
                    LOGGER.info("Rule '{}' has been removed.", key);
                }
            });

            // deal with changed rules
            existingKeys.stream().filter(key -> newKeys.contains(key)).forEach(key -> {
                if (changed(configInEffect.get(key), newConfig.get(key))) {
                    if (deleteRule(find(deployedRules, key))) {
                        configInEffect.remove(key);
                        deployRule(key, newConfig.get(key));
                        configInEffect.put(key, newConfig.get(key));
                        LOGGER.info("Rule '{}' has been updated.", key);
                    }
                }
            });
        } catch (Exception e) {
            LOGGER.warn("Unhandled error: \n" + e.getMessage(), e);
        }
    }

    private Map<String, String> extractConfigItems(Map<String, String> configFiles) {
        Map<String, String> ret = new HashMap();
        for (Map.Entry entry : configFiles.entrySet()) {
            JsonArray ja = JsonParser.parseString(entry.getValue().toString()).getAsJsonArray();
            Iterator<JsonElement> iterator = ja.iterator();
            while (iterator.hasNext()) {
                JsonObject jo = iterator.next().getAsJsonObject();
                String contents = readFile(jo.get("file").getAsString());
                if (StringUtils.isNotBlank(contents)) {
                    ret.put(jo.get("closedControlLoopName").getAsString(), contents);
                }
            }
        }
        return ret;
    }

    private String normalizePath(String path) {
        if (!path.startsWith("/")) {
            return Paths.get(new File(configFile).getParent(), path).toString();
        }
        return path;
    }

    private String readFile(String path) {
        String finalPath = normalizePath(path);
        File file = new File(finalPath);
        if (file.exists() && !file.isDirectory() && file.length() <= FILE_SIZE_LMT) {
            return FileUtils.readTextFile(finalPath);
        } else {
            LOGGER.warn("The file {} does not exist or it is a directory or it is too large to load.", finalPath);
        }
        return null;
    }

    private RuleResult4API find(final List<RuleResult4API> rules, String clName) {
        for (RuleResult4API rule : rules) {
            if (rule.getLoopControlName().equals(clName)) {
                return rule;
            }
        }
        return null;
    }

    private boolean changed(String con1, String con2) {
        // if either of the arguments is null, consider it as invalid and unchanged
        if (con1 == null || con2 == null) {
            return false;
        }

        if (!con1.replaceAll("\\s", StringUtils.EMPTY)
                .equals(con2.replaceAll("\\s", StringUtils.EMPTY))) {
            return true;
        }

        return false;
    }

    private List<RuleResult4API> getExistingRules() {
        RuleQueryListResponse ruleQueryListResponse = JerseyClient.newInstance().get(url, RuleQueryListResponse.class);
        List<RuleResult4API> deployedRules = Collections.EMPTY_LIST;
        if (null != ruleQueryListResponse) {
            deployedRules = ruleQueryListResponse.getCorrelationRules();
        }
        return deployedRules;
    }

    private boolean deployRule(String clName, String contents) {
        RuleCreateRequest ruleCreateRequest = getRuleCreateRequest(clName, contents);
        if (JerseyClient.newInstance().header("Accept", MediaType.APPLICATION_JSON)
                .put(url, Entity.json(ruleCreateRequest)) == null) {
            LOGGER.error("Failed to deploy rule: {}.", clName);
            return false;
        }
        return true;
    }

    private RuleCreateRequest getRuleCreateRequest(String clName, String contents) {
        RuleCreateRequest ruleCreateRequest = new RuleCreateRequest();
        ruleCreateRequest.setLoopControlName(clName);
        ruleCreateRequest.setRuleName(clName);
        ruleCreateRequest.setContent(contents);
        ruleCreateRequest.setDescription("");
        ruleCreateRequest.setEnabled(1);
        return ruleCreateRequest;
    }

    private boolean deleteRule(RuleResult4API rule) {
        if (rule == null) {
            LOGGER.info("No rule found, nothing to delete.");
            return false;
        }
        if (null == JerseyClient.newInstance().delete(url + "/" + rule.getRuleId())) {
            LOGGER.warn("Failed to delete rule, the rule id is: {}", rule.getRuleId());
            return false;
        }
        return true;
    }
}
