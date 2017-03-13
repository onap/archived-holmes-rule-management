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


import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.openo.holmes.rulemgt.bean.request.CorrelationDeployRule4Engine;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.rule.PowerMockRule;

@PrepareForTest({HttpClients.class, CloseableHttpClient.class})
public class EngineServiceTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();
    @Rule
    public PowerMockRule powerMockRule = new PowerMockRule();
    private EngineService engineService;
    private HttpResponse httpResponseMock;
    private CloseableHttpClient closeableHttpClient;
    private CorrelationDeployRule4Engine correlationDeployRule4Engine;
    private CloseableHttpResponse closeableHttpResponseMock;

    @Before
    public void setUp() {
        engineService = new EngineService();
        closeableHttpClient = PowerMock.createMock(CloseableHttpClient.class);
        httpResponseMock = PowerMock.createMock(HttpResponse.class);
        closeableHttpResponseMock = PowerMock.createMock(CloseableHttpResponse.class);
        correlationDeployRule4Engine = new CorrelationDeployRule4Engine();
        correlationDeployRule4Engine.setContent("{\"package\":\"test\"}");
        correlationDeployRule4Engine.setEngineId("engine_id");
    }
}