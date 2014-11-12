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

package com.matriks.protocol.mqtt.sampler;

import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.Interruptible;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.ThreadListener;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/*
* @author Ayhan Alkan
* @author Koray Sariteke
*/
public class MQTTSubscriberSampler extends AbstractSampler implements Interruptible, ThreadListener {

    private static final long serialVersionUID = 240L;

    private static final Logger log = LoggingManager.getLoggerForClass();

    private BlockingQueue<Message> msgQueue;

    private MqttClient mqttClient;
    private Random random = new Random();

    private static final Set<String> APPLIABLE_CONFIG_CLASSES = new HashSet<String>(
            Arrays.asList(new String[]{
                    "org.apache.jmeter.config.gui.LoginConfigGui",
                    "com.matriks.protocol.mqtt.config.gui.MqttConfigGui",
                    "org.apache.jmeter.config.gui.SimpleConfigGui"}));

    public static final String SERVER = "MQTTSampler.server"; // $NON-NLS-1$

    public static final String TOPICS = "MQTTSampler.topics"; // $NON-NLS-1$

    public MQTTSubscriberSampler() {
    }

    private static class Message {
        private String topic;
        private MqttMessage message;

        Message(String topic, MqttMessage message) {
            this.topic = topic;
            this.message = message;
        }

        public String getTopic() {
            return topic;
        }

        public MqttMessage getMessage() {
            return message;
        }
    }

    public String getUsername() {
        return getPropertyAsString(ConfigTestElement.USERNAME);
    }

    public String getPassword() {
        return getPropertyAsString(ConfigTestElement.PASSWORD);
    }

    public String getServer() {
        return getPropertyAsString(SERVER);
    }

    private String getTopics() {
        return getPropertyAsString(TOPICS);
    }

    /**
     * Returns a formatted string label describing this sampler Example output:
     *
     * @return a formatted string label describing this sampler
     */
    public String getLabel() {
        StringBuilder sb = new StringBuilder();
        //TODO implement sample label
        return sb.toString();
    }

    @Override
    public SampleResult sample(Entry e) {
        SampleResult res = null;
        if (null != msgQueue) {
            try {
                res = new SampleResult();
                res.sampleStart();
                final Message msg = msgQueue.take();
                res.setSampleLabel(msg.getTopic());
                res.setBytes(msg.getMessage().getPayload().length);
                res.setResponseOK();
                res.setSuccessful(true);
                res.sampleEnd();
            } catch (InterruptedException e1) {
                res = null;
                e1.printStackTrace();
            }
        }
        return res;
    }

    /**
     * @see org.apache.jmeter.samplers.AbstractSampler#applies(org.apache.jmeter.config.ConfigTestElement)
     */
    @Override
    public boolean applies(ConfigTestElement configElement) {
        String guiClass = configElement.getProperty(TestElement.GUI_CLASS).getStringValue();
        return APPLIABLE_CONFIG_CLASSES.contains(guiClass);
    }

    @Override
    public boolean interrupt() {
        threadFinished();
        return true;
    }

    @Override
    public void threadStarted() {
        log.info("connection preparation started...");
        MqttConnectOptions connectOptions = new MqttConnectOptions();
        connectOptions.setCleanSession(true);
        connectOptions.setConnectionTimeout(2000);
        connectOptions.setUserName(getUsername());
        connectOptions.setPassword(getPassword().toCharArray());
        try {

            log.info("url: " + getServer());
            String[] urlArr = getServer().split(";");

            mqttClient = new MqttClient(urlArr[random.nextInt(urlArr.length)], String.valueOf(random.nextLong() + System.nanoTime()));

            IMqttToken result = mqttClient.connectWithResult(connectOptions);
            if (result.isComplete() && null != result.getException()) {
                throw new MqttException(result.getException());
            }

            log.info("connection established to " + mqttClient.getServerURI() + " as " + mqttClient.getClientId());

            mqttClient.subscribe(getTopics().split(";"));
            msgQueue = new LinkedBlockingQueue<>();
            mqttClient.setCallback(new MqttClientCallback(msgQueue, mqttClient, connectOptions));
        } catch (MqttException e) {
            log.error("connection NOT ESTABLISHED!!! to " + mqttClient.getServerURI() + " with cause: " + e);
            try {
                Thread.sleep(TimeUnit.SECONDS.toMillis(3));
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            threadStarted();
        }
    }

    private static class MqttClientCallback implements MqttCallback {
        private final BlockingQueue<Message> msgQueue;
        private final MqttClient mqttClient;
        private final MqttConnectOptions connectOptions;

        public MqttClientCallback(BlockingQueue<Message> msgQueue, MqttClient mqttClient, MqttConnectOptions connectOptions) {
            this.msgQueue = msgQueue;
            this.mqttClient = mqttClient;
            this.connectOptions = connectOptions;
        }

        @Override
        public void connectionLost(Throwable throwable) {
            log.error("connection lost: " + mqttClient.getServerURI() + " with cause: " + throwable);

            try {
                log.info("retry to connected to server: " + mqttClient.getServerURI() + " with client id: " + mqttClient.getClientId());
                IMqttToken result = mqttClient.connectWithResult(connectOptions);
                if (result.isComplete() && null != result.getException()) {
                    throw new MqttException(result.getException());
                }
                log.info("connection established to server: " + mqttClient.getServerURI() + " with client id: " + mqttClient.getClientId());
            } catch (MqttException e) {
                connectionLost(e);
            }
        }

        @Override
        public void messageArrived(String mqttTopic, MqttMessage mqttMessage) throws Exception {
            msgQueue.put(new Message(mqttTopic, mqttMessage));
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
            //TODO
        }
    }

    @Override
    public void threadFinished() {
        try {
            msgQueue = null;
            if (null != mqttClient && mqttClient.isConnected()) {
                mqttClient.unsubscribe(getTopics().split(";"));
                mqttClient.disconnect();
                log.info("disconnected...");
            }
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}
