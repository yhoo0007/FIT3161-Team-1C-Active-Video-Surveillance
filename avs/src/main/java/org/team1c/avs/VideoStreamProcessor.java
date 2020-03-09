package org.team1c.avs;

import java.util.Base64;
import java.util.Collections;
import java.util.Properties;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoWriter;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class VideoStreamProcessor {
	
	static { System.load("E:\\OpenCV_4.1.2\\opencv\\build\\java\\x64\\opencv_java412.dll"); }

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
		
		final String videoFilePath = "G:\\output.avi";
		final double CAMERA_FPS = 25.0;
		final Size frameSize = new Size(640, 480);
		VideoWriter videoWriter = new VideoWriter(
				videoFilePath, 
				VideoWriter.fourcc('M', 'J', 'P', 'G'), 
				CAMERA_FPS, 
				frameSize, 
				true);
		
		while (true) {
			ConsumerRecords<String, String> consumerRecords = consumer.poll(1000);
			for (ConsumerRecord<String, String> record : consumerRecords) {
				String cameraId = record.key();
				JsonObject obj = gson.fromJson(record.value(), JsonObject.class);
				byte[] bytes = Base64.getDecoder().decode(obj.get("data").getAsString());
				Mat mat = ba2Mat(480, 640, CvType.CV_8UC3, bytes);
				System.out.printf("New Frame: CamID: %s Res: %d %d\n", cameraId, mat.cols(), mat.rows());
				
				processFrame(mat);
				
				// write to video file
				videoWriter.write(mat);
			}
		}
	}
	
	/**
	 * Performs processing on the given image frame.
	 * @param mat image frame to process
	 */
	private static void processFrame(Mat mat) {
		
	}

	/**
	 * Creates a Mat object from a byte array.
	 * @param rows number of rows
	 * @param cols number of columns
	 * @param type CvType of the image, CV_8UC1, CV_8UC2, etc
	 * @param byteArray byte array containing pixel values
	 * @return Mat object representing image of the given byte array
	 */
	private static Mat ba2Mat(int rows, int cols, int type, byte[] byteArray) {
		Mat mat = new Mat(rows, cols, type);
		mat.put(0, 0, byteArray);
		return mat;
	}

}
