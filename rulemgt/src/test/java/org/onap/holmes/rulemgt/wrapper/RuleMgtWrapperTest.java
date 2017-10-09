/**
 * Copyright 2017 ZTE Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onap.holmes.rulemgt.wrapper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.onap.holmes.rulemgt.bean.request.CorrelationCheckRule4Engine;
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
import org.powermock.api.easymock.PowerMock;
import org.powermock.modules.junit4.rule.PowerMockRule;
import org.powermock.reflect.Whitebox;

public class RuleMgtWrapperTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();
    @Rule
    public PowerMockRule powerMockRule = new PowerMockRule();

    private RuleMgtWrapper ruleMgtWrapper;

    private EngineWrapper engineWrapperMock;

    private DbDaoUtil dbDaoUtilMock;

    private CorrelationRuleQueryDao correlationRuleQueryDaoMock;

    private CorrelationRuleDao correlationRuleDaoMock;

    private static final String USER_NAME = "admin";

    @Before
    public void setUp() throws Exception {

        ruleMgtWrapper = new RuleMgtWrapper();

        engineWrapperMock = PowerMock.createMock(EngineWrapper.class);
        correlationRuleQueryDaoMock = PowerMock.createMock(CorrelationRuleQueryDao.class);
        dbDaoUtilMock = PowerMock.createMock(DbDaoUtil.class);
        correlationRuleDaoMock = PowerMock.createMock(CorrelationRuleDao.class);

        Whitebox.setInternalState(ruleMgtWrapper, "daoUtil", dbDaoUtilMock);
        Whitebox.setInternalState(ruleMgtWrapper, "correlationRuleQueryDao", correlationRuleQueryDaoMock);
        Whitebox.setInternalState(ruleMgtWrapper, "engineWarpper", engineWrapperMock);
        Whitebox.setInternalState(ruleMgtWrapper, "correlationRuleDao", correlationRuleDaoMock);

        PowerMock.resetAll();
    }

    @Test
    public void initDaoUtil_normal() {
        ruleMgtWrapper.initDaoUtil();
    }

    @Test
    public void addCorrelationRule_name_is_null() throws Exception {
        thrown.expect(CorrelationException.class);
        thrown.expectMessage("The name of the rule can not be empty.");

        ruleMgtWrapper.addCorrelationRule(USER_NAME, createRuleCreateRequest(null, "This is a rule for testing.",
                "Mocked contents.", 0));
    }

    @Test
    public void addCorrelationRule_request_null() throws Exception {
        thrown.expect(CorrelationException.class);
        thrown.expectMessage("The request object can not be empty!");

        ruleMgtWrapper.addCorrelationRule(USER_NAME, null);
    }

    @Test
    public void addCorrelationRule_name_is_empty() throws Exception {
        thrown.expect(CorrelationException.class);
        thrown.expectMessage("The name of the rule can not be empty.");

        ruleMgtWrapper.addCorrelationRule("admin", createRuleCreateRequest("", "This is a rule for testing.",
                "Mocked contents.", 0));
    }

    @Test
    public void addCorrelationRule_content_is_empty() throws Exception {
        thrown.expect(CorrelationException.class);
        thrown.expectMessage("The contents of the rule can not be empty!");

        ruleMgtWrapper.addCorrelationRule("admin", createRuleCreateRequest("test", "This is a rule for testing.",
                "", 0));
    }

    @Test
    public void addCorrelationRule_enabled_is_off_limit() throws Exception {
        thrown.expect(CorrelationException.class);
        thrown.expectMessage("Invalid rule status. Only 0 (disabled) and 1 (enabled) are allowed.");

        ruleMgtWrapper.addCorrelationRule("admin", createRuleCreateRequest("test", "This is a rule for testing.",
                "Mocked contents.", 3));
    }

    @Test
    public void addCorrelationRule_duplicated_rule() throws Exception {

        final String ruleName = "Rule-001";

        RuleCreateRequest ruleCreateRequest = createRuleCreateRequest(ruleName, "This is a rule for testing.",
                "Mocked contents.", 0);
        CorrelationRule correlationRule = convertCreateRequest2CorrelationRule(ruleCreateRequest);

        thrown.expect(CorrelationException.class);
        thrown.expectMessage("A rule with the same name already exists.");

        EasyMock.expect(correlationRuleDaoMock.queryRuleByRuleName(ruleName)).andReturn(correlationRule);
        PowerMock.replayAll();

        ruleMgtWrapper.addCorrelationRule("admin", ruleCreateRequest);

        PowerMock.verifyAll();
    }

    @Test
    public void addCorrelationRule_normal() throws Exception {
        final String ruleName = "Rule-001";

        RuleCreateRequest ruleCreateRequest = createRuleCreateRequest(ruleName, "This is a rule for testing.",
                "Mocked contents.", 1);
        ruleCreateRequest.setLoopControlName("loopName");

        CorrelationRule correlationRuleRet = new CorrelationRule();
        correlationRuleRet.setRid("rule_" + System.currentTimeMillis());

        EasyMock.expect(correlationRuleDaoMock.queryRuleByRuleName(ruleName)).andReturn(null);
        EasyMock.expect(engineWrapperMock.checkRuleFromEngine(EasyMock.anyObject(CorrelationCheckRule4Engine.class)))
                .andReturn(true);
        EasyMock.expect(engineWrapperMock.deployEngine(EasyMock.anyObject(CorrelationDeployRule4Engine.class)))
                .andReturn("package-001");
        EasyMock.expect(correlationRuleDaoMock.saveRule(EasyMock.anyObject(CorrelationRule.class)))
                .andReturn(correlationRuleRet);

        PowerMock.replayAll();

        RuleAddAndUpdateResponse response = ruleMgtWrapper.addCorrelationRule("admin", ruleCreateRequest);
        PowerMock.verifyAll();

        assertThat(response.getRuleId(), equalTo(correlationRuleRet.getRid()));
    }

    @Test
    public void updateCorrelationRule_param_null() throws Exception {
        thrown.expect(CorrelationException.class);
        thrown.expectMessage("The request object can not be empty!");

        ruleMgtWrapper.updateCorrelationRule(USER_NAME, null);
    }

    @Test
    public void updateCorrelationRule_normal() throws Exception {
        CorrelationRule oldCorrelationRule = new CorrelationRule();
        oldCorrelationRule.setRid("rule_1");
        oldCorrelationRule.setName("name");
        oldCorrelationRule.setDescription("des1");
        oldCorrelationRule.setContent("content");
        oldCorrelationRule.setPackageName("testName");
        oldCorrelationRule.setEnabled(1);
        RuleUpdateRequest ruleUpdateRequest = createRuleUpdateRequest("rule_1", "des2", "contetnt2", 1);

        EasyMock.expect(correlationRuleDaoMock.queryRuleByRid("rule_1")).andReturn(oldCorrelationRule);
        EasyMock.expect(engineWrapperMock.deleteRuleFromEngine("testName")).andReturn(true);
        correlationRuleDaoMock.updateRule(EasyMock.anyObject(CorrelationRule.class));
        EasyMock.expectLastCall();
        EasyMock.expect(engineWrapperMock.checkRuleFromEngine(EasyMock.anyObject(CorrelationCheckRule4Engine.class)))
                .andReturn(true);
        EasyMock.expect(engineWrapperMock.deployEngine(EasyMock.anyObject(CorrelationDeployRule4Engine.class)))
                .andReturn("packageName1");
        PowerMock.replayAll();

        ruleMgtWrapper.updateCorrelationRule(USER_NAME, ruleUpdateRequest);

        PowerMock.verifyAll();

        assertThat(oldCorrelationRule.getRid(), equalTo(ruleUpdateRequest.getRuleId()));
    }

    @Test
    public void updateCorrelationRule_param_no_change() throws Exception {
        CorrelationRule oldCorrelationRule = new CorrelationRule();
        oldCorrelationRule.setRid("rule_1");
        oldCorrelationRule.setName("name");
        oldCorrelationRule.setDescription("des1");
        oldCorrelationRule.setContent("content");
        oldCorrelationRule.setPackageName("testName");
        oldCorrelationRule.setEnabled(1);
        RuleUpdateRequest ruleUpdateRequest = createRuleUpdateRequest("rule_1", "des1", "content", 1);

        EasyMock.expect(correlationRuleDaoMock.queryRuleByRid("rule_1")).andReturn(oldCorrelationRule);

        PowerMock.replayAll();

        ruleMgtWrapper.updateCorrelationRule(USER_NAME, ruleUpdateRequest);

        PowerMock.verifyAll();

        assertThat(oldCorrelationRule.getRid(), equalTo(ruleUpdateRequest.getRuleId()));
    }

    @Test
    public void updateCorrelationRule_rule_not_exist() throws Exception {
        thrown.expect(CorrelationException.class);
        thrown.expectMessage("You're trying to update a rule which does not exist in the system.");

        EasyMock.expect(correlationRuleDaoMock.queryRuleByRid(EasyMock.anyObject(String.class))).andReturn(null);

        PowerMock.replayAll();

        ruleMgtWrapper.updateCorrelationRule(USER_NAME, new RuleUpdateRequest());

        PowerMock.verifyAll();
    }

    @Test
    public void deleteCorrelationRule_request_null() throws Exception {
        thrown.expect(CorrelationException.class);
        thrown.expectMessage("The request object can not be empty!");

        ruleMgtWrapper.deleteCorrelationRule(null);
    }

    @Test
    public void deleteCorrelationRule_rule_not_exit() throws Exception {
        thrown.expect(CorrelationException.class);
        thrown.expectMessage("You're trying to delete a rule which does not exist in the system.");

        RuleDeleteRequest ruleDeleteRequest = createRuleDeleteRequest("rule_" + System.currentTimeMillis());

        EasyMock.expect(dbDaoUtilMock.getJdbiDaoByOnDemand(CorrelationRuleDao.class)).andReturn(
                correlationRuleDaoMock).anyTimes();
        EasyMock.expect(correlationRuleDaoMock.queryRuleByRid(ruleDeleteRequest.getRuleId()))
                .andReturn(null);

        PowerMock.replayAll();

        ruleMgtWrapper.deleteCorrelationRule(ruleDeleteRequest);

        PowerMock.verifyAll();
    }

    @Test
    public void deleteCorrelationRule_normal() throws Exception {
        RuleDeleteRequest ruleDeleteRequest = createRuleDeleteRequest("rule_" + System.currentTimeMillis());
        CorrelationRule correlationRule = new CorrelationRule();
        correlationRule.setEnabled(1);
        EasyMock.expect(correlationRuleDaoMock.queryRuleByRid(ruleDeleteRequest.getRuleId()))
                .andReturn(correlationRule);
        EasyMock.expect(engineWrapperMock.deleteRuleFromEngine(EasyMock.anyObject(String.class))).andReturn(true);
        correlationRuleDaoMock.deleteRule(EasyMock.anyObject(CorrelationRule.class));
        EasyMock.expectLastCall();
        PowerMock.replayAll();

        ruleMgtWrapper.deleteCorrelationRule(ruleDeleteRequest);

        PowerMock.verifyAll();
    }

    @Test
    public void getCorrelationRuleByCondition_data_format_exception() throws Exception {
        thrown.expect(CorrelationException.class);
        thrown.expectMessage("An error occurred while building the query SQL.");

        EasyMock.expect(correlationRuleQueryDaoMock.getCorrelationRulesByCondition(EasyMock.anyObject(
                RuleQueryCondition.class)))
                .andThrow(new CorrelationException("An error occurred while building the query SQL."));

        PowerMock.replay(correlationRuleQueryDaoMock, CorrelationRuleQueryDao.class);

        ruleMgtWrapper.getCorrelationRuleByCondition(new RuleQueryCondition());

        PowerMock.verify(correlationRuleQueryDaoMock, CorrelationRuleQueryDao.class);
    }

    @Test
    public void getCorrelationRuleByCondition_db_exception() throws Exception {
        thrown.expect(CorrelationException.class);
        thrown.expectMessage("Failed to query the rule.");

        EasyMock.expect(correlationRuleQueryDaoMock.getCorrelationRulesByCondition(EasyMock.anyObject(
                RuleQueryCondition.class)))
                .andThrow(new CorrelationException("Failed to query the rule."));

        PowerMock.replay(correlationRuleQueryDaoMock, CorrelationRuleQueryDao.class);

        ruleMgtWrapper.getCorrelationRuleByCondition(new RuleQueryCondition());

        PowerMock.verify(correlationRuleQueryDaoMock, CorrelationRuleQueryDao.class);
    }

    @Test
    public void getCorrelationRuleByCondition_normal() throws Exception {
        List<CorrelationRule> correlationRuleList = new ArrayList<CorrelationRule>(10);
        for (int i = 0; i < 10; ++i) {
            CorrelationRule correlationRule = new CorrelationRule();
            correlationRule.setContent("content" + i);
            correlationRule.setName("name" + i);
            correlationRule.setRid("rule_" + i);
            correlationRule.setEngineType("engineType" + (i % 2 + 1));
            correlationRule.setEngineID("engineId" + i);
            correlationRule.setCreateTime(new Date());
            correlationRule.setCreator(USER_NAME);
            correlationRule.setDescription("description" + i);
            correlationRule.setPackageName("package" + i);
            correlationRuleList.add(correlationRule);
        }

        EasyMock.expect(correlationRuleQueryDaoMock.getCorrelationRulesByCondition(EasyMock.anyObject(
                RuleQueryCondition.class))).andReturn(correlationRuleList);

        PowerMock.replay(correlationRuleQueryDaoMock, CorrelationRuleQueryDao.class);

        RuleQueryListResponse response = ruleMgtWrapper.getCorrelationRuleByCondition(new RuleQueryCondition());

        PowerMock.verify(correlationRuleQueryDaoMock, CorrelationRuleQueryDao.class);

        assertThat(response.getTotalCount(), is(10));

        for (int i = 0; i < 10; ++i) {
            assertThat(response.getCorrelationRules().get(i).getRuleId(),
                    equalTo(correlationRuleList.get(i).getRid()));
        }
    }

    private RuleCreateRequest createRuleCreateRequest(String ruleName, String description, String content,
            int enabled) {
        RuleCreateRequest rcr;
        rcr = new RuleCreateRequest();
        rcr.setRuleName(ruleName);
        rcr.setDescription(description);
        rcr.setContent(content);
        rcr.setEnabled(enabled);
        return rcr;
    }

    private RuleUpdateRequest createRuleUpdateRequest(String ruleId, String description, String content, int enabled) {
        RuleUpdateRequest ruleUpdateRequest = new RuleUpdateRequest();
        ruleUpdateRequest.setRuleId(ruleId);
        ruleUpdateRequest.setDescription(description);
        ruleUpdateRequest.setContent(content);
        ruleUpdateRequest.setEnabled(enabled);
        return ruleUpdateRequest;
    }

    private RuleDeleteRequest createRuleDeleteRequest(String ruleId) {
        RuleDeleteRequest ruleDeleteRequest = new RuleDeleteRequest();
        ruleDeleteRequest.setRuleId(ruleId);
        return ruleDeleteRequest;
    }

    private CorrelationRule convertCreateRequest2CorrelationRule(RuleCreateRequest ruleCreateRequest) {
        CorrelationRule correlationRule = new CorrelationRule();
        correlationRule.setContent(ruleCreateRequest.getContent());
        correlationRule.setDescription(ruleCreateRequest.getDescription());
        correlationRule.setName(ruleCreateRequest.getRuleName());
        correlationRule.setCreator(USER_NAME);
        correlationRule.setModifier(USER_NAME);
        correlationRule.setEnabled(ruleCreateRequest.getEnabled());
        return correlationRule;
    }

    private CorrelationRule convertUpdateRequest2CorrelationRule(RuleUpdateRequest ruleUpdateRequest) {
        CorrelationRule correlationRule = new CorrelationRule();
        correlationRule.setRid(ruleUpdateRequest.getRuleId());
        correlationRule.setContent(ruleUpdateRequest.getContent());
        correlationRule.setDescription(ruleUpdateRequest.getDescription());
        correlationRule.setEnabled(ruleUpdateRequest.getEnabled());
        correlationRule.setUpdateTime(new Date());
        correlationRule.setModifier(USER_NAME);
        return correlationRule;
    }
}