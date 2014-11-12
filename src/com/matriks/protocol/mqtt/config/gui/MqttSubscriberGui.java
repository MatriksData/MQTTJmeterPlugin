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

package com.matriks.protocol.mqtt.config.gui;

import com.matriks.protocol.mqtt.sampler.MQTTSubscriberSampler;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.config.gui.AbstractConfigGui;
import org.apache.jmeter.gui.util.HorizontalPanel;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;

import javax.swing.*;
import java.awt.*;

/*
* @author Ayhan Alkan
* @author Koray Sariteke
*/
public class MqttSubscriberGui extends AbstractConfigGui {

    private static final long serialVersionUID = 240L;

    private JTextField server;

    private JTextField topics;

    private boolean displayName = true;

    public MqttSubscriberGui() {
        this(true);
    }

    public MqttSubscriberGui(boolean displayName) {
        this.displayName = displayName;
        init();
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
        return null; //TODO
    }

    @Override
    public void configure(TestElement element) {
        super.configure(element); // TODO - should this be done for embedded usage?
        // Note: the element is a ConfigTestElement when used standalone, so we cannot use FTPSampler access methods
        server.setText(element.getPropertyAsString(MQTTSubscriberSampler.SERVER));
        topics.setText(element.getPropertyAsString(MQTTSubscriberSampler.TOPICS));
    }

    @Override
    public TestElement createTestElement() {
        ConfigTestElement element = new ConfigTestElement();
        modifyTestElement(element);
        return element;
    }

    /**
     * Modifies a given TestElement to mirror the data in the gui components.
     *
     * @see org.apache.jmeter.gui.JMeterGUIComponent#modifyTestElement(org.apache.jmeter.testelement.TestElement)
     */
    @Override
    public void modifyTestElement(TestElement element) {
        configureTestElement(element);
        // Note: the element is a ConfigTestElement, so cannot use FTPSampler access methods
        element.setProperty(MQTTSubscriberSampler.SERVER,server.getText());
        element.setProperty(MQTTSubscriberSampler.TOPICS, topics.getText());
    }

    /**
     * Implements JMeterGUIComponent.clearGui
     */
    @Override
    public void clearGui() {
        super.clearGui();

        server.setText(""); //$NON-NLS-1$
        topics.setText(""); //$NON-NLS-1$
    }

    private JPanel createServerPanel() {
        JLabel label = new JLabel(JMeterUtils.getResString("server")); //$NON-NLS-1$

        server = new JTextField(10);
        label.setLabelFor(server);

        JPanel serverPanel = new JPanel(new BorderLayout(5, 0));
        serverPanel.add(label, BorderLayout.WEST);
        serverPanel.add(server, BorderLayout.CENTER);
        return serverPanel;
    }

    private JPanel createTopicsPanel() {
        JLabel label = new JLabel("MQTT Topics"); //$NON-NLS-1$

        topics = new JTextField();
        label.setLabelFor(topics);

        JPanel contentsPanel = new JPanel(new BorderLayout(5, 0));
        contentsPanel.add(label, BorderLayout.WEST);
        contentsPanel.add(topics, BorderLayout.CENTER);
        return contentsPanel;
    }

    private void init() {
        setLayout(new BorderLayout(0, 5));

        if (displayName) {
            setBorder(makeBorder());
            add(makeTitlePanel(), BorderLayout.NORTH);
        }

        // MAIN PANEL
        VerticalPanel mainPanel = new VerticalPanel();
        JPanel serverPanel = new HorizontalPanel();
        serverPanel.add(createServerPanel(), BorderLayout.CENTER);
        mainPanel.add(serverPanel);
        mainPanel.add(createTopicsPanel());

        add(mainPanel, BorderLayout.CENTER);
    }
}
