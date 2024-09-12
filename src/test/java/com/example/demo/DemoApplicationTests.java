package com.example.demo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationListener;
import org.springframework.test.annotation.DirtiesContext;

import com.example.demo.proto.HelloReply;
import com.example.demo.proto.HelloRequest;
import com.example.demo.proto.SimpleGrpc;

import io.grpc.Server;
import net.devh.boot.grpc.client.inject.GrpcClient;
import net.devh.boot.grpc.server.event.GrpcServerStartedEvent;

@SpringBootTest(properties = { "grpc.client.test.address=static://localhost:9090",
		"grpc.client.test.negotiationType=plaintext" })
public class DemoApplicationTests {

	private static Log log = LogFactory.getLog(DemoApplicationTests.class);

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	@GrpcClient("test")
	private SimpleGrpc.SimpleBlockingStub stub;

	@Test
	@DirtiesContext
	void contextLoads() {
	}

	@Test
	@DirtiesContext
	void serverResponds() {
		log.info("Testing");
		HelloReply response = stub.sayHello(HelloRequest.newBuilder().setName("Alien").build());
		assertEquals("Hello ==> Alien", response.getMessage());
	}

	@TestConfiguration
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
