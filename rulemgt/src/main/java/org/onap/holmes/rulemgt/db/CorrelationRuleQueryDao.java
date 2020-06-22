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

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.jvnet.hk2.annotations.Service;
import org.onap.holmes.rulemgt.bean.request.RuleQueryCondition;
import org.onap.holmes.rulemgt.constant.RuleMgtConstant;
import org.onap.holmes.common.api.entity.CorrelationRule;
import org.onap.holmes.common.exception.CorrelationException;
import org.onap.holmes.common.utils.DbDaoUtil;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.Query;

@Service
@Slf4j
public class CorrelationRuleQueryDao {

    @Inject
    private DbDaoUtil dbDaoUtil;

    public List<CorrelationRule> getCorrelationRulesByCondition(RuleQueryCondition ruleQueryCondition)
            throws CorrelationException {
        List<CorrelationRule> correlationRules = new ArrayList<CorrelationRule>();
        Handle handle = null;
        String whereStr = getWhereStrByRequestEntity(ruleQueryCondition);
        try {
            StringBuilder querySql = new StringBuilder("SELECT * FROM APLUS_RULE ").append(whereStr);
            handle = dbDaoUtil.getHandle();
            Query query = handle.createQuery(querySql.toString());
            for (Object value : query.list()) {
                CorrelationRule correlationRule = getCorrelationRule((Map) value);
                correlationRules.add(correlationRule);
            }
            return correlationRules;
        } catch (Exception e) {
            log.warn("Failed to query the rule: id =" + ruleQueryCondition.getRid() + ".");
            throw new CorrelationException("Failed to query the rule.", e);
        } finally {
            dbDaoUtil.close(handle);
        }
    }

    private CorrelationRule getCorrelationRule(Map value) {
        CorrelationRule correlationRule = new CorrelationRule();
        correlationRule.setName((String) value.get("name"));
        correlationRule.setRid((String) value.get("rid"));
        correlationRule.setDescription((String) value.get("description"));
        correlationRule.setEnabled((Integer) value.get("enable"));
        correlationRule.setTemplateID((Long) value.get("templateID"));
        correlationRule.setEngineID((String) value.get("engineID"));
        correlationRule.setEngineType((String) value.get("engineType"));
        correlationRule.setCreator((String) value.get("creator"));
        correlationRule.setCreateTime((Date) value.get("createTime"));
        correlationRule.setModifier((String) value.get("updator"));
        correlationRule.setUpdateTime((Date) value.get("updateTime"));
        correlationRule.setParams((Properties) value.get("params"));
        correlationRule.setContent((String) value.get("content"));
        correlationRule.setVendor((String) value.get("vendor"));
        correlationRule.setPackageName((String) value.get("package"));
        correlationRule.setClosedControlLoopName((String) value.get("ctrlloop"));
        correlationRule.setEngineInstance((String) value.get("engineInstance"));
        return correlationRule;
    }

    private String getWhereStrByRequestEntity(RuleQueryCondition ruleQueryCondition) throws CorrelationException {
        try {
            Class clazz = ruleQueryCondition.getClass();
            Field[] fields = clazz.getDeclaredFields();
            String whereSql = " WHERE ";

            for (Field field : fields) {
                // Jacoco will cause an exception when calculating the coverage of the UT
                // Remove this if jacoco solves this problem in the future
                if (field.getName().contains("jacoco")) {
                    continue;
                }
                PropertyDescriptor pd = new PropertyDescriptor((String)field.getName(),
                        clazz);
                Method getMethod = pd.getReadMethod();
                Object o = getMethod.invoke(ruleQueryCondition);
                if (o != null) {
                    String tempName = field.getName();
                    if ("enabled".equals(tempName)) {
                        int enabled = (int) o;
                        if (enabled != RuleMgtConstant.STATUS_RULE_ALL) {
                            whereSql = whereSql + "enable =" + enabled;
                            whereSql += " AND ";
                        }
                    } else if ("name".equals(tempName)) {
                        if (!"".equals(o.toString().trim())) {
                            whereSql = whereSql + field.getName() + "  like '%" + o + "%'  AND ";
                        }
                    } else if (!"".equals(o.toString().trim())) {
                        whereSql = whereSql + field.getName() + "='" + o + "'  AND ";
                    }
                }
            }
            whereSql = whereSql.trim();
            if(!"WHERE".equals(whereSql)){
                return whereSql.substring(0, whereSql.length() - "AND".length());
            }
            return "";
        } catch (Exception e) {
            throw new CorrelationException("An error occurred while building the query SQL.", e);
        }
    }
}
