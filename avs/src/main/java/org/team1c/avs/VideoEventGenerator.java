package org.team1c.avs;

import java.sql.Timestamp;
import java.util.Base64;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

//import com.google.gson.Gson;
//import com.google.gson.JsonObject;

public class VideoEventGenerator implements Runnable {
	private String cameraId;
	private String url;
	private Producer<String, byte[]> producer;
	private String topic;
	
	public VideoEventGenerator(String cameraId, String url, Producer<String, byte[]> producer, String topic) {
		this.cameraId = cameraId;
		this.url = url;
		this.producer = producer;
		this.topic = topic;
	}
	
	// load OpenCV
	// static { System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }  // TODO might have to change to load direct path instead
//	static { System.loadLibrary("opencv_videoio_ffmpeg412_64"); }
	//static { System.load("E:\\OpenCV_4.1.2\\opencv\\build\\java\\x64\\opencv_java412.dll"); }
	//static { System.load("E:\\OpenCV_4.1.2\\opencv\\build\\bin\\opencv_videoio_ffmpeg412_64.dll"); }
	static { System.load("/home/ubuntu/opencv/opencv-3.4/build/lib/libopencv_java3410.so"); }
	
	@Override
	public void run() {
		try {
			generateEvent(cameraId, url, producer, topic);
		} catch (Exception e) {
			// log error
		}
	}

	private void generateEvent(String cameraId, String url, Producer<String, byte[]> producer, String topic) throws Exception {
		// attempt to open camera
		VideoCapture camera = null;
		if (isNumeric(url)) {
			camera = new VideoCapture();
			camera.open(Integer.parseInt(url));
		} else {
			camera = new VideoCapture();
			camera.open(url);
		}
		if (!camera.isOpened()) {
			Thread.sleep(5000);
			if (!camera.isOpened()) {
				throw new Exception("Error opening camera: " + cameraId + " url: " + url + " check file path or url in property file");
			}
		}
		
		Mat mat = new Mat();
//		Gson gson = new Gson();
		while (camera.read(mat)) {
			// resize image
			Imgproc.resize(mat, mat, new Size(640, 480), 0, 0, Imgproc.INTER_CUBIC);
			
			// create byte array from mat object
			byte[] data = new byte[(int) (mat.total() * mat.channels())];
			mat.get(0, 0, data);
			
//			// create and populate JSON object
//			JsonObject obj = new JsonObject();  // TODO try to move instantiation out of while loop
//			obj.addProperty("cameraId", cameraId);
//			obj.addProperty("timestamp", new Timestamp(System.currentTimeMillis()).toString());
//			obj.addProperty("rows", mat.rows());
//			obj.addProperty("cols", mat.cols());
//			obj.addProperty("type", mat.type());
//			obj.addProperty("data", Base64.getEncoder().encodeToString(data));
//			
//			// serialize JSON object to string
//			String serialized = gson.toJson(obj);
			
			// publish video frame as string
			producer.send(new ProducerRecord<String, byte[]>(topic, cameraId, data), new EventGeneratorCallback(cameraId));			
		}
		camera.release();
		mat.release();
	}
	
	private class EventGeneratorCallback implements Callback {
		private String cameraId;
		
		public EventGeneratorCallback(String cameraId) {
			super();
			this.cameraId = cameraId;
		}

		@Override
		public void onCompletion(RecordMetadata metadata, Exception exception) {
			if (metadata != null) {
				// log metadata with camera Id
			}
			if (exception != null) {
				exception.printStackTrace();
			}
		}
	}
	
	public static boolean isNumeric(String string) {
	    if (string == null) {
	        return false;
	    }
	    try {
	        Double.parseDouble(string);  // attempt to parse
	    } catch (NumberFormatException e) {
	        return false;
	    }
	    return true;
	}

}
