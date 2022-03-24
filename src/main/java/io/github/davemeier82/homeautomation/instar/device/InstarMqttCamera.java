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

package io.github.davemeier82.homeautomation.instar.device;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.davemeier82.homeautomation.core.device.mqtt.DefaultMqttSubscriber;
import io.github.davemeier82.homeautomation.core.device.property.DeviceProperty;
import io.github.davemeier82.homeautomation.core.device.property.defaults.DefaultMotionSensor;
import io.github.davemeier82.homeautomation.core.event.DataWithTimestamp;
import io.github.davemeier82.homeautomation.core.event.EventPublisher;
import io.github.davemeier82.homeautomation.core.event.factory.EventFactory;
import io.github.davemeier82.homeautomation.instar.InstarMqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.lang.Integer.parseInt;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Instar (https://www.instar.com/) camera with MTQQ support.
 *
 * @author David Meier
 * @since 0.1.0
 */
public class InstarMqttCamera extends DefaultMqttSubscriber {
  private static final Logger log = LoggerFactory.getLogger(InstarMqttCamera.class);
  public static final String MQTT_TOPIC = "instar";
  public static final String TYPE = "instar-camera";

  private final String id;
  private final DefaultMotionSensor motionSensor;
  private final String baseTopic;
  private final ObjectMapper objectMapper;

  /**
   * Constructor.
   *
   * @param id                device id (LAN MAC Address)
   * @param displayName       the display name
   * @param objectMapper      object mapper used to map MQTT message payload
   * @param eventPublisher    the event publisher
   * @param eventFactory      the event factory
   * @param customIdentifiers optional custom identifiers
   */
  public InstarMqttCamera(String id,
                          String displayName,
                          ObjectMapper objectMapper,
                          EventPublisher eventPublisher,
                          EventFactory eventFactory,
                          Map<String, String> customIdentifiers
  ) {
    super(displayName, customIdentifiers);
    this.id = id;
    this.objectMapper = objectMapper;
    motionSensor = new DefaultMotionSensor(0, this, eventPublisher, eventFactory);
    baseTopic = MQTT_TOPIC + "/" + id + "/";
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
      if (topic.startsWith(baseTopic + "status/alarm/triggered")) {
        try {
          InstarMqttMessage instarMqttMessage = objectMapper.readValue(message, InstarMqttMessage.class);
          motionSensor.setMotionDetected(new DataWithTimestamp<>(ZonedDateTime.now(), parseInt(instarMqttMessage.getVal()) > 0));
        } catch (JsonProcessingException e) {
          throw new UncheckedIOException(e);
        }
      }
    });
  }

  @Override
  public List<? extends DeviceProperty> getDeviceProperties() {
    return List.of(motionSensor);
  }
}
