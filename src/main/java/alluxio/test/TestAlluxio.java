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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
  String alluxioFilePath = DEFAULT_PATH;
  boolean keepOpen = false;
  boolean go = false;
  public static void main(String[] args) throws IOException, InterruptedException {
    final TestAlluxio ta = new TestAlluxio();
    Options opts = new Options();
    opts.addOption("h", false, "help");
    opts.addOption("path", true, "alluxio file path. default " + DEFAULT_PATH);
    opts.addOption("go", false, "after everything done, terminate.");
    opts.addOption("keepOpen", false, "keey open.");
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
            ta.alluxioFilePath = cl.getOptionValue("path");
          }
          ta.go = cl.hasOption("go");
        }
      } else {
        System.out.println("You are using default argument, use -h argument to get help.");
      }
    } catch (ParseException e) {
      e.printStackTrace();
      return;
    }
    System.out.println("path : " + ta.alluxioFilePath);
    System.out.println("go : " + ta.go);
    System.out.println("keepOpen : " + cl.hasOption("keepOpen"));
    System.out.println("start test:");
    //    AlluxioURI path = new AlluxioURI("/ns2/user/maobaolong/mbltest/mbltest.txt");
    ExecutorService fixedThreadPool = Executors.newFixedThreadPool(4000);
    for (int i = 0; i < 4000; i++) {
      if (i % 40 == 0) {
        Thread.sleep(10 * 1000L);
        System.out.printf("precent %d  .\n", i / 40);
      }
      fixedThreadPool.execute(new Runnable() {
        @Override
        public void run() {
          ta.doTest();
        }
      });

    }
    System.out.println("test Ok!");
    while (!ta.go) {
      System.out.println("I am alive!");
      Thread.sleep(60 * 1000L);
    }
  }

  public void doTest() {
    try {
      Path path = new Path(alluxioFilePath);
      Configuration configuration = new Configuration();
      FileSystem fileSystem = path.getFileSystem(configuration);

      long size = fileSystem.getFileStatus(path).getLen();
//      System.out.println("size: " + size);
      FSDataInputStream inputStream = fileSystem.open(path);
      if (!keepOpen)
        inputStream.close();
      /*while (!go) {
        //System.out.println("I am alive!");
        Thread.sleep(60 * 1000L);
      }*/
    }catch(IOException e){
      e.printStackTrace();
    } /*catch (InterruptedException e) {
      e.printStackTrace();
    }*/
  }
}
