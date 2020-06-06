package org.team1c.avs;

import java.util.Map;

import org.apache.kafka.clients.producer.Partitioner;
import org.apache.kafka.common.Cluster;

public class AvsCustomPartitioner implements Partitioner {

  public AvsCustomPartitioner() {}

  @Override
  /**
   * 
   * @param configs
   */
  public void configure(Map<String, ?> configs) {}

  @Override
  /**
   * 
   * @param topic
   * @param key
   * @param keyBytes
   * @param value
   * @param valueBytes
   * @param cluster
   * @return
   */
  public int partition(String topic, Object key, byte[] keyBytes, Object value, byte[] valueBytes,
      Cluster cluster) {
    return Integer.parseInt((String)key);
  }


  @Override
  /**
   * 
   */
  public void close() {}

}