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

package org.onap.holmes.rulemgt.wrapper;

import org.jvnet.hk2.annotations.Service;
import org.onap.holmes.common.api.entity.CorrelationRule;
import org.onap.holmes.common.exception.CorrelationException;
import org.onap.holmes.common.utils.DbDaoUtil;
import org.onap.holmes.rulemgt.db.CorrelationRuleDao;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.List;

@Service
public class RuleQueryWrapper {

    @Inject
    private DbDaoUtil daoUtil;
    private CorrelationRuleDao correlationRuleDao;

    @PostConstruct
    public void initDaoUtil() {
        correlationRuleDao = daoUtil.getJdbiDaoByOnDemand(CorrelationRuleDao.class);
    }

    public List<CorrelationRule> queryRuleByEnable(int enable) throws CorrelationException {
        List<CorrelationRule> ruleTemp = daoUtil.getJdbiDaoByOnDemand(CorrelationRuleDao.class)
                .queryRuleByRuleEnable(enable);
        return ruleTemp;
    }

    public List<CorrelationRule> queryRuleByEngineInstance(String instance) throws CorrelationException {
        List<CorrelationRule> ruleTemp = daoUtil.getJdbiDaoByOnDemand(CorrelationRuleDao.class)
                .queryRuleByRuleEngineInstance(instance);
        return ruleTemp;
    }
}
