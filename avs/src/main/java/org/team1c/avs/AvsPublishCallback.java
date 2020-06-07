package org.team1c.avs;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.RecordMetadata;


public class AvsPublishCallback implements Callback {
    
    private String cameraId;

    public AvsPublishCallback(String cameraId) {
        super();
        this.cameraId = cameraId;
    }

    @Override
    public void onCompletion(RecordMetadata metadata, Exception exception) {
        if (metadata != null) {
            System.out.println("No metadata received from publishing frame; cam ID: " + cameraId);
        }
        if (exception != null) {
            exception.printStackTrace();
        }
    }
}