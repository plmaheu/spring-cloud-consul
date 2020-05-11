/*
 * Copyright 2013-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.consul.test;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;

public class ConsulTestcontainers
		implements ApplicationContextInitializer<ConfigurableApplicationContext> {

	static final Logger logger = LoggerFactory.getLogger(ConsulTestcontainers.class);

	public static GenericContainer<?> consul = new GenericContainer<>("consul:1.7.2")
			.waitingFor(Wait.forHttp("/v1/status/leader")).withExposedPorts(8500)
			.withCommand("agent", "-dev", "-server", "-bootstrap", "-client", "0.0.0.0",
					"-log-level", "trace");

	@Override
	public void initialize(ConfigurableApplicationContext context) {
		start();

		MutablePropertySources sources = context.getEnvironment().getPropertySources();

		if (!sources.contains("consulTestcontainer")) {
			Integer mappedPort = consul.getMappedPort(8500);
			HashMap<String, Object> map = new HashMap<>();
			map.put("spring.cloud.consul.port", String.valueOf(mappedPort));

			sources.addFirst(new MapPropertySource("consulTestcontainer", map));
		}
	}

	public static void start() {
		if (!consul.isRunning()) {
			consul.start();
			consul.followOutput(new Slf4jLogConsumer(logger).withSeparateOutputStreams());
		}
	}

	public static Integer getPort() {
		if (!consul.isRunning()) {
			throw new IllegalStateException("consul Testcontainer is not running");
		}
		return consul.getMappedPort(8500);
	}

}