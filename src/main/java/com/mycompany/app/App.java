package com.mycompany.app;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;
import java.io.FileInputStream;

import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import org.jets3t.service.CloudFrontService;
import org.jets3t.service.utils.ServiceUtils;

// snippet-end:[presigned.java2.generatepresignedurl.import]

/**
 * Before running this Java V2 code example, set up your development environment, including your credentials.
 *
 * For more information, see the following documentation topic:
 *
 * https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/get-started.html
 */

public class App {

    public static void main(String[] args) {

        String bucketName = "bucket-test";
        String keyName = "s3-demo.txt";
        ProfileCredentialsProvider credentialsProvider = ProfileCredentialsProvider.create();
        Region region = Region.AP_NORTHEAST_1;
        S3Presigner presigner = S3Presigner.builder()
                .region(region)
                .credentialsProvider(credentialsProvider)
                .build();

        signBucket(presigner, bucketName, keyName);
        presigner.close();
    }

    // snippet-start:[presigned.java2.generatepresignedurl.main]
    public static void signBucket(S3Presigner presigner, String bucketName, String keyName) {

        try {
            PutObjectRequest objectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(keyName)
                    .contentType("text/plain")
                    .build();

            PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(10))
                    .putObjectRequest(objectRequest)
                    .build();

            PresignedPutObjectRequest presignedRequest = presigner.presignPutObject(presignRequest);
            String myURL = presignedRequest.url().toString();
            // 使用加速点代替
            myURL = myURL.replaceAll("s3.ap-northeast-1", "s3-accelerate");
            System.out.println("Presigned URL to upload a file to: " +myURL);
            System.out.println("Which HTTP method needs to be used when uploading a file: " +
                    presignedRequest.httpRequest().method());

            // Upload content to the Amazon S3 bucket by using this URL.
            URL url = presignedRequest.url();

            // Create the connection and use it to upload the new object by using the presigned URL.
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type","text/plain");
            connection.setRequestMethod("PUT");
            OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
            out.write("This text was uploaded as an object by using a presigned URL.");
            out.close();

            connection.getResponseCode();
            System.out.println("HTTP response code is " + connection.getResponseCode());

            //presign url
            String signedUrlCanned = "";
            String distributionDomain = "d285rqguoi73yy.cloudfront.net";
            String keyPairId = "K2RTGRJT7GAP1F";
            String privateKeyFilePath = "/path-to/private_key.der";
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
            } catch (Exception e) {
               e.printStackTrace();
            }
            
            System.out.println(signedUrlCanned);

        } catch (S3Exception | IOException e) {
            e.getStackTrace();
        }
    }
}