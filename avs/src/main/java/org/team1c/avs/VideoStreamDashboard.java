// File:         AvsCustomAssignor.java
// Author:       Ho Yi Ping, Khaifung Lim, Fernando Ng and Chong Chiu Gin
// Last Modified Date:  5-June-2020         
// 
// Description:  This class is to run the dashboard that will show the live frames, 
//analytical information as well as the FPS and Bitrate Meter
package org.team1c.avs;

import java.util.Properties;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Base64;
import java.util.stream.Collectors;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.data.xy.XYDataset; 
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.Millisecond;

import eu.hansolo.steelseries.gauges.Radial;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.PartitionInfo;

import org.opencv.core.Mat;
import org.opencv.core.CvType;
import org.opencv.imgproc.Imgproc;
import org.opencv.core.Size;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.team1c.avs.VideoStreamCollector;

import java.util.Date;


public class VideoStreamDashboard extends JFrame {

    // load OpenCV libraries
	// Use NATIVE_LIBRARY_NAME if it is available for your machine, otherwise load the library 
	// directly
	// static { System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }
	// static { System.load("/home/ubuntu/opencv/opencv-3.4/build/lib/libopencv_java3410.so"); }
    static { System.load("E:\\OpenCV_4.1.2\\opencv\\build\\java\\x64\\opencv_java412.dll"); }

    public static final String PROPERTIES_FP = "./properties/dashboard.properties";

    // top panels
    static JPanel topLeftPanel;
    static JLabel topLeftLabel;
    static JPanel topRightTopPanel;
    static JPanel topRightBottomPanel;

    // charts
    static TimeSeries faceCountSeries;
    static TimeSeries cumulativeSeries;
    static JFreeChart faceCountChart;
    static JFreeChart cumulativeChart;

    // gauges
    static Radial fpsGauge;
    static Radial bitRateGauge;

    // bottom panels
    static JPanel bottomLeftPanel;
    static JPanel bottomRightPanel;
    static JPanel miscPanel;
    static JLabel datetimeLabel;
    static JLabel resolutionLabel;
    static JLabel latencyLabel;

    // divider positions
    private static int TOP_RIGHT_DIV = 360;
    private static int TOP_DIV = 1070;
    private static int BOTTOM_DIV = 200;
    private static int TOP_BOTTOM_DIV = 780;

    static int currentCameraId = 0;
    static Consumer<String, String> consumer;

    /**
     * this method is to initialize the dashboard by
     * - create kafka consumer
     * - create top left panel (video feed)
     * - create top right top panel for first chart (Face count)
     * - create top right bottom panel for the second chart (Cummulative face count)
     * - create bottom left panel for camera button selector
     *  - create bottom right panel for FPS and Bitrate meter
     * 
     * @throws Exception
     */
    public VideoStreamDashboard() throws Exception {
        // create kafka consumer
        Properties consumerProp = Util.getProperties(PROPERTIES_FP);
        consumer = new KafkaConsumer<String, String>(consumerProp);
        List<Integer> cameraIds = Util.getCameraIds(
            consumer, 
            consumerProp.getProperty("kafka.topic")
        );
        System.out.printf("Found %d number of cameras/partitions", cameraIds.size());
        consumer.subscribe(Collections.singletonList(consumerProp.getProperty("kafka.topic")));

        // create top left panel (video feed)
        topLeftPanel = new JPanel();
        topLeftLabel = new JLabel();
        topLeftPanel.add(topLeftLabel);

        // create top right panel (graphs)
        topRightTopPanel = new JPanel();
        topRightTopPanel.setLayout(new BorderLayout());
        topRightBottomPanel = new JPanel();
        topRightBottomPanel.setLayout(new BorderLayout());
        JSplitPane topRightPanel = new JSplitPane(
            JSplitPane.VERTICAL_SPLIT,
            topRightTopPanel,
            topRightBottomPanel
        );
        topRightPanel.setDividerLocation(TOP_RIGHT_DIV);

        // create top right top panel chart (number of faces chart)
        faceCountSeries = new TimeSeries("Face Count");
        faceCountChart = drawChart(
            "Total Faces / Time",
            "Time (s)",
            "Number of Faces",
            faceCountSeries
        );
        ChartPanel faceCountChartPanel = new ChartPanel(faceCountChart);
        topRightTopPanel.add(faceCountChartPanel);

        // create top right bottom panel chart (Cumulative Face Count)
        cumulativeSeries = new TimeSeries("Cumulative Face Count");
        cumulativeChart = drawChart(
            "Cumulative Faces / Time",
            "Time (s)",
            "Total number of Faces",
            cumulativeSeries
        );
        ChartPanel cumulativeChartPanel = new ChartPanel(cumulativeChart);
        topRightBottomPanel.add(cumulativeChartPanel);

        // merge top left and right panels
         JSplitPane topPanel = new JSplitPane(
             JSplitPane.HORIZONTAL_SPLIT,
             topLeftPanel,
             topRightPanel
         );
         topPanel.setDividerLocation(TOP_DIV);

        // create bottom left panel (camera selector)
        bottomLeftPanel = new JPanel();
        for (int cameraId : cameraIds) {
            JButton cameraBtn = new JButton("Camera " + Integer.toString(cameraId));
            bottomLeftPanel.add(cameraBtn);
            cameraBtn.addActionListener(new CameraButtonListener(cameraId));
        }
        bottomLeftPanel.setLayout(new GridLayout(cameraIds.size(), 1, 10, 10));
        JScrollPane bottomLeftScroll = new JScrollPane(bottomLeftPanel);
        bottomLeftScroll.setHorizontalScrollBarPolicy(
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS
        );
        bottomLeftPanel.setSize(375, 250);

        // create bottom right panel (misc information)
        bottomRightPanel = new JPanel();

        // FPS Gauge
        fpsGauge = new Radial();
        fpsGauge.setTitle("FPS Meter");
        fpsGauge.setUnitString("Frame per seconds");
        fpsGauge.setValue(0.0);

        // Bitrate Gauge
        bitRateGauge = new Radial();
        bitRateGauge.setTitle("Bitrate");
        bitRateGauge.setUnitString("Bits per seconds");
        bitRateGauge.setValue(0.0);
        
        // misc panel
        miscPanel = new JPanel();
        miscPanel.setSize(100, 100);
        miscPanel.setLayout(new GridLayout(3, 1));
        resolutionLabel = new JLabel("Resolution:");
        resolutionLabel.setFont(new Font("Serif", Font.PLAIN, 36));
        miscPanel.add(resolutionLabel);
        datetimeLabel = new JLabel("Datetime:");
        datetimeLabel.setFont(new Font("Serif", Font.PLAIN, 36));
        miscPanel.add(datetimeLabel);
        latencyLabel = new JLabel("Latency: 0%");
        latencyLabel.setFont(new Font("Serif", Font.PLAIN, 36));
        miscPanel.add(latencyLabel);

        bottomRightPanel.setLayout(new GridLayout(1, 3));
        bottomRightPanel.add(miscPanel);
        bottomRightPanel.add(fpsGauge);
        bottomRightPanel.add(bitRateGauge);
        bottomRightPanel.validate();

        // merge bottom left and right panels
        JSplitPane bottomPanel = new JSplitPane(
            JSplitPane.HORIZONTAL_SPLIT,
            bottomLeftScroll,
            bottomRightPanel
        );
        bottomPanel.setDividerLocation(BOTTOM_DIV);

        // merge top and bottom panels
        JSplitPane mainPanel = new JSplitPane(
            JSplitPane.VERTICAL_SPLIT,
            topPanel,
            bottomPanel
        );
        mainPanel.setDividerLocation(TOP_BOTTOM_DIV);

        setContentPane(mainPanel);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(1500, 1000));
        setVisible(true);
    }

    /**
     * This method will return a JFreeChart 
     * 
     * @param title title of the chart
     * @param xaxis x axis label
     * @param yaxis y axis label
     * @param series the series for the chart
     * @return JFreeChart
     */
    private static JFreeChart drawChart(String title, String xaxis, String yaxis, 
        TimeSeries series) {
        XYDataset collection = new TimeSeriesCollection(series);
        JFreeChart chart = ChartFactory.createTimeSeriesChart(
            title,
            xaxis,
            yaxis,
            collection,
            true,
            true,
            false
        );
        return chart;
    }

  
    
    private static class CameraButtonListener implements ActionListener {
        private int cameraId;


        public CameraButtonListener(int cameraId) {
            this.cameraId = cameraId;
        }

        @Override
        /**
         * this function will change the currentCameraId if the currentCameraId differ
         * with the cameraId. if it is different, camera change is required
         */
        public void actionPerformed(ActionEvent actionEvent) {
            if (currentCameraId != this.cameraId) {  // camera change required
                currentCameraId = this.cameraId;
            }
        }
    }

    /**
     * this method is to run the dashboard such as:
     * - Extracting information from record
     * - display frame and analytical data into dashboard
     * 
     * 
     */
    public static void run() {
        Gson gson = new Gson();
        int cumulativeFaces = 0;
        int nframes = 0;
        long fpsPrevTime = System.currentTimeMillis();

        while (true) {
            ConsumerRecords<String, String> consumerRecords = consumer.poll(1000);
            for (ConsumerRecord<String, String> record : consumerRecords) {

                // extract info from record
                String cameraId = record.key();
                JsonObject obj = gson.fromJson(record.value(), JsonObject.class);
                int resolutionx = obj.get("resolutionx").getAsInt();
                int resolutiony = obj.get("resolutiony").getAsInt();
                int channels = obj.get("channels").getAsInt();
                double fps = obj.get("fps").getAsDouble();
                long initTime = obj.get("initTime").getAsLong();
                long procTime = obj.get("procTime").getAsLong();

                byte[] bytes = Base64.getDecoder().decode(obj.get("frame").getAsString());
                Mat mat = Util.ba2Mat(resolutiony, resolutionx, CvType.CV_8UC3, bytes);
                Size upSize = new Size(1024, 768);
                Imgproc.resize(mat, mat, upSize);
                int nfaces = obj.get("nfaces").getAsInt();
                cumulativeFaces += nfaces;

                // filter frames for the given ID
                if (Integer.parseInt(record.key()) == currentCameraId) {
                    System.out.printf("New frame: CamID: %s\n", record.key());

                    // display frame
                    displayImage(Util.Mat2BufferedImage(mat));
                    nframes++;

                    // display analytical data
                    displayAnalytics(
                        nfaces,
                        cumulativeFaces
                    );
                    long currentTime = System.currentTimeMillis();
                    long totalLatency = currentTime - initTime;
                    latencyLabel.setText("Latency: " + Long.toString(totalLatency));
                    if (System.currentTimeMillis() > fpsPrevTime + 1000) {
                        fpsGauge.setValue(nframes);
                        bitRateGauge.setValue(record.serializedValueSize());
                        nframes = 0;
                        fpsPrevTime = System.currentTimeMillis();
                    }
                }
            }
        }
    }

    /**
     * this method will update the topLeftPanel of the dashboard into a new image
     * 
     * @param img new image that want to be updated
     */
    public static void displayImage(Image img) {
		ImageIcon icon = new ImageIcon(img);
	    topLeftPanel.setSize(img.getWidth(null)+50, img.getHeight(null)+50);     
	    topLeftLabel.setIcon(icon);
	    topLeftPanel.add(topLeftLabel);
	    topLeftPanel.repaint();
	    topLeftPanel.validate();
	}

    /**
     * this method is used to add new data into faceCountSeries and cumulativeSeries
     * as a result, the graph will be dynamically changing
     * 
     * @param nfaces
     * @param cumulativeFaces
     */
    public static void displayAnalytics(int nfaces, int cumulativeFaces) {
        faceCountSeries.add(new Millisecond(new Date()), nfaces);
        cumulativeSeries.add(new Millisecond(new Date()), cumulativeFaces);
    }


    public static void main(String[] args) throws Exception {
        new VideoStreamDashboard();
        run();
    }
}