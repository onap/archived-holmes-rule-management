/**
 * Copyright 2017-2021 ZTE Corporation.
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
package org.onap.holmes.rulemgt.db.jdbi;

import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.onap.holmes.common.api.entity.CorrelationRule;
import org.onap.holmes.common.utils.CorrelationRuleMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RegisterRowMapper(CorrelationRuleMapper.class)
public interface CorrelationRuleDao {

    @GetGeneratedKeys
    @SqlUpdate("INSERT INTO APLUS_RULE  (NAME,CTRLLOOP,DESCRIPTION,ENABLE,TEMPLATEID,ENGINETYPE,CREATOR,UPDATOR,PARAMS,CONTENT ,VENDOR,CREATETIME,UPDATETIME,ENGINEID,PACKAGE,RID, ENGINEINSTANCE) VALUES (:name,:closedControlLoopName,:description,:enabled,:templateID,:engineType,:creator,:modifier,:params,:content,:vendor,:createTime,:updateTime,:engineID,:packageName,:rid,:engineInstance)")
    String addRule(@BindBean CorrelationRule correlationRule);

    @SqlUpdate("UPDATE APLUS_RULE SET CTRLLOOP=:closedControlLoopName,DESCRIPTION=:description,ENABLE=:enabled,CONTENT=:content,UPDATOR=:modifier,UPDATETIME=:updateTime, PACKAGE=:packageName, ENGINEINSTANCE=:engineInstance WHERE RID=:rid")
    int updateRuleByRid(@BindBean CorrelationRule correlationRule);

    @SqlUpdate("DELETE FROM APLUS_RULE WHERE RID=:rid")
    int deleteRuleByRid(@Bind("rid") String rid);

    @SqlUpdate("DELETE FROM APLUS_RULE WHERE RID=:rid AND NAME=:name")
    int deleteRuleByRidAndName(@Bind("rid") String rid, @Bind("name") String name);

    @SqlQuery("SELECT * FROM APLUS_RULE")
    List<CorrelationRule> queryAllRules();

    @SqlQuery("SELECT * FROM APLUS_RULE WHERE RID=:rid")
    CorrelationRule queryRuleById(@Bind("rid") String rid);

    @SqlQuery("SELECT * FROM APLUS_RULE WHERE NAME=:name")
    CorrelationRule queryRuleByName(@Bind("name") String name);

    @SqlQuery("SELECT * FROM APLUS_RULE WHERE enable=:enable")
    List<CorrelationRule> queryRuleByEnable(@Bind("enable") int enable);

    @SqlQuery("SELECT * FROM APLUS_RULE WHERE engineinstance=:engineinstance")
   List<CorrelationRule> queryRuleByEngineInstance(@Bind("engineinstance") String engineinstance);
}

