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
package org.onap.holmes.rulemgt.msb;

import org.onap.holmes.common.dropwizard.ioc.utils.ServiceLocatorHolder;
import org.onap.holmes.rulemgt.send.Ip4AddingRule;
import org.onap.holmes.rulemgt.send.RuleAllocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static java.util.concurrent.TimeUnit.SECONDS;

public class MsbQuery {

    static final private Logger log = LoggerFactory.getLogger(MsbQuery.class);
    final private RuleAllocator ruleAllocator;
    private Ip4AddingRule ip4AddingRule;
    private EngineInsQueryTool engineInsQueryTool;

    public MsbQuery() {
        ruleAllocator = new RuleAllocator();
        ip4AddingRule = ServiceLocatorHolder.getLocator().getService(Ip4AddingRule.class);
        engineInsQueryTool = ServiceLocatorHolder.getLocator().getService(EngineInsQueryTool.class);
    }

    public void startTimer() {
        try {
            new Timer().schedule(new TimerTask() {

                public void run() {
                    try {
                        List<String> timerIpList = engineInsQueryTool.getInstanceList();
                        log.info(String.format("There are %d engine instance(s) running currently.", timerIpList.size()));

                        ip4AddingRule.setIpList(timerIpList);
                        ruleAllocator.allocateRules(timerIpList);
                    } catch (Exception e) {
                        log.error("The timing query engine instance failed ", e);
                    }
                }

            }, SECONDS.toMillis(10), SECONDS.toMillis(30));
        } catch (Exception e) {
            log.error("MSBQuery startTimer timer task failed !" + e.getMessage(), e);
            try {
                SECONDS.sleep(30);
            } catch (InterruptedException e1) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
