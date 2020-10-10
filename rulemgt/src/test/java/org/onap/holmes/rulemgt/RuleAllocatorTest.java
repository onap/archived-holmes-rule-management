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

package org.onap.holmes.rulemgt;


import org.glassfish.hk2.api.ServiceLocator;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.holmes.common.api.entity.CorrelationRule;
import org.onap.holmes.common.dropwizard.ioc.utils.ServiceLocatorHolder;
import org.onap.holmes.common.utils.DbDaoUtil;
import org.onap.holmes.rulemgt.bolt.enginebolt.EngineWrapper;
import org.onap.holmes.rulemgt.db.CorrelationRuleDao;
import org.onap.holmes.rulemgt.tools.EngineTools;
import org.onap.holmes.rulemgt.wrapper.RuleMgtWrapper;
import org.onap.holmes.rulemgt.wrapper.RuleQueryWrapper;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

import static org.easymock.EasyMock.*;
import static org.onap.holmes.rulemgt.RuleAllocator.ENABLE;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ServiceLocator.class, RuleMgtWrapper.class, RuleQueryWrapper.class, EngineWrapper.class,
        EngineTools.class, DbDaoUtil.class, ServiceLocatorHolder.class})
public class RuleAllocatorTest {

    private RuleMgtWrapper ruleMgtWrapperMock;
    private RuleQueryWrapper ruleQueryWrapperMock;
    private EngineWrapper engineWrapperMock;
    private EngineTools engineToolsMock;
    private DbDaoUtil dbDaoUtilMock;
    private CorrelationRuleDao correlationRuleDaoMock;

    private List<CorrelationRule> rules;
    private List<String> existingIps;

    @Before
    public void before() {
        ruleMgtWrapperMock = PowerMock.createMock(RuleMgtWrapper.class);
        ruleQueryWrapperMock = PowerMock.createMock(RuleQueryWrapper.class);
        engineWrapperMock = PowerMock.createMock(EngineWrapper.class);
        engineToolsMock = PowerMock.createMock(EngineTools.class);
        dbDaoUtilMock = PowerMock.createMock(DbDaoUtil.class);
        correlationRuleDaoMock = PowerMock.createMock(CorrelationRuleDao.class);

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

        List<String> newEngineInstances = new ArrayList();
        newEngineInstances.add("127.0.0.1");
        newEngineInstances.add("10.23.0.72");

        List<String> ipListFromMsb = new ArrayList();
        ipListFromMsb.addAll(newEngineInstances);
        ipListFromMsb.addAll(existingIps);

        expect(dbDaoUtilMock.getJdbiDaoByOnDemand(CorrelationRuleDao.class)).andReturn(correlationRuleDaoMock);
        expect(engineToolsMock.getInstanceList()).andReturn(ipListFromMsb);
        expect(engineToolsMock.getLegacyEngineInstances()).andReturn(existingIps);
        expect(ruleQueryWrapperMock.queryRuleByEnable(ENABLE)).andReturn(rules.stream()
                .filter(r -> r.getEnabled() == ENABLE).collect(Collectors.toList()));
        for (String ip : existingIps) {
            expect(ruleQueryWrapperMock.queryRuleByEngineInstance(ip))
                    .andReturn(rules.stream().filter(r -> r.getEngineInstance().equals(ip)).collect(Collectors.toList()));

        }
        expect(engineWrapperMock.deleteRuleFromEngine(anyObject(String.class),
                anyObject(String.class))).andReturn(true).anyTimes();
        expect(ruleQueryWrapperMock.queryRuleByEngineInstance(anyObject(String.class)))
                .andReturn(new ArrayList<>()).times(2);

        expect(ruleMgtWrapperMock.deployRule2Engine(anyObject(CorrelationRule.class),
                anyObject(String.class))).andReturn("").anyTimes();
        correlationRuleDaoMock.updateRule(anyObject(CorrelationRule.class));
        expectLastCall().anyTimes();

        PowerMock.replayAll();

        RuleAllocator ruleAllocator = new RuleAllocator(ruleMgtWrapperMock, ruleQueryWrapperMock,
                engineWrapperMock, engineToolsMock, dbDaoUtilMock);
        ruleAllocator.allocateRules();

        PowerMock.verifyAll();

    }

    @Test
    public void allocateRuleTest_engine_scaled_in() throws Exception {

        List<String> ipListFromMsb = new ArrayList<>();
        ipListFromMsb.addAll(existingIps);
        ipListFromMsb.remove(0);

        expect(dbDaoUtilMock.getJdbiDaoByOnDemand(CorrelationRuleDao.class)).andReturn(correlationRuleDaoMock);
        expect(engineToolsMock.getInstanceList()).andReturn(ipListFromMsb);
        expect(engineToolsMock.getLegacyEngineInstances()).andReturn(existingIps);
        for (String ip : existingIps) {
            expect(ruleQueryWrapperMock.queryRuleByEngineInstance(anyObject(String.class)))
                    .andReturn(rules.stream().filter(r -> r.getEngineInstance().equals(ip)).collect(Collectors.toList()));

        }
        expect(ruleMgtWrapperMock.deployRule2Engine(anyObject(CorrelationRule.class), anyString())).andReturn("anyId").times(2);
        correlationRuleDaoMock.updateRule(anyObject(CorrelationRule.class));
        expectLastCall().times(2);

        PowerMock.replayAll();

        RuleAllocator ruleAllocator = new RuleAllocator(ruleMgtWrapperMock, ruleQueryWrapperMock,
                engineWrapperMock, engineToolsMock, dbDaoUtilMock);

        ruleAllocator.allocateRules();

        PowerMock.verifyAll();

    }
}
