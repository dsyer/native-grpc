package com.example.demo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationListener;

import com.asarkar.grpc.test.GrpcCleanupExtension;
import com.asarkar.grpc.test.Resources;
import com.example.demo.proto.HelloReply;
import com.example.demo.proto.HelloRequest;
import com.example.demo.proto.SimpleGrpc;

import io.grpc.Grpc;
import io.grpc.InsecureChannelCredentials;
import io.grpc.Server;
import net.devh.boot.grpc.server.event.GrpcServerStartedEvent;

@SpringBootTest
@ExtendWith(GrpcCleanupExtension.class)
class DemoApplicationTests {

	@Autowired
	private TestListener listener;

	@Test
	void contextLoads(Resources resources) {
	}

	@Test
	void serverResponds(Resources resources) {
		Server server = listener.getServer();
		resources.register(server, Duration.ofSeconds(5L));
		var channel = Grpc.newChannelBuilderForAddress("0.0.0.0", server.getPort(), InsecureChannelCredentials.create())
				.build();
		resources.register(channel, Duration.ofSeconds(5L));
		var stub = SimpleGrpc.newBlockingStub(channel);
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
