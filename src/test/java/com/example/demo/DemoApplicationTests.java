package com.example.demo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationListener;

import com.example.demo.proto.HelloReply;
import com.example.demo.proto.HelloRequest;
import com.example.demo.proto.SimpleGrpc;

import io.grpc.Grpc;
import io.grpc.InsecureChannelCredentials;
import io.grpc.Server;
import net.devh.boot.grpc.server.event.GrpcServerStartedEvent;

@SpringBootTest
// @ExtendWith(GrpcCleanupExtension.class)
class DemoApplicationTests {

	// @Autowired
	private TestListener listener;

	@Test
	void contextLoads() {
	}

	@Test
	void serverResponds() {
		var channel = Grpc.newChannelBuilderForAddress("0.0.0.0", 9090, InsecureChannelCredentials.create())
				.build();
		var stub = SimpleGrpc.newBlockingStub(channel);
		HelloReply response = stub.sayHello(HelloRequest.newBuilder().setName("Alien").build());
		assertEquals("Hello ==> Alien", response.getMessage());
	}

	// @TestConfiguration
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
