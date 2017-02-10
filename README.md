## Description

A tool to test Alluxio thread pool full problem.

I use `alluxio-core-client-1.5.0-SNAPSHOT-jar-with-dependencies.jar` to import class what I need.
After started the test program, it give you 3 second to ensure your argument then, it will started 
a specific count thread, every thread will do the same test.

In test body, we open a file and close it.

## How to build it

You can get the source to by clone it or download the zip.

Use maven to build it by execute the follow command.

```bash
$ mvn install
```

After build finish, you can execute `/alluxio-test.sh -h` to verify your build is successful.

## How to use it

You can use the `alluxio-test.sh` to execute the program. use `-h` argument to get more help 
information.



```bash

$ ./alluxio-test.sh -h
usage: Options
 -go                after everything done, terminate it. Otherwise, main thread will not quit.
 -h                 help
 -keepOpen          keep open, open the file and do not close it.
 -path <arg>        alluxio file path. default
                    alluxio://127.0.0.1:19998/ns2/user/maobaolong/mbltest/mbltest.txt
 -testCount <arg>   test count default 100.
 
```

## Test

### Test1 alluxio

First, execute script `./alluxio-test.sh`, you can see the output as follow:

```bash
$ ./alluxio-test.sh -file alluxio://127.0.0.1:19998/ns2/user/maobaolong/mbltest/mbltest.txt
path : alluxio://127.0.0.1:19998/ns2/user/maobaolong/mbltest/mbltest.txt
go : false
keepOpen : false
testCount : 100
testIndex : 0
After 3 second , test will started !
0
1
2
start test:
 0 / 100 .
 1 / 100 .
 ....
 99 / 100 .
test Ok!
I am alive!
```
Because of there is none `-go` flag, so after the test finish, main thread will still alive and say 
`I am alive` every minute. The default test count is 100. so there are 100 thread to do the test, 
every test open the file `alluxio://127.0.0.1:19998/ns2/user/maobaolong/mbltest/mbltest.txt` and 
close it. 

We can think about it, although there are 100 thread executed the `open` and `close` function, the 
alluxio master must close all thread, because we close it. But it is different from what I expected.

Use `jps` to get the pid of current `TestAlluxio` process is `27055`.

Use `netstat ` to view the alluxio rpc server connection information.

```bash
$ netstat -apn | grep 19998|grep tcp6 
tcp6       0      0 127.0.0.1:9426          127.0.0.1:19998         ESTABLISHED 27055/java          
tcp6       0      0 127.0.0.1:9428          127.0.0.1:19998         ESTABLISHED 27055/java          
tcp6       0      0 127.0.0.1:9424          127.0.0.1:19998         ESTABLISHED 27055/java
```

The `netstat` result say there are 3 thread open 3 port connected to port `19998` use tcp6 and the 
state is `ESTABLISHED`.

After we use `CTRL + C` to exit the TestAlluxio process, we re-execute the `netstat` again. You can 
see the 3 thread's state all changed to `TIME_WAIT`. After a minute the 3 thread gone. 

```bash
$ netstat -apn | grep 19998|grep tcp6
tcp6       0      0 127.0.0.1:9426          127.0.0.1:19998         TIME_WAIT   -                   
tcp6       0      0 127.0.0.1:9428          127.0.0.1:19998         TIME_WAIT   -                   
tcp6       0      0 127.0.0.1:9424          127.0.0.1:19998         TIME_WAIT   -

$ netstat -apn | grep 19998|grep tcp6
$
```

### Test2 hdfs

In the test, you must get your hadoop cluster namenode ip and port. Usually, default the port 8021.

First, execute script `./alluxio-test.sh`, change to your namenode ip and port, 
you can see the output as follow:

```bash 
$ ./alluxio-test.sh -path hdfs://172.111.112.121:8021/user/maobaolong/mbltest/mbltest.txt
path : hdfs://172.111.112.121:8021/user/maobaolong/mbltest/mbltest.txt
go : false
keepOpen : false
testCount : 100
testIndex : 0
After 3 second , test will started !
0
1
2
start test:
 0 / 100 .
 1 / 100 .
 ....
 98 / 100 .
  99 / 100 .
 test Ok!
 I am alive!
 I am alive!
 I am alive!
```

During the output displaying, you must execute `netstat` in another terminal,

```bash
$ netstat -apn | grep 8021|grep tcp6     
tcp6       0      0 172.111.112.101:26920    172.111.112.121:8021     ESTABLISHED 27623/java
```

The `172.111.112.101` is local ip, the 172.111.112.121 is namenode ip.

Although testAlluxio process still running and output `I am alive!` every minute, but after we 
re-execute `netstat`, we will see the state is `TIME_WAIT`:
 
```bash
$ netstat -apn | grep 8021|grep tcp6
tcp6       0      0 172.111.112.101:26920    172.111.112.121:8021     TIME_WAIT   -
```
And after a minite, re-execute `netstat`, the connection has gone although testAlluxio process still
 running .
 
 
##Test conclusion

The handle function of the file open and close between hdfs and Alluxio are different, hdfs namenode can 
close a unused connection automatically, but alluxio master hold the connection until the client 
process terminate. So if there are a lot of client connect to alluxio master and keep alive, 
alluxio's thread pool will full although every client call close in the right way.  


