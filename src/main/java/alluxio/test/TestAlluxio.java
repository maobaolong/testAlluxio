package alluxio.test;


import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
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
  private static final String DEFAULT_PATH =
      "alluxio://172.16.150.101:19998/ns2/user/maobaolong/mbltest/mbltest.txt";

  public static void main(String[] args) throws IOException, InterruptedException {
    String alluxioFilePath = DEFAULT_PATH;
    boolean go = false;

    Options opts = new Options();
    opts.addOption("h", false, "help");
    opts.addOption("path", true, "alluxio file path. default " + DEFAULT_PATH);
    opts.addOption("go", false, "after everything done, terminate.");
    BasicParser parser = new BasicParser();
    CommandLine cl;
    try {
      cl = parser.parse(opts, args);
      if (cl.getOptions().length > 0) {
        if (cl.hasOption('h')) {
          HelpFormatter hf = new HelpFormatter();
          hf.printHelp("Options", opts);
          return;
        } else {
          if (cl.hasOption("path")) {
            alluxioFilePath = cl.getOptionValue("path");
          }
          go = cl.hasOption("go");
        }
      } else {
        System.out.println("You are using default argument, use -h argument to get help.");
      }
    } catch (ParseException e) {
      e.printStackTrace();
      return;
    }
    System.out.println("path : " + alluxioFilePath);
    System.out.println("go : " + go);

    System.out.println("start test:");
    //    AlluxioURI path = new AlluxioURI("/ns2/user/maobaolong/mbltest/mbltest.txt");
    for (int i = 0; i < 4000; i++) {
      if (i % 40 == 0) {
        System.out.printf("precent %d  .\n", i / 40);
      }
      Path path = new Path(alluxioFilePath);
      Configuration configuration = new Configuration();
      FileSystem fileSystem = path.getFileSystem(configuration);

      long size = fileSystem.getFileStatus(path).getLen();
//      System.out.println("size: " + size);
      FSDataInputStream inputStream = fileSystem.open(path);
      inputStream.close();
    }

    while (!go) {
      Thread.sleep(60 * 1000L);
      System.out.println("I am alive!");
    }
  }
}
