# Spring Tips (Apache RocketMQ)

In order to use this you'll need to follow the steps in the [RocketMQ](https://rocketmq.apache.org/docs/quick-start/) quickguide. 

This guide introduces Apache RocketMQ, originally a technology developed and used internally at Alibaba.

Also, be sure you're using Java 8. The newer ones don't work so well. If you're on a Mac, you can use:

```
sdk use java 8.0.242.hs-adpt
```

That'll install a version that works if it's not already installed. 
Once that's done, then you'll need to run the NameServer.

```
${ROCKETMQ_HOME}/bin/mqnamesrv 
```

Then you'll need to run the Broker.

```
${ROCKETMQ_HOME}/bin/mqbroker -n localhost:9876
```

If you want to use SQL-based filtering, you need to add a property to the broker's configuration, `$ROCKETMQ_HOME/conf/broker.conf`:

```
enablePropertyFilter = true
```

I use a script like this to launch everything.

``` 
export JAVA_HOME=$HOME/.sdkman/candidates/java/8.0.242.hs-adpt
${ROCKETMQ_HOME}/bin/mqnamesrv &  
${ROCKETMQ_HOME}/bin/mqbroker -n localhost:9876 -c ${ROCKETMQ_HOME}/conf/broker.conf
```