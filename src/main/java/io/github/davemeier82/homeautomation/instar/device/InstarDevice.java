package io.github.davemeier82.homeautomation.instar.device;

import io.github.davemeier82.homeautomation.core.device.DeviceType;
import io.github.davemeier82.homeautomation.core.device.mqtt.AbstractDevice;

import java.util.Map;

import static io.github.davemeier82.homeautomation.instar.device.InstarDeviceType.INSTAR_CAMERA;

public class InstarDevice extends AbstractDevice {
  private final String topic;

  public InstarDevice(String topic,
                      String displayName,
                      Map<String, String> customIdentifiers
  ) {
    super(displayName, customIdentifiers);
    this.topic = topic;
  }

  @Override
  public DeviceType getType() {
    return INSTAR_CAMERA;
  }

  @Override
  public String getId() {
    return topic;
  }

}