package org.team1c.avs;

import java.util.Map;

import org.apache.kafka.clients.producer.Partitioner;
import org.apache.kafka.common.Cluster;


public class AvsCustomPartitioner implements Partitioner {

  public AvsCustomPartitioner() {}

  @Override
  public void configure(Map<String, ?> configs) {}

  @Override
  public int partition(String topic, Object key, byte[] keyBytes, Object value, byte[] valueBytes,
      Cluster cluster) {
    return Integer.parseInt((String)key);
  }

  @Override
  public void close() {}

}