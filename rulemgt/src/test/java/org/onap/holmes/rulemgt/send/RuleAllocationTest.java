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

package org.onap.holmes.rulemgt.send;


import org.junit.Test;
import org.powermock.api.easymock.PowerMock;
import org.powermock.reflect.Whitebox;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class RuleAllocationTest {

    private RuleAllocation ruleAllocation = new RuleAllocation();
    @Test
    public void extendCompareIpTest() throws Exception{
        List<String> newList = new ArrayList<>();
        newList.add("10.96.33.34");
        newList.add("10.74.65.24");

        List<String> oldList = new ArrayList<>();
        oldList.add("10.96.33.34");
        List<String> extendIp = Whitebox.invokeMethod(ruleAllocation,"extendCompareIp",newList,oldList);

        PowerMock.verifyAll();

        assertThat(extendIp.get(0),equalTo("10.74.65.24"));
    }

    @Test
    public void destroyCompareIpTest() throws Exception{
        List<String> newList = new ArrayList<>();
        newList.add("10.96.33.34");

        List<String> oldList = new ArrayList<>();
        oldList.add("10.96.33.34");
        oldList.add("10.74.65.24");
        List<String> destoryIp = Whitebox.invokeMethod(ruleAllocation,"destroyCompareIp",newList,oldList);

        PowerMock.verifyAll();

        assertThat(destoryIp.get(0),equalTo("10.74.65.24"));
    }

}
