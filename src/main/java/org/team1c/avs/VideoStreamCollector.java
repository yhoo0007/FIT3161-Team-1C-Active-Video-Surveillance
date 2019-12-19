package org.team1c.avs;

import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;

public class VideoStreamCollector {

	public static void main(String[] args) throws Exception {
		// set producer properties
		Properties producerProp = new Properties();
		producerProp.put("bootstrap.servers", "localhost:9092");
		producerProp.put("acks", "all");
		producerProp.put("retries", "1");
		producerProp.put("batch.size", "20971520");  // 20MB
		producerProp.put("linger.ms", "5");
		producerProp.put("max.request.size", "2097152");  // 2MB
		producerProp.put("kafka.topic", "video-input");
		producerProp.put("compression.type", "gzip");
		producerProp.put("camera.url", "");  // TODO add camera URL
		producerProp.put("camera.id", "vid-01");
		producerProp.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		producerProp.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		
		Producer<String, String> producer = new KafkaProducer<String, String>(producerProp);
		generateEvent(producer,
				producerProp.getProperty("kafka.topic"),
				producerProp.getProperty("camera.id"),
				producerProp.getProperty("camera.url"));
	}

	private static void generateEvent(Producer<String, String> producer, String topic, String camId, String videoUrl) throws Exception {
		String[] urls = videoUrl.split(",");
		String[] ids = camId.split(",");
		
		if (urls.length != ids.length) {
			throw new Exception("There should be same number of camera Ids and Urls");
		}
		
		// Create a thread for each camera
		for (int i = 0; i < urls.length; i++) {
			Thread t = new Thread(new VideoEventGenerator(ids[i].trim(), urls[i].trim(), producer, topic));
			t.start();
		}
	}

}
