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
package org.onap.holmes.rulemgt.dcae;

import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.onap.holmes.common.dcae.DcaeConfigurationQuery;
import org.onap.holmes.common.dcae.entity.DcaeConfigurations;
import org.onap.holmes.common.dcae.entity.Rule;
import org.onap.holmes.common.utils.HttpsUtils;
import org.onap.holmes.rulemgt.bean.response.RuleQueryListResponse;
import org.onap.holmes.rulemgt.bean.response.RuleResult4API;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.expect;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.powermock.api.easymock.PowerMock.*;

@PrepareForTest({HttpsUtils.class, DcaeConfigurationQuery.class})
@SuppressStaticInitializationFor("org.onap.holmes.common.utils.HttpsUtils")
@RunWith(PowerMockRunner.class)
public class DcaeConfigurationPollingTest {

    @org.junit.Rule
    public ExpectedException thrown = ExpectedException.none();


    @Test
    public void run() throws Exception {
        DcaeConfigurations dcaeConfigurations = new DcaeConfigurations();
        dcaeConfigurations.addDefaultRule(new Rule("test", "clName", "contents", 1));
        mockStatic(DcaeConfigurationQuery.class);
        expect(DcaeConfigurationQuery.getDcaeConfigurations(anyObject(String.class))).andReturn(dcaeConfigurations);
        DcaeConfigurationPolling dcaeConfigurationPolling = new DcaeConfigurationPolling("localhost");

        Whitebox.setInternalState(dcaeConfigurationPolling, "url", "http://127.0.0.1");

        RuleQueryListResponse ruleQueryListResponse = new RuleQueryListResponse();
        List<RuleResult4API> ruleResult4APIList = new ArrayList<RuleResult4API>(){
            {
                add(new RuleResult4API());
            }
        };
        ruleQueryListResponse.setCorrelationRules(ruleResult4APIList);
        ruleQueryListResponse.setTotalCount(ruleResult4APIList.size());

        CloseableHttpClient clientMock = createMock(CloseableHttpClient.class);
        HttpResponse httpResponseMock = createMock(HttpResponse.class);
        expect(HttpsUtils.getHttpClient(30000)).andReturn(clientMock);
        expect(HttpsUtils.get(anyObject(HttpGet.class), anyObject(HashMap.class), anyObject(CloseableHttpClient.class)))
                .andReturn(httpResponseMock);
        expect(HttpsUtils.extractResponseEntity(httpResponseMock)).andReturn(JSONObject.toJSONString(ruleQueryListResponse));
        clientMock.close();
        expectLastCall();

        expect(HttpsUtils.getHttpClient(30000)).andReturn(clientMock);
        expect(HttpsUtils.delete(anyObject(HttpDelete.class), anyObject(HashMap.class), anyObject(CloseableHttpClient.class)))
                .andReturn(httpResponseMock);
        clientMock.close();
        expectLastCall();

        expect(HttpsUtils.getHttpClient(30000)).andReturn(clientMock);
        expect(HttpsUtils.put(anyObject(HttpPut.class), anyObject(HashMap.class), anyObject(HashMap.class),
                anyObject(StringEntity.class), anyObject(CloseableHttpClient.class)))
                .andReturn(httpResponseMock);
        clientMock.close();
        expectLastCall();

        StatusLine sl = createMock(StatusLine.class);
        expect(httpResponseMock.getStatusLine()).andReturn(sl);
        expect(sl.getStatusCode()).andReturn(200);

        replayAll();

        dcaeConfigurationPolling.run();

        verifyAll();
    }

    @Test
    public void run_identical_contents() throws Exception {
        DcaeConfigurations dcaeConfigurations = new DcaeConfigurations();
        dcaeConfigurations.addDefaultRule(new Rule("test", "clName", "contents", 1));
        mockStatic(DcaeConfigurationQuery.class);
        expect(DcaeConfigurationQuery.getDcaeConfigurations(anyObject(String.class))).andReturn(dcaeConfigurations).times(2);
        DcaeConfigurationPolling dcaeConfigurationPolling = createPartialMock(DcaeConfigurationPolling.class,
                "getAllCorrelationRules");
        Whitebox.setInternalState(dcaeConfigurationPolling, "url", "http://127.0.0.1");

        RuleQueryListResponse ruleQueryListResponse = new RuleQueryListResponse();
        List<RuleResult4API> ruleResult4APIList = new ArrayList<RuleResult4API>(){
            {
                add(new RuleResult4API());
            }
        };
        ruleQueryListResponse.setCorrelationRules(ruleResult4APIList);
        ruleQueryListResponse.setTotalCount(ruleResult4APIList.size());
        expect(dcaeConfigurationPolling.getAllCorrelationRules()).andReturn(ruleQueryListResponse);

        CloseableHttpClient clientMock = createMock(CloseableHttpClient.class);
        HttpResponse httpResponseMock = createMock(HttpResponse.class);
        expect(HttpsUtils.getHttpClient(30000)).andReturn(clientMock);
        expect(HttpsUtils.delete(anyObject(HttpDelete.class), anyObject(HashMap.class), anyObject(CloseableHttpClient.class)))
                .andReturn(httpResponseMock);
        clientMock.close();
        expectLastCall();

        expect(HttpsUtils.getHttpClient(30000)).andReturn(clientMock);
        expect(HttpsUtils.put(anyObject(HttpPut.class), anyObject(HashMap.class), anyObject(HashMap.class),
                anyObject(StringEntity.class), anyObject(CloseableHttpClient.class)))
                .andReturn(httpResponseMock);
        clientMock.close();
        expectLastCall();

        StatusLine sl = createMock(StatusLine.class);
        expect(httpResponseMock.getStatusLine()).andReturn(sl);
        expect(sl.getStatusCode()).andReturn(200);

        replayAll();

        dcaeConfigurationPolling.run();
        dcaeConfigurationPolling.run();

        verifyAll();
    }



    @Test
    public void getAllCorrelationRules() throws Exception {

        CloseableHttpClient clientMock = createMock(CloseableHttpClient.class);
        HttpResponse httpResponseMock = createMock(HttpResponse.class);
        expect(HttpsUtils.getHttpClient(30000)).andReturn(clientMock);
        expect(HttpsUtils.get(anyObject(HttpGet.class), anyObject(HashMap.class), anyObject(CloseableHttpClient.class)))
                .andReturn(httpResponseMock);
        expect(HttpsUtils.extractResponseEntity(httpResponseMock)).andReturn("{\"correlationRules\": [], \"totalCount\": 0}");
        clientMock.close();
        expectLastCall();

        replayAll();
        DcaeConfigurationPolling daceConfigurationPolling = new DcaeConfigurationPolling("holmes-rule-mgmt");
        RuleQueryListResponse response = daceConfigurationPolling.getAllCorrelationRules();
        assertThat(response.getTotalCount(), is(0));
        verifyAll();
    }

    @Before
    public void setUp() {
        mockStatic(HttpsUtils.class);
    }

    @After
    public void tearDown() {
        resetAll();
    }
}