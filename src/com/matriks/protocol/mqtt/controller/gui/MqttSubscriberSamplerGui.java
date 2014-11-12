/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.matriks.protocol.mqtt.controller.gui;

import com.matriks.protocol.mqtt.config.gui.MqttSubscriberGui;
import com.matriks.protocol.mqtt.sampler.MQTTSubscriberSampler;
import org.apache.jmeter.config.gui.LoginConfigGui;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;

import javax.swing.*;
import java.awt.*;

/*
* @author Ayhan Alkan
* @author Koray Sariteke
*/
public class MqttSubscriberSamplerGui extends AbstractSamplerGui {
    private static final long serialVersionUID = 240L;

    private LoginConfigGui loginPanel;

    private MqttSubscriberGui mqttDefaultPanel;

    public MqttSubscriberSamplerGui() {
        init();
    }

    @Override
    public void configure(TestElement element) {
        super.configure(element);
        loginPanel.configure(element);
        mqttDefaultPanel.configure(element);
    }

    @Override
    public TestElement createTestElement() {
        MQTTSubscriberSampler sampler = new MQTTSubscriberSampler();
        modifyTestElement(sampler);
        return sampler;
    }

    /**
     * Modifies a given TestElement to mirror the data in the gui components.
     *
     * @see org.apache.jmeter.gui.JMeterGUIComponent#modifyTestElement(org.apache.jmeter.testelement.TestElement)
     */
    @Override
    public void modifyTestElement(TestElement sampler) {
        sampler.clear();
        mqttDefaultPanel.modifyTestElement(sampler);
        loginPanel.modifyTestElement(sampler);
        this.configureTestElement(sampler);
    }

    /**
     * Implements JMeterGUIComponent.clearGui
     */
    @Override
    public void clearGui() {
        super.clearGui();

        mqttDefaultPanel.clearGui();
        loginPanel.clearGui();
    }
    @Override
    public String getStaticLabel() {
        return "MQTT Subscribe Request";
    }

    @Override
    public String getDocAnchor() {
        return "MQTT Subscribe Request";
    }

    @Override
    public String getName() {
        return "MQTT Subscribe Request";
    }

    @Override
    public String getLabelResource() {
        return null; // $NON-NLS-1$
    }

    private void init() {
        setLayout(new BorderLayout(0, 5));
        setBorder(makeBorder());

        add(makeTitlePanel(), BorderLayout.NORTH);

        VerticalPanel mainPanel = new VerticalPanel();

        mqttDefaultPanel = new MqttSubscriberGui(false);
        mainPanel.add(mqttDefaultPanel);

        loginPanel = new LoginConfigGui(false);
        loginPanel.setBorder(BorderFactory.createTitledBorder(JMeterUtils.getResString("login_config"))); // $NON-NLS-1$
        mainPanel.add(loginPanel);

        add(mainPanel, BorderLayout.CENTER);
    }
}
