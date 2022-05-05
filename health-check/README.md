# Health Check Service example

## Run example

In two different terminals, run (in order of appearance):

* Running the server:

```bash
sbt health-server/run
```

* Running the client to check the status of the server as a whole:

```bash
sbt "health-client/run check"
```

or to check the status of service `A`:

```bash
sbt "health-client/run check A"
```

### Expected result

The client program logs the service's status to stdout.

## Run streaming example

In two different terminals, run (in order of appearance):

* Running the server:

```bash
sbt health-server-fs2/run
```

* Running the client to watch the status of service `A`:

```bash
sbt "health-client/run watch A"
```

### Expected result

The client program logs a stream of status updates to stdout.

[comment]: # (Start Copyright)
# Copyright

Mu is designed and developed by 47 Degrees

Copyright (C) 2017-2020 47 Degrees. <http://47deg.com>

[comment]: # (End Copyright)
