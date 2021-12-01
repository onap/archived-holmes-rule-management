/**
 * Copyright 2017-2021 ZTE Corporation.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.onap.holmes.rulemgt.dcae;


import lombok.extern.slf4j.Slf4j;
import org.onap.holmes.common.dcae.DcaeConfigurationQuery;
import org.onap.holmes.common.dcae.entity.DcaeConfigurations;
import org.onap.holmes.common.dcae.entity.Rule;
import org.onap.holmes.common.exception.CorrelationException;
import org.onap.holmes.common.utils.JerseyClient;
import org.onap.holmes.common.utils.Md5Util;
import org.onap.holmes.rulemgt.bean.request.RuleCreateRequest;
import org.onap.holmes.rulemgt.bean.response.RuleQueryListResponse;
import org.onap.holmes.rulemgt.bean.response.RuleResult4API;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Slf4j
@Deprecated
public class DcaeConfigurationPolling implements Runnable {

    public static final long POLLING_PERIOD = 30 * 1000L;

    private String hostname;

    private String url = "https://127.0.0.1:9101/api/holmes-rule-mgmt/v1/rule";

    public DcaeConfigurationPolling(String hostname) {
        this.hostname = hostname;
    }

    private String prevConfigMd5 = Md5Util.md5(null);

    private boolean prevResult = false;

    @Override
    public void run() {
        DcaeConfigurations dcaeConfigurations = null;
        try {
            dcaeConfigurations = DcaeConfigurationQuery.getDcaeConfigurations(hostname);
            String md5 = Md5Util.md5(dcaeConfigurations);
            if (prevResult && prevConfigMd5.equals(md5)) {
                log.info("Operation aborted due to identical configurations.");
                return;
            }
            prevConfigMd5 = md5;
            prevResult = false;
        } catch (CorrelationException e) {
            log.error("Failed to fetch DCAE configurations. " + e.getMessage(), e);
        } catch (Exception e) {
            log.info("Failed to generate the MD5 information for new configurations.", e);
        }
        RuleQueryListResponse ruleQueryListResponse = null;
        if (dcaeConfigurations != null) {
            try {
                ruleQueryListResponse = getAllCorrelationRules();
            } catch (Exception e) {
                log.error("Failed to get deployed rules from the rule management module: " + e.getMessage(), e);
            }
        }
        if (ruleQueryListResponse != null) {
            List<RuleResult4API> ruleResult4APIs = ruleQueryListResponse.getCorrelationRules();
            deleteAllCorrelationRules(ruleResult4APIs);
            try {
                prevResult = addAllCorrelationRules(dcaeConfigurations);
            } catch (CorrelationException e) {
                log.error("Failed to add rules. " + e.getMessage(), e);
                prevResult = false;
            }
        }
    }

    private RuleQueryListResponse getAllCorrelationRules() {
        return JerseyClient.newInstance().get(url, RuleQueryListResponse.class);
    }

    private boolean addAllCorrelationRules(DcaeConfigurations dcaeConfigurations) throws CorrelationException {
        boolean suc = false;
        for (Rule rule : dcaeConfigurations.getDefaultRules()) {
            RuleCreateRequest ruleCreateRequest = getRuleCreateRequest(rule);
            suc = JerseyClient.newInstance().header("Accept", MediaType.APPLICATION_JSON)
                    .put(url, Entity.json(ruleCreateRequest)) != null;

            if (!suc) {
                break;
            }
        }
        return suc;
    }

    private void deleteAllCorrelationRules(List<RuleResult4API> ruleResult4APIs) {
        ruleResult4APIs.forEach(correlationRule -> {
            if (null == JerseyClient.newInstance().delete(url + "/" + correlationRule.getRuleId())) {
                log.warn("Failed to delete rule, the rule id is: {}", correlationRule.getRuleId());
            }
        });
    }

    private RuleCreateRequest getRuleCreateRequest(Rule rule) {
        RuleCreateRequest ruleCreateRequest = new RuleCreateRequest();
        ruleCreateRequest.setLoopControlName(rule.getLoopControlName());
        ruleCreateRequest.setRuleName(rule.getName());
        ruleCreateRequest.setContent(rule.getContents());
        ruleCreateRequest.setDescription("");
        ruleCreateRequest.setEnabled(1);
        return ruleCreateRequest;
    }
}
