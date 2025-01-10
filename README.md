# Spring Boot gRPC Sample

This project is a copy one of the samples from the [gRPC Spring Boot Starter](https://github.com/yidongnan/grpc-spring-boot-starter/blob/master/examples/local-grpc-server/build.gradle). Build and run any way you like to run Spring Boot. E.g:

```
$ ./mvnw spring-boot:run
...
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v3.0.0)

2022-12-08T05:32:24.934-08:00  INFO 551632 --- [           main] com.example.demo.DemoApplication         : Starting DemoApplication using Java 17.0.5 with PID 551632 (/home/dsyer/dev/scratch/demo/target/classes started by dsyer in /home/dsyer/dev/scratch/demo)
2022-12-08T05:32:24.938-08:00  INFO 551632 --- [           main] com.example.demo.DemoApplication         : No active profile set, falling back to 1 default profile: "default"
2022-12-08T05:32:25.377-08:00  WARN 551632 --- [           main] ocalVariableTableParameterNameDiscoverer : Using deprecated '-debug' fallback for parameter name resolution. Compile the affected code with '-parameters' instead or avoid its introspection: net.devh.boot.grpc.server.autoconfigure.GrpcHealthServiceAutoConfiguration
2022-12-08T05:32:25.416-08:00  WARN 551632 --- [           main] ocalVariableTableParameterNameDiscoverer : Using deprecated '-debug' fallback for parameter name resolution. Compile the affected code with '-parameters' instead or avoid its introspection: net.devh.boot.grpc.server.autoconfigure.GrpcServerAutoConfiguration
2022-12-08T05:32:25.425-08:00  WARN 551632 --- [           main] ocalVariableTableParameterNameDiscoverer : Using deprecated '-debug' fallback for parameter name resolution. Compile the affected code with '-parameters' instead or avoid its introspection: net.devh.boot.grpc.server.autoconfigure.GrpcServerFactoryAutoConfiguration
2022-12-08T05:32:25.427-08:00  INFO 551632 --- [           main] g.s.a.GrpcServerFactoryAutoConfiguration : Detected grpc-netty: Creating NettyGrpcServerFactory
2022-12-08T05:32:25.712-08:00  INFO 551632 --- [           main] n.d.b.g.s.s.AbstractGrpcServerFactory    : Registered gRPC service: Simple, bean: grpcServerService, class: com.example.demo.GrpcServerService
2022-12-08T05:32:25.712-08:00  INFO 551632 --- [           main] n.d.b.g.s.s.AbstractGrpcServerFactory    : Registered gRPC service: grpc.health.v1.Health, bean: grpcHealthService, class: io.grpc.protobuf.services.HealthServiceImpl
2022-12-08T05:32:25.712-08:00  INFO 551632 --- [           main] n.d.b.g.s.s.AbstractGrpcServerFactory    : Registered gRPC service: grpc.reflection.v1alpha.ServerReflection, bean: protoReflectionService, class: io.grpc.protobuf.services.ProtoReflectionService
2022-12-08T05:32:25.820-08:00  INFO 551632 --- [           main] n.d.b.g.s.s.GrpcServerLifecycle          : gRPC Server started, listening on address: *, port: 9090
2022-12-08T05:32:25.831-08:00  INFO 551632 --- [           main] com.example.demo.DemoApplication         : Started DemoApplication in 1.264 seconds (process running for 1.623)
```

## Test with gRPCurl

The server starts by default on port 9090. Test with [gRPCurl](https://github.com/fullstorydev/grpcurl):

```
$ grpcurl -d '{"name":"Hi"}' -plaintext localhost:9090 Simple.SayHello
{
  "message": "Hello ==\u003e Hi"
}
```

## Test with Curl

Encode the message:

```
$ echo 'name:"Hi"' | protoc --encode=HelloRequest src/main/proto/hello.proto > target/req.buf
```

Have a look at the encoded data and you will find it is a vanilla protobuf `\x0a\x02\x48\x69`

```
$ od -v -tx1 -A n target/req.buf | tr -d ' \n' | sed 's/../\\x&/g'
\x0a\x02\x48\x69
```

The first and only field is a string (`\x0a`) with length 2 (`\x02`) and value "Hi". To send it as a [gRPC message](https://github.com/grpc/grpc/blob/master/doc/PROTOCOL-HTTP2.md) you need a 5-byte prefix of `\x00` (compression flag off) plus the length of the message in big-endian format (in this case `\x04`). You can use `printf` to generate the prefix and `curl` to send it:

```
$ printf '\x00\x00\x00\x00\x04\x0a\x02\x48\x69' | curl --http2-prior-knowledge -H "TE: trailers" -v -H "Content-Type: application/grpc" --data-binary @- localhost:9090/Simple/SayHello > target/res.buf
```

(With a servlet container you can skip the `--http2-prior-knowledge` flag).
The response has the 5-byte prefix and the message:

```
$ dd if=target/res.buf bs=5 skip=1 2>/dev/null | protoc --decode=HelloReply src/main/proto/hello.proto
message: "Hello ==> Hi"
```

You don't actually need the HTTP/2 prior knowledge flag because our server is Tomcat (you would for a Netty server from gRPC Java). So putting it all together:

```
$ printf '\x00\x00\x00\x00\x04\x0a\x02\x48\x69' | \
curl -H "Content-Type: application/grpc" --http2-prior-knowledge -H "TE: trailers" --data-binary @- 2> /dev/null \
localhost:9090/Simple/SayHello | \
dd bs=5 skip=1 2> /dev/null | \
protoc --decode=HelloReply src/main/proto/hello.proto
```

Output:

```
message: "Hello ==> Hi"
```

## Native Image

The app compiles to a native image if the JVM is GraalVM:

```
$ ./mvnw -Pnative native:compile
$ ./target/demo
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v3.0.0)

2022-12-08T05:36:54.365-08:00  INFO 554359 --- [           main] com.example.demo.DemoApplication         : Starting AOT-processed DemoApplication using Java 17.0.5 with PID 554359 (/home/dsyer/dev/scratch/demo/target/demo started by dsyer in /home/dsyer/dev/scratch/demo)
2022-12-08T05:36:54.366-08:00  INFO 554359 --- [           main] com.example.demo.DemoApplication         : No active profile set, falling back to 1 default profile: "default"
2022-12-08T05:36:54.377-08:00  INFO 554359 --- [           main] g.s.a.GrpcServerFactoryAutoConfiguration : Detected grpc-netty: Creating NettyGrpcServerFactory
2022-12-08T05:36:54.392-08:00  INFO 554359 --- [           main] n.d.b.g.s.s.AbstractGrpcServerFactory    : Registered gRPC service: Simple, bean: grpcServerService, class: com.example.demo.GrpcServerService
2022-12-08T05:36:54.392-08:00  INFO 554359 --- [           main] n.d.b.g.s.s.AbstractGrpcServerFactory    : Registered gRPC service: grpc.health.v1.Health, bean: grpcHealthService, class: io.grpc.protobuf.services.HealthServiceImpl
2022-12-08T05:36:54.392-08:00  INFO 554359 --- [           main] n.d.b.g.s.s.AbstractGrpcServerFactory    : Registered gRPC service: grpc.reflection.v1alpha.ServerReflection, bean: protoReflectionService, class: io.grpc.protobuf.services.ProtoReflectionService
2022-12-08T05:36:54.396-08:00  INFO 554359 --- [           main] n.d.b.g.s.s.GrpcServerLifecycle          : gRPC Server started, listening on address: *, port: 9090
2022-12-08T05:36:54.396-08:00  INFO 554359 --- [           main] com.example.demo.DemoApplication         : Started DemoApplication in 0.046 seconds (process running for 0.052)
```

The patches needed to make it work in native were a reflection hint (upstream change: https://github.com/oracle/graalvm-reachability-metadata/pull/148) and some autoconfig metadata (upstream change: https://github.com/yidongnan/grpc-spring-boot-starter/pull/775).