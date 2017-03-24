/**
 * Copyright 2017 ZTE Corporation.
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
package org.openo.holmes.rulemgt.bolt.enginebolt;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.jvnet.hk2.annotations.Service;
import org.openo.holmes.common.exception.CorrelationException;
import org.openo.holmes.common.utils.I18nProxy;
import org.openo.holmes.rulemgt.bean.request.CorrelationCheckRule4Engine;
import org.openo.holmes.rulemgt.bean.request.CorrelationDeployRule4Engine;
import org.openo.holmes.rulemgt.constant.RuleMgtConstant;

@Service
@Slf4j
public class EngineWrapper {

    @Inject
    private EngineService engineService;

    public String deployEngine(CorrelationDeployRule4Engine correlationRule) throws CorrelationException {
        Response response;
        try {
            response = engineService.deploy(correlationRule);
        } catch (Exception e) {
            throw new CorrelationException(I18nProxy.RULE_MANAGEMENT_CALL_DEPLOY_RULE_REST_FAILED, e);
        }
        if (response.getStatus() == RuleMgtConstant.RESPONSE_STATUS_OK) {
            log.info("Call deploy rule rest interface in engine successfully.");
            try {
                JSONObject json = JSONObject.fromObject(response.readEntity(String.class));
                return json.get(RuleMgtConstant.PACKAGE).toString();
            } catch (Exception e) {
                throw new CorrelationException(I18nProxy.RULE_MANAGEMENT_PARSE_DEPLOY_RESULT_ERROR, e);
            }
        } else {
            throw new CorrelationException(I18nProxy.ENGINE_DEPLOY_RULE_FAILED);
        }
    }

    public boolean deleteRuleFromEngine(String packageName) throws CorrelationException {
        Response response;
        try {
            response = engineService.delete(packageName);
        } catch (Exception e) {
            throw new CorrelationException(I18nProxy.RULE_MANAGEMENT_CALL_DELETE_RULE_REST_FAILED, e);
        }
        if (response.getStatus() == RuleMgtConstant.RESPONSE_STATUS_OK) {
            log.info("Call delete rule rest interface in engine successfully.");
            return true;
        } else {
            throw new CorrelationException(I18nProxy.RULE_MANAGEMENT_DELETE_RULE_FAILED);
        }
    }

    public boolean checkRuleFromEngine(CorrelationCheckRule4Engine correlationCheckRule4Engine)
            throws CorrelationException {
        log.info("content:" + correlationCheckRule4Engine.getContent());
        Response response;
        try {
            response = engineService.check(correlationCheckRule4Engine);
        } catch (Exception e) {
            throw new CorrelationException(I18nProxy.RULE_MANAGEMENT_CALL_CHECK_RULE_REST_FAILED, e);
        }
        if (response.getStatus() == RuleMgtConstant.RESPONSE_STATUS_OK) {
            log.info("Call check rule rest interface in engine successfully.");
            return true;
        } else {
            log.info(response.getStatus() + " " + response.getStatusInfo() + " " + response.getEntity());
            throw new CorrelationException(I18nProxy.RULE_MANAGEMENT_CHECK_NO_PASS);
        }
    }
}
