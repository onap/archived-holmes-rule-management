/**
 * Copyright 2017-2022 ZTE Corporation.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.onap.holmes.rulemgt.resources;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.SwaggerDefinition;
import jakarta.ws.rs.core.MediaType;
import lombok.extern.slf4j.Slf4j;
import org.onap.holmes.common.exception.CorrelationException;
import org.onap.holmes.common.utils.ExceptionUtil;
import org.onap.holmes.common.utils.GsonUtil;
import org.onap.holmes.common.utils.UserUtil;
import org.onap.holmes.rulemgt.bean.request.RuleCreateRequest;
import org.onap.holmes.rulemgt.bean.request.RuleDeleteRequest;
import org.onap.holmes.rulemgt.bean.request.RuleQueryCondition;
import org.onap.holmes.rulemgt.bean.request.RuleUpdateRequest;
import org.onap.holmes.rulemgt.bean.response.RuleAddAndUpdateResponse;
import org.onap.holmes.rulemgt.bean.response.RuleQueryListResponse;
import org.onap.holmes.rulemgt.constant.RuleMgtConstant;
import org.onap.holmes.rulemgt.wrapper.RuleMgtWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController
@SwaggerDefinition
@RequestMapping("/rule")
@Api(tags = {"Holmes Rule Management"})
public class RuleMgtResources {

    @Autowired
    private RuleMgtWrapper ruleMgtWrapper;

    @ResponseBody
    @PutMapping(produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Save a rule into the database; deploy it to the Drools engine if it is enabled.",
            response = RuleAddAndUpdateResponse.class)
    public RuleAddAndUpdateResponse addCorrelationRule(HttpServletRequest request,
               @ApiParam(value =
                       "The request entity of the HTTP call, which comprises \"ruleName\"(required), "
                               + "\"loopControlName\"(required), \"content\"(required), \"enabled\"(required) "
                               + "and \"description\"(optional)", required = true)
               @RequestBody RuleCreateRequest ruleCreateRequest) {
        RuleAddAndUpdateResponse ruleChangeResponse;
        try {
            ruleChangeResponse = ruleMgtWrapper
                    .addCorrelationRule(UserUtil.getUserName(request), ruleCreateRequest);
            return ruleChangeResponse;
        } catch (CorrelationException e) {
            log.error(String.format("failed to create the rule: %s", ruleCreateRequest.getRuleName()), e);
            throw ExceptionUtil.buildExceptionResponse(e.getMessage());
        }
    }

    @ResponseBody
    @PostMapping(produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Update an existing rule; deploy it to the Drools engine if it is enabled.", response = RuleAddAndUpdateResponse.class)
    public RuleAddAndUpdateResponse updateCorrelationRule(HttpServletRequest request,
              @ApiParam(value =
                      "The request entity of the HTTP call, which comprises \"ruleId\"(required), "
                              + "\"content\"(required), \"enabled\"(required) and \"description\"(optional)", required = true)
              @RequestBody RuleUpdateRequest ruleUpdateRequest) {
        RuleAddAndUpdateResponse ruleChangeResponse;
        try {
            ruleChangeResponse = ruleMgtWrapper
                    .updateCorrelationRule(UserUtil.getUserName(request), ruleUpdateRequest);
            log.info("update rule:" + ruleUpdateRequest.getRuleId() + " successful");
            return ruleChangeResponse;
        } catch (CorrelationException e) {
            log.error(String.format("failed to update the rule: %s", ruleUpdateRequest.getRuleId()), e);
            throw ExceptionUtil.buildExceptionResponse(e.getMessage());
        }
    }

    @DeleteMapping("/{ruleid}")
    @ApiOperation(value = "Remove a rule from Holmes.")
    public ResponseEntity deleteCorrelationRule(@PathVariable("ruleid") String ruleId) {
        try {
            ruleMgtWrapper.deleteCorrelationRule(new RuleDeleteRequest(ruleId));
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (CorrelationException e) {
            log.error(String.format("failed to delete the rule: %s", ruleId), e);
            throw ExceptionUtil.buildExceptionResponse(e.getMessage());
        }
    }

    @ResponseBody
    @GetMapping(produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Query rules using certain criteria.", response = RuleQueryListResponse.class)
    public RuleQueryListResponse getCorrelationRules(
             @ApiParam(value =
                     "A JSON string used as a query parameter, which comprises \"ruleid\"(optional), "
                             + "\"rulename\"(optional), \"creator\"(optional), "
                             + "\"modifier\"(optional) and \"enabled\"(optional). E.g. {\"ruleid\":\"rule_1484727187317\"}")
             @RequestParam(value = "queryrequest", required = false) String ruleQueryRequest) {
        RuleQueryListResponse ruleQueryListResponse;

        RuleQueryCondition ruleQueryCondition = getRuleQueryCondition(ruleQueryRequest);
        try {
            ruleQueryListResponse = ruleMgtWrapper
                    .getCorrelationRuleByCondition(ruleQueryCondition);
            return ruleQueryListResponse;
        } catch (CorrelationException e) {
            log.error(String.format("failed to query the rule: %s", ruleQueryCondition.getName()), e);
            throw ExceptionUtil.buildExceptionResponse(e.getMessage());
        }
    }

    private RuleQueryCondition getRuleQueryCondition(String queryRequest) {
        RuleQueryCondition ruleQueryCondition = GsonUtil
                .jsonToBean(queryRequest, RuleQueryCondition.class);
        if (queryRequest == null) {
            if (ruleQueryCondition == null) {
                ruleQueryCondition = new RuleQueryCondition();
            }
            ruleQueryCondition.setEnabled(RuleMgtConstant.STATUS_RULE_ALL);
        } else if (queryRequest.indexOf("enabled") == -1) {
            ruleQueryCondition.setEnabled(RuleMgtConstant.STATUS_RULE_ALL);
        }
        return ruleQueryCondition;
    }
}
