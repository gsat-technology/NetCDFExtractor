package com.gsat.netcdfextractor.aws;


import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.util.IOUtils;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.gsat.netcdfextractor.client.Downloader;
import org.apache.commons.io.FileUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class S3Module {

    private String bucket;
    private Downloader downloader;
    private AmazonS3 s3Client;

    @Inject
    public S3Module(@Named("s3Store") String bucket, Downloader downloader, AmazonS3 s3Client) {
        this.bucket = bucket;
        this.downloader = downloader;
        this.s3Client = s3Client;
    }

    public void urlToS3(String targetKey, String url) {
        InputStream inputStream = downloader.urlToInputStream(url);
        this.inputStreamToS3(inputStream, targetKey);
    }

    private void inputStreamToS3(InputStream inputStream, String targetKey) {
        try {
            byte[] bytes = IOUtils.toByteArray(inputStream);
            inputStream.close();

            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(bytes.length);

            PutObjectResult putObjectResult = s3Client.putObject(this.bucket, targetKey, byteArrayInputStream, metadata);

        } catch (java.io.IOException e) {
            System.out.println(e);
        }
    }

    public String downloadObjectFromS3(String key, String targetFileName) {
        S3Object object = this.s3Client.getObject(new GetObjectRequest(this.bucket, key));
        InputStream objectData = object.getObjectContent();

        try {
            FileUtils.copyInputStreamToFile(objectData, new File(targetFileName));
        } catch (java.io.IOException ioe) {
            System.out.println(ioe);
            return null;
        }

        return targetFileName;
    }

    public void stringToS3(String targetKey, String content) {
        try {
            InputStream inputStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8.name()));
            this.inputStreamToS3(inputStream, targetKey);
        } catch (java.io.UnsupportedEncodingException uee) {
            System.out.println(uee);
        }
    }

    public Boolean objectExists(String targetKey) {
        return this.s3Client.doesObjectExist(this.bucket, targetKey);
    }
}
