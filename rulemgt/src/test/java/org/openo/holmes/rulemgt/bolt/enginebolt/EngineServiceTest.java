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


import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openo.holmes.rulemgt.RuleAppConfig;
import org.powermock.api.easymock.PowerMock;
import org.powermock.modules.junit4.rule.PowerMockRule;
import org.powermock.reflect.Whitebox;

public class EngineServiceTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();
    @Rule
    public PowerMockRule powerMockRule = new PowerMockRule();
    private EngineService engineService;
    private HttpEntity httpEntityMock;
    private HttpResponse httpResponseMock;
    private HttpClient httpClient;
    private RuleAppConfig ruleAppConfig = new RuleAppConfig();

    @Before
    public void setUp() {
        engineService = new EngineService();
        httpEntityMock = PowerMock.createMock(HttpEntity.class);
        httpResponseMock = PowerMock.createMock(HttpResponse.class);
        httpClient = PowerMock.createMock(HttpClient.class);
        Whitebox.setInternalState(engineService, "ruleAppConfig", ruleAppConfig);
    }

    @Test
    public void getResponseContent_http_entity_is_null() throws Exception {
        EasyMock.expect(httpResponseMock.getEntity()).andReturn(null);
        PowerMock.replayAll();

        engineService.getResponseContent(httpResponseMock);

        PowerMock.verifyAll();
    }
}