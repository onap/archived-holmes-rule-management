/**
 * Copyright 2017-2021 ZTE Corporation.
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
package org.onap.holmes.rulemgt.bolt.enginebolt;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;
import org.jvnet.hk2.annotations.Service;
import org.onap.holmes.common.exception.CorrelationException;
import org.onap.holmes.rulemgt.bean.request.CorrelationCheckRule4Engine;
import org.onap.holmes.rulemgt.bean.request.CorrelationDeployRule4Engine;
import org.onap.holmes.rulemgt.constant.RuleMgtConstant;

import javax.inject.Inject;

@Service
@Slf4j
public class EngineWrapper {

    @Inject
    private EngineService engineService;

    public String deployEngine(CorrelationDeployRule4Engine correlationRule, String ip) throws CorrelationException {
        String response = engineService.deploy(correlationRule, ip);
        if (response != null) {
            try {
                JsonObject json = JsonParser.parseString(response).getAsJsonObject();
                return json.get(RuleMgtConstant.PACKAGE).getAsString();
            } catch (Exception e) {
                throw new CorrelationException("Failed to parse the value returned by the engine management service.", e);
            }
        } else {
            throw new CorrelationException("Failed to deploy the rule!");
        }
    }

    public boolean deleteRuleFromEngine(String packageName, String ip) throws CorrelationException {
        if (engineService.delete(packageName, ip)) {
            return true;
        } else {
            throw new CorrelationException("Failed to delete the rule!");
        }
    }

    public boolean checkRuleFromEngine(CorrelationCheckRule4Engine correlationCheckRule4Engine, String ip)
            throws CorrelationException {
        log.info("Rule Contents: " + correlationCheckRule4Engine.getContent());
        if (!engineService.check(correlationCheckRule4Engine, ip)) {
            throw new CorrelationException("Failed to verify the rule. The contents of the rule are invalid.");
        }
        return true;
    }
}
