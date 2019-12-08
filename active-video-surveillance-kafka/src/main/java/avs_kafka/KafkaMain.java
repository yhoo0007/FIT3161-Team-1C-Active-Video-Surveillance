package avs_kafka;

import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.utils.Bytes;

public class KafkaMain {

	public static void main(String[] args) {
		if (args[0].equals("consumer")) {
			runConsumer();
		} else if (args[0].equals("producer")) {
			runProducer();
		} else {
			System.out.println("Invalid Argument Provided");
		}
	}
	
	private static void runProducer() {
		Producer<Bytes, Bytes> producer = ProducerCreator.createProducer();
		
		// send some messages!
		for (int i = 0; i < 100; i++) {
			Bytes message = new Bytes(("this is a messsage " + i).getBytes());
			ProducerRecord<Bytes, Bytes> record = new ProducerRecord<Bytes, Bytes>("avs-video-input", message);
			try {
				RecordMetadata metadata = producer.send(record).get();
				System.out.println("Record sent with key " + i + " with offset " + metadata.offset());
			} catch (ExecutionException e) {
				System.out.println("Error in sending record");
				System.out.println(e);
			} catch (InterruptedException e) {
				System.out.println("Error in sending record");
				System.out.println(e);
			}
		}
	}

	private static void runConsumer() {
		Consumer<Bytes, Bytes> consumer = ConsumerCreator.createConsumer();
		
		int noMessageFound = 0;
		while (true) {
			ConsumerRecords<Bytes, Bytes> consumerRecords = consumer.poll(1000);
			if (consumerRecords.count() == 0) {
				noMessageFound++;
				if (noMessageFound > 1000)
					break;
				else
					continue;
			}
			
			consumerRecords.forEach(record -> {
				System.out.println("Record Key " + record.key());
				System.out.println("Record Value " + record.value());
				System.out.println("Record partition " + record.partition());
				System.out.println("Record offset " + record.offset());
			});
			
			consumer.commitAsync();
		}
		consumer.close();
	}
}
