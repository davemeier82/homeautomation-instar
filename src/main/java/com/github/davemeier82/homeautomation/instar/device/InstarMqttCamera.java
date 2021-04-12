/*
 * Copyright 2021-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.davemeier82.homeautomation.instar.device;

import com.github.davemeier82.homeautomation.core.device.mqtt.MqttSubscriber;
import com.github.davemeier82.homeautomation.core.device.property.DeviceProperty;
import com.github.davemeier82.homeautomation.core.event.EventFactory;
import com.github.davemeier82.homeautomation.core.event.EventPublisher;
import com.github.davemeier82.homeautomation.instar.device.property.InstarCameraMotionSensor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static java.nio.charset.StandardCharsets.UTF_8;

public class InstarMqttCamera implements MqttSubscriber {
  private static final Logger log = LoggerFactory.getLogger(InstarMqttCamera.class);
  private static final String MQTT_TOPIC = "instar/";
  public static final String TYPE = "instar-camera";

  private final String id;
  private final InstarCameraMotionSensor motionSensor;
  private final String baseTopic;
  private String displayName;

  public InstarMqttCamera(String id, String displayName, EventPublisher eventPublisher, EventFactory eventFactory) {
    this.id = id;
    this.displayName = displayName;
    motionSensor = new InstarCameraMotionSensor(0, this, eventPublisher, eventFactory);
    baseTopic = MQTT_TOPIC + id + "/";
  }

  @Override
  public String getType() {
    return TYPE;
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public String getTopic() {
    return baseTopic + "#";
  }

  @Override
  public void processMessage(String topic, Optional<ByteBuffer> payload) {
    payload.ifPresent(byteBuffer -> {
      String message = UTF_8.decode(byteBuffer).toString();
      log.debug("{}: {}", topic, message);
      if (topic.startsWith(baseTopic + "status/alarm")) {
        motionSensor.setLastMotionDetected(ZonedDateTime.now());
      }
    });
  }

  @Override
  public String getDisplayName() {
    return displayName;
  }

  @Override
  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  @Override
  public List<? extends DeviceProperty> getDeviceProperties() {
    return List.of(motionSensor);
  }
}
