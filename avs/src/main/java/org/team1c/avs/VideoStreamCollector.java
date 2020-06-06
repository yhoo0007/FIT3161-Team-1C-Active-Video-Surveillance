package org.team1c.avs;

import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;

public class VideoStreamCollector {

	public static final String PROPERTIES_FP = "./properties/collector.properties";

	public static void main(String[] args) throws Exception {
		Properties collectorProp = Util.getProperties(PROPERTIES_FP);

		// String[] urls = collectorProp.getProperty("camera.url").split(",");
		String[] ids = collectorProp.getProperty("camera.id").split(",");
		
		// Create a thread for each camera
		for (int i = 0; i < ids.length; i++) {
			System.out.println("Starting thread for camera: " + ids[i]);
			Thread t = new Thread(new VideoEventGenerator(
				ids[i].trim(),
				new KafkaProducer<String, String>(collectorProp),
				collectorProp.getProperty("kafka.topic"),
				Integer.parseInt(collectorProp.getProperty("camera.retries"))
			));
			t.start();
		}
	}
}
