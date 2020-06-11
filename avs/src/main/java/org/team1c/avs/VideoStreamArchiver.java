// File:         VideoStreamArchiver.java
// Author:       Ho Yi Ping, Khaifung Lim, Fernando Ng and Chong Chiu Gin
// Last Modified Date:  6-June-2020         
// 
// Description:   stores the video and other information into archive
package org.team1c.avs;

import java.util.Properties;
import java.util.Collections;
import java.util.List;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import org.team1c.avs.Util;

/**
 * This class is used to store video and other relevant information into archive
 * 
 */
public class VideoStreamArchiver {

    public static final String PROPERTIES_FP = "./properties/archiver.properties";

    public static void main(String[] args) throws Exception {
        Properties consumerProp = Util.getProperties(PROPERTIES_FP);

        // query kafka cluster for number of cameras
        Consumer<String, String> dummyConsumer = new KafkaConsumer<String, String>(consumerProp);
        dummyConsumer.subscribe(
            Collections.singletonList(consumerProp.getProperty("kafka.topic"))
        );
        List<Integer> cameraIds = Util.getCameraIds(
            dummyConsumer, 
            consumerProp.getProperty("kafka.topic")
        );

        // create a thread for each camera
        for (int camId : cameraIds) {
            System.out.println("Starting thread for camera: " + Integer.toString(camId));
            Thread t = new Thread(new VideoStreamWriter(
                camId,
                consumerProp
            ));
            t.start();
        }
    }
}