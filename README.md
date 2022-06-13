# AWS MSK DEMO

## use case

complie

```linux
mvn clean compile assembly:single
```

run on aws ec2 with correct iam role and security group

```linux
java -jar target/my-app-1.0-SNAPSHOT-jar-with-dependencies.jar
```

## enviroment

```linux
java -version

openjdk version "1.8.0_292"
OpenJDK Runtime Environment (AdoptOpenJDK)(build 1.8.0_292-b10)
OpenJDK 64-Bit Server VM (AdoptOpenJDK)(build 25.292-b10, mixed mode)
```

```linux
mvn --version

Apache Maven 3.8.4 (9b656c72d54e5bacbed989b64718c159fe39b537)
Maven home: /opt/homebrew/Cellar/maven/3.8.4/libexec
Java version: 1.8.0_292, vendor: AdoptOpenJDK, runtime: /Library/Java/JavaVirtualMachines/adoptopenjdk-8.jdk/Contents/Home/jre
Default locale: en_CN, platform encoding: UTF-8
OS name: "mac os x", version: "10.16", arch: "x86_64", family: "mac"
```

## reference

[AWS BLOG](https://aws.amazon.com/cn/blogs/big-data/securing-apache-kafka-is-easy-and-familiar-with-iam-access-control-for-amazon-msk/)
[AWS DOCUMENT](https://docs.aws.amazon.com/msk/latest/developerguide/create-serverless-cluster-client.html)
[CODE](https://www.tutorialspoint.com/apache_kafka/apache_kafka_simple_producer_example.htm)
# s3-upload-download-demo
