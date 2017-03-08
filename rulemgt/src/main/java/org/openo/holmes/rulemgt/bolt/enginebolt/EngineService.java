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
package org.openo.holmes.rulemgt.bolt.enginebolt;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jvnet.hk2.annotations.Service;
import org.openo.holmes.common.config.MicroServiceConfig;
import org.openo.holmes.common.exception.CorrelationException;
import org.openo.holmes.common.utils.I18nProxy;
import org.openo.holmes.rulemgt.bean.request.CorrelationCheckRule4Engine;
import org.openo.holmes.rulemgt.bean.request.CorrelationDeployRule4Engine;
import org.openo.holmes.rulemgt.constant.RuleMgtConstant;

@Service
public class EngineService {

    protected HttpResponse delete(String packageName) throws IOException {
        return deleteRequest(MicroServiceConfig.getMsbServerAddr() + RuleMgtConstant.ENGINE_PATH + "/" + packageName);
    }

    protected HttpResponse check(CorrelationCheckRule4Engine correlationCheckRule4Engine)
            throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        String content = mapper.writeValueAsString(correlationCheckRule4Engine);
        return postRequest(MicroServiceConfig.getMsbServerAddr() + RuleMgtConstant.ENGINE_PATH, content);
    }

    protected HttpResponse deploy(CorrelationDeployRule4Engine correlationDeployRule4Engine) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        String content = mapper.writeValueAsString(correlationDeployRule4Engine);
        return putRequest(MicroServiceConfig.getMsbServerAddr() + RuleMgtConstant.ENGINE_PATH, content);
    }

    private HttpResponse postRequest(String url, String content) throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        try {
            HttpPost httpPost = new HttpPost(url);
            setHeader(httpPost);
            if (StringUtils.isNotEmpty(content)) {
                httpPost.setEntity(new ByteArrayEntity(content.getBytes()));
            }
            return httpClient.execute(httpPost);
        } finally {
            httpClient.close();
        }
    }

    private HttpResponse putRequest(String url, String content) throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        try {
            HttpPut httpPut = new HttpPut(url);
            setHeader(httpPut);
            if (StringUtils.isNotEmpty(content)) {
                httpPut.setEntity(new ByteArrayEntity(content.getBytes()));
            }
            return httpClient.execute(httpPut);
        } finally {
            httpClient.close();
        }
    }

    private HttpResponse deleteRequest(String url) throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        try {
            HttpDelete httpDelete = new HttpDelete(url);
            setHeader(httpDelete);
            return httpClient.execute(httpDelete);
        } finally {
            httpClient.close();
        }
    }

    private void setHeader(HttpRequestBase httpRequestBase) {
        httpRequestBase.setHeader("Content-Type", "text/html;charset=UTF-8");
        httpRequestBase.setHeader("Accept", "application/json");
        httpRequestBase.setHeader("Content-Type", "application/json");
    }

    public byte[] getData(HttpEntity httpEntity) throws IOException {
        BufferedHttpEntity bufferedHttpEntity = new BufferedHttpEntity(httpEntity);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bufferedHttpEntity.writeTo(byteArrayOutputStream);
        byte[] responseBytes = byteArrayOutputStream.toByteArray();
        return responseBytes;
    }

    public String getResponseContent(HttpResponse httpResponse) throws CorrelationException {
        byte[] dataByte;
        String result = null;
        try {
            HttpEntity httpEntity = httpResponse.getEntity();
            if (httpEntity != null) {
                byte[] responseBytes = getData(httpEntity);
                dataByte = responseBytes;
                result = bytesToString(dataByte);
            }
            return result;
        } catch (Exception e) {
            throw new CorrelationException(I18nProxy.RULE_MANAGEMENT_PARSE_DEPLOY_RESULT_ERROR, e);
        }
    }

    private String bytesToString(byte[] bytes) throws UnsupportedEncodingException {
        if (bytes != null) {
            String returnStr = new String(bytes, "utf-8");
            returnStr = StringUtils.trim(returnStr);
            return returnStr;
        }
        return null;
    }
}
