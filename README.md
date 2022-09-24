# s3上传访问最佳实践

## 整体架构

架构图如下所示

![架构图](img/S3-use.drawio.png)

- 用户访问EC2获取上传文件的预签名地址，在签名中，用户可以设置上传文件所在的bucket、key、大小、格式等要求，并使用S3 Transfer Accerleation作为上传点
- 用户获取到预签名URL后，使用该URL进行对象的上传
- 用户通过CloudFront的签名URL的方式访问对应的对象

## 生成预签名URL

### 生成预签名链接

```java
PutObjectRequest objectRequest = PutObjectRequest.builder()
        .bucket(bucketName)
        .key(keyName)
        //.contentLength(new Long(61))
        //.contentType("text/plain")
        .build();

PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
        .signatureDuration(Duration.ofMinutes(10))
        .putObjectRequest(objectRequest)
        .build();

PresignedPutObjectRequest presignedRequest = presigner.presignPutObject(presignRequest);
String myURL = presignedRequest.url().toString();
```

设置上传的bucket、key。通过PutObjectPresignRequest类对象，生成10分钟有效的上传链接

### 使用S3 Tansfer Acceleration

```java
S3Presigner presigner = S3Presigner.builder()
    .region(region)
    .serviceConfiguration(S3Configuration.builder()
    .accelerateModeEnabled(true).build())
    .credentialsProvider(credentialsProvider)
    .build();
```

通过调用.accelerateModeEnabled(true)方法开启上传加速功能

## 上传文件

```java
HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.setDoOutput(true);
    connection.setRequestProperty("Content-Type","text/plain");
    connection.setRequestMethod("PUT");
    OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
    out.write("This text was uploaded as an object by using a presigned URL.");
    out.close();

    connection.getResponseCode();
    System.out.println("HTTP response code is " + connection.getResponseCode() + " response is " + connection.getResponseMessage());
```

使用java方向上传内容到上一步生成的预签名URL中

## 访问上传对象链接

```java
String signedUrlCanned = "";
String distributionDomain = "awscdn.zonepp.com";
String keyPairId = "K2RTGRJT7GAP1F";
String privateKeyFilePath = "//Users/jdhuang/git/aws/cloudfrontkey/private_key.der";
String s3ObjectKey = "s3-demo.txt";
String policyResourcePath = "https://" + distributionDomain + "/" + s3ObjectKey;
byte[] derPrivateKey = null;

try {
    derPrivateKey = ServiceUtils.readInputStreamToBytes(new FileInputStream(privateKeyFilePath));
    signedUrlCanned = CloudFrontService.signUrlCanned(
        policyResourcePath,
        keyPairId,     // Certificate identifier, 
                        // an active trusted signer for the distribution
        derPrivateKey, // DER Private key data
        ServiceUtils.parseIso8601Date("2022-11-14T22:20:00.000Z") // DateLessThan
    );
```

在CloudFront中配置好证书后，通过相应的私钥生成可访问的签名URL

## 代码执行

### 执行情况

```linux
/usr/bin/env /Library/Internet\ Plug-Ins/JavaAppletPlugin.plugin/Contents/Home/bin/java -cp /var/folders/nv/1kf6gsg93fz0q0nbpmr5hdkr0000gs/T/cp_58c9590jf4k
p564vcrqfodmsf.jar com.mycompany.app.App 
Presigned URL to upload a file to: https://jdhuang-test.s3-accelerate.amazonaws.com/s3-demo.txt?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Date=20220924T101425Z&X-Amz-SignedHeaders=host&X-Amz-Expires=600&X-Amz-Credential=AKIA5DGG3Z7QRKJV3WOX%2F20220924%2Fap-northeast-1%2Fs3%2Faws4_request&X-Amz-Signature=face886a35b95e775478d904d151a08ff06885f60398e524836e7fd315cc01c9
Which HTTP method needs to be used when uploading a file: PUT
HTTP response code is 200 response is OK
https://awscdn.zonepp.com/s3-demo.txt?Expires=1668464400&Signature=CVN2srtWXntxc-stFpgq1QjBcIRfsTvR2CiPDGEA3-~Dv1480oECIcJL6QWQ-V-Nq01~FYE3h1WggJSpPODou4hnmCzfUIvyUXueoKylRHNPNITP9lFMIrGkonj-p25ecMJjYma4Imdy8ocuhxlSawwCbM-YZiM8LYjfl2eoCPI-g1RJ80Kl0RH92ffNFlG01Jxgr9Ih62d4GIaFbkKClwGrv1eiBUUrvRl2YLWnNlNRE9H3HNJYqi-IMU7tEdvwDWVzMoGyAMSmiRGKcAinSi9PcECbyHjAiLOYAYo9xlf-AD5~nlKP4w0Kb0dWWrab68oa9trJ9K~aVkZsV1LPow__&Key-Pair-Id=K2RTGRJT7GAP1F
```

### 访问签名URL

```linux
curl 'https://awscdn.zonepp.com/s3-demo.txt?Expires=1668464400&Signature=CVN2srtWXntxc-stFpgq1QjBcIRfsTvR2CiPDGEA3-~Dv1480oECIcJL6QWQ-V-Nq01~FYE3h1WggJSpPODou4hnmCzfUIvyUXueoKylRHNPNITP9lFMIrGkonj-p25ecMJjYma4Imdy8ocuhxlSawwCbM-YZiM8LYjfl2eoCPI-g1RJ80Kl0RH92ffNFlG01Jxgr9Ih62d4GIaFbkKClwGrv1eiBUUrvRl2YLWnNlNRE9H3HNJYqi-IMU7tEdvwDWVzMoGyAMSmiRGKcAinSi9PcECbyHjAiLOYAYo9xlf-AD5~nlKP4w0Kb0dWWrab68oa9trJ9K~aVkZsV1LPow__&Key-Pair-Id=K2RTGRJT7GAP1F'
This text was uploaded as an object by using a presigned URL.  
```

## 参考链接

[生成预签名以上传对象](https://docs.aws.amazon.com/zh_cn/AmazonS3/latest/userguide/PresignedUrlUploadObject.html)

[使用S3 Tansfer Acceleration](https://docs.aws.amazon.com/zh_cn/AmazonS3/latest/userguide/transfer-acceleration.html)

[使用签名URL](https://docs.aws.amazon.com/zh_cn/AmazonCloudFront/latest/DeveloperGuide/private-content-signed-urls.html)

[使用凭证](https://docs.aws.amazon.com/zh_cn/sdk-for-java/latest/developer-guide/credentials.html)
