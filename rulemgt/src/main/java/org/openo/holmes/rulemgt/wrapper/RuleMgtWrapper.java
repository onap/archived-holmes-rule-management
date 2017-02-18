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
package org.openo.holmes.rulemgt.wrapper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.jvnet.hk2.annotations.Service;
import org.openo.holmes.common.api.entity.CorrelationRule;
import org.openo.holmes.common.exception.CorrelationException;
import org.openo.holmes.common.utils.DbDaoUtil;
import org.openo.holmes.common.utils.I18nProxy;
import org.openo.holmes.rulemgt.bean.request.CorrelationCheckRule4Engine;
import org.openo.holmes.rulemgt.bean.request.CorrelationDeployRule4Engine;
import org.openo.holmes.rulemgt.bean.request.RuleCreateRequest;
import org.openo.holmes.rulemgt.bean.request.RuleDeleteRequest;
import org.openo.holmes.rulemgt.bean.request.RuleQueryCondition;
import org.openo.holmes.rulemgt.bean.request.RuleUpdateRequest;
import org.openo.holmes.rulemgt.bean.response.RuleAddAndUpdateResponse;
import org.openo.holmes.rulemgt.bean.response.RuleQueryListResponse;
import org.openo.holmes.rulemgt.bean.response.RuleResult4API;
import org.openo.holmes.rulemgt.bolt.enginebolt.EngineWrapper;
import org.openo.holmes.rulemgt.constant.RuleMgtConstant;
import org.openo.holmes.rulemgt.db.CorrelationRuleDao;
import org.openo.holmes.rulemgt.db.CorrelationRuleQueryDao;


@Service
@Singleton
@Slf4j
public class RuleMgtWrapper {

    @Inject
    private CorrelationRuleQueryDao correlationRuleQueryDao;
    @Inject
    private EngineWrapper engineWarpper;
    @Inject
    private DbDaoUtil daoUtil;

    public RuleAddAndUpdateResponse addCorrelationRule(String creator,
            RuleCreateRequest ruleCreateRequest)
            throws CorrelationException {
        CorrelationRule correlationRule = convertRuleCreateRequest2CorrelationRule(creator,
                ruleCreateRequest);
        if (correlationRule.getName() == null || "".equals(correlationRule.getName().trim())) {
            throw new CorrelationException(I18nProxy.RULE_MANAGEMENT_RULE_NAME_IS_EMPTY);
        }
        CorrelationRule ruleTemp;
        try {
            ruleTemp = daoUtil.getJdbiDaoByOnDemand(CorrelationRuleDao.class)
                    .queryRuleByRuleName(correlationRule.getName());
        } catch (Exception e) {
            throw new CorrelationException(I18nProxy.RULE_MANAGEMENT_DB_ERROR, e);
        }
        if (ruleTemp != null) {
            throw new CorrelationException(I18nProxy.RULE_MANAGEMENT_REPEAT_RULE_NAME);
        }
        correlationRule.setPackageName(deployRule2Engine(correlationRule));
        try {
            correlationRule = daoUtil.getJdbiDaoByOnDemand(CorrelationRuleDao.class)
                    .saveRule(correlationRule);
        } catch (Exception e) {
            throw new CorrelationException(I18nProxy.RULE_MANAGEMENT_CREATE_RULE_FAILED, e);
        }
        RuleAddAndUpdateResponse ruleAddAndUpdateResponse = new RuleAddAndUpdateResponse();
        ruleAddAndUpdateResponse.setRuleId(correlationRule.getRid());
        return ruleAddAndUpdateResponse;
    }

    public RuleAddAndUpdateResponse updateCorrelationRule(String modifier,
            RuleUpdateRequest ruleUpdateRequest)
            throws CorrelationException {
        if (ruleUpdateRequest != null) {
            CorrelationRule oldCorrelationRule;
            try {
                oldCorrelationRule = daoUtil
                        .getJdbiDaoByOnDemand(CorrelationRuleDao.class)
                        .getRuleByRid(ruleUpdateRequest.getRuleId());
            } catch (Exception e) {
                throw new CorrelationException(I18nProxy.RULE_MANAGEMENT_DB_ERROR, e);
            }
            if (oldCorrelationRule == null) {
                throw new CorrelationException(I18nProxy.RULE_MANAGEMENT_RULE_NOT_EXIST_DATABASE);
            }
            CorrelationRule newCorrelationRule = convertRuleUpdateRequest2CorrelationRule(modifier,
                    ruleUpdateRequest);
            checkCorrelation(newCorrelationRule, oldCorrelationRule);
            RuleAddAndUpdateResponse ruleChangeResponse = new RuleAddAndUpdateResponse();
            try {
                if (oldCorrelationRule.getEnabled() == RuleMgtConstant.STATUS_RULE_OPEN) {
                    engineWarpper.deleteRuleFromEngine(oldCorrelationRule.getPackageName());
                }
                daoUtil.getJdbiDaoByOnDemand(CorrelationRuleDao.class)
                        .updateRule(newCorrelationRule);
            } catch (Exception e) {
                throw new CorrelationException(I18nProxy.RULE_MANAGEMENT_UPDATE_RULE_FAILED, e);
            }
            ruleChangeResponse.setRuleId(newCorrelationRule.getRid());
            deployRule2Engine(newCorrelationRule);
            return ruleChangeResponse;
        } else {
            throw new CorrelationException(I18nProxy.RULE_MANAGEMENT_REQUEST_OBJECT_IS_EMPTY);
        }

    }

    public void checkCorrelation(CorrelationRule newCorrelationRule,
            CorrelationRule oldCorrelationRule) throws CorrelationException {
        int newEnabled = newCorrelationRule.getEnabled();
        if (newCorrelationRule.getContent() == null) {
            newCorrelationRule.setContent(oldCorrelationRule.getContent());
        }
        if (newEnabled != RuleMgtConstant.STATUS_RULE_CLOSE
                && newEnabled != RuleMgtConstant.STATUS_RULE_OPEN) {
            throw new CorrelationException(I18nProxy.RULE_MANAGEMENT_PARAMETER_ENABLED_ERROR);
        }
    }

    public void deleteCorrelationRule(RuleDeleteRequest ruleDeleteRequest)
            throws CorrelationException {
        if (ruleDeleteRequest != null) {
            CorrelationRule correlationRule;
            try {
                correlationRule = daoUtil
                        .getJdbiDaoByOnDemand(CorrelationRuleDao.class)
                        .getRuleByRid(ruleDeleteRequest.getRuleId());
            } catch (Exception e) {
                throw new CorrelationException(I18nProxy.RULE_MANAGEMENT_DB_ERROR);
            }
            if (correlationRule == null) {
                log.warn("the rule:rule id=" + ruleDeleteRequest.getRuleId()
                        + " does not exist the database.");
                throw new CorrelationException(I18nProxy.RULE_MANAGEMENT_RULE_NOT_EXIST_DATABASE);
            }
            try {
                if (correlationRule.getEnabled() == RuleMgtConstant.STATUS_RULE_OPEN) {
                    engineWarpper.deleteRuleFromEngine(correlationRule.getPackageName());
                }
                daoUtil.getJdbiDaoByOnDemand(CorrelationRuleDao.class)
                        .deleteRule(correlationRule);
            } catch (Exception e) {
                throw new CorrelationException(I18nProxy.RULE_MANAGEMENT_DELETE_RULE_FAILED, e);
            }
        } else {
            throw new CorrelationException(I18nProxy.RULE_MANAGEMENT_REQUEST_OBJECT_IS_EMPTY);
        }

    }

    public CorrelationRule convertRuleCreateRequest2CorrelationRule(String userName,
            RuleCreateRequest ruleCreateRequest) throws CorrelationException {
        if (ruleCreateRequest != null) {
            if (ruleCreateRequest.getEnabled() != RuleMgtConstant.STATUS_RULE_OPEN
                    && ruleCreateRequest.getEnabled() != RuleMgtConstant.STATUS_RULE_CLOSE) {
                throw new CorrelationException(I18nProxy.RULE_MANAGEMENT_REQUEST_OBJECT_IS_EMPTY);
            }
            CorrelationRule correlationRule = new CorrelationRule();
            String ruleId = "rule_" + System.currentTimeMillis();
            correlationRule.setRid(ruleId);
            correlationRule.setContent(ruleCreateRequest.getContent());
            correlationRule.setDescription(ruleCreateRequest.getDescription());
            correlationRule.setCreateTime(new Date());
            correlationRule.setUpdateTime(new Date());
            correlationRule.setName(ruleCreateRequest.getRuleName());
            correlationRule.setEngineId("correlation-d");
            correlationRule.setEngineType("");
            correlationRule.setIsManual(0);
            correlationRule.setTemplateID(0);
            correlationRule.setVendor("");
            correlationRule.setCreator(userName);
            correlationRule.setModifier(userName);
            correlationRule.setEnabled(ruleCreateRequest.getEnabled());
            return correlationRule;
        } else {
            throw new CorrelationException(I18nProxy.RULE_MANAGEMENT_REQUEST_OBJECT_IS_EMPTY);
        }

    }

    private CorrelationRule convertRuleUpdateRequest2CorrelationRule(String modifier,
            RuleUpdateRequest ruleUpdateRequest) throws CorrelationException {
        if (ruleUpdateRequest != null) {
            CorrelationRule correlationRule = new CorrelationRule();
            correlationRule.setRid(ruleUpdateRequest.getRuleId());
            correlationRule.setContent(ruleUpdateRequest.getContent());
            correlationRule.setDescription(ruleUpdateRequest.getDescription());
            correlationRule.setEnabled(ruleUpdateRequest.getEnabled());
            correlationRule.setUpdateTime(new Date());
            correlationRule.setModifier(modifier);
            return correlationRule;
        } else {
            throw new CorrelationException(I18nProxy.RULE_MANAGEMENT_REQUEST_OBJECT_IS_EMPTY);
        }

    }

    private String deployRule2Engine(CorrelationRule correlationRule) throws CorrelationException {
        if (engineWarpper.checkRuleFromEngine(correlationRules2CheckRule(correlationRule))) {
            if (correlationRule.getEnabled() == RuleMgtConstant.STATUS_RULE_OPEN) {
                return engineWarpper.deployEngine(correlationRules2DeployRule(correlationRule));
            }
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
            ruleResult4API.setRuleId(correlationRule.getRid());
            ruleResult4API.setRuleName(correlationRule.getName());
            ruleResult4API.setDescription(correlationRule.getDescription());
            ruleResult4API.setContent(correlationRule.getContent());
            ruleResult4API.setCreateTime(correlationRule.getCreateTime());
            ruleResult4API.setCreator(correlationRule.getCreator());
            ruleResult4API.setUpdateTime(correlationRule.getUpdateTime());
            ruleResult4API.setModifier(correlationRule.getModifier());
            ruleResult4API.setEnabled(correlationRule.getEnabled());
            ruleResult4APIs.add(ruleResult4API);
        }
        return ruleResult4APIs;
    }

    private CorrelationDeployRule4Engine correlationRules2DeployRule(
            CorrelationRule correlationRule) {
        CorrelationDeployRule4Engine correlationDeployRule4Engine = new CorrelationDeployRule4Engine();
        correlationDeployRule4Engine.setContent(correlationRule.getContent());
        correlationDeployRule4Engine.setEngineId(correlationRule.getEngineId());
        return correlationDeployRule4Engine;
    }

    private CorrelationCheckRule4Engine correlationRules2CheckRule(
            CorrelationRule correlationRule) {
        CorrelationCheckRule4Engine correlationCheckRule4Engine = new CorrelationCheckRule4Engine();
        correlationCheckRule4Engine.setContent(correlationRule.getContent());
        return correlationCheckRule4Engine;
    }
}
