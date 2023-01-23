package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import io.grpc.ServerInterceptor;
import io.micrometer.core.instrument.binder.grpc.ObservationGrpcServerInterceptor;
import io.micrometer.observation.ObservationRegistry;
import net.devh.boot.grpc.common.util.InterceptorOrder;
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;

@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

}

@Configuration
class MicrometerTracingConfiguration {

	/**
	 * Configures a global server interceptor that applies micrometer tracing logic to
	 * the requests.
	 *
	 * @param observations The observation registry bean.
	 * @return The tracing server interceptor bean.
	 */
	@GrpcGlobalServerInterceptor
	@Order(InterceptorOrder.ORDER_TRACING_METRICS + 1)
	public ServerInterceptor globalTraceServerInterceptorConfigurer(final ObservationRegistry observations) {
		return new ObservationGrpcServerInterceptor(observations);
	}

}