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

    public HdfsWriter() throws IOException {
        Configuration conf = new Configuration();
        conf.addResource(new Path(System.getenv("HADOOP_HOME") + "/etc/hadoop/core-site.xml"));
        conf.addResource(new Path(System.getenv("HADOOP_HOME") + "/etc/hadoop/hdfs-site.xml"));
        this.fs = FileSystem.get(conf);
    }

    public void send(String localFp, String hdfsFp) throws IOException {
        fs.copyFromLocalFile(new Path(localFp), new Path(hdfsFp));
    }

}