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

    public VideoStreamDashboard() throws Exception {
        // create kafka consumer
        Properties consumerProp = Util.getProperties(PROPERTIES_FP);
        consumer = new KafkaConsumer<String, String>(consumerProp);
        List<Integer> cameraIds = Util.getCameraIds(consumer, consumerProp.getProperty("kafka.topic"));
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

        // create top right bottom panel chart (?? chart)
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


    private static JFreeChart drawChart(String title, String xaxis, String yaxis, TimeSeries series) {
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
        public void actionPerformed(ActionEvent actionEvent) {
            if (currentCameraId != this.cameraId) {  // camera change required
                currentCameraId = this.cameraId;
            }
        }
    }


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


    public static void displayImage(Image img) {
		ImageIcon icon = new ImageIcon(img);
	    topLeftPanel.setSize(img.getWidth(null)+50, img.getHeight(null)+50);     
	    topLeftLabel.setIcon(icon);
	    topLeftPanel.add(topLeftLabel);
	    topLeftPanel.repaint();
	    topLeftPanel.validate();
	}


    public static void displayAnalytics(int nfaces, int cumulativeFaces) {
        faceCountSeries.add(new Millisecond(new Date()), nfaces);
        cumulativeSeries.add(new Millisecond(new Date()), cumulativeFaces);
    }


    public static void main(String[] args) throws Exception {
        new VideoStreamDashboard();
        run();
    }
}