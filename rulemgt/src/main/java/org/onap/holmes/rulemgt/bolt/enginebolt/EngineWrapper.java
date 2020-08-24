/**
 * Copyright 2017-2020 ZTE Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onap.holmes.rulemgt.bolt.enginebolt;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.jvnet.hk2.annotations.Service;
import org.onap.holmes.common.exception.CorrelationException;
import org.onap.holmes.common.utils.HttpsUtils;
import org.onap.holmes.rulemgt.bean.request.CorrelationCheckRule4Engine;
import org.onap.holmes.rulemgt.bean.request.CorrelationDeployRule4Engine;
import org.onap.holmes.rulemgt.constant.RuleMgtConstant;

import javax.inject.Inject;

@Service
@Slf4j
public class EngineWrapper {

    @Inject
    private EngineService engineService;

    public String deployEngine(CorrelationDeployRule4Engine correlationRule,String ip) throws CorrelationException {
        HttpResponse response;
        try {
            response = engineService.deploy(correlationRule, ip);
        } catch (Exception e) {
            throw new CorrelationException("Failed to call the rule deployment RESTful API.", e);
        }
        if (response.getStatusLine().getStatusCode() == RuleMgtConstant.RESPONSE_STATUS_OK) {
            log.info("Succeeded in calling the rule deployment RESTful API from the engine management service.");
            try {
                JsonObject json = JsonParser.parseString(HttpsUtils.extractResponseEntity(response)).getAsJsonObject();
                return json.get(RuleMgtConstant.PACKAGE).getAsString();
            } catch (Exception e) {
                throw new CorrelationException("Failed to parse the value returned by the engine management service.", e);
            }
        } else {
            throw new CorrelationException("Failed to deploy the rule!");
        }
    }

    public boolean deleteRuleFromEngine(String packageName,String ip) throws CorrelationException {
        HttpResponse response;
        try {
            response = engineService.delete(packageName, ip);
        } catch (Exception e) {
            throw new CorrelationException("Failed to call the rule deleting RESTful API.", e);
        }
        if (response.getStatusLine().getStatusCode() == RuleMgtConstant.RESPONSE_STATUS_OK) {
            log.info("Succeeded in calling the rule deleting RESTful API from the engine management service.");
            return true;
        } else {
            throw new CorrelationException("Failed to delete the rule!");
        }
    }

    public boolean checkRuleFromEngine(CorrelationCheckRule4Engine correlationCheckRule4Engine,String ip)
            throws CorrelationException {
        log.info("Rule Contents: " + correlationCheckRule4Engine.getContent());
        HttpResponse response;
        try {
            response = engineService.check(correlationCheckRule4Engine, ip);
        } catch (Exception e) {
            throw new CorrelationException("Failed to call the rule verification RESTful API.", e);
        }
        if (response.getStatusLine().getStatusCode() == RuleMgtConstant.RESPONSE_STATUS_OK) {
            log.info("Succeeded in calling the rule verification RESTful API from the engine management service.");
            return true;
        } else {
            log.info(response.getStatusLine().getStatusCode() + " " + response.getEntity());
            throw new CorrelationException("Failed to verify the rule. The contents of the rule are invalid.");
        }
    }
}
