/*
 * Copyright 2021-2024 the original author or authors.
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

package io.github.davemeier82.homeautomation.instar;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.davemeier82.homeautomation.core.device.DeviceId;
import io.github.davemeier82.homeautomation.core.device.mqtt.MqttSubscriber;
import io.github.davemeier82.homeautomation.core.device.property.DevicePropertyId;
import io.github.davemeier82.homeautomation.core.updater.MotionStateValueUpdateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.time.OffsetDateTime;
import java.util.Optional;

import static io.github.davemeier82.homeautomation.instar.device.InstarDeviceType.INSTAR_CAMERA;
import static java.lang.Integer.parseInt;
import static java.nio.charset.StandardCharsets.UTF_8;

public class InstarMqttSubscriber implements MqttSubscriber {
  public static final String MQTT_TOPIC = "instar";
  private static final Logger log = LoggerFactory.getLogger(InstarMqttSubscriber.class);
  private final ObjectMapper objectMapper;
  private final MotionStateValueUpdateService motionStateValueUpdateService;

  public InstarMqttSubscriber(ObjectMapper objectMapper,
                              MotionStateValueUpdateService motionStateValueUpdateService
  ) {
    this.objectMapper = objectMapper;
    this.motionStateValueUpdateService = motionStateValueUpdateService;
  }

  @Override
  public String getTopic() {
    return MQTT_TOPIC + "/#";
  }

  @Override
  public void processMessage(String topic, Optional<ByteBuffer> payload) {
    String[] topicParts = topic.split("/");
    if (topicParts.length < 4 || !topicParts[2].equals("status") || !topicParts[3].equals("alarm")) {
      log.debug("ignoring message for topic {}", topic);
      return;
    }
    payload.ifPresent(byteBuffer -> {
      String message = UTF_8.decode(byteBuffer).toString();
      log.debug("{}: {}", topic, message);
      DeviceId deviceId = new DeviceId(topicParts[1], INSTAR_CAMERA);
      try {
        InstarAlarmStatusMessage instarAlarmStatusMessage = objectMapper.readValue(message, InstarAlarmStatusMessage.class);
        motionStateValueUpdateService.setValue(parseInt(instarAlarmStatusMessage.getVal()) > 0, OffsetDateTime.now(), new DevicePropertyId(deviceId, "motion"), "Motion State");

      } catch (JsonProcessingException e) {
        throw new UncheckedIOException(e);
      }
    });
  }

}
