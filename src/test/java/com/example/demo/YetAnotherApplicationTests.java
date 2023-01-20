package com.example.demo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;

import com.example.demo.proto.HelloReply;
import com.example.demo.proto.HelloRequest;
import com.example.demo.proto.SimpleGrpc;

import io.grpc.Grpc;
import io.grpc.InsecureChannelCredentials;

@SpringBootTest
class YetAnotherApplicationTests {

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
	static class ExtraConfiguration {
	}
}
