/**
 * Copyright 2021-2022 ZTE Corporation.
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

import org.apache.commons.lang3.StringUtils;
import org.onap.holmes.common.api.entity.CorrelationRule;
import org.onap.holmes.common.exception.CorrelationException;
import org.onap.holmes.rulemgt.db.jdbi.CorrelationRuleDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CorrelationRuleService {

    @Autowired
    private CorrelationRuleDao correlationRuleDao;

    public List<CorrelationRule> queryRuleByRuleEngineInstance(String enginetype) {
        return correlationRuleDao.queryRuleByEngineInstance(enginetype);
    }

    public List<CorrelationRule> queryRuleByRuleEnable(int enable) {
        return correlationRuleDao.queryRuleByEnable(enable);
    }


    private void deleteRule2DbInner(CorrelationRule correlationRule) {
        String name = correlationRule.getName() != null ? correlationRule.getName().trim() : "";
        String rid = correlationRule.getRid() != null ? correlationRule.getRid().trim() : "";
        if (!StringUtils.EMPTY.equals(name) && !StringUtils.EMPTY.equals(rid)) {
            correlationRuleDao.deleteRuleByRidAndName(rid, name);
        } else if (!"".equals(rid)) {
            correlationRuleDao.deleteRuleByRid(rid);
        }
    }

    public CorrelationRule saveRule(CorrelationRule correlationRule) throws CorrelationException {
        try {
            correlationRuleDao.addRule(correlationRule);
            return correlationRule;
        } catch (Exception e) {
            throw new CorrelationException("Can not access the database. Please contact the administrator for help.", e);
        }
    }

    public void updateRule(CorrelationRule correlationRule) throws CorrelationException {
        try {
            correlationRuleDao.updateRuleByRid(correlationRule);
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
            return correlationRuleDao.queryRuleById(rid);
        } catch (Exception e) {
            throw new CorrelationException("Can not access the database. Please contact the administrator for help.", e);
        }
    }

    public CorrelationRule queryRuleByRuleName(String name) throws CorrelationException {
        try {
            return correlationRuleDao.queryRuleByName(name);
        } catch (Exception e) {
            throw new CorrelationException("Can not access the database. Please contact the administrator for help.", e);
        }
    }
}
