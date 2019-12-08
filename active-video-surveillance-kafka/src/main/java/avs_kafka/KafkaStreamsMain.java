package avs_kafka;

import java.util.Properties;
import java.util.concurrent.CountDownLatch;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.KStream;

public class KafkaStreamsMain {

	public static void main(String[] args) {
		Properties props = getDefaultProperties();
		Topology topology = getTopology();
		System.out.println(topology.describe());  // print topology created
		
		final KafkaStreams streams = new KafkaStreams(topology, props);
		
		// attach shutdown handler
		final CountDownLatch latch = new CountDownLatch(1);
		Runtime.getRuntime().addShutdownHook(new Thread("avs-shutdown-hook") {
			@Override
			public void run() {
				streams.close();
				latch.countDown();
			}
		});
		
		// start application
		try {
			streams.start();
			latch.await();
		} catch (Throwable e) {
			System.exit(1);
		}
		System.exit(0);
	}

	private static Properties getDefaultProperties() {
		Properties props = new Properties();
		props.put(StreamsConfig.APPLICATION_ID_CONFIG, "streams-avs");
		props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
		props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.Long().getClass());  // frame number
		props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.Bytes().getClass());
		return props;
	}

	private static Topology getTopology() {
		final StreamsBuilder builder = new StreamsBuilder();
		
		KStream<byte[], byte[]> source = builder.stream("avs-video-input");
		// do some processing
		
		
		source.to("avs-video-output");
		
		return builder.build();
	}

}
