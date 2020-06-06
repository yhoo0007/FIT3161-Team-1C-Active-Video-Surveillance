package org.team1c.avs;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.RecordMetadata;


public class AvsPublishCallback implements Callback {
    private String cameraId;

    /**
     * constructor method to create AvsPublishCallback class which store the correspond camera id
     * @param cameraId
     */
    public AvsPublishCallback(String cameraId) {
        super();
        this.cameraId = cameraId;
    }

    @Override
    /**
     * this method is to check if there is metadata and exception
     * if there is, there is error and will output to terminal the error information
     * 
     * @param metadata
     * @param exception
     */
    public void onCompletion(RecordMetadata metadata, Exception exception) {
        if (metadata != null) {
            System.out.println("No metadata received from publishing frame; cam ID: " + cameraId);
        }
        if (exception != null) {
            exception.printStackTrace();
        }
    }
}