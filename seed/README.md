# Seed RPC example

This directory demonstrates two ways to build a dead-simple purely functional RPC service using [Mu](https://github.com/higherkindness/mu-scala) -- one way expresses the communication protocol via Avro Schema Definition, and the other expresses the protocol via Protobuf.  These examples both consist of (a) a simple API: `PeopleService[F]`, (b) a basic interpreter for the API: `PeopleServiceHandler`, and (c) an RPC client, `ClientApp`, that consumes `PeopleService[F]`.  These examples are intended as a lightweight example to demonstrate how to build microservices using Mu with Avro or Protobuf.

## Execution

### Avro
Running the server:

```bash
sbt runAvroServer
```

Running the client:

```bash
sbt runAvroClient
```

### Protobuf

Running the server:

```bash
sbt runProtoServer
```

Running the client:

```bash
sbt runProtoClient
```

[comment]: # (Start Copyright)
# Copyright

Mu is designed and developed by 47 Degrees

Copyright (C) 2017-2018 47 Degrees. <http://47deg.com>

[comment]: # (End Copyright)