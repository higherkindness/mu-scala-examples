
# Route Guide Example

This module shows a simple example using [mu](https://github.com/higherkindness/mu), based on the Route Guide Demo
from [this example in grpc-java](https://github.com/grpc/grpc-java/tree/v1.10.x/examples/src/main/java/io/grpc/examples/routeguide).

## Running the Example

Run server:

```bash
sbt routeguide-server/run
```

Run client interpreting to `cats.effect.IO`:

```bash
sbt "project routeguide-client" runClientIO
```

[comment]: # (Start Copyright)
# Copyright

Mu is designed and developed by 47 Degrees

Copyright (C) 2017 47 Degrees. <http://47deg.com>

[comment]: # (End Copyright)
