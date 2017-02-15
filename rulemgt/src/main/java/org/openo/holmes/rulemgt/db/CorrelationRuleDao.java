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
package org.openo.holmes.rulemgt.db;

import org.openo.holmes.common.api.entity.CorrelationRule;
import org.openo.holmes.rulemgt.db.mapper.CorrelationRuleMapper;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.BindBean;
import org.skife.jdbi.v2.sqlobject.GetGeneratedKeys;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

import java.util.List;

@RegisterMapper(CorrelationRuleMapper.class)
public abstract class CorrelationRuleDao {
    @GetGeneratedKeys
    @SqlUpdate("INSERT INTO APLUS_RULE  (NAME,DESCRIPTION,ENABLE,TEMPLATEID,ENGINETYPE,CREATOR,UPDATOR,PARAMS,DOMAIN ,CONTENT ,VENDOR,CREATETIME,UPDATETIME,ENGINEID,ISMANUAL,PACKAGE,RID) VALUES (:name,:description,:enabled,:templateID,:engineType,:creator,:modifier,:params,:domain,:content,:vendor,:createTime,:updateTime,:engineId,:isManual,:packageName,:rid)")
    protected abstract int addRule(@BindBean CorrelationRule correlationRule);

    @SqlUpdate("UPDATE APLUS_RULE SET DESCRIPTION=:description,ENABLE=:enabled,CONTENT=:content,UPDATOR=:modifier,UPDATETIME=:updateTime WHERE RID=:rid")
    protected abstract int updateRuleByRid(@BindBean CorrelationRule correlationRule);

    @SqlUpdate("DELETE FROM APLUS_RULE WHERE RID=:rid")
    protected abstract int deleteRuleByRid(@Bind("rid") String rid);

    @SqlUpdate("DELETE FROM APLUS_RULE WHERE RID=:rid AND NAME=:name")
    protected abstract int deleteRuleByRidAndName(@Bind("rid") String rid, @Bind("name") String name);

    @SqlQuery("SELECT * FROM APLUS_RULE")
    protected abstract List<CorrelationRule> queryAllRules();

    @SqlQuery("SELECT * FROM APLUS_RULE WHERE RID=:rid")
    public abstract CorrelationRule queryRuleByRid(@Bind("rid") String rid);

    @SqlQuery("SELECT * FROM APLUS_RULE WHERE NAME=:name")
    public abstract CorrelationRule queryRuleByName(@Bind("name") String name);

    private void deleteRule2DbInner(CorrelationRule correlationRule) {
        String name = correlationRule.getName() != null ? correlationRule.getName().trim() : "";
        String rid = correlationRule.getRid() != null ? correlationRule.getRid().trim() : "";
        if (!name.equals("") && !rid.equals("")) {
            deleteRuleByRidAndName(rid, name);
        } else if (!rid.equals("")) {
            deleteRuleByRid(rid);
        }
    }

    public CorrelationRule saveRule(CorrelationRule correlationRule) {
        addRule(correlationRule);
        return correlationRule;
    }

    public void updateRule(CorrelationRule correlationRule){
        updateRuleByRid(correlationRule);
    }

    public void deleteRule(CorrelationRule correlationRule) {
        deleteRule2DbInner(correlationRule);
    }


    public CorrelationRule getRuleByRid(String rid) {
        return queryRuleByRid(rid);
    }

    public CorrelationRule queryRuleByRuleName(String name) {
        return queryRuleByName(name);
    }
}

