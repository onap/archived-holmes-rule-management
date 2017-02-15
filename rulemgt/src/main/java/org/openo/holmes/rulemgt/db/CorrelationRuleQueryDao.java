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
import org.openo.holmes.common.exception.DataFormatException;
import org.openo.holmes.common.exception.DbException;
import org.openo.holmes.common.utils.DbDaoUtil;
import org.openo.holmes.common.utils.I18nProxy;
import org.openo.holmes.rulemgt.bean.request.RuleQueryCondition;
import org.openo.holmes.rulemgt.constant.RuleMgtConstant;
import lombok.extern.slf4j.Slf4j;
import org.jvnet.hk2.annotations.Service;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.Query;

import javax.inject.Inject;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@Service
@Slf4j
public class CorrelationRuleQueryDao {
    @Inject
    private DbDaoUtil dbDaoUtil;

    public List<CorrelationRule> getCorrelationRulesByCondition(RuleQueryCondition ruleQueryCondition) throws DataFormatException, DbException {
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
            log.warn("Query rule: rule id =" + ruleQueryCondition.getRid() + " failed");
            throw new DbException(I18nProxy.RULE_MANAGEMENT_QUERY_RULE_FAILED, e);
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
        correlationRule.setTemplateID((Integer) value.get("templateID"));
        correlationRule.setEngineId((String) value.get("engineID"));
        correlationRule.setEngineType((String) value.get("engineType"));
        correlationRule.setCreator((String) value.get("creator"));
        correlationRule.setCreateTime((Date) value.get("createTime"));
        correlationRule.setModifier((String) value.get("updator"));
        correlationRule.setUpdateTime((Date) value.get("updateTime"));
        correlationRule.setParams((Properties) value.get("params"));
        correlationRule.setDomain((String) value.get("domain"));
        correlationRule.setContent((String) value.get("content"));
        correlationRule.setIsManual((Integer) value.get("isManual"));
        correlationRule.setVendor((String) value.get("vendor"));
        correlationRule.setPackageName((String) value.get("package"));
        return correlationRule;
    }

    private String getWhereStrByRequestEntity(RuleQueryCondition ruleQueryCondition) throws DataFormatException {
        try {
            Class clazz = ruleQueryCondition.getClass();
            Field[] fields = clazz.getDeclaredFields();
            String whereSql = " WHERE ";

            for (Field field : fields) {
                PropertyDescriptor pd = new PropertyDescriptor(field.getName(),
                        clazz);
                Method getMethod = pd.getReadMethod();//获得get方法
                Object o = getMethod.invoke(ruleQueryCondition);//执行get方法返回一个Object
                if (o != null) {
                    if (field.getName().equals("enabled")) {
                        int enabled = (int) o;
                        if (enabled != RuleMgtConstant.STATUS_RULE_ALL) {
                            whereSql = whereSql + "enable =" + enabled;
                            whereSql += " AND ";
                        }
                    } else if (field.getName().equals("name")) {
                        if (!"".equals(o.toString().trim())) {
                            whereSql = whereSql + field.getName() + "  like '%" + o + "%'  AND ";
                        }
                    } else {
                        if (!"".equals(o.toString().trim())) {
                            whereSql = whereSql + field.getName() + "='" + o + "'  AND ";
                        }
                    }
                }
            }
            if (whereSql.indexOf("AND") > -1) {
                whereSql = whereSql.trim();
                return whereSql.substring(0, whereSql.length() - "AND".length());
            }
            return "";
        } catch (Exception e) {
            throw new DataFormatException(I18nProxy.RULE_MANAGEMENT_CREATE_QUERY_SQL_FAILED, e);
        }
    }
}
