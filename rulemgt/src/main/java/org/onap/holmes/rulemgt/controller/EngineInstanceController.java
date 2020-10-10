/**
 * Copyright 2020 ZTE Corporation.
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

package org.onap.holmes.rulemgt.controller;

import org.jvnet.hk2.annotations.Service;
import org.onap.holmes.common.engine.entity.EngineEntity;
import org.onap.holmes.common.engine.service.EngineEntityService;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;

@Service
public class EngineInstanceController extends TimerTask {
    private static final long INTERVAL = SECONDS.toMillis(15);
    private static final long THRESHOLD = 3 * INTERVAL;
    private Timer timer = new Timer("EngineInstanceController", true);

    @Inject
    private EngineEntityService engineEntityService;

    @PostConstruct
    public void initialize() {
        timer.schedule(this, MINUTES.toMillis(1), INTERVAL);
    }

    @Override
    public void run() {
        List<EngineEntity> entityList = engineEntityService.getAllEntities();
        for (EngineEntity entity : entityList) {
            if (System.currentTimeMillis() - entity.getLastModified() > THRESHOLD) {
                engineEntityService.deleteEntity(entity.getId());
            }
        }
    }
}
