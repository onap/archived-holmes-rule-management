/**
 * Copyright 2017 ZTE Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.onap.holmes.rulemgt.dcae;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import lombok.extern.slf4j.Slf4j;
import org.glassfish.jersey.client.ClientConfig;
import org.onap.holmes.common.config.MicroServiceConfig;
import org.onap.holmes.common.dcae.entity.DcaeConfigurations;
import org.onap.holmes.common.dcae.entity.Rule;
import org.onap.holmes.common.dcae.utils.DcaeConfigurationParser;
import org.onap.holmes.common.exception.CorrelationException;
import org.onap.holmes.rulemgt.bean.request.RuleCreateRequest;
import org.onap.holmes.rulemgt.bean.response.RuleQueryListResponse;
import org.onap.holmes.rulemgt.bean.response.RuleResult4API;

@Slf4j
public class DaceConfigurationPolling implements Runnable {

    public static long POLLING_PERIOD = 10 * 1000;

    private String hostname;

    private String url = "http://127.0.0.1/api/holmes-rule-mgmt/v1/rule";

    public DaceConfigurationPolling(String hostname) {
        this.hostname = hostname;
    }

    @Override
    public void run() {
        DcaeConfigurations dcaeConfigurations = null;
        try {
            dcaeConfigurations = getDcaeConfigurations();
        } catch (CorrelationException e) {
            log.error("Failed to polling dcae configurations" + e.getMessage());
        }
        if (dcaeConfigurations != null) {
            RuleQueryListResponse ruleQueryListResponse = getAllCorrelationRules();
            List<RuleResult4API> ruleResult4APIs = ruleQueryListResponse.getCorrelationRules();
            deleteAllCorrelationRules(ruleResult4APIs);
            try {
                addAllCorrelationRules(dcaeConfigurations);
            } catch (CorrelationException e) {
                log.error("Failed to add rules" + e.getMessage());
            }
        }
    }

    private DcaeConfigurations getDcaeConfigurations() throws CorrelationException {
        String serviceAddrInfo = MicroServiceConfig.getServiceAddrInfoFromCBS(hostname);
        DcaeConfigurations dcaeConfigurations = null;
        dcaeConfigurations = DcaeConfigurationParser.parse(serviceAddrInfo);
        return dcaeConfigurations;
    }

    private RuleQueryListResponse getAllCorrelationRules() {
        Client client = ClientBuilder.newClient(new ClientConfig());
        WebTarget webTarget = client.target(url);
        return webTarget.request("application/json").get()
                .readEntity(RuleQueryListResponse.class);
    }

    private void addAllCorrelationRules(DcaeConfigurations dcaeConfigurations) throws CorrelationException {
        for (Rule rule : dcaeConfigurations.getDefaultRules()) {
            RuleCreateRequest ruleCreateRequest = getRuleCreateRequest(rule);
            Client client = ClientBuilder.newClient(new ClientConfig());
            ObjectMapper mapper = new ObjectMapper();
            String content = null;
            try {
                content = mapper.writeValueAsString(ruleCreateRequest);
            } catch (JsonProcessingException e) {
                throw new CorrelationException("Failed to convert the message object to a json string.", e);
            }
            WebTarget webTarget = client.target(url);
            webTarget.request(MediaType.APPLICATION_JSON)
                    .put(Entity.entity(content, MediaType.APPLICATION_JSON));
        }
    }

    private void deleteAllCorrelationRules(List<RuleResult4API> ruleResult4APIs){
        ruleResult4APIs.forEach(correlationRule ->{
            Client client = ClientBuilder.newClient(new ClientConfig());
            WebTarget webTarget = client.target(url + "/" + correlationRule.getRuleId());
            webTarget.request(MediaType.APPLICATION_JSON).delete();
        });
    }

    private RuleCreateRequest getRuleCreateRequest(Rule rule) {
        RuleCreateRequest ruleCreateRequest = new RuleCreateRequest();
        ruleCreateRequest.setRuleName(rule.getName());
        ruleCreateRequest.setContent(rule.getContents());
        ruleCreateRequest.setDescription("");
        ruleCreateRequest.setEnabled(1);
        return ruleCreateRequest;
    }
}
