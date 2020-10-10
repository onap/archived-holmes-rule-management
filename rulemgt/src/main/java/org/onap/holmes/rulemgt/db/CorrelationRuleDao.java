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
package org.onap.holmes.rulemgt.db;

import java.util.List;

import org.jvnet.hk2.annotations.Service;
import org.onap.holmes.common.api.entity.CorrelationRule;
import org.onap.holmes.common.exception.CorrelationException;
import org.onap.holmes.common.utils.CorrelationRuleMapper;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.BindBean;
import org.skife.jdbi.v2.sqlobject.GetGeneratedKeys;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

@Service
@RegisterMapper(CorrelationRuleMapper.class)
public abstract class CorrelationRuleDao {

    @GetGeneratedKeys
    @SqlUpdate("INSERT INTO APLUS_RULE  (NAME,CTRLLOOP,DESCRIPTION,ENABLE,TEMPLATEID,ENGINETYPE,CREATOR,UPDATOR,PARAMS,CONTENT ,VENDOR,CREATETIME,UPDATETIME,ENGINEID,PACKAGE,RID, ENGINEINSTANCE) VALUES (:name,:closedControlLoopName,:description,:enabled,:templateID,:engineType,:creator,:modifier,:params,:content,:vendor,:createTime,:updateTime,:engineID,:packageName,:rid,:engineInstance)")
    protected abstract String addRule(@BindBean CorrelationRule correlationRule);

    @SqlUpdate("UPDATE APLUS_RULE SET CTRLLOOP=:closedControlLoopName,DESCRIPTION=:description,ENABLE=:enabled,CONTENT=:content,UPDATOR=:modifier,UPDATETIME=:updateTime, PACKAGE=:packageName, ENGINEINSTANCE=:engineInstance WHERE RID=:rid")
    protected abstract int updateRuleByRid(@BindBean CorrelationRule correlationRule);

    @SqlUpdate("DELETE FROM APLUS_RULE WHERE RID=:rid")
    protected abstract int deleteRuleByRid(@Bind("rid") String rid);

    @SqlUpdate("DELETE FROM APLUS_RULE WHERE RID=:rid AND NAME=:name")
    protected abstract int deleteRuleByRidAndName(@Bind("rid") String rid, @Bind("name") String name);

    @SqlQuery("SELECT * FROM APLUS_RULE")
    protected abstract List<CorrelationRule> queryAllRules();

    @SqlQuery("SELECT * FROM APLUS_RULE WHERE RID=:rid")
    protected abstract CorrelationRule queryRuleById(@Bind("rid") String rid);

    @SqlQuery("SELECT * FROM APLUS_RULE WHERE NAME=:name")
    protected abstract CorrelationRule queryRuleByName(@Bind("name") String name);

    @SqlQuery("SELECT * FROM APLUS_RULE WHERE enable=:enable")
    public abstract List<CorrelationRule> queryRuleByEnable(@Bind("enable") int enable);

    @SqlQuery("SELECT * FROM APLUS_RULE WHERE engineinstance=:engineinstance")
    public abstract List<CorrelationRule> queryRuleByEngineInstance(@Bind("engineinstance") String engineinstance);

    public List<CorrelationRule> queryRuleByRuleEngineInstance(String enginetype) {
        return queryRuleByEngineInstance(enginetype);
    }

    public List<CorrelationRule> queryRuleByRuleEnable(int enable) {
        return queryRuleByEnable(enable);
    }


    private void deleteRule2DbInner(CorrelationRule correlationRule) {
        String name = correlationRule.getName() != null ? correlationRule.getName().trim() : "";
        String rid = correlationRule.getRid() != null ? correlationRule.getRid().trim() : "";
        if (!"".equals(name) && !"".equals(rid)) {
            deleteRuleByRidAndName(rid, name);
        } else if (!"".equals(rid)) {
            deleteRuleByRid(rid);
        }
    }

    public CorrelationRule saveRule(CorrelationRule correlationRule) throws CorrelationException {
        try {
            addRule(correlationRule);
            return correlationRule;
        } catch (Exception e) {
            throw new CorrelationException("Can not access the database. Please contact the administrator for help.", e);
        }
    }

    public void updateRule(CorrelationRule correlationRule) throws CorrelationException {
        try {
            updateRuleByRid(correlationRule);
        } catch (Exception e) {
            throw new CorrelationException("Can not access the database. Please contact the administrator for help.", e);
        }
    }

    public void deleteRule(CorrelationRule correlationRule) throws CorrelationException {
        try {
            deleteRule2DbInner(correlationRule);
        } catch (Exception e) {
            throw new CorrelationException("Can not access the database. Please contact the administrator for help.", e);
        }
    }


    public CorrelationRule queryRuleByRid(String rid) throws CorrelationException {
        try {
            return queryRuleById(rid);
        } catch (Exception e) {
            throw new CorrelationException("Can not access the database. Please contact the administrator for help.", e);
        }
    }

    public CorrelationRule queryRuleByRuleName(String name) throws CorrelationException {
        try {
            return queryRuleByName(name);
        } catch (Exception e) {
            throw new CorrelationException("Can not access the database. Please contact the administrator for help.", e);
        }
    }
}

