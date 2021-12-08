/**
 * Copyright 2021 ZTE Corporation.
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

package org.onap.holmes.rulemgt.dcae;

import org.easymock.EasyMock;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.holmes.common.ConfigFileScanner;
import org.onap.holmes.common.utils.FileUtils;
import org.onap.holmes.common.utils.JerseyClient;
import org.onap.holmes.rulemgt.bean.response.RuleQueryListResponse;
import org.onap.holmes.rulemgt.bean.response.RuleResult4API;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(PowerMockRunner.class)
@PrepareForTest({JerseyClient.class})
@SuppressStaticInitializationFor({"org.onap.holmes.common.utils.JerseyClient"})
public class ConfigFileScanningTaskTest {

    @Test
    public void run_add_rules() throws Exception {
        System.setProperty("ENABLE_ENCRYPT", "true");

        String clName = "ControlLoop-VOLTE-2179b738-fd36-4843-a71a-a8c24c70c55b";
        String indexPath = getFilePath("index-add.json");
        String contents = FileUtils.readTextFile(indexPath);

        ConfigFileScanningTask cfst = new ConfigFileScanningTask(null);
        Whitebox.setInternalState(cfst, "configFile", indexPath);

        // mock for getExistingRules
        JerseyClient jcMock = PowerMock.createMock(JerseyClient.class);
        PowerMock.expectNew(JerseyClient.class).andReturn(jcMock).anyTimes();
        RuleQueryListResponse rqlr = new RuleQueryListResponse();
        rqlr.getCorrelationRules().add(getRuleResult4API(clName, contents));
        EasyMock.expect(jcMock.get(EasyMock.anyString(), EasyMock.anyObject())).andReturn(rqlr);

        // mock for deployRule
        EasyMock.expect(jcMock.header(EasyMock.anyString(), EasyMock.anyObject())).andReturn(jcMock);
        EasyMock.expect(jcMock.put(EasyMock.anyString(), EasyMock.anyObject())).andReturn("");

        PowerMock.replayAll();
        cfst.run();
        PowerMock.verifyAll();

        Map<String, String> config = Whitebox.getInternalState(cfst, "configInEffect");
        assertThat(config.size(), is(1));

        System.clearProperty("ENABLE_ENCRYPT");
    }

    @Test
    public void run_remove_rules_normal() throws Exception {
        System.setProperty("ENABLE_ENCRYPT", "false");

        String clName = "ControlLoop-VOLTE-2179b738-fd36-4843-a71a-a8c24c70c55b";
        String indexPath = getFilePath("index-add.json");
        String contents = FileUtils.readTextFile(indexPath);
        Map<String, String> configInEffect = new HashMap<>();
        configInEffect.put(clName, contents);

        ConfigFileScanningTask cfst = new ConfigFileScanningTask(new ConfigFileScanner());
        Whitebox.setInternalState(cfst, "configFile", getFilePath("index-empty.json"));
        Whitebox.setInternalState(cfst, "configInEffect", configInEffect);

        // mock for getExistingRules
        JerseyClient jcMock = PowerMock.createMock(JerseyClient.class);
        PowerMock.expectNew(JerseyClient.class).andReturn(jcMock).anyTimes();
        RuleQueryListResponse rqlr = new RuleQueryListResponse();
        rqlr.getCorrelationRules().add(getRuleResult4API(clName, contents));
        EasyMock.expect(jcMock.get(EasyMock.anyString(), EasyMock.anyObject())).andReturn(rqlr);

        // mock for deleteRule
        EasyMock.expect(jcMock.delete(EasyMock.anyString())).andReturn("");

        PowerMock.replayAll();
        cfst.run();
        PowerMock.verifyAll();

        Map<String, String> config = Whitebox.getInternalState(cfst, "configInEffect");
        assertThat(config.size(), is(0));

        System.clearProperty("ENABLE_ENCRYPT");
    }

    @Test
    public void run_remove_rules_delete_null_pointer() throws Exception {
        String clName = "ControlLoop-VOLTE-2179b738-fd36-4843-a71a-a8c24c70c55b";
        String indexPath = getFilePath("index-add.json");
        String contents = FileUtils.readTextFile(indexPath);
        Map<String, String> configInEffect = new HashMap<>();
        configInEffect.put(clName, contents);

        ConfigFileScanningTask cfst = new ConfigFileScanningTask(new ConfigFileScanner());
        Whitebox.setInternalState(cfst, "configFile", indexPath);
        Whitebox.setInternalState(cfst, "configInEffect", configInEffect);

        // mock for getExistingRules
        JerseyClient jcMock = PowerMock.createMock(JerseyClient.class);
        PowerMock.expectNew(JerseyClient.class).andReturn(jcMock).anyTimes();
        RuleQueryListResponse rqlr = new RuleQueryListResponse();
        rqlr.getCorrelationRules().add(getRuleResult4API("a-non-existing-rule", contents));
        EasyMock.expect(jcMock.get(EasyMock.anyString(), EasyMock.anyObject())).andReturn(rqlr);

        PowerMock.replayAll();
        cfst.run();
        PowerMock.verifyAll();

        Map<String, String> config = Whitebox.getInternalState(cfst, "configInEffect");
        assertThat(config.size(), is(1));
    }

    @Test
    public void run_remove_rules_api_calling_returning_null() throws Exception {
        String clName = "ControlLoop-VOLTE-2179b738-fd36-4843-a71a-a8c24c70c55b";
        String indexPath = getFilePath("index-add.json");
        String contents = FileUtils.readTextFile(indexPath);
        Map<String, String> configInEffect = new HashMap<>();
        configInEffect.put(clName, contents);

        ConfigFileScanningTask cfst = new ConfigFileScanningTask(new ConfigFileScanner());
        Whitebox.setInternalState(cfst, "configFile", indexPath);
        Whitebox.setInternalState(cfst, "configInEffect", configInEffect);

        // mock for getExistingRules
        JerseyClient jcMock = PowerMock.createMock(JerseyClient.class);
        PowerMock.expectNew(JerseyClient.class).andReturn(jcMock).anyTimes();
        RuleQueryListResponse rqlr = new RuleQueryListResponse();
        rqlr.getCorrelationRules().add(getRuleResult4API(clName, contents));
        EasyMock.expect(jcMock.get(EasyMock.anyString(), EasyMock.anyObject())).andReturn(rqlr);

        // mock for deleteRule
        EasyMock.expect(jcMock.delete(EasyMock.anyString())).andReturn(null);

        PowerMock.replayAll();
        cfst.run();
        PowerMock.verifyAll();

        Map<String, String> config = Whitebox.getInternalState(cfst, "configInEffect");
        assertThat(config.size(), is(1));
    }

    @Test
    public void run_change_rules_normal() throws Exception {
        String clName = "ControlLoop-VOLTE-2179b738-fd36-4843-a71a-a8c24c70c55b";
        String oldDrlPath = getFilePath("ControlLoop-VOLTE-2179b738-fd36-4843-a71a-a8c24c70c55b.drl");
        String oldDrlContents = FileUtils.readTextFile(oldDrlPath);
        Map<String, String> configInEffect = new HashMap<>();
        configInEffect.put(clName, oldDrlContents);

        ConfigFileScanningTask cfst = new ConfigFileScanningTask(new ConfigFileScanner());
        Whitebox.setInternalState(cfst, "configFile", getFilePath("index-rule-changed.json"));
        Whitebox.setInternalState(cfst, "configInEffect", configInEffect);

        // mock for getExistingRules
        JerseyClient jcMock = PowerMock.createMock(JerseyClient.class);
        PowerMock.expectNew(JerseyClient.class).andReturn(jcMock).anyTimes();
        RuleQueryListResponse rqlr = new RuleQueryListResponse();
        rqlr.getCorrelationRules().add(getRuleResult4API(clName, oldDrlContents));
        EasyMock.expect(jcMock.get(EasyMock.anyString(), EasyMock.anyObject())).andReturn(rqlr);

        // mock for deleteRule
        EasyMock.expect(jcMock.delete(EasyMock.anyString())).andReturn("");

        // mock for deployRule
        EasyMock.expect(jcMock.header(EasyMock.anyString(), EasyMock.anyObject())).andReturn(jcMock);
        EasyMock.expect(jcMock.put(EasyMock.anyString(), EasyMock.anyObject())).andReturn("");

        PowerMock.replayAll();
        cfst.run();
        PowerMock.verifyAll();

        Map<String, String> config = Whitebox.getInternalState(cfst, "configInEffect");
        assertThat(config.size(), is(1));
        assertThat(config.get(clName),
                equalTo(FileUtils.readTextFile(
                        getFilePath("ControlLoop-VOLTE-2179b738-fd36-4843-a71a-a8c24c70c55b-changed.drl"))));
    }

    @Test
    public void run_change_rules_no_change_except_for_spaces() throws Exception {
        String clName = "ControlLoop-VOLTE-2179b738-fd36-4843-a71a-a8c24c70c55b";
        String oldDrlPath = getFilePath("ControlLoop-VOLTE-2179b738-fd36-4843-a71a-a8c24c70c55b.drl");
        String oldDrlContents = FileUtils.readTextFile(oldDrlPath);
        Map<String, String> configInEffect = new HashMap<>();
        configInEffect.put(clName, oldDrlContents);

        ConfigFileScanningTask cfst = new ConfigFileScanningTask(new ConfigFileScanner());
        Whitebox.setInternalState(cfst, "configFile", getFilePath("index-rule-spaces-test.json"));
        Whitebox.setInternalState(cfst, "configInEffect", configInEffect);

        // mock for getExistingRules
        JerseyClient jcMock = PowerMock.createMock(JerseyClient.class);
        PowerMock.expectNew(JerseyClient.class).andReturn(jcMock).anyTimes();
        RuleQueryListResponse rqlr = new RuleQueryListResponse();
        rqlr.getCorrelationRules().add(getRuleResult4API(clName, oldDrlContents));
        EasyMock.expect(jcMock.get(EasyMock.anyString(), EasyMock.anyObject())).andReturn(rqlr);

        PowerMock.replayAll();
        cfst.run();
        PowerMock.verifyAll();

        Map<String, String> config = Whitebox.getInternalState(cfst, "configInEffect");
        assertThat(config.size(), is(1));
        assertThat(config.get(clName),
                equalTo(FileUtils.readTextFile(
                        getFilePath("ControlLoop-VOLTE-2179b738-fd36-4843-a71a-a8c24c70c55b.drl"))));
    }

    private String getFilePath(String fileName) {
        return ConfigFileScanningTaskTest.class.getResource("/" + fileName).getFile();
    }

    private RuleResult4API getRuleResult4API(String clName, String contents) {
        RuleResult4API ruleResult4API = new RuleResult4API();
        ruleResult4API.setRuleId(clName);
        ruleResult4API.setRuleName(clName);
        ruleResult4API.setLoopControlName(clName);
        ruleResult4API.setContent(contents);
        ruleResult4API.setDescription("");
        ruleResult4API.setEnabled(1);
        return ruleResult4API;
    }


}