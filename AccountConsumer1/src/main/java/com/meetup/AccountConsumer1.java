package com.meetup;

import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;

/**
 * Created by Sunny on 01-03-2016.
 */
@Component
public class AccountConsumer1 {

    @Value("${bootstrap.servers}")
    private String bootstrapServer;

    @Value("${topic.name}")
    private String topicName;

    @Value("${account.group.id}")
    private String accountGroupId;

    @Value("${auto.offset.reset}")
    private String offsetReset;

    @Value("${enable.auto.commit}")
    private String autoCommit;

    @Value("${account1.client.id}")
    private String acc1ClientId;


    @Async
    public void consume(){

        Properties props = new Properties();
        //A list of host/port pairs to use for establishing the initial connection to the Kafka cluster.
        props.put("bootstrap.servers", bootstrapServer);
        //A unique string that identifies the consumer group this consumer belongs to
        props.put("group.id", accountGroupId);
        //Deserializer class for key that implements the Deserializer interface.
        props.put("key.deserializer", StringDeserializer.class.getName());
        //Deserializer class for value that implements the Deserializer interface.
        props.put("value.deserializer", OrderSerializer.class.getName());
        //What to do when there is no initial offset in Kafka
        props.put("auto.offset.reset", offsetReset);
        //The maximum amount of data per-partition the server will return
        //props.put("max.partition.fetch.bytes", 5242880);
        //If true the consumer's offset will be periodically committed in the background.
        props.put("enable.auto.commit", autoCommit);
        props.put("client.id", acc1ClientId);
        KafkaConsumer<String, Object> consumer = new KafkaConsumer<>(props);

        consumer.subscribe(Arrays.asList(topicName), new ConsumerRebalanceListenerImp());

        boolean running = true;

        try {
            while (running) {
                //The time, in milliseconds, spent waiting in poll if data is not available. If 0, returns immediately with any records that are available now
                ConsumerRecords<String, Object> records = consumer.poll(1000);
                for (ConsumerRecord<String, Object> record : records) {
                    Order order = (Order)record.value();
                    System.out.println("new order received with price "+ order.getItemPrice() + " for city " + order.getCity());
                }
                //Commit offsets returned on the last poll() for all the subscribed list of topics and partitions.
                consumer.commitSync();
            }
        } finally {
            consumer.close();
        }


    }

    private class ConsumerRebalanceListenerImp implements ConsumerRebalanceListener {

        @Override
        public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
            System.out.println("partition revoked for  " + partitions);
        }

        @Override
        public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
            System.out.println("partition assigned for  " + partitions);
        }
    }
}
