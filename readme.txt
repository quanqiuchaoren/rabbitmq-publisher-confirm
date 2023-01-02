confirm模式能达到的效果：
1、开启之后，消息发布者发送一条消息，就会执行publisher confirm回调。
2、消息达到了exchange，则回调的参数值中的ack为true，不管消息是否被exchange分发给任何queue；消息没有到达exchange，则ack为false。

confirm模式的开发方式：
1、在yml中开启rabbitmq的确认选项：spring.rabbitmq.publisher-confirms设置为true。
2、给RabbitTemplate设置回调函数。