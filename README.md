# RabbitMQ Stress Test

This project is part of a cooperative lectured course at the [Leipzig University of Applied Sciences](http://www.htwk-leipzig.de/en/) and the [University Leipzig](http://www.zv.uni-leipzig.de/en/).
 
It is dedicated to doing a stress test on the *Advanced Message Queuing Protocol*, especially the implementation of [RabbitMQ (TM)](https://www.rabbitmq.com/). The focus is on the causing of a denial of service (DOS).

The main part is the **AMQPstress** [java application](https://github.com/philippsied/amqp-stress-test/tree/master/amqp_clients), which provides a commandline interface to run the attack cases.

A further part ist the measurement of the impact, caused by the attacks. Therefor we use [glances](https://github.com/nicolargo/glances) to measure the workload on the server and we use [RabbitMQ Performance Tools](https://github.com/rabbitmq/rabbitmq-perf-html) to benchmarking a couple of default scenarios.

The last part is the visualisation of the results. For this purpose we reuse the visualisation part of the [RabbitMQ Performance Tools](https://github.com/rabbitmq/rabbitmq-perf-html) and customize it
to our [needs](https://github.com/philippsied/amqp-stress-test/tree/master/perfBenchmark/reporting).
