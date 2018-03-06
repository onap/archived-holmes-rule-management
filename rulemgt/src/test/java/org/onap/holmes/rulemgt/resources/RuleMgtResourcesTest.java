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

package org.onap.holmes.rulemgt.resources;

import com.google.gson.JsonSyntaxException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import org.easymock.EasyMock;
import org.eclipse.jetty.server.Request;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.onap.holmes.rulemgt.bean.request.RuleCreateRequest;
import org.onap.holmes.rulemgt.bean.request.RuleDeleteRequest;
import org.onap.holmes.rulemgt.wrapper.RuleMgtWrapper;
import org.onap.holmes.common.exception.CorrelationException;
import org.onap.holmes.rulemgt.bean.request.RuleQueryCondition;
import org.onap.holmes.rulemgt.bean.request.RuleUpdateRequest;
import org.onap.holmes.rulemgt.bean.response.RuleAddAndUpdateResponse;
import org.onap.holmes.rulemgt.bean.response.RuleQueryListResponse;
import org.powermock.api.easymock.PowerMock;
import org.powermock.reflect.Whitebox;

public class RuleMgtResourcesTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private HttpServletRequest request = PowerMock.createMock(HttpServletRequest.class);

    private RuleMgtWrapper ruleMgtWrapper = PowerMock.createMock(RuleMgtWrapper.class);

    private RuleMgtResources ruleMgtResources = new RuleMgtResources();

    private Request requestMock = PowerMock.createMock(Request.class);

    @Before
    public void setUp() throws Exception {
        Whitebox.setInternalState(ruleMgtResources, "ruleMgtWrapper", ruleMgtWrapper);
        PowerMock.resetAll();
    }

    @Test
    public void addCorrelationRule_correlation_exception() throws Exception {
        PowerMock.resetAll();
        thrown.expect(WebApplicationException.class);

        final RuleCreateRequest ruleCreateRequest = new RuleCreateRequest();
        EasyMock.expect(ruleMgtWrapper.addCorrelationRule("admin", ruleCreateRequest))
                .andThrow(new CorrelationException(EasyMock.anyObject(String.class)));
        EasyMock.expect(request.getHeader("language-option")).andReturn("en_US");
        EasyMock.expect(request.getHeader("username")).andReturn("admin");
        PowerMock.replayAll();
        ruleMgtResources.addCorrelationRule(request, ruleCreateRequest);
        PowerMock.verifyAll();
    }

    @Test
    public void addCorrelationRule_normal() throws Exception {
        StringBuilder stringBuilder = new StringBuilder("http://localhost");
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
    public void updateCorrelationRule_correlation_exception() throws Exception {
        thrown.expect(WebApplicationException.class);

        final RuleUpdateRequest ruleUpdateRequest = new RuleUpdateRequest();
        EasyMock.expect(ruleMgtWrapper.updateCorrelationRule("admin", ruleUpdateRequest))
                .andThrow(new CorrelationException(EasyMock.anyObject(String.class)));
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
    public void deleteCorrelationRule_correlation_exception() throws Exception {
        thrown.expect(WebApplicationException.class);

        final String ruleId = "mockedRule";
        ruleMgtWrapper.deleteCorrelationRule(EasyMock.anyObject(RuleDeleteRequest.class));
        EasyMock.expectLastCall().andThrow(new CorrelationException(EasyMock.anyObject(String.class)));
        EasyMock.expect(request.getHeader("language-option")).andReturn("en_US");
        PowerMock.replayAll();
        ruleMgtResources.deleteCorrelationRule(request, ruleId);
        PowerMock.verifyAll();
    }

    @Test
    public void deleteCorrelationRule_normal() throws Exception {
        final String ruleId = "mockedRule";
        ruleMgtWrapper.deleteCorrelationRule(EasyMock.anyObject(RuleDeleteRequest.class));
        EasyMock.expectLastCall();
        EasyMock.expect(request.getHeader("language-option")).andReturn("en_US");
        PowerMock.replayAll();
        ruleMgtResources.deleteCorrelationRule(request, ruleId);
        PowerMock.verifyAll();
    }

    @Test
    public void getCorrelationRules_data_format_exception() throws Exception {
        thrown.expect(WebApplicationException.class);

        final String requestStr = "{\"ruleid\":\"rule_001\",\"rulename\":\"Rule-001\","
                + "\"enabled\":0,\"creator\":\"admin\"}";
        EasyMock.expect(ruleMgtWrapper.getCorrelationRuleByCondition(EasyMock.anyObject(RuleQueryCondition.class)))
                .andThrow(new CorrelationException(EasyMock.anyObject(String.class)));
        EasyMock.expect(request.getHeader("language-option")).andReturn("en_US").times(2);
        PowerMock.replayAll();
        ruleMgtResources.getCorrelationRules(request, requestStr);
        PowerMock.verifyAll();
    }

    @Test
    public void getCorrelationRules_param_translate_exception() {
        thrown.expect(JsonSyntaxException.class);

        String queryRequest = "this is error param";
        EasyMock.expect(request.getHeader("language-option")).andReturn("en_US").times(2);

        PowerMock.replayAll();
        ruleMgtResources.getCorrelationRules(request, queryRequest);
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