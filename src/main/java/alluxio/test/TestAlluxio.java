package alluxio.test;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * alluxio.test.
 *
 * @author maobaolong@jd.com
 * @version 1.0.0
 */
public class TestAlluxio {
  private static final int TEST_COUNT = 100;
  private static final int TEST_INDEX = 0;
  private static final String DEFAULT_PATH =
      "alluxio://127.0.0.1:19998/ns2/user/maobaolong/mbltest/mbltest.txt";

  String alluxioFilePath = DEFAULT_PATH;
  int testCount = TEST_COUNT;
  int testIndex = TEST_INDEX;
  boolean keepOpen = false;
  boolean go = false;

  public void doTest0() {
    try {
      Path path = new Path(alluxioFilePath);
      Configuration configuration = new Configuration();
      FileSystem fileSystem = path.getFileSystem(configuration);
      long size = fileSystem.getFileStatus(path).getLen();
//      System.out.println("size: " + size);
      FSDataInputStream inputStream = fileSystem.open(path);
      if (!keepOpen) {
        inputStream.close();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

//  public void doTest1() {
//    try {
//      alluxio.client.file.FileSystem fileSystem = alluxio.client.file.FileSystem.Factory.get();
//      System.out.println("fileSystem = " + fileSystem);
//      Closer closer = Closer.create();
//      try {
//        OpenFileOptions options = OpenFileOptions.defaults().setReadType(ReadType.CACHE_PROMOTE);
//        FileInStream in = closer.register(((alluxio.client.file.FileSystem) fileSystem).openFile
//            (new AlluxioURI("/ns2/user/maobaolong/mbltest/mbltest.txt"), options));
//        byte[] buf = new byte[8 * Constants.MB];
//        while (in.read(buf) != -1) {
//        }
//        in.close();
//        in = null;
//      } catch (Exception e) {
//        throw closer.rethrow(e);
//      } finally {
//        closer.close();
//      }
//    } catch (IOException e) {
//      e.printStackTrace();
//    }
//  }


  public static void main(String[] args) throws IOException, InterruptedException {
    final TestAlluxio ta = new TestAlluxio();
    Options opts = new Options();
    opts.addOption("h", false, "help");
    opts.addOption("path", true, "alluxio file path. default " + DEFAULT_PATH);
    opts.addOption("go", false, "after everything done, terminate it. Otherwise, main thread "
         + "will not quit.");
    opts.addOption("keepOpen", false, "keep open, open the file and do not close it.");
    opts.addOption("testCount", true, "test count default " + TEST_COUNT + ".");
//    opts.addOption("testIndex", true, "test index default " + TEST_INDEX + ".");
    DefaultParser parser = new DefaultParser();
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
          ta.keepOpen = cl.hasOption("keepOpen");
          if (cl.hasOption("testCount")) {
            ta.testCount = Integer.parseInt(cl.getOptionValue("testCount"));
          }
          if (cl.hasOption("testIndex")) {
            ta.testIndex = Integer.parseInt(cl.getOptionValue("testIndex"));
          }
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
    System.out.println("keepOpen : " + ta.keepOpen);
    System.out.println("testCount : " + ta.testCount);
    System.out.println("testIndex : " + ta.testIndex);

    System.out.println("After 3 second , test will started !");
    for (int i = 0; i < 3; i++) {
      System.out.println(i);
      Thread.sleep(1000);
    }

    System.out.println("start test:");
    ExecutorService fixedThreadPool = Executors.newFixedThreadPool(1);
    int step = ta.testCount / 100;
    //use CountDownLatch to ensure that until every test finish shutdown the pool.
    final CountDownLatch endLatch = new CountDownLatch(ta.testCount);
    for (int i = 0; i < ta.testCount; i++) {
      if (i % step == 0) {
        Thread.sleep(100);
        System.out.printf(" %d / %d .\n", i / step, 100);
      }
      fixedThreadPool.execute(new Runnable() {
        @Override
        public void run() {
          if (ta.testIndex == 0) {
            ta.doTest0();
          } /*else if (ta.testIndex == 1) {
            ta.doTest1();
          }*/
          endLatch.countDown();
        }
      });
    }
    endLatch.await();
    fixedThreadPool.shutdown();
    System.out.println("test Ok!");
    while (!ta.go) {
      System.out.println("I am alive!");
      Thread.sleep(60 * 1000L);
    }
  }
}
