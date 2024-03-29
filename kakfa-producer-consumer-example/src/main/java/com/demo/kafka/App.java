package com.demo.kafka;

import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import com.demo.kafka.constants.IKafkaConstants;
import com.demo.kafka.consumer.ConsumerCreator;
import com.demo.kafka.producer.ProducerCreator;

public class App implements Runnable {
	
	Thread t, t2;
	
	App() {
		t = new Thread(this, "PROD");
		System.out.println(t);
		t.start();
		t2 = new Thread(new A(), "CONS");
		System.out.println(t2);
		t2.start();
	}
	
	public void run() {
		runProducer();
	}
	
	class A implements Runnable {
		public void run() {
			runConsumer();
		}
	}
	
	public static void main(String[] args) {
		App a1 = new App();
		App a2 = new App();
		//App a3 = new App();
		//App a4 = new App();
	}

	static void runConsumer() {
		Consumer<Long, String> consumer = ConsumerCreator.createConsumer();

		int noMessageToFetch = 0;

		while (true) {
			final ConsumerRecords<Long, String> consumerRecords = consumer.poll(1000);
			if (consumerRecords.count() == 0) {
				noMessageToFetch++;
				if (noMessageToFetch > IKafkaConstants.MAX_NO_MESSAGE_FOUND_COUNT)
					break;
				else
					continue;
			}

			consumerRecords.forEach(record -> {
				System.out.println("Record value " + record.value());
				System.out.println("Record partition " + record.partition());
				System.out.println("Record offset " + record.offset());
			});
			consumer.commitAsync();
		}
		consumer.close();
	}

	static void runProducer() {
		Producer<Long, String> producer = ProducerCreator.createProducer();

		for (int index = 0; index < IKafkaConstants.MESSAGE_COUNT; index++) {
			final ProducerRecord<Long, String> record = new ProducerRecord<Long, String>(IKafkaConstants.TOPIC_NAME,
					"This is record " + index);
			try {
				RecordMetadata metadata = producer.send(record).get();
				System.out.println("Record sent with key " + index + " to partition " + metadata.partition()
						+ " with offset " + metadata.offset());
				if(index == 500) {
					try {
						Thread.sleep(60000);
					} catch(Exception e) {
						e.printStackTrace();
					}
				}
			} catch (ExecutionException e) {
				System.out.println("Error in sending record");
				System.out.println(e);
			} catch (InterruptedException e) {
				System.out.println("Error in sending record");
				System.out.println(e);
			}
		}
	}
}
