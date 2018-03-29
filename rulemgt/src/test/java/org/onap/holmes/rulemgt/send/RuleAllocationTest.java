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

package org.onap.holmes.rulemgt.send;


import org.easymock.EasyMock;
import org.glassfish.hk2.api.ServiceLocator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.holmes.common.dropwizard.ioc.utils.ServiceLocatorHolder;
import org.onap.holmes.common.utils.DbDaoUtil;
import org.onap.holmes.rulemgt.bolt.enginebolt.EngineWrapper;
import org.onap.holmes.rulemgt.db.CorrelationRuleDao;
import org.onap.holmes.rulemgt.msb.EngineIpList;
import org.onap.holmes.rulemgt.wrapper.RuleMgtWrapper;
import org.onap.holmes.rulemgt.wrapper.RuleQueryWrapper;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ServiceLocator.class, RuleMgtWrapper.class, RuleQueryWrapper.class, EngineWrapper.class,
        EngineIpList.class, DbDaoUtil.class, RuleAllocation.class, ServiceLocatorHolder.class})
public class RuleAllocationTest {

    @Before
    public void prepare() {

        ServiceLocator locator = PowerMock.createMock(ServiceLocator.class);
        RuleMgtWrapper ruleMgtWrapper = PowerMock.createMock(RuleMgtWrapper.class);
        RuleQueryWrapper ruleQueryWrapper = PowerMock.createMock(RuleQueryWrapper.class);
        EngineWrapper engineWrapper = PowerMock.createMock(EngineWrapper.class);
        EngineIpList engineIpList = PowerMock.createMock(EngineIpList.class);
        CorrelationRuleDao correlationRuleDao = PowerMock.createMock(CorrelationRuleDao.class);
        DbDaoUtil daoUtil = PowerMock.createMock(DbDaoUtil.class);
        PowerMock.mockStatic(ServiceLocatorHolder.class);

        EasyMock.expect(ServiceLocatorHolder.getLocator()).andReturn(locator);
        EasyMock.expect(locator.getService(RuleMgtWrapper.class)).andReturn(ruleMgtWrapper);
        EasyMock.expect(locator.getService(RuleQueryWrapper.class)).andReturn(ruleQueryWrapper);
        EasyMock.expect(locator.getService(EngineWrapper.class)).andReturn(engineWrapper);
        EasyMock.expect(locator.getService(EngineIpList.class)).andReturn(engineIpList);
        EasyMock.expect(locator.getService(DbDaoUtil.class)).andReturn(daoUtil);
        EasyMock.expect(daoUtil.getJdbiDaoByOnDemand(CorrelationRuleDao.class)).andReturn(correlationRuleDao);
        try {
            EasyMock.expect(engineIpList.getServiceCount()).andReturn(new ArrayList());
        } catch (Exception e) {
            // Do nothing
        }


        PowerMock.replayAll();

    }

    @After
    public void destroy() {
        PowerMock.resetAll();
    }

    @Test
    public void extendCompareIpTest() throws Exception{
        RuleAllocation ruleAllocation = new RuleAllocation();

        List<String> newList = new ArrayList<>();
        newList.add("10.96.33.34");
        newList.add("10.74.65.24");

        List<String> oldList = new ArrayList<>();
        oldList.add("10.96.33.34");
        List<String> extendIp = Whitebox.invokeMethod(ruleAllocation,"extendCompareIp",newList,oldList);

        PowerMock.verifyAll();

        assertThat(extendIp.get(0),equalTo("10.74.65.24"));
    }

    @Test
    public void destroyCompareIpTest() throws Exception{
        RuleAllocation ruleAllocation = new RuleAllocation();

        List<String> newList = new ArrayList<>();
        newList.add("10.96.33.34");

        List<String> oldList = new ArrayList<>();
        oldList.add("10.96.33.34");
        oldList.add("10.74.65.24");
        List<String> destoryIp = Whitebox.invokeMethod(ruleAllocation,"destroyCompareIp",newList,oldList);

        PowerMock.verifyAll();

        assertThat(destoryIp.get(0),equalTo("10.74.65.24"));
    }

}
