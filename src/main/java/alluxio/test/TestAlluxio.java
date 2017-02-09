package alluxio.test;


import alluxio.AlluxioURI;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import java.io.IOException;

/**
 * alluxio.test <br>
 * <p>
 * Copyright: Copyright (c) 17-2-9 上午11:01
 * <p>
 * Company: 京东
 * <p>
 *
 * @author maobaolong@jd.com
 * @version 1.0.0
 */
public class TestAlluxio {
  public static void main(String[] args) throws IOException {
    System.out.println("go:");
//    AlluxioURI path = new AlluxioURI("/ns2/user/maobaolong/mbltest/mbltest.txt");
    Path path = new Path("alluxio://172.16.150.101:19998/ns2/user/maobaolong/mbltest/mbltest.txt");
    Configuration configuration = new Configuration();
    FileSystem fileSystem = path.getFileSystem(configuration);

    long size = fileSystem.getFileStatus(path).getLen();
    System.out.println(size);
//    FSDataInputStream inputStream = fileSystem.open(path);
  }
}
