/**
 * Copyright 2017 ZTE Corporation.
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


import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.jvnet.hk2.annotations.Service;
import org.onap.holmes.common.api.entity.ServiceEntity;
import org.onap.holmes.common.api.entity.ServiceNode4Query;
import org.onap.holmes.common.config.MicroServiceConfig;
import org.onap.holmes.common.utils.GsonUtil;
import org.onap.holmes.common.utils.HttpsUtils;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
@Slf4j
public class EngineInsQueryTool {

    private String url;

    @PostConstruct
    public void init() {
        String[] msbAddrInfo = MicroServiceConfig.getMsbIpAndPort();
        url = String.format("http://%s:%s/api/microservices/v1/services/holmes-engine-mgmt/version/v1",
                msbAddrInfo[0], msbAddrInfo[1]);
    }

    public List<String> getInstanceList() throws Exception {
		String response;
		HttpGet httpGet = new HttpGet(url);
		try (CloseableHttpClient httpClient = HttpsUtils.getConditionalHttpsClient(HttpsUtils.DEFUALT_TIMEOUT)) {
			HttpResponse httpResponse = HttpsUtils.get(httpGet, new HashMap<>(), httpClient);
			response = HttpsUtils.extractResponseEntity(httpResponse);
		} catch (Exception e) {
			throw e;
		} finally {
			httpGet.releaseConnection();

		}
		ServiceEntity service = GsonUtil.jsonToBean(response, ServiceEntity.class);
		List<ServiceNode4Query> nodesList = service.getNodes();
		List<String> ipList = new ArrayList<>();
		for (ServiceNode4Query node : nodesList) {
			ipList.add(node.getIp());
		}
		return ipList;

    }

}
