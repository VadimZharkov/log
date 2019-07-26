# Log

A simple logger where you can easily customize the output and format of the message.

Usage example:

```java
Log.d("Test1");
```
This will produce the next output by default:

25-07-19:14:19:952 [DEBUG] Main.java:main:16 - Test1

Or you can use custom log:
```java
Log log = new Log();
log.setOutput(System.out::println);
log.setFormat((level, params, message) -> level + " - " + message);
log.debug("Test2");
```
This will produce the output:

DEBUG - Test2
