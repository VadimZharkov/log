# Log

A simple logger where you can easily customize the output and format of the message.

Usage example:

```java
Log.d("Test log %d", 1);
```
This will produce the next output by default:
```java
26-07-19:10:08:203 (main:1) [DEBUG] Main:main:15 - Test log 1
```
Or you can use custom log:
```java
Log log = new Log();
log.setOutput(System.out::println);
log.setFormat((level, params, message) -> level + " - " + message);
log.debug("Test log 2");
```
This will produce the output:

DEBUG - Test log 2
