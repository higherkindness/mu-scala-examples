## Mu-Scala distributed tracing example

This is an example of distributed tracing across 3 applications:

* Client
* Service A (running on port 12345)
* Service B (running on port 12346)

```
Client --RPC--> Service A --RPC--> Service B
```

The client makes an RPC request to service A, which in turn makes an RPC request
to service B, then responds to the client.

The tracing implementation used in this example is
[Jaeger](https://www.jaegertracing.io/). Mu-Scala integrates with
[Natchez](https://github.com/tpolecat/natchez) to abstract over this, so the
only mention of Jaeger is in the entrypoint of each application.

## How to run the example

First start a Jaeger server in a Docker container:

```
docker run -d --name jaeger \
  -e COLLECTOR_ZIPKIN_HTTP_PORT=9411 \
  -p 5775:5775/udp \
  -p 6831:6831/udp \
  -p 6832:6832/udp \
  -p 5778:5778 \
  -p 16686:16686 \
  -p 14268:14268 \
  -p 9411:9411 \
  jaegertracing/all-in-one:1.33
```

Next start Service B:

```
sbt tracing-server-B/run
```

Then in another terminal window, start Service A:

```
sbt tracing-server-A/run
```

Finally start the client application:

```
sbt tracing-client/run
```

Enter your name when prompted. The client will send a request (actually a few
identical requests) to Service A.

Open the Jaeger UI
([http://localhost:16686/search](http://localhost:16686/search)) and search for
traces for the `my-client-application` service. Open the most recent trace. You
should see a trace that spans across all 3 services.
