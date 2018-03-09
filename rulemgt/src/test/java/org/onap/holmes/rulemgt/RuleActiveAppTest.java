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

package org.onap.holmes.rulemgt;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
@Slf4j
public class RuleActiveAppTest {


    @Test
    public void getName() throws Exception {
        RuleActiveApp app = new RuleActiveApp();
        assertThat(app.getName(), equalTo("Holmes Rule Management ActiveApp APP "));
    }

    public static void main(String[] args) throws Exception {
        log.info("bbbb");
        String filePath = "F:\\code\\ONAP\\rule-management\\rulemgt-standalone\\src\\main\\assembly\\conf\\rulemgt.yml";
        new RuleActiveApp().run(new String[]{"server", filePath});
    }

    @Test
    public void test() {
        log.trace("==111111111=======trace ===================");

        log.debug("============22222===debug2222222====================");
        log.info("=====================33333333info333333333====================");
        log.warn(" =====================warn=444444444444===================");
        log.error(" =====================error====55555555555================");
    }
}