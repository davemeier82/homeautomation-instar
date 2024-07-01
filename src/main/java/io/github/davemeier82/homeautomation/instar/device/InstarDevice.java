package io.github.davemeier82.homeautomation.instar.device;

import io.github.davemeier82.homeautomation.core.device.DeviceType;
import io.github.davemeier82.homeautomation.core.device.mqtt.AbstractDevice;

import java.util.Map;

public class InstarDevice extends AbstractDevice {
  private final String id;
  private final DeviceType type;

  public InstarDevice(String id,
                      DeviceType type,
                      String displayName,
                      Map<String, String> customIdentifiers
  ) {
    super(displayName, customIdentifiers);
    this.id = id;
    this.type = type;
  }

  @Override
  public DeviceType getType() {
    return type;
  }

  @Override
  public String getId() {
    return id;
  }

}