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

package org.openo.holmes.rulemgt.resources;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openo.holmes.common.exception.CallException;
import org.openo.holmes.common.exception.DataFormatException;
import org.openo.holmes.common.exception.DbException;
import org.openo.holmes.rulemgt.bean.request.RuleCreateRequest;
import org.openo.holmes.rulemgt.bean.request.RuleDeleteRequest;
import org.openo.holmes.rulemgt.bean.request.RuleQueryCondition;
import org.openo.holmes.rulemgt.bean.request.RuleUpdateRequest;
import org.openo.holmes.rulemgt.bean.response.RuleAddAndUpdateResponse;
import org.openo.holmes.rulemgt.bean.response.RuleQueryListResponse;
import org.openo.holmes.rulemgt.wrapper.RuleMgtWrapper;
import org.powermock.api.easymock.PowerMock;
import org.powermock.reflect.Whitebox;

public class RuleMgtResourcesTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private HttpServletRequest request = PowerMock.createMock(HttpServletRequest.class);

    private RuleMgtWrapper ruleMgtWrapper = PowerMock.createMock(RuleMgtWrapper.class);

    private RuleMgtResources ruleMgtResources = new RuleMgtResources();

    @Before
    public void setUp() throws Exception {
        Whitebox.setInternalState(ruleMgtResources, "ruleMgtWrapper", ruleMgtWrapper);
        PowerMock.resetAll();
    }

    @Test
    public void addCorrelationRule_call_exception() throws Exception {
        thrown.expect(WebApplicationException.class);

        final RuleCreateRequest ruleCreateRequest = new RuleCreateRequest();
        EasyMock.expect(ruleMgtWrapper.addCorrelationRule("admin", ruleCreateRequest))
                .andThrow(new CallException(""));
        EasyMock.expect(request.getHeader("language-option")).andReturn("en_US");
        EasyMock.expect(request.getHeader("username")).andReturn("admin");
        PowerMock.replayAll();
        ruleMgtResources.addCorrelationRule(request, ruleCreateRequest);
        PowerMock.verifyAll();
    }

    @Test
    public void addCorrelationRule_db_exception() throws Exception {
        thrown.expect(WebApplicationException.class);

        final RuleCreateRequest ruleCreateRequest = new RuleCreateRequest();
        EasyMock.expect(ruleMgtWrapper.addCorrelationRule("admin", ruleCreateRequest)).andThrow(new DbException(""));
        EasyMock.expect(request.getHeader("language-option")).andReturn("en_US");
        EasyMock.expect(request.getHeader("username")).andReturn("admin");
        PowerMock.replayAll();
        ruleMgtResources.addCorrelationRule(request, ruleCreateRequest);
        PowerMock.verifyAll();
    }

    @Test
    public void addCorrelationRule_data_format_exception() throws Exception {
        thrown.expect(WebApplicationException.class);

        final RuleCreateRequest ruleCreateRequest = new RuleCreateRequest();
        EasyMock.expect(ruleMgtWrapper.addCorrelationRule("admin", ruleCreateRequest))
                .andThrow(new DataFormatException(""));
        EasyMock.expect(request.getHeader("language-option")).andReturn("en_US");
        EasyMock.expect(request.getHeader("username")).andReturn("admin");
        PowerMock.replayAll();
        ruleMgtResources.addCorrelationRule(request, ruleCreateRequest);
        PowerMock.verifyAll();
    }

    @Test
    public void addCorrelationRule_exception() throws Exception {
        thrown.expect(WebApplicationException.class);

        final RuleCreateRequest ruleCreateRequest = new RuleCreateRequest();
        EasyMock.expect(ruleMgtWrapper.addCorrelationRule("admin", ruleCreateRequest))
                .andThrow(new RuntimeException(""));
        EasyMock.expect(request.getHeader("language-option")).andReturn("en_US");
        EasyMock.expect(request.getHeader("username")).andReturn("admin");
        PowerMock.replayAll();
        ruleMgtResources.addCorrelationRule(request, ruleCreateRequest);
        PowerMock.verifyAll();
    }

    @Test
    public void addCorrelationRule_normal() throws Exception {
        final RuleCreateRequest ruleCreateRequest = new RuleCreateRequest();
        EasyMock.expect(ruleMgtWrapper.addCorrelationRule("admin",
                ruleCreateRequest)).andReturn(new RuleAddAndUpdateResponse());
        EasyMock.expect(request.getHeader("language-option")).andReturn("en_US");
        EasyMock.expect(request.getHeader("username")).andReturn("admin");
        PowerMock.replayAll();
        ruleMgtResources.addCorrelationRule(request, ruleCreateRequest);
        PowerMock.verifyAll();
    }

    @Test
    public void updateCorrelationRule_call_exception() throws Exception {
        thrown.expect(WebApplicationException.class);

        final RuleUpdateRequest ruleUpdateRequest = new RuleUpdateRequest();
        EasyMock.expect(ruleMgtWrapper.updateCorrelationRule("admin", ruleUpdateRequest))
                .andThrow(new CallException(""));
        EasyMock.expect(request.getHeader("language-option")).andReturn("en_US");
        EasyMock.expect(request.getHeader("username")).andReturn("admin");
        PowerMock.replayAll();
        ruleMgtResources.updateCorrelationRule(request, ruleUpdateRequest);
        PowerMock.verifyAll();
    }

    @Test
    public void updateCorrelationRule_data_format_exception() throws Exception {
        thrown.expect(WebApplicationException.class);

        final RuleUpdateRequest ruleUpdateRequest = new RuleUpdateRequest();
        EasyMock.expect(ruleMgtWrapper.updateCorrelationRule("admin", ruleUpdateRequest))
                .andThrow(new DataFormatException(""));
        EasyMock.expect(request.getHeader("language-option")).andReturn("en_US");
        EasyMock.expect(request.getHeader("username")).andReturn("admin");
        PowerMock.replayAll();
        ruleMgtResources.updateCorrelationRule(request, ruleUpdateRequest);
        PowerMock.verifyAll();
    }

    @Test
    public void updateCorrelationRule_db_exception() throws Exception {
        thrown.expect(WebApplicationException.class);

        final RuleUpdateRequest ruleUpdateRequest = new RuleUpdateRequest();
        EasyMock.expect(ruleMgtWrapper.updateCorrelationRule("admin", ruleUpdateRequest))
                .andThrow(new DbException(""));
        EasyMock.expect(request.getHeader("language-option")).andReturn("en_US");
        EasyMock.expect(request.getHeader("username")).andReturn("admin");
        PowerMock.replayAll();
        ruleMgtResources.updateCorrelationRule(request, ruleUpdateRequest);
        PowerMock.verifyAll();
    }

    @Test
    public void updateCorrelationRule_exception() throws Exception {
        thrown.expect(WebApplicationException.class);

        final RuleUpdateRequest ruleUpdateRequest = new RuleUpdateRequest();
        EasyMock.expect(ruleMgtWrapper.updateCorrelationRule("admin", ruleUpdateRequest))
                .andThrow(new RuntimeException(""));
        EasyMock.expect(request.getHeader("language-option")).andReturn("en_US");
        EasyMock.expect(request.getHeader("username")).andReturn("admin");
        PowerMock.replayAll();
        ruleMgtResources.updateCorrelationRule(request, ruleUpdateRequest);
        PowerMock.verifyAll();
    }

    @Test
    public void updateCorrelationRule_normal() throws Exception {
        final RuleUpdateRequest ruleUpdateRequest = new RuleUpdateRequest();
        EasyMock.expect(ruleMgtWrapper.updateCorrelationRule("admin",
                ruleUpdateRequest)).andReturn(new RuleAddAndUpdateResponse());
        EasyMock.expect(request.getHeader("language-option")).andReturn("en_US");
        EasyMock.expect(request.getHeader("username")).andReturn("admin");
        PowerMock.replayAll();
        ruleMgtResources.updateCorrelationRule(request, ruleUpdateRequest);
        PowerMock.verifyAll();
    }

    @Test
    public void deleteCorrelationRule_call_exception() throws Exception {
        thrown.expect(WebApplicationException.class);

        final RuleDeleteRequest ruleDeleteRequest = new RuleDeleteRequest();
        ruleMgtWrapper.deleteCorrelationRule(ruleDeleteRequest);
        EasyMock.expectLastCall().andThrow(new CallException(""));
        EasyMock.expect(request.getHeader("language-option")).andReturn("en_US");
        PowerMock.replayAll();
        ruleMgtResources.deleteCorrelationRule(request, ruleDeleteRequest);
        PowerMock.verifyAll();
    }

    @Test
    public void deleteCorrelationRule_data_format_exception() throws Exception {
        thrown.expect(WebApplicationException.class);

        final RuleDeleteRequest ruleDeleteRequest = new RuleDeleteRequest();
        ruleMgtWrapper.deleteCorrelationRule(ruleDeleteRequest);
        EasyMock.expectLastCall().andThrow(new DataFormatException(""));
        EasyMock.expect(request.getHeader("language-option")).andReturn("en_US");
        PowerMock.replayAll();
        ruleMgtResources.deleteCorrelationRule(request, ruleDeleteRequest);
        PowerMock.verifyAll();
    }

    @Test
    public void deleteCorrelationRule_db_exception() throws Exception {
        thrown.expect(WebApplicationException.class);

        final RuleDeleteRequest ruleDeleteRequest = new RuleDeleteRequest();
        ruleMgtWrapper.deleteCorrelationRule(ruleDeleteRequest);
        EasyMock.expectLastCall().andThrow(new DbException(""));
        EasyMock.expect(request.getHeader("language-option")).andReturn("en_US");
        PowerMock.replayAll();
        ruleMgtResources.deleteCorrelationRule(request, ruleDeleteRequest);
        PowerMock.verifyAll();
    }

    @Test
    public void deleteCorrelationRule_exception() throws Exception {
        thrown.expect(WebApplicationException.class);

        final RuleDeleteRequest ruleDeleteRequest = new RuleDeleteRequest();
        ruleMgtWrapper.deleteCorrelationRule(ruleDeleteRequest);
        EasyMock.expectLastCall().andThrow(new RuntimeException(""));
        EasyMock.expect(request.getHeader("language-option")).andReturn("en_US");
        PowerMock.replayAll();
        ruleMgtResources.deleteCorrelationRule(request, ruleDeleteRequest);
        PowerMock.verifyAll();
    }

    @Test
    public void deleteCorrelationRule_normal() throws Exception {
        final RuleDeleteRequest ruleDeleteRequest = new RuleDeleteRequest();
        ruleMgtWrapper.deleteCorrelationRule(ruleDeleteRequest);
        EasyMock.expectLastCall();
        EasyMock.expect(request.getHeader("language-option")).andReturn("en_US");
        PowerMock.replayAll();
        ruleMgtResources.deleteCorrelationRule(request, ruleDeleteRequest);
        PowerMock.verifyAll();
    }

    @Test
    public void getCorrelationRules_data_format_exception() throws Exception {
        thrown.expect(WebApplicationException.class);

        final String requestStr = "{\"ruleid\":\"rule_001\",\"rulename\":\"Rule-001\","
                + "\"enabled\":0,\"creator\":\"admin\"}";
        EasyMock.expect(ruleMgtWrapper.getCorrelationRuleByCondition(EasyMock.anyObject(RuleQueryCondition.class)))
                .andThrow(new DataFormatException(""));
        EasyMock.expect(request.getHeader("language-option")).andReturn("en_US").times(2);
        PowerMock.replayAll();
        ruleMgtResources.getCorrelationRules(request, requestStr);
        PowerMock.verifyAll();
    }

    @Test
    public void getCorrelationRules_db_exception() throws Exception {
        thrown.expect(WebApplicationException.class);

        final String requestStr = "{\"ruleid\":\"rule_001\",\"rulename\":\"Rule-001\","
                + "\"enabled\":0,\"creator\":\"admin\"}";
        EasyMock.expect(ruleMgtWrapper.getCorrelationRuleByCondition(EasyMock.anyObject(RuleQueryCondition.class)))
                .andThrow(new DbException(""));
        EasyMock.expect(request.getHeader("language-option")).andReturn("en_US").times(2);
        PowerMock.replayAll();
        ruleMgtResources.getCorrelationRules(request, requestStr);
        PowerMock.verifyAll();
    }

    @Test
    public void getCorrelationRules_exception() throws Exception {
        thrown.expect(WebApplicationException.class);

        final String requestStr = "{\"ruleid\":\"rule_001\",\"rulename\":\"Rule-001\","
                + "\"enabled\":0,\"creator\":\"admin\"}";
        EasyMock.expect(ruleMgtWrapper.getCorrelationRuleByCondition(EasyMock.anyObject(RuleQueryCondition.class)))
                .andThrow(new RuntimeException(""));
        EasyMock.expect(request.getHeader("language-option")).andReturn("en_US").times(2);
        PowerMock.replayAll();
        ruleMgtResources.getCorrelationRules(request, requestStr);
        PowerMock.verifyAll();
    }


    @Test
    public void getCorrelationRules_normal_request_string_null() throws Exception {
        EasyMock.expect(ruleMgtWrapper.getCorrelationRuleByCondition(EasyMock.anyObject(RuleQueryCondition.class)))
                .andReturn(new RuleQueryListResponse());
        EasyMock.expect(request.getHeader("language-option")).andReturn("en_US").times(2);
        PowerMock.replayAll();
        ruleMgtResources.getCorrelationRules(request, null);
        PowerMock.verifyAll();
    }

    @Test
    public void getCorrelationRules_normal_request_string_enabled_missing() throws Exception {
        final String requestStr = "{\"ruleid\":\"rule_001\",\"rulename\":\"Rule-001\","
                + "\"creator\":\"admin\"}";
        EasyMock.expect(ruleMgtWrapper.getCorrelationRuleByCondition(EasyMock.anyObject(RuleQueryCondition.class)))
                .andReturn(new RuleQueryListResponse());
        EasyMock.expect(request.getHeader("language-option")).andReturn("en_US").times(2);
        PowerMock.replayAll();
        ruleMgtResources.getCorrelationRules(request, requestStr);
        PowerMock.verifyAll();
    }
}