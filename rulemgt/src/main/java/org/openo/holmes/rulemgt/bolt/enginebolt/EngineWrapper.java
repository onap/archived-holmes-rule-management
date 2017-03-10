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

import java.io.IOException;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
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
        HttpResponse httpResponse;
        try {
            httpResponse = engineService.deploy(correlationRule);
        } catch (Exception e) {
            throw new CorrelationException(I18nProxy.RULE_MANAGEMENT_CALL_DEPLOY_RULE_REST_FAILED, e);
        }
        if (httpResponse.getStatusLine().getStatusCode() == RuleMgtConstant.RESPONSE_STATUS_OK) {
            log.info("Call deploy rule rest interface in engine successfully.");
            String content = engineService.getResponseContent(httpResponse);
            try {
                JSONObject json = JSONObject.fromObject(content);
                return json.get(RuleMgtConstant.PACKAGE).toString();
            } catch (Exception e) {
                throw new CorrelationException(I18nProxy.RULE_MANAGEMENT_PARSE_DEPLOY_RESULT_ERROR, e);
            }
        } else {
            throw new CorrelationException(I18nProxy.ENGINE_DEPLOY_RULE_FAILED);
        }
    }

    public boolean deleteRuleFromEngine(String packageName) throws CorrelationException {
        HttpResponse httpResponse;
        try {
            httpResponse = engineService.delete(packageName);
        } catch (Exception e) {
            throw new CorrelationException(I18nProxy.RULE_MANAGEMENT_CALL_DELETE_RULE_REST_FAILED, e);
        }
        if (httpResponse.getStatusLine().getStatusCode() == RuleMgtConstant.RESPONSE_STATUS_OK) {
            log.info("Call delete rule rest interface in engine successfully.");
            return true;
        } else {
            throw new CorrelationException(I18nProxy.ENGINE_DELETE_RULE_FAILED);
        }
    }

    public boolean checkRuleFromEngine(CorrelationCheckRule4Engine correlationCheckRule4Engine)
            throws CorrelationException {
        log.info("content:" + correlationCheckRule4Engine.getContent());
        HttpResponse httpResponse;
        try {
            httpResponse = engineService.check(correlationCheckRule4Engine);
        } catch (Exception e) {
            throw new CorrelationException(I18nProxy.RULE_MANAGEMENT_CALL_CHECK_RULE_REST_FAILED, e);
        }
        if (httpResponse.getStatusLine().getStatusCode() == RuleMgtConstant.RESPONSE_STATUS_OK) {
            log.info("Call check rule rest interface in engine successfully.");
            return true;
        } else {
            try {
                log.info(httpResponse.getStatusLine().getStatusCode() + "," + EntityUtils
                        .toString(httpResponse.getEntity()));
                throw new CorrelationException(I18nProxy.RULE_MANAGEMENT_CHECK_NO_PASS);
            } catch (IOException e) {
                throw new CorrelationException(I18nProxy.RULE_MANAGEMENT_CHECK_NO_PASS, e);
            }
        }
    }
}
