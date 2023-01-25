package com.example.demo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationListener;

import com.example.demo.proto.HelloReply;
import com.example.demo.proto.HelloRequest;
import com.example.demo.proto.SimpleGrpc;

import io.grpc.Server;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import net.devh.boot.grpc.client.inject.GrpcClient;
import net.devh.boot.grpc.client.inject.GrpcClientBean;
import net.devh.boot.grpc.server.event.GrpcServerStartedEvent;

@SpringBootTest(properties = { "grpc.client.test.address=static://localhost:9090",
		"grpc.client.test.negotiationType=plaintext", "debug=true" })
@AutoConfigureObservability
class DemoApplicationTests {

	private static Log log = LogFactory.getLog(DemoApplicationTests.class);

	@Autowired
	private SimpleGrpc.SimpleBlockingStub stub;

	@Autowired
	private ObservationRegistry observations;

	@Test
	void contextLoads() {
	}

	@Test
	void serverResponds() {
		Observation observation = Observation.createNotStarted("test", this.observations);
		observation.observe(() -> {
			log.info("Testing");
			HelloReply response = stub.sayHello(HelloRequest.newBuilder().setName("Alien").build());
			assertEquals("Hello ==> Alien", response.getMessage());
		});
	}

	@TestConfiguration
	@GrpcClientBean(clazz = SimpleGrpc.SimpleBlockingStub.class, beanName = "stub", client = @GrpcClient("test"))
	static class TestListener implements ApplicationListener<GrpcServerStartedEvent> {

		private Server server;

		@Override
		public void onApplicationEvent(GrpcServerStartedEvent event) {
			this.server = event.getServer();
		}

		public Server getServer() {
			return this.server;
		}

	}

}
