package com.qqcr.train.rabbitmq.springboot.confirm.producer;

import com.qqcr.train.rabbitmq.springboot.confirm.producer.config.RabbitMQConfig;
import org.junit.After;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.TimeUnit;

@SpringBootTest
@RunWith(SpringRunner.class)
public class PublisherConfirmTest {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @After
    public void after() throws InterruptedException {
        /*
        在测试方法中，防止资源被释放，导致ack为false。
        如果没有这里的sleep，将会出现以下错误：
            correlationData: [null]
            ack: [false]
            cause: [clean channel shutdown; protocol method: #method<channel.close>(reply-code=200, reply-text=OK, class-id=0, method-id=0)]
         */
        TimeUnit.SECONDS.sleep(2);
    }

    @DisplayName("有exchange收到了消息，消息没有被转发到任何queue，则ack为true")
    @Test
    public void should_return_true_when_exchange_received_message_but_no_queue_received() throws InterruptedException {
        /* 给template设置confirm回调函数 */
        rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
            /**
             * 消息发布者确认回调方法
             * @param correlationData correlation data for the callback. 发送消息的时候的相关参数，如果发送消息的时候设置了，
             *                        则这里也会有数据
             * @param ack true for ack, false for nack. 交换机是否收到了消息。
             *            如果交换机收到了消息，则这里为true，交换机没收到消息，则这里为false。
             * @param cause An optional cause, for nack, when available, otherwise null.
             *              如果ack为false，则这里一般就会有值，表示为什么为false，为什么交换机没有收到消息。
             */
            @Override
            public void confirm(@NonNull CorrelationData correlationData, boolean ack, @Nullable String cause) {
                System.out.println("correlationData: [" + correlationData + "]"); // null
                System.out.println("ack: [" + ack + "]"); // true
                System.out.println("cause: [" + cause + "]"); // null
                Assertions.assertTrue(ack);
            }
        });
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, "key_confirm_no_queue_received", "boot mq hello~~~");
    }

    @DisplayName("有exchange收到了消息，消息被转发到了queue，则ack为true")
    @Test
    public void should_return_true_when_exchange_received_message_and_queue_received() throws InterruptedException {
        /* 给template设置confirm回调函数 */
        rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
            @Override
            public void confirm(@NonNull CorrelationData correlationData, boolean ack, @Nullable String cause) {
                System.out.println("correlationData: [" + correlationData + "]"); // null
                System.out.println("ack: [" + ack + "]"); // true
                System.out.println("cause: [" + cause + "]"); // null
                Assertions.assertTrue(ack);
            }
        });
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, "key_confirm", "boot mq hello~~~");
    }

    @DisplayName("有exchange收到了消息，但是exchange没有绑定任何队列，则ack依然为true")
    @Test
    public void should_return_true_when_exchange_received_message_and_not_reach_queue() {
        /* 给template设置confirm回调函数 */
        rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
            @Override
            public void confirm(@NonNull CorrelationData correlationData, boolean ack, @Nullable String cause) {
                System.out.println("correlationData: [" + correlationData + "]"); // null
                System.out.println("ack: [" + ack + "]"); // true
                System.out.println("cause: [" + cause + "]"); // null
                Assertions.assertTrue(ack);
            }
        });
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_WITHOUT_QUEUE, "key_confirm", "boot mq hello~~~");
    }

    @DisplayName("没有exchange收到消息，则ack为false")
    @Test
    public void should_return_false_when_exchange_not_existed() throws InterruptedException {
        /* 给template设置confirm回调函数 */
        rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
            @Override
            public void confirm(@NonNull CorrelationData correlationData, boolean ack, @Nullable String cause) {
                /*
                输出如下：
                correlationData: [null]
                ack: [false]
                cause: [channel error; protocol method: #method<channel.close>(reply-code=404, reply-text=NOT_FOUND - no exchange 'not_existed_exchange' in vhost '/', class-id=60, method-id=40)]
                 */
                System.out.println("correlationData: [" + correlationData + "]");
                System.out.println("ack: [" + ack + "]");
                System.out.println("cause: [" + cause + "]");
                Assertions.assertFalse(ack);
            }
        });
        rabbitTemplate.convertAndSend("not_existed_exchange", "key_confirm", "boot mq hello~~~");
    }
}
