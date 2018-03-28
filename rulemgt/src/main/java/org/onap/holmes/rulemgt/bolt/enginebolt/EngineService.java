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
package org.onap.holmes.rulemgt.bolt.enginebolt;

import java.util.HashMap;
import javax.ws.rs.core.MediaType;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.entity.StringEntity;
import org.jvnet.hk2.annotations.Service;
import org.onap.holmes.common.utils.GsonUtil;
import org.onap.holmes.common.utils.HttpsUtils;
import org.onap.holmes.rulemgt.bean.request.CorrelationCheckRule4Engine;
import org.onap.holmes.rulemgt.bean.request.CorrelationDeployRule4Engine;
import org.onap.holmes.rulemgt.constant.RuleMgtConstant;
import org.onap.holmes.common.config.MicroServiceConfig;

@Slf4j
@Service
public class EngineService {

    private static final String PREFIX = "https://";
    private static final String PORT = ":9102";

    protected HttpResponse delete(String packageName, String ip) throws Exception {
        HashMap headers = createHeaders();
        String url = PREFIX + ip + PORT + RuleMgtConstant.ENGINE_PATH + "/" + packageName;
        return HttpsUtils.delete(url, headers);
    }

    protected HttpResponse check(CorrelationCheckRule4Engine correlationCheckRule4Engine, String ip)
            throws Exception {
        String content = GsonUtil.beanToJson(correlationCheckRule4Engine);
        HashMap headers = createHeaders();
        String url = PREFIX + ip + PORT + RuleMgtConstant.ENGINE_PATH;
        return HttpsUtils.post(url, headers, new HashMap<>(), new StringEntity(content));
    }

    protected HttpResponse deploy(CorrelationDeployRule4Engine correlationDeployRule4Engine, String ip) throws Exception {
        String content = GsonUtil.beanToJson(correlationDeployRule4Engine);
        HashMap headers = createHeaders();
        String url = PREFIX + ip + PORT + RuleMgtConstant.ENGINE_PATH;
        return HttpsUtils.put(url, headers, new HashMap<>(), new StringEntity(content));
    }

    private HashMap<String, String> createHeaders() {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Content-Type", MediaType.APPLICATION_JSON);
        headers.put("Accept", MediaType.APPLICATION_JSON);
        return headers;
    }
}
