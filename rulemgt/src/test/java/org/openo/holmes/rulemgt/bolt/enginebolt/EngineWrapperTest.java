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


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import javax.ws.rs.core.Response;
import org.apache.http.StatusLine;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openo.holmes.common.exception.CorrelationException;
import org.openo.holmes.common.utils.I18nProxy;
import org.openo.holmes.rulemgt.bean.request.CorrelationCheckRule4Engine;
import org.openo.holmes.rulemgt.bean.request.CorrelationDeployRule4Engine;
import org.powermock.api.easymock.PowerMock;
import org.powermock.reflect.Whitebox;

public class EngineWrapperTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();
    private EngineWrapper engineWrapper = new EngineWrapper();
    private EngineService engineServiceMock;
    private Response response;
    private StatusLine statusLineMock;

    @Before
    public void setUp() throws Exception {
        engineServiceMock = PowerMock.createMock(EngineService.class);
        response = PowerMock.createMock(Response.class);
        statusLineMock = PowerMock.createMock(StatusLine.class);
        Whitebox.setInternalState(engineWrapper, "engineService", engineServiceMock);
    }

    @Test
    public void deployEngine_invoke_rule_deploy_exception() throws Exception {
        thrown.expect(CorrelationException.class);
        thrown.expectMessage(I18nProxy.RULE_MANAGEMENT_CALL_DEPLOY_RULE_REST_FAILED);

        EasyMock.expect(engineServiceMock.deploy(EasyMock.anyObject(CorrelationDeployRule4Engine.class))).andThrow(
                new RuntimeException(""));
        PowerMock.replayAll();

        engineWrapper.deployEngine(new CorrelationDeployRule4Engine());

        PowerMock.verifyAll();
    }

    @Test
    public void deployEngine_http_status_not_ok() throws Exception {
        thrown.expect(CorrelationException.class);
        thrown.expectMessage(I18nProxy.ENGINE_DEPLOY_RULE_FAILED);

        EasyMock.expect(engineServiceMock.deploy(EasyMock.anyObject(CorrelationDeployRule4Engine.class)))
                .andReturn(response);
        EasyMock.expect(response.getStatus()).andReturn(400);
        PowerMock.replayAll();

        engineWrapper.deployEngine(new CorrelationDeployRule4Engine());

        PowerMock.verifyAll();
    }

    @Test
    public void deployEngine_parse_content_exception() throws Exception {
        String content = "";

        thrown.expect(CorrelationException.class);
        thrown.expectMessage(I18nProxy.RULE_MANAGEMENT_PARSE_DEPLOY_RESULT_ERROR);
        EasyMock.expect(engineServiceMock.deploy(EasyMock.anyObject(CorrelationDeployRule4Engine.class)))
                .andReturn(response);
        EasyMock.expect(response.getStatus()).andReturn(200);
        EasyMock.expect(response.readEntity(String.class)).andReturn(content);
        PowerMock.replayAll();

        engineWrapper.deployEngine(new CorrelationDeployRule4Engine());

        PowerMock.verifyAll();
    }

    @Test
    public void deployEngine_success() throws Exception {
        String content = "{\"package\":\"test\"}";
        EasyMock.expect(engineServiceMock.deploy(EasyMock.anyObject(CorrelationDeployRule4Engine.class)))
                .andReturn(response);
        EasyMock.expect(response.getStatus()).andReturn(200);
        EasyMock.expect(response.readEntity(String.class)).andReturn(content);
        PowerMock.replayAll();

        String result = engineWrapper.deployEngine(new CorrelationDeployRule4Engine());

        assertThat(result, equalTo("test"));

    }

    @Test
    public void deleteRuleFromEngine_invoke_rule_delete_exception() throws Exception {
        thrown.expect(CorrelationException.class);
        thrown.expectMessage(I18nProxy.RULE_MANAGEMENT_CALL_DELETE_RULE_REST_FAILED);

        EasyMock.expect(engineServiceMock.delete(EasyMock.anyObject(String.class))).andThrow(
                new RuntimeException(""));
        PowerMock.replayAll();

        engineWrapper.deleteRuleFromEngine("");

        PowerMock.verifyAll();
    }

    @Test
    public void deleteRuleFromEngine_http_status_not_ok() throws Exception {
        thrown.expect(CorrelationException.class);
        thrown.expectMessage(I18nProxy.ENGINE_DELETE_RULE_FAILED);

        EasyMock.expect(engineServiceMock.delete(EasyMock.anyObject(String.class)))
                .andReturn(response);
        EasyMock.expect(response.getStatus()).andReturn(400);

        PowerMock.replayAll();

        engineWrapper.deleteRuleFromEngine("");

        PowerMock.verifyAll();
    }

    @Test
    public void deleteRuleFromEngine_success() throws Exception {
        EasyMock.expect(engineServiceMock.delete(EasyMock.anyObject(String.class)))
                .andReturn(response);
        EasyMock.expect(response.getStatus()).andReturn(200);

        PowerMock.replayAll();

        boolean result = engineWrapper.deleteRuleFromEngine("");

        assertThat(result, equalTo(true));
    }

    @Test
    public void checkRuleFromEngine_rule_delete_exception() throws Exception {
        thrown.expect(CorrelationException.class);
        thrown.expectMessage(I18nProxy.RULE_MANAGEMENT_CALL_CHECK_RULE_REST_FAILED);

        EasyMock.expect(engineServiceMock.check(EasyMock.anyObject(CorrelationCheckRule4Engine.class))).andThrow(
                new RuntimeException(""));
        PowerMock.replayAll();

        engineWrapper.checkRuleFromEngine(new CorrelationCheckRule4Engine());

        PowerMock.verifyAll();
    }

    @Test
    public void checkRuleFromEngine_success() throws Exception {
        EasyMock.expect(engineServiceMock.check(EasyMock.anyObject(CorrelationCheckRule4Engine.class)))
                .andReturn(response);
        EasyMock.expect(response.getStatus()).andReturn(200);

        PowerMock.replayAll();

        boolean result = engineWrapper.checkRuleFromEngine(new CorrelationCheckRule4Engine());

        assertThat(result, equalTo(true));
    }
}