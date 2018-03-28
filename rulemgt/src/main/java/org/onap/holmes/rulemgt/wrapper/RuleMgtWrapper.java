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
package org.onap.holmes.rulemgt.wrapper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.jvnet.hk2.annotations.Service;
import org.onap.holmes.rulemgt.bean.request.CorrelationCheckRule4Engine;
import org.onap.holmes.rulemgt.bean.response.RuleResult4API;
import org.onap.holmes.rulemgt.constant.RuleMgtConstant;
import org.onap.holmes.rulemgt.db.CorrelationRuleDao;
import org.onap.holmes.common.api.entity.CorrelationRule;
import org.onap.holmes.common.exception.CorrelationException;
import org.onap.holmes.common.utils.DbDaoUtil;
import org.onap.holmes.rulemgt.bean.request.CorrelationDeployRule4Engine;
import org.onap.holmes.rulemgt.bean.request.RuleCreateRequest;
import org.onap.holmes.rulemgt.bean.request.RuleDeleteRequest;
import org.onap.holmes.rulemgt.bean.request.RuleQueryCondition;
import org.onap.holmes.rulemgt.bean.request.RuleUpdateRequest;
import org.onap.holmes.rulemgt.bean.response.RuleAddAndUpdateResponse;
import org.onap.holmes.rulemgt.bean.response.RuleQueryListResponse;
import org.onap.holmes.rulemgt.bolt.enginebolt.EngineWrapper;
import org.onap.holmes.rulemgt.db.CorrelationRuleQueryDao;
import org.onap.holmes.rulemgt.send.Ip4AddingRule;


@Service
@Singleton
@Slf4j
public class RuleMgtWrapper {

    @Inject
    private Ip4AddingRule ip4AddingRule;

    @Inject
    private RuleQueryWrapper ruleQueryWrapper;

    @Inject
    private CorrelationRuleQueryDao correlationRuleQueryDao;
    @Inject
    private EngineWrapper engineWarpper;
    @Inject
    private DbDaoUtil daoUtil;

    private CorrelationRuleDao correlationRuleDao;

    @PostConstruct
    public void initDaoUtil() {
        correlationRuleDao = daoUtil.getJdbiDaoByOnDemand(CorrelationRuleDao.class);
    }

    public RuleAddAndUpdateResponse addCorrelationRule(String creator, RuleCreateRequest ruleCreateRequest)
            throws CorrelationException {
        if (ruleCreateRequest == null) {
            throw new CorrelationException("The request object can not be empty!");
        }
        CorrelationRule correlationRule = convertCreateRequest2Rule(creator,
                ruleCreateRequest);
        checkCorrelation(correlationRule);
        CorrelationRule ruleTemp = correlationRuleDao.queryRuleByRuleName(correlationRule.getName());
        if (ruleTemp != null) {
            throw new CorrelationException("A rule with the same name already exists.");
        }
        String ip ="";
        try{
            ip = ip4AddingRule.getEngineIp4AddRule();
        }catch(Exception e){
            log.error("When adding rules, can not get engine instance ip");
        }
        String packageName = deployRule2Engine(correlationRule, ip);
        correlationRule.setPackageName(packageName);
        correlationRule.setEngineInstance(ip);
        CorrelationRule result = null;
        try {
            result = correlationRuleDao.saveRule(correlationRule);
        } catch (CorrelationException e) {
            engineWarpper.deleteRuleFromEngine(packageName, ip);
            throw new CorrelationException(e.getMessage(), e);
        }
        RuleAddAndUpdateResponse ruleAddAndUpdateResponse = new RuleAddAndUpdateResponse();
        ruleAddAndUpdateResponse.setRuleId(result.getRid());
        return ruleAddAndUpdateResponse;
    }

    public RuleAddAndUpdateResponse updateCorrelationRule(String modifier, RuleUpdateRequest ruleUpdateRequest)
            throws CorrelationException {
        if (ruleUpdateRequest == null) {
            throw new CorrelationException("The request object can not be empty!");
        }
        CorrelationRule oldCorrelationRule = correlationRuleDao.queryRuleByRid(ruleUpdateRequest.getRuleId());
        if (oldCorrelationRule == null) {
            throw new CorrelationException("You're trying to update a rule which does not exist in the system.");
        }
        String updateIp = "";
        updateIp = oldCorrelationRule.getEngineInstance();
        CorrelationRule newCorrelationRule = convertRuleUpdateRequest2CorrelationRule(modifier,
                ruleUpdateRequest, oldCorrelationRule.getName());
        newCorrelationRule.setEngineInstance(updateIp);
        checkCorrelation(newCorrelationRule);
        RuleAddAndUpdateResponse ruleChangeResponse = new RuleAddAndUpdateResponse();
        ruleChangeResponse.setRuleId(newCorrelationRule.getRid());

        if (!haveChange(newCorrelationRule, oldCorrelationRule)) {
            return ruleChangeResponse;
        }
        if (oldCorrelationRule.getEnabled() == RuleMgtConstant.STATUS_RULE_OPEN) {
            String oldRuleEngineInstance = oldCorrelationRule.getEngineInstance();
            engineWarpper.deleteRuleFromEngine(oldCorrelationRule.getPackageName(), oldRuleEngineInstance);
        }
        newCorrelationRule.setPackageName(deployRule2Engine(newCorrelationRule, updateIp));
        correlationRuleDao.updateRule(newCorrelationRule);
        return ruleChangeResponse;
    }

    private void checkCorrelation(CorrelationRule correlationRule) throws CorrelationException {
        int enabled = correlationRule.getEnabled();
        String ruleName = correlationRule.getName() == null ? "" : correlationRule.getName().trim();
        String content = correlationRule.getContent() == null ? "" : correlationRule.getContent().trim();
        if ("".equals(content)) {
            throw new CorrelationException("The contents of the rule can not be empty!");
        }
        if (enabled != RuleMgtConstant.STATUS_RULE_CLOSE
                && enabled != RuleMgtConstant.STATUS_RULE_OPEN) {
            throw new CorrelationException("Invalid rule status. Only 0 (disabled) and 1 (enabled) are allowed.");
        }
        if ("".equals(ruleName)) {
            throw new CorrelationException("The name of the rule can not be empty.");
        }
    }

    private boolean haveChange(CorrelationRule newCorrelationRule, CorrelationRule oldCorrelationRule) {
        String newContent = newCorrelationRule.getContent();
        String oldContent = oldCorrelationRule.getContent();
        int newEnabled = newCorrelationRule.getEnabled();
        int oldEnabled = oldCorrelationRule.getEnabled();
        String newDes = newCorrelationRule.getDescription();
        String oldDes = oldCorrelationRule.getDescription();
        String oldControlLoop = oldCorrelationRule.getClosedControlLoopName();
        String newControlLoop = newCorrelationRule.getClosedControlLoopName();
        if (newContent.equals(oldContent) && newEnabled == oldEnabled
                && newDes.equals(oldDes) && newControlLoop.equals(oldControlLoop)) {
            return false;
        }
        return true;
    }

    public void deleteCorrelationRule(RuleDeleteRequest ruleDeleteRequest)
            throws CorrelationException {
        if (ruleDeleteRequest == null) {
            throw new CorrelationException("The request object can not be empty!");
        }
        CorrelationRule correlationRule = correlationRuleDao.queryRuleByRid(ruleDeleteRequest.getRuleId());
        if (correlationRule == null) {
            log.warn("the rule:rule id=" + ruleDeleteRequest.getRuleId() + " does not exist the database.");
            throw new CorrelationException("You're trying to delete a rule which does not exist in the system.");
        }
        if (correlationRule.getEnabled() == RuleMgtConstant.STATUS_RULE_OPEN) {
            String ip = correlationRule.getEngineInstance();
            engineWarpper.deleteRuleFromEngine(correlationRule.getPackageName(), ip);
        }
        correlationRuleDao.deleteRule(correlationRule);
    }

    private CorrelationRule convertCreateRequest2Rule(String userName,
            RuleCreateRequest ruleCreateRequest) throws CorrelationException {
        String tempContent = ruleCreateRequest.getContent();
        CorrelationRule correlationRule = new CorrelationRule();
        String ruleId = "rule_" + System.currentTimeMillis();
        String description = ruleCreateRequest.getDescription() == null ? "" : ruleCreateRequest.getDescription();
        correlationRule.setRid(ruleId);
        if (tempContent != null) {
            correlationRule.setContent(tempContent.trim());
        }
        correlationRule.setDescription(description);
        correlationRule.setCreateTime(new Date());
        correlationRule.setUpdateTime(new Date());
        correlationRule.setName(ruleCreateRequest.getRuleName());
        correlationRule.setEngineID("correlation-d");
        correlationRule.setEngineType("");
        correlationRule.setTemplateID(0);
        correlationRule.setVendor("");
        correlationRule.setCreator(userName);
        correlationRule.setModifier(userName);
        correlationRule.setEnabled(ruleCreateRequest.getEnabled());
        correlationRule.setClosedControlLoopName(ruleCreateRequest.getLoopControlName());
        return correlationRule;
    }

    private CorrelationRule convertRuleUpdateRequest2CorrelationRule(String modifier,
            RuleUpdateRequest ruleUpdateRequest, String ruleName) throws CorrelationException {
        CorrelationRule correlationRule = new CorrelationRule();
        String description = ruleUpdateRequest.getDescription() == null ? "" : ruleUpdateRequest.getDescription();
        correlationRule.setRid(ruleUpdateRequest.getRuleId());
        correlationRule.setContent(ruleUpdateRequest.getContent());
        correlationRule.setDescription(description);
        correlationRule.setEnabled(ruleUpdateRequest.getEnabled());
        correlationRule.setUpdateTime(new Date());
        correlationRule.setModifier(modifier);
        correlationRule.setName(ruleName);
        correlationRule.setClosedControlLoopName(ruleUpdateRequest.getLoopControlName());
        return correlationRule;
    }

    public String deployRule2Engine(CorrelationRule correlationRule, String ip)
            throws CorrelationException {
        if (engineWarpper.checkRuleFromEngine(correlationRules2CheckRule(correlationRule), ip) && (
                correlationRule.getEnabled() == RuleMgtConstant.STATUS_RULE_OPEN)) {
            return engineWarpper.deployEngine(correlationRules2DeployRule(correlationRule), ip);
        }
        return "";
    }

    public RuleQueryListResponse getCorrelationRuleByCondition(
            RuleQueryCondition ruleQueryCondition) throws CorrelationException {
        List<CorrelationRule> correlationRule = correlationRuleQueryDao
                .getCorrelationRulesByCondition(ruleQueryCondition);
        RuleQueryListResponse ruleQueryListResponse = new RuleQueryListResponse();
        ruleQueryListResponse.setTotalCount(correlationRule.size());
        ruleQueryListResponse
                .setCorrelationRules(correlationRules2RuleResult4APIs(correlationRule));
        return ruleQueryListResponse;
    }

    private List<RuleResult4API> correlationRules2RuleResult4APIs(
            List<CorrelationRule> correlationRules) {
        List<RuleResult4API> ruleResult4APIs = new ArrayList<RuleResult4API>();
        for (CorrelationRule correlationRule : correlationRules) {
            RuleResult4API ruleResult4API = new RuleResult4API();
            String description = correlationRule.getDescription() == null ? "" : correlationRule.getDescription();
            ruleResult4API.setRuleId(correlationRule.getRid());
            ruleResult4API.setRuleName(correlationRule.getName());
            ruleResult4API.setDescription(description);
            ruleResult4API.setContent(correlationRule.getContent());
            ruleResult4API.setCreateTime(correlationRule.getCreateTime());
            ruleResult4API.setCreator(correlationRule.getCreator());
            ruleResult4API.setUpdateTime(correlationRule.getUpdateTime());
            ruleResult4API.setModifier(correlationRule.getModifier());
            ruleResult4API.setEnabled(correlationRule.getEnabled());
            ruleResult4API.setLoopControlName(correlationRule.getClosedControlLoopName());
            ruleResult4APIs.add(ruleResult4API);
        }
        return ruleResult4APIs;
    }

    private CorrelationDeployRule4Engine correlationRules2DeployRule(
            CorrelationRule correlationRule) {
        CorrelationDeployRule4Engine correlationDeployRule4Engine = new CorrelationDeployRule4Engine();
        correlationDeployRule4Engine.setContent(correlationRule.getContent());
        correlationDeployRule4Engine.setEngineId(correlationRule.getEngineID());
        correlationDeployRule4Engine.setLoopControlName(correlationRule.getClosedControlLoopName());
        return correlationDeployRule4Engine;
    }

    private CorrelationCheckRule4Engine correlationRules2CheckRule(
            CorrelationRule correlationRule) {
        CorrelationCheckRule4Engine correlationCheckRule4Engine = new CorrelationCheckRule4Engine();
        correlationCheckRule4Engine.setContent(correlationRule.getContent());
        return correlationCheckRule4Engine;
    }
}
