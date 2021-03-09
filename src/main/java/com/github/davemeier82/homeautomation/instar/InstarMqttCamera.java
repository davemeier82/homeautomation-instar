package com.github.davemeier82.homeautomation.instar;

import com.github.davemeier82.homeautomation.core.device.MotionSensor;
import com.github.davemeier82.homeautomation.core.device.mqtt.MqttCamera;
import com.github.davemeier82.homeautomation.core.event.EventFactory;
import com.github.davemeier82.homeautomation.core.event.EventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.time.ZonedDateTime;
import java.util.Optional;

import static java.nio.charset.StandardCharsets.UTF_8;

public class InstarMqttCamera implements MqttCamera, MotionSensor {
  private static final Logger log = LoggerFactory.getLogger(InstarMqttCamera.class);
  private static final String MQTT_TOPIC = "instar/";
  public static final String TYPE = "instar/camera";

  private final String id;
  private final EventPublisher eventPublisher;
  private final EventFactory eventFactory;
  private final String baseTopic;
  private String displayName;

  public InstarMqttCamera(String id, String displayName, EventPublisher eventPublisher, EventFactory eventFactory) {
    this.id = id;
    this.displayName = displayName;
    this.eventPublisher = eventPublisher;
    this.eventFactory = eventFactory;
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
        eventPublisher.publishEvent(eventFactory.createMotionDetectedEvent(this, ZonedDateTime.now()));
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
}
