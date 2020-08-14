/**
 * Copyright 2017 ZTE Corporation.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.onap.holmes.rulemgt.bolt.enginebolt;


import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.onap.holmes.common.utils.HttpsUtils;
import org.onap.holmes.rulemgt.bean.request.CorrelationDeployRule4Engine;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.rule.PowerMockRule;
import org.powermock.reflect.Whitebox;

import java.util.HashMap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@PrepareForTest({HttpClients.class, CloseableHttpClient.class, HttpsUtils.class})
@PowerMockIgnore("javax.net.ssl.*")
@RunWith(PowerMockRunner.class)
public class EngineServiceTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();
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

    @Test
    public void testEngineService_createHeaders_ok() throws Exception {
        PowerMock.resetAll();
        HashMap<String, String> headers = Whitebox.invokeMethod(engineService, "createHeaders");
        assertThat(headers.get("Content-Type"), equalTo("application/json"));
        assertThat(headers.get("Accept"), equalTo("application/json"));
    }

    @Test
    public void testEngineService_closeHttpClient_ok() throws Exception {
        PowerMock.resetAll();
        CloseableHttpClient closeableHttpClient = HttpsUtils
                .getConditionalHttpsClient(HttpsUtils.DEFUALT_TIMEOUT);
        Whitebox.invokeMethod(engineService, "closeHttpClient", closeableHttpClient);
    }

}