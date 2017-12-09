package com.gsat.netcdfextractor;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gsat.netcdfextractor.aws.S3Operations;
import com.gsat.netcdfextractor.client.Downloader;
import com.gsat.netcdfextractor.domain.configuration.Config;
import com.gsat.netcdfextractor.domain.request.Event;
import com.gsat.netcdfextractor.domain.request.LambdaProxyRequest;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.Scanner;

public class Handler implements RequestStreamHandler {

    S3Operations s3Operations;
    ObjectMapper mapper;
    String netcdfKeyName;
    String ncTmpFile;
    String headerTxtKey;

    public Handler(Config config) {
        System.out.println("constructor(config)");

        ncTmpFile = "/tmp/downloaded.nc";
        headerTxtKey = "header.txt";

        AmazonS3 s3Client = config != null ? AmazonS3ClientBuilder.standard()
                .withCredentials(new ProfileCredentialsProvider(config.aws.namedProfile))
                .withRegion(config.aws.region)
                .build() : AmazonS3ClientBuilder.standard().build();

        String s3Store = System.getenv("s3Store") != null
                ? System.getenv("s3Store")
                : config.environmentVariables.s3Store;

        System.out.println("s3Store: " + s3Store);

        this.s3Operations = new S3Operations(
                s3Client,
                new Downloader(),
                s3Store);

        this.mapper = new ObjectMapper();
    }

    public Handler() {
        this(null);
    }


    private String enc(String in) {
        try {
            return URLEncoder.encode(in, "UTF-8");
        } catch(java.io.UnsupportedEncodingException e) {
            return null;
        }
    }

    private String convertStreamToString(InputStream inputStream) {
        Scanner s = new Scanner(inputStream).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    public void handleRequest(InputStream inputStream, OutputStream outStream, Context context) {
        System.out.println("handler");
        String eventString = convertStreamToString(inputStream);
        System.out.println(eventString);

        Event event = null;

        try {
            LambdaProxyRequest proxyRequest = this.mapper.readValue(eventString, LambdaProxyRequest.class);
            event = this.mapper.readValue(proxyRequest.body, Event.class);
        } catch (java.io.IOException e) {
            System.out.println(e);
        }


        String[] urlSplit = event.url.split("/");
        String ncKey = enc(event.url) + "/" + urlSplit[urlSplit.length-1];

        System.out.println(event.cache);
        if (!this.s3Operations.objectExists(ncKey) || !event.cache) {
            this.s3Operations.urlToS3(ncKey, event.url);

            String tmpFile = this.s3Operations.downloadObjectFromS3(ncKey, this.ncTmpFile);
            String ncHeader = NetCDF.read(tmpFile);

            String headerKey = enc(event.url) + "/" + this.headerTxtKey;
            System.out.println(headerKey);
            System.out.println(ncHeader);
            this.s3Operations.stringToS3(headerKey, ncHeader);
        }


    }
}
