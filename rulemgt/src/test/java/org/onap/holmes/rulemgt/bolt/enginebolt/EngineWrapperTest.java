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

package org.onap.holmes.rulemgt.bolt.enginebolt;


import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.onap.holmes.common.exception.CorrelationException;
import org.onap.holmes.common.utils.HttpsUtils;
import org.onap.holmes.rulemgt.bean.request.CorrelationCheckRule4Engine;
import org.onap.holmes.rulemgt.bean.request.CorrelationDeployRule4Engine;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@PrepareForTest({EngineWrapper.class, EngineService.class, HttpsUtils.class, HttpResponse.class,
        StatusLine.class})
@RunWith(PowerMockRunner.class)
public class EngineWrapperTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();
    private EngineWrapper engineWrapper = new EngineWrapper();
    private EngineService engineServiceMock;
    private HttpResponse httpResponse;
    private StatusLine statusLineMock;

    @Before
    public void setUp() throws Exception {
        engineServiceMock = PowerMock.createMock(EngineService.class);
        httpResponse = PowerMock.createMock(HttpResponse.class);
        statusLineMock = PowerMock.createMock(StatusLine.class);
        Whitebox.setInternalState(engineWrapper, "engineService", engineServiceMock);
    }

    @Test
    public void deployEngine_invoke_rule_deploy_exception() throws Exception {
        thrown.expect(CorrelationException.class);
        thrown.expectMessage("Failed to call the rule deployment RESTful API.");

        EasyMock.expect(
                engineServiceMock.deploy(EasyMock.anyObject(CorrelationDeployRule4Engine.class),
                        EasyMock.anyObject(String.class)))
                .andThrow(
                        new RuntimeException(""));
        PowerMock.replayAll();

        engineWrapper.deployEngine(new CorrelationDeployRule4Engine(),"127.0.0.1");

        PowerMock.verifyAll();
    }

    @Test
    public void deployEngine_http_status_not_ok() throws Exception {
        thrown.expect(CorrelationException.class);
        thrown.expectMessage("Failed to deploy the rule!");

        EasyMock.expect(
                engineServiceMock.deploy(EasyMock.anyObject(CorrelationDeployRule4Engine.class),
                        EasyMock.anyObject(String.class)))
                .andReturn(httpResponse);
        EasyMock.expect(httpResponse.getStatusLine()).andReturn(statusLineMock);
        EasyMock.expect(statusLineMock.getStatusCode()).andReturn(400);
        PowerMock.replayAll();

        engineWrapper.deployEngine(new CorrelationDeployRule4Engine(),"127.0.0.1");

        PowerMock.verifyAll();
    }

    @Test
    public void deployEngine_parse_content_exception() throws Exception {
        PowerMock.resetAll();
        String content = "";
        PowerMock.mockStatic(HttpsUtils.class);
        thrown.expect(CorrelationException.class);
        thrown.expectMessage(
                "Failed to parse the value returned by the engine management service.");
        EasyMock.expect(
                engineServiceMock.deploy(EasyMock.anyObject(CorrelationDeployRule4Engine.class),
                        EasyMock.anyObject(String.class)))
                .andReturn(httpResponse);
        EasyMock.expect(httpResponse.getStatusLine()).andReturn(statusLineMock);
        EasyMock.expect(statusLineMock.getStatusCode()).andReturn(200);
        EasyMock.expect(HttpsUtils.extractResponseEntity(httpResponse)).andReturn(content);
        PowerMock.replayAll();

        engineWrapper.deployEngine(new CorrelationDeployRule4Engine(),"127.0.0.1");

        PowerMock.verifyAll();
    }

    @Test
    public void deployEngine_success() throws Exception {
        PowerMock.resetAll();
        String content = "{\"packageName\":\"test\"}";
        PowerMock.mockStatic(HttpsUtils.class);
        EasyMock.expect(
                engineServiceMock.deploy(EasyMock.anyObject(CorrelationDeployRule4Engine.class),
                        EasyMock.anyObject(String.class)))
                .andReturn(httpResponse);
        EasyMock.expect(httpResponse.getStatusLine()).andReturn(statusLineMock);
        EasyMock.expect(statusLineMock.getStatusCode()).andReturn(200);
        EasyMock.expect(HttpsUtils.extractResponseEntity(httpResponse)).andReturn(content);
        PowerMock.replayAll();

        String result = engineWrapper.deployEngine(new CorrelationDeployRule4Engine(),"127.0.0.1");

        assertThat(result, equalTo("test"));

    }

    @Test
    public void deleteRuleFromEngine_invoke_rule_delete_exception() throws Exception {
        thrown.expect(CorrelationException.class);
        thrown.expectMessage("Failed to call the rule deleting RESTful API.");

        EasyMock.expect(engineServiceMock.delete(EasyMock.anyObject(String.class),
                EasyMock.anyObject(String.class))).andThrow(
                new RuntimeException(""));
        PowerMock.replayAll();

        engineWrapper.deleteRuleFromEngine("","127.0.0.1");

        PowerMock.verifyAll();
    }

    @Test
    public void deleteRuleFromEngine_http_status_not_ok() throws Exception {
        thrown.expect(CorrelationException.class);
        thrown.expectMessage("Failed to delete the rule!");

        EasyMock.expect(engineServiceMock.delete(EasyMock.anyObject(String.class),
                EasyMock.anyObject(String.class)))
                .andReturn(httpResponse);
        EasyMock.expect(httpResponse.getStatusLine()).andReturn(statusLineMock);
        EasyMock.expect(statusLineMock.getStatusCode()).andReturn(400);

        PowerMock.replayAll();

        engineWrapper.deleteRuleFromEngine("","127.0.0.1");

        PowerMock.verifyAll();
    }

    @Test
    public void deleteRuleFromEngine_success() throws Exception {
        EasyMock.expect(engineServiceMock.delete(EasyMock.anyObject(String.class),
                EasyMock.anyObject(String.class)))
                .andReturn(httpResponse);
        EasyMock.expect(httpResponse.getStatusLine()).andReturn(statusLineMock);
        EasyMock.expect(statusLineMock.getStatusCode()).andReturn(200);

        PowerMock.replayAll();

        boolean result = engineWrapper.deleteRuleFromEngine("","127.0.0.1");

        assertThat(result, equalTo(true));
    }

    @Test
    public void checkRuleFromEngine_rule_delete_exception() throws Exception {
        thrown.expect(CorrelationException.class);
        thrown.expectMessage("Failed to call the rule verification RESTful API.");

        EasyMock.expect(
                engineServiceMock.check(EasyMock.anyObject(CorrelationCheckRule4Engine.class),
                        EasyMock.anyObject(String.class)))
                .andThrow(
                        new RuntimeException(""));
        PowerMock.replayAll();

        engineWrapper.checkRuleFromEngine(new CorrelationCheckRule4Engine(),"127.0.0.1");

        PowerMock.verifyAll();
    }

    @Test
    public void checkRuleFromEngine_success() throws Exception {
        EasyMock.expect(
                engineServiceMock.check(EasyMock.anyObject(CorrelationCheckRule4Engine.class),
                        EasyMock.anyObject(String.class)))
                .andReturn(httpResponse);
        EasyMock.expect(httpResponse.getStatusLine()).andReturn(statusLineMock);
        EasyMock.expect(statusLineMock.getStatusCode()).andReturn(200);

        PowerMock.replayAll();

        boolean result = engineWrapper.checkRuleFromEngine(new CorrelationCheckRule4Engine(),"127.0.0.1");

        assertThat(result, equalTo(true));
    }
}