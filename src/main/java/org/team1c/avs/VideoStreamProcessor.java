package org.team1c.avs;

import java.util.Base64;
import java.util.Collections;
import java.util.Properties;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class VideoStreamProcessor {

	public static void main(String[] args) {
		// set consumer properties
		Properties consumerProp = new Properties();
		consumerProp.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
		consumerProp.put(ConsumerConfig.GROUP_ID_CONFIG, "consumerGroup1");
		consumerProp.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		consumerProp.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		consumerProp.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "1");
		consumerProp.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
		consumerProp.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
		
		Consumer<String, String> consumer = new KafkaConsumer<String, String>(consumerProp);
		consumer.subscribe(Collections.singletonList("video-input"));
		processFrames(consumer);
	}
	
	public static void processFrames(Consumer<String, String> consumer) {
		Gson gson = new Gson();
		
		while (true) {
			ConsumerRecords<String, String> consumerRecords = consumer.poll(1000);
			consumerRecords.forEach(record -> {
				// extract data from serialized frame
				String cameraId = record.key();
				JsonObject obj = gson.fromJson(record.value(), JsonObject.class);
				byte[] img = Base64.getDecoder().decode(obj.get("data").getAsString());
				
				// perform face recognition
				
			});
		}
	}

}
