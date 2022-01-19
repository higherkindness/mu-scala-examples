# Health Check Service example

## Run example

In two different terminals, run (in order of appearance):

* Running the server:

```bash
sbt health-server-fs2/run
```

* Running the client:

```bash
sbt "health-client/run simple"
```

### Expected result
Client terminal shows status checking. 

## Run streaming example

In four different terminals, run (in order of appearance):

* Running the server:

```bash
sbt health-server-fs2/run
```

* Running the client (for watching "example1" service to update health status):

```bash
sbt "health-client/run watch 1"
```

* Running the client (for watching "example2" service to update health status):

```bash
sbt "health-client/run watch 2"
```

* Running the client (for updating health status for example 1):

```bash
sbt "health-client/run update 1"
```

### Expected result

Terminal two shows updated status. 

[comment]: # (Start Copyright)
# Copyright

Mu is designed and developed by 47 Degrees

Copyright (C) 2017-2020 47 Degrees. <http://47deg.com>

[comment]: # (End Copyright)
