// File:         HdfsWriter.java
// Author:       Ho Yi Ping, Khaifung Lim, Fernando Ng and Chong Chiu Gin
// Last Modified Date:  5-June-2020         
// 
// Description:  This is a Hdfs writer which will be used to copy from localfile into hdfs file.

package org.team1c.avs;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.util.Tool;


public class HdfsWriter {
    private FileSystem fs;

    /**
     * This is a constructor method to create a new Hdfs writer
     * 
     * @throws IOException
     */
    public HdfsWriter() throws IOException {
        Configuration conf = new Configuration();
        conf.addResource(new Path(System.getenv("HADOOP_HOME") + "/etc/hadoop/core-site.xml"));
        conf.addResource(new Path(System.getenv("HADOOP_HOME") + "/etc/hadoop/hdfs-site.xml"));
        this.fs = FileSystem.get(conf);
    }
    /**
     * 
     * this method will copy the file from localFp path into hdfsFp path
     * 
     * @param localFp path of local file in string
     * @param hdfsFp path of hdfs in string
     * @throws IOException
     */
    public void send(String localFp, String hdfsFp) throws IOException {
        fs.copyFromLocalFile(new Path(localFp), new Path(hdfsFp));
    }

}