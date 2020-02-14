# Spring Tips RSocket  

In order to use this you'll need to follow the steps in the [RocketMQ](https://rocketmq.apache.org/docs/quick-start/) quickguide. 

Also, be sure you're using Java 8. The newer ones don't work so well. If you're on a Mac, you can use:

```
sdk use java 8.0.242.hs-adpt
```

That'll install a version that works if it's not already installed. 
Once that's done, then you'll need to run the NameServer.

```
bin/mqnamesrv 
```

Then you'll need to run the Broker.

```
bin/mqbroker -n localhost:9876
```
