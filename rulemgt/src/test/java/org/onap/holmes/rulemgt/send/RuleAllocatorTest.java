/**
 * Copyright 2017-2020 ZTE Corporation.
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

package org.onap.holmes.rulemgt.send;


import org.easymock.EasyMock;
import org.glassfish.hk2.api.ServiceLocator;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.onap.holmes.common.api.entity.CorrelationRule;
import org.onap.holmes.common.dropwizard.ioc.utils.ServiceLocatorHolder;
import org.onap.holmes.common.utils.DbDaoUtil;
import org.onap.holmes.rulemgt.bolt.enginebolt.EngineWrapper;
import org.onap.holmes.rulemgt.db.CorrelationRuleDao;
import org.onap.holmes.rulemgt.msb.EngineInsQueryTool;
import org.onap.holmes.rulemgt.wrapper.RuleMgtWrapper;
import org.onap.holmes.rulemgt.wrapper.RuleQueryWrapper;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.onap.holmes.rulemgt.send.RuleAllocator.ENABLE;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ServiceLocator.class, RuleMgtWrapper.class, RuleQueryWrapper.class, EngineWrapper.class,
        EngineInsQueryTool.class, DbDaoUtil.class, ServiceLocatorHolder.class})
public class RuleAllocatorTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private RuleMgtWrapper ruleMgtWrapperMock;
    private RuleQueryWrapper ruleQueryWrapperMock;
    private EngineWrapper engineWrapperMock;
    private EngineInsQueryTool engineInsQueryToolMock;
    private DbDaoUtil dbDaoUtilMock;
    private CorrelationRuleDao correlationRuleDaoMock;

    private List<CorrelationRule> rules;
    private List<String> existingIps;

    @Before
    public void before() {
        PowerMock.mockStatic(ServiceLocatorHolder.class);
        ServiceLocator locator = PowerMock.createMock(ServiceLocator.class);
        EasyMock.expect(ServiceLocatorHolder.getLocator()).andReturn(locator);

        ruleMgtWrapperMock = PowerMock.createMock(RuleMgtWrapper.class);
        ruleQueryWrapperMock = PowerMock.createMock(RuleQueryWrapper.class);
        engineWrapperMock = PowerMock.createMock(EngineWrapper.class);
        engineInsQueryToolMock = PowerMock.createMock(EngineInsQueryTool.class);
        dbDaoUtilMock = PowerMock.createMock(DbDaoUtil.class);
        correlationRuleDaoMock = PowerMock.createMock(CorrelationRuleDao.class);

        EasyMock.expect(locator.getService(RuleMgtWrapper.class)).andReturn(ruleMgtWrapperMock);
        EasyMock.expect(locator.getService(RuleQueryWrapper.class)).andReturn(ruleQueryWrapperMock);
        EasyMock.expect(locator.getService(EngineWrapper.class)).andReturn(engineWrapperMock);
        EasyMock.expect(locator.getService(EngineInsQueryTool.class)).andReturn(engineInsQueryToolMock);
        EasyMock.expect(locator.getService(DbDaoUtil.class)).andReturn(dbDaoUtilMock);
        EasyMock.expect(dbDaoUtilMock.getJdbiDaoByOnDemand(CorrelationRuleDao.class)).andReturn(correlationRuleDaoMock);

        rules = new ArrayList<>();
        for (int i = 0; i < 20; ++i) {
            CorrelationRule rule = new CorrelationRule();
            rule.setRid("rid-" + i);
            rule.setName("rule-" + i);
            rule.setDescription("desc-" + i);
            rule.setEnabled(1);
            rule.setTemplateID((long) i);
            rule.setEngineID("engine-" + i);
            rule.setCreateTime(Calendar.getInstance().getTime());
            rule.setUpdateTime(Calendar.getInstance().getTime());
            rule.setPackageName("package-" + i);
            rule.setClosedControlLoopName("CL-" + i);
            rule.setEngineInstance("10.15.3." + (i % 10));
            rules.add(rule);
        }

        existingIps = new ArrayList<>();
        for (int i = 0; i < 10; ++i) {
            existingIps.add("10.15.3." + i);
        }
    }

    @After
    public void after() {
        PowerMock.resetAll();
    }

    @Test
    public void allocateRuleTest_engine_scaled_out() throws Exception {

        List<String> ipListFromMsb = new ArrayList<>();
        ipListFromMsb.add("127.0.0.1");
        ipListFromMsb.add("10.23.0.72");
        ipListFromMsb.addAll(existingIps);

        EasyMock.expect(engineInsQueryToolMock.getInstanceList()).andReturn(existingIps);
        EasyMock.expect(ruleQueryWrapperMock.queryRuleByEnable(ENABLE)).andReturn(rules.stream()
                .filter(r -> r.getEnabled() == ENABLE).collect(Collectors.toList()));
        for (String ip : existingIps) {
            EasyMock.expect(ruleQueryWrapperMock.queryRuleByEngineInstance(EasyMock.anyObject(String.class)))
                    .andReturn(rules.stream().filter(r -> r.getEngineInstance().equals(ip)).collect(Collectors.toList()));

        }
        EasyMock.expect(engineWrapperMock.deleteRuleFromEngine(EasyMock.anyObject(String.class),
                EasyMock.anyObject(String.class))).andReturn(true).anyTimes();
        EasyMock.expect(ruleQueryWrapperMock.queryRuleByEngineInstance(EasyMock.anyObject(String.class)))
                .andReturn(new ArrayList<>()).times(2);

        EasyMock.expect(ruleMgtWrapperMock.deployRule2Engine(EasyMock.anyObject(CorrelationRule.class),
                EasyMock.anyObject(String.class))).andReturn("").anyTimes();
        correlationRuleDaoMock.updateRule(EasyMock.anyObject(CorrelationRule.class));
        EasyMock.expectLastCall().anyTimes();

        PowerMock.replayAll();

        RuleAllocator ruleAllocator = new RuleAllocator();
        ruleAllocator.allocateRules(ipListFromMsb);

        PowerMock.verifyAll();

    }

    @Test
    public void allocateRuleTest_engine_scaled_in() throws Exception {

        List<String> ipListFromMsb = new ArrayList<>();
        ipListFromMsb.addAll(existingIps);
        ipListFromMsb.remove(0);

        List<CorrelationRule> rules = new ArrayList<>();


        EasyMock.expect(engineInsQueryToolMock.getInstanceList()).andReturn(existingIps);
        for (String ip : existingIps) {
            EasyMock.expect(ruleQueryWrapperMock.queryRuleByEngineInstance(EasyMock.anyObject(String.class)))
                    .andReturn(rules.stream().filter(r -> r.getEngineInstance().equals(ip)).collect(Collectors.toList()));

        }
        EasyMock.expect(engineWrapperMock.deleteRuleFromEngine(EasyMock.anyObject(String.class),
                EasyMock.anyObject(String.class))).andReturn(true).anyTimes();

        PowerMock.replayAll();

        RuleAllocator ruleAllocator = new RuleAllocator();
        ruleAllocator.allocateRules(ipListFromMsb);

        PowerMock.verifyAll();

    }

    @Test
    public void allocateRuleTest_empty_param() throws Exception {

        EasyMock.expect(engineInsQueryToolMock.getInstanceList()).andReturn(Collections.emptyList());

        thrown.expect(NullPointerException.class);

        PowerMock.replayAll();

        RuleAllocator ruleAllocator = new RuleAllocator();
        ruleAllocator.allocateRules(null);

        PowerMock.verifyAll();

    }

    @Test
    public void allocateRuleTest_equal_engine_instance_num() throws Exception {

        List<String> ipListFromMsb = new ArrayList<>();
        ipListFromMsb.addAll(existingIps);

        EasyMock.expect(engineInsQueryToolMock.getInstanceList()).andReturn(existingIps);

        PowerMock.replayAll();

        RuleAllocator ruleAllocator = new RuleAllocator();
        ruleAllocator.allocateRules(ipListFromMsb);

        PowerMock.verifyAll();

    }

}
