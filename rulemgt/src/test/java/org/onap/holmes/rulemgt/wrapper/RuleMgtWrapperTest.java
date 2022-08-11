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


import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.onap.holmes.common.api.entity.CorrelationRule;
import org.onap.holmes.common.exception.CorrelationException;
import org.onap.holmes.rulemgt.bean.request.*;
import org.onap.holmes.rulemgt.bean.response.RuleAddAndUpdateResponse;
import org.onap.holmes.rulemgt.bean.response.RuleQueryListResponse;
import org.onap.holmes.rulemgt.bolt.enginebolt.EngineWrapper;
import org.onap.holmes.rulemgt.db.CorrelationRuleQueryService;
import org.onap.holmes.rulemgt.db.CorrelationRuleService;
import org.onap.holmes.rulemgt.tools.EngineTools;
import org.powermock.api.easymock.PowerMock;
import org.powermock.reflect.Whitebox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class RuleMgtWrapperTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private RuleMgtWrapper ruleMgtWrapper;

    private EngineWrapper engineWrapperMock;

    private CorrelationRuleQueryService correlationRuleQueryDaoMock;

    private CorrelationRuleService correlationRuleServiceMock;

    private EngineTools engineToolsMock;

    private static final String USER_NAME = "admin";

    @Before
    public void setUp() {

        ruleMgtWrapper = new RuleMgtWrapper();

        engineWrapperMock = PowerMock.createMock(EngineWrapper.class);
        correlationRuleQueryDaoMock = PowerMock.createMock(CorrelationRuleQueryService.class);
        correlationRuleServiceMock = PowerMock.createMock(CorrelationRuleService.class);
        engineToolsMock = PowerMock.createMock(EngineTools.class);

        Whitebox.setInternalState(ruleMgtWrapper, "correlationRuleService", correlationRuleServiceMock);
        Whitebox.setInternalState(ruleMgtWrapper, "correlationRuleQueryDao", correlationRuleQueryDaoMock);
        Whitebox.setInternalState(ruleMgtWrapper, "engineWarpper", engineWrapperMock);
        Whitebox.setInternalState(ruleMgtWrapper,"engineTools", engineToolsMock);

        PowerMock.resetAll();
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

        EasyMock.expect(correlationRuleServiceMock.queryRuleByRuleName(ruleName)).andReturn(correlationRule);
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

        EasyMock.expect(correlationRuleServiceMock.queryRuleByRuleName(ruleName)).andReturn(null);
        EasyMock.expect(engineToolsMock.getEngineWithLeastRules()).andReturn("127.0.0.1");
        EasyMock.expect(engineWrapperMock.checkRuleFromEngine(EasyMock.anyObject(CorrelationCheckRule4Engine.class)
                , EasyMock.anyObject(String.class)))
                .andReturn(true);
        EasyMock.expect(engineWrapperMock.deployEngine(EasyMock.anyObject(CorrelationDeployRule4Engine.class)
                , EasyMock.anyObject(String.class)))
                .andReturn("package-001");
        EasyMock.expect(correlationRuleServiceMock.saveRule(EasyMock.anyObject(CorrelationRule.class)))
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
        oldCorrelationRule.setClosedControlLoopName("cl-name");
        oldCorrelationRule.setEngineInstance("127.0.0.1");
        RuleUpdateRequest ruleUpdateRequest = createRuleUpdateRequest("rule_1", "cl-name", "des2", "contetnt2", 1);

        EasyMock.expect(correlationRuleServiceMock.queryRuleByRid("rule_1")).andReturn(oldCorrelationRule);
        EasyMock.expect(engineToolsMock.getInstanceList()).andReturn(Arrays.asList("127.0.0.1", "127.0.0.2")).times(2);
        EasyMock.expect(engineWrapperMock.deleteRuleFromEngine("testName", "127.0.0.1")).andReturn(true);
        correlationRuleServiceMock.updateRule(EasyMock.anyObject(CorrelationRule.class));
        EasyMock.expectLastCall();
        EasyMock.expect(engineWrapperMock.checkRuleFromEngine(EasyMock.anyObject(CorrelationCheckRule4Engine.class)
                , EasyMock.anyObject(String.class)))
                .andReturn(true);
        EasyMock.expect(engineWrapperMock.deployEngine(EasyMock.anyObject(CorrelationDeployRule4Engine.class)
                , EasyMock.anyObject(String.class)))
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
        oldCorrelationRule.setClosedControlLoopName("cl-name");
        oldCorrelationRule.setEngineInstance("127.0.0.1");
        RuleUpdateRequest ruleUpdateRequest = createRuleUpdateRequest("rule_1", "cl-name", "des1", "content", 1);

        EasyMock.expect(correlationRuleServiceMock.queryRuleByRid("rule_1")).andReturn(oldCorrelationRule);
        EasyMock.expect(engineToolsMock.getInstanceList()).andReturn(Arrays.asList("127.0.0.1", "127.0.0.2"));

        PowerMock.replayAll();

        ruleMgtWrapper.updateCorrelationRule(USER_NAME, ruleUpdateRequest);

        PowerMock.verifyAll();

        assertThat(oldCorrelationRule.getRid(), equalTo(ruleUpdateRequest.getRuleId()));
    }

    @Test
    public void updateCorrelationRule_rule_not_exist() throws Exception {
        thrown.expect(CorrelationException.class);
        thrown.expectMessage("You're trying to update a rule which does not exist in the system.");

        EasyMock.expect(correlationRuleServiceMock.queryRuleByRid(EasyMock.anyObject(String.class))).andReturn(null);

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

        EasyMock.expect(correlationRuleServiceMock.queryRuleByRid(ruleDeleteRequest.getRuleId()))
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
        EasyMock.expect(correlationRuleServiceMock.queryRuleByRid(ruleDeleteRequest.getRuleId()))
                .andReturn(correlationRule);
        EasyMock.expect(engineWrapperMock.deleteRuleFromEngine(EasyMock.anyObject(String.class)
                , EasyMock.anyObject(String.class))).andReturn(true);
        correlationRuleServiceMock.deleteRule(EasyMock.anyObject(CorrelationRule.class));
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

        PowerMock.replay(correlationRuleQueryDaoMock, CorrelationRuleQueryService.class);

        ruleMgtWrapper.getCorrelationRuleByCondition(new RuleQueryCondition());

        PowerMock.verify(correlationRuleQueryDaoMock, CorrelationRuleQueryService.class);
    }

    @Test
    public void getCorrelationRuleByCondition_db_exception() throws Exception {
        thrown.expect(CorrelationException.class);
        thrown.expectMessage("Failed to query the rule.");

        EasyMock.expect(correlationRuleQueryDaoMock.getCorrelationRulesByCondition(EasyMock.anyObject(
                RuleQueryCondition.class)))
                .andThrow(new CorrelationException("Failed to query the rule."));

        PowerMock.replay(correlationRuleQueryDaoMock, CorrelationRuleQueryService.class);

        ruleMgtWrapper.getCorrelationRuleByCondition(new RuleQueryCondition());

        PowerMock.verify(correlationRuleQueryDaoMock, CorrelationRuleQueryService.class);
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

        PowerMock.replay(correlationRuleQueryDaoMock, CorrelationRuleQueryService.class);

        RuleQueryListResponse response = ruleMgtWrapper.getCorrelationRuleByCondition(new RuleQueryCondition());

        PowerMock.verify(correlationRuleQueryDaoMock, CorrelationRuleQueryService.class);

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

    private RuleUpdateRequest createRuleUpdateRequest(String ruleId, String clName, String description,
            String content, int enabled) {
        RuleUpdateRequest ruleUpdateRequest = new RuleUpdateRequest();
        ruleUpdateRequest.setRuleId(ruleId);
        ruleUpdateRequest.setDescription(description);
        ruleUpdateRequest.setContent(content);
        ruleUpdateRequest.setEnabled(enabled);
        ruleUpdateRequest.setLoopControlName(clName);
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