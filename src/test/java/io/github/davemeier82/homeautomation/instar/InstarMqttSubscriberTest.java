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

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.davemeier82.homeautomation.core.device.DeviceId;
import io.github.davemeier82.homeautomation.core.device.property.DevicePropertyId;
import io.github.davemeier82.homeautomation.core.repositories.DeviceRepository;
import io.github.davemeier82.homeautomation.core.updater.MotionStateValueUpdateService;
import io.github.davemeier82.homeautomation.instar.device.InstarDevice;
import io.github.davemeier82.homeautomation.instar.device.InstarDeviceFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.ByteBuffer;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;

import static io.github.davemeier82.homeautomation.instar.device.InstarDeviceType.INSTAR_CAMERA;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InstarMqttSubscriberTest {

  @Mock
  InstarDeviceFactory instarDeviceFactory;
  @Mock
  DeviceRepository deviceRepository;
  @Mock
  MotionStateValueUpdateService motionStateValueUpdateService;
  @Spy
  ObjectMapper objectMapper = new ObjectMapper();
  @InjectMocks
  InstarMqttSubscriber mqttSubscriber;

  @Test
  void processMessage() {
    var message = """
        {"val":"7"}
        """;

    InstarDevice instarDevice = new InstarDevice("instar", INSTAR_CAMERA, "abc", Map.of());
    when(instarDeviceFactory.createDevice(INSTAR_CAMERA, "1234567890AB", "instar-camera-1234567890AB", Map.of(), Map.of())).thenReturn(Optional.of(instarDevice));

    mqttSubscriber.processMessage("instar/1234567890AB/status/alarm/triggered", Optional.of(ByteBuffer.wrap(message.getBytes(UTF_8))));

    verify(deviceRepository).save(instarDevice);
    verify(motionStateValueUpdateService).setValue(eq(true), any(OffsetDateTime.class), eq(new DevicePropertyId(new DeviceId("1234567890AB", INSTAR_CAMERA), "instar-camera-1234567890AB: motion")),
        eq("Motion State"));

  }

}