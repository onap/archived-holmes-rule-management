/**
 * Copyright 2017 ZTE Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onap.holmes.rulemgt.db;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.onap.holmes.common.api.entity.CorrelationRule;
import org.onap.holmes.common.exception.CorrelationException;
import org.onap.holmes.common.utils.DbDaoUtil;
import org.onap.holmes.rulemgt.bean.request.RuleQueryCondition;
import org.powermock.api.easymock.PowerMock;
import org.powermock.modules.junit4.rule.PowerMockRule;
import org.powermock.reflect.Whitebox;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.Query;


public class CorrelationRuleQueryDaoTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Rule
    public PowerMockRule powerMockRule = new PowerMockRule();
    private DbDaoUtil dbDaoUtil;

    private Handle handle;

    private Query query;

    private CorrelationRuleQueryDao correlationRuleQueryDao;
    private RuleQueryCondition ruleQueryCondition;

    @Before
    public void setUp() throws Exception {
        correlationRuleQueryDao = new CorrelationRuleQueryDao();

        dbDaoUtil = PowerMock.createMock(DbDaoUtil.class);
        handle = PowerMock.createMock(Handle.class);
        query = PowerMock.createMock(Query.class);

        Whitebox.setInternalState(correlationRuleQueryDao, "dbDaoUtil", dbDaoUtil);

        ruleQueryCondition = createRuleQueryCondition();
    }


    @Test
    public void getCorrelationRulesByCondition_db_exception() throws Exception {

        thrown.expect(CorrelationException.class);
        thrown.expectMessage("Failed to query the rule.");

        EasyMock.expect(dbDaoUtil.getHandle()).andReturn(handle);
        EasyMock.expect(handle.createQuery(EasyMock.anyObject(String.class))).andReturn(query);
        EasyMock.expect(query.list()).andThrow(new RuntimeException()).anyTimes();
        dbDaoUtil.close(handle);
        EasyMock.expectLastCall();

        PowerMock.replayAll();

        correlationRuleQueryDao.getCorrelationRulesByCondition(ruleQueryCondition);

        PowerMock.verifyAll();
    }

    @Test
    public void getCorrelationRulesByCondition_normal() throws Exception {
        EasyMock.expect(dbDaoUtil.getHandle()).andReturn(handle);
        EasyMock.expect(handle.createQuery(EasyMock.anyObject(String.class))).andReturn(query);
        EasyMock.expect(query.list()).andReturn(createQueryResult()).anyTimes();
        dbDaoUtil.close(handle);
        EasyMock.expectLastCall();

        PowerMock.replayAll();

        List<CorrelationRule> result = correlationRuleQueryDao.getCorrelationRulesByCondition(ruleQueryCondition);
        assertThat(result.size(), is(1));

        PowerMock.verifyAll();
    }

    @Test
    public void getCorrelationRulesByCondition_get_where_sql_exception() throws Exception {
        thrown.expect(CorrelationException.class);
        thrown.expectMessage("An error occurred while building the query SQL.");

        EasyMock.expect(dbDaoUtil.getHandle()).andReturn(handle);
        EasyMock.expect(handle.createQuery(EasyMock.anyObject(String.class))).andReturn(query);
        EasyMock.expect(query.list()).andReturn(createQueryResult()).anyTimes();
        dbDaoUtil.close(handle);
        EasyMock.expectLastCall();

        PowerMock.replayAll();

        correlationRuleQueryDao.getCorrelationRulesByCondition(null);

        PowerMock.verifyAll();
    }

    private List<Map<String, Object>> createQueryResult() {
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> value = new HashMap<>();
        value.put("name", "Rule-001");
        value.put("rid", "rule_" + System.currentTimeMillis());
        value.put("description", "desc");
        value.put("enable", 0);
        value.put("templateID", 1);
        value.put("engineId", "engine-001");
        value.put("engineType", "engineType-001");
        value.put("creator", "admin");
        value.put("createTime", new Date());
        value.put("updator", "admin");
        value.put("updateTime", new Date());
        value.put("params", new Properties());
        value.put("domain", "Domain");
        value.put("isManual", 0);
        value.put("vendor", "Vendor");
        value.put("content", "Contents");
        value.put("package", "package");
        list.add(value);
        return list;
    }

    private RuleQueryCondition createRuleQueryCondition() {
        RuleQueryCondition ruleQueryCondition = new RuleQueryCondition();
        ruleQueryCondition.setRid("rule_" + System.currentTimeMillis());
        ruleQueryCondition.setName("Rule-001");
        ruleQueryCondition.setEnabled(0);
        ruleQueryCondition.setCreator("admin");
        ruleQueryCondition.setModifier("admin");
        return ruleQueryCondition;
    }

}
