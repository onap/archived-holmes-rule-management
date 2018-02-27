/**
 * Copyright 2017 ZTE Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onap.holmes.rulemgt.resources;

import com.codahale.metrics.annotation.Timed;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.SwaggerDefinition;
import java.util.Locale;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.jvnet.hk2.annotations.Service;
import org.onap.holmes.common.exception.CorrelationException;
import org.onap.holmes.common.utils.ExceptionUtil;
import org.onap.holmes.common.utils.GsonUtil;
import org.onap.holmes.common.utils.LanguageUtil;
import org.onap.holmes.common.utils.UserUtil;
import org.onap.holmes.rulemgt.bean.request.RuleCreateRequest;
import org.onap.holmes.rulemgt.bean.request.RuleDeleteRequest;
import org.onap.holmes.rulemgt.bean.request.RuleQueryCondition;
import org.onap.holmes.rulemgt.bean.request.RuleUpdateRequest;
import org.onap.holmes.rulemgt.bean.response.RuleAddAndUpdateResponse;
import org.onap.holmes.rulemgt.bean.response.RuleQueryListResponse;
import org.onap.holmes.rulemgt.constant.RuleMgtConstant;
import org.onap.holmes.rulemgt.wrapper.RuleMgtWrapper;

@Service
@SwaggerDefinition
@Path("/rule")
@Api(tags = {"Holmes Rule Management"})
@Produces(MediaType.APPLICATION_JSON)
@Slf4j
public class RuleMgtResources {

    @Inject
    private RuleMgtWrapper ruleMgtWrapper;

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Save a rule into the database; deploy it to the Drools engine if it is enabled.",
            response = RuleAddAndUpdateResponse.class)
    @Timed
    public RuleAddAndUpdateResponse addCorrelationRule(@Context HttpServletRequest request,
            @ApiParam(value =
                    "The request entity of the HTTP call, which comprises \"rulename\"(required), "
                            + "\"loopcontrolname\"(required), \"content\"(required), \"enabled\"(required) "
                            + "and \"description\"(optional)", required = true)
                    RuleCreateRequest ruleCreateRequest) {
        Locale locale = LanguageUtil.getLocale(request);
        RuleAddAndUpdateResponse ruleChangeResponse;
        try {
            ruleChangeResponse = ruleMgtWrapper
                    .addCorrelationRule(UserUtil.getUserName(request), ruleCreateRequest);
            log.info("create rule:" + ruleCreateRequest.getRuleName() + " success.");
            return ruleChangeResponse;
        } catch (CorrelationException e) {
            log.error("create rule:" + ruleCreateRequest.getRuleName() + " failed", e);
            throw ExceptionUtil.buildExceptionResponse(e.getMessage());
        }
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Update an existing rule; deploy it to the Drools engine if it is enabled.", response = RuleAddAndUpdateResponse.class)
    @Timed
    public RuleAddAndUpdateResponse updateCorrelationRule(@Context HttpServletRequest request,
            @ApiParam(value =
                    "The request entity of the HTTP call, which comprises \"ruleid\"(required), "
                            + "\"content\"(required), \"enabled\"(required) and \"description\"(optional)", required = true)
                    RuleUpdateRequest ruleUpdateRequest) {
        Locale locale = LanguageUtil.getLocale(request);
        RuleAddAndUpdateResponse ruleChangeResponse;
        try {
            ruleChangeResponse = ruleMgtWrapper
                    .updateCorrelationRule(UserUtil.getUserName(request), ruleUpdateRequest);
            log.info("update rule:" + ruleUpdateRequest.getRuleId() + " successful");
            return ruleChangeResponse;
        } catch (CorrelationException e) {
            log.error("update rule:" + ruleUpdateRequest.getContent() + " failed", e);
            throw ExceptionUtil.buildExceptionResponse(e.getMessage());
        }
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Remove a rule from Holmes.")
    @Timed
    @Path("/{ruleid}")
    public boolean deleteCorrelationRule(@Context HttpServletRequest request,
            @PathParam("ruleid") String ruleId) {
        Locale locale = LanguageUtil.getLocale(request);
        try {
            ruleMgtWrapper.deleteCorrelationRule(new RuleDeleteRequest(ruleId));
            log.info("delete rule:" + ruleId + " successful");
            return true;
        } catch (CorrelationException e) {
            log.error("delete rule:" + ruleId + " failed", e);
            throw ExceptionUtil.buildExceptionResponse(e.getMessage());
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Query rules using certain criteria.", response = RuleQueryListResponse.class)
    @Timed
    public RuleQueryListResponse getCorrelationRules(@Context HttpServletRequest request,
            @ApiParam(value =
                    "A JSON string used as a query parameter, which comprises \"ruleid\"(optional), "
                            + "\"rulename\"(optional), \"creator\"(optional), "
                            + "\"modifier\"(optional) and \"enabled\"(optional). E.g. {\"ruleid\":\"rule_1484727187317\"}",
                    required = false) @QueryParam("queryrequest") String ruleQueryRequest) {
        Locale locale = LanguageUtil.getLocale(request);
        RuleQueryListResponse ruleQueryListResponse;
        RuleQueryCondition ruleQueryCondition = getRuleQueryCondition(ruleQueryRequest, request);
        try {
            ruleQueryListResponse = ruleMgtWrapper
                    .getCorrelationRuleByCondition(ruleQueryCondition);
            log.info("query rule successful by condition:" + JSONObject
                    .fromObject(ruleQueryCondition));
            return ruleQueryListResponse;
        } catch (CorrelationException e) {
            log.error("query rule failed,cause query condition conversion failure", e);
            throw ExceptionUtil.buildExceptionResponse(e.getMessage());
        }
    }

    private RuleQueryCondition getRuleQueryCondition(String queryRequest,
            HttpServletRequest request) {
        Locale locale = LanguageUtil.getLocale(request);

        RuleQueryCondition ruleQueryCondition = GsonUtil.jsonToBean(queryRequest, RuleQueryCondition.class);
        if (queryRequest == null) {
            ruleQueryCondition.setEnabled(RuleMgtConstant.STATUS_RULE_ALL);
        } else if (queryRequest.indexOf("enabled") == -1) {
            ruleQueryCondition.setEnabled(RuleMgtConstant.STATUS_RULE_ALL);
        }
        return ruleQueryCondition;
    }
}
