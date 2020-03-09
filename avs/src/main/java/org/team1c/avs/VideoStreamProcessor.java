package org.team1c.avs;

import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.Base64;
import java.util.Collections;
import java.util.Properties;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
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
		consumerProp.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class.getName());
		consumerProp.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "1");
		consumerProp.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
		consumerProp.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
		
		Consumer<String, byte[]> consumer = new KafkaConsumer<String, byte[]>(consumerProp);
		consumer.subscribe(Collections.singletonList("video-input"));
		processFrames(consumer);
	}
	
	public static void processFrames(Consumer<String, byte[]> consumer) {
		Gson gson = new Gson();
		
		final String videoFilePath = "G:\\output.avi";
		final double CAMERA_FPS = 20.0;
		final Size frameSize = new Size(640, 480);
		VideoWriter videoWriter = new VideoWriter(
				videoFilePath, 
				VideoWriter.fourcc('M', 'J', 'P', 'G'), 
				CAMERA_FPS, 
				frameSize, 
				true);
		
		JFrame frame=new JFrame();
		JLabel lbl=new JLabel();
		while (true) {
			ConsumerRecords<String, byte[]> consumerRecords = consumer.poll(1000);
			
			for (ConsumerRecord<String, byte[]> record : consumerRecords) {
				String cameraId = record.key();
//				JsonObject obj = gson.fromJson(record.value(), JsonObject.class);
//				byte[] bytes = Base64.getDecoder().decode(obj.get("data").getAsString());
				Mat mat = ba2Mat(480, 640, CvType.CV_8UC3, record.value());
				System.out.printf("New Frame: CamID: %s Res: %d %d\n", cameraId, mat.cols(), mat.rows());
				
				processFrame(mat);
				
				System.out.println("Showing frame");
				displayImage(Mat2BufferedImage(mat), frame, lbl);
				
				// write to video file
				System.out.println("Writing frame");
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
	
	public static BufferedImage Mat2BufferedImage(Mat m) {
	    // Fastest code
	    // output can be assigned either to a BufferedImage or to an Image

	    int type = BufferedImage.TYPE_BYTE_GRAY;
	    if ( m.channels() > 1 ) {
	        type = BufferedImage.TYPE_3BYTE_BGR;
	    }
	    int bufferSize = m.channels()*m.cols()*m.rows();
	    byte [] b = new byte[bufferSize];
	    m.get(0,0,b); // get all the pixels
	    BufferedImage image = new BufferedImage(m.cols(),m.rows(), type);
	    final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
	    System.arraycopy(b, 0, targetPixels, 0, b.length);  
	    return image;
	}
	
	public static void displayImage(Image img2, JFrame frame, JLabel lbl) {
	    //BufferedImage img=ImageIO.read(new File("/HelloOpenCV/lena.png"));
		ImageIcon icon=new ImageIcon(img2);
	    frame.setLayout(new FlowLayout());
	    frame.setSize(img2.getWidth(null)+50, img2.getHeight(null)+50);     
	    
	    lbl.setIcon(icon);
	    frame.add(lbl);
	    frame.setVisible(true);
	    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

}
