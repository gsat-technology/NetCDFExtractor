package com.gsat.netcdfextractor;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gsat.netcdfextractor.aws.S3Operations;
import com.gsat.netcdfextractor.client.Downloader;
import com.gsat.netcdfextractor.domain.configuration.Config;
import com.gsat.netcdfextractor.domain.netcdf.NetCDFMetadata;
import com.gsat.netcdfextractor.domain.request.Event;
import com.gsat.netcdfextractor.domain.request.LambdaProxyRequest;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.gsat.netcdfextractor.domain.response.LambdaProxyResponse;
import com.gsat.netcdfextractor.domain.response.Locations;
import com.gsat.netcdfextractor.domain.response.ResponseBody;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;


import java.io.*;
import java.net.URLEncoder;
import java.util.*;

public class Handler implements RequestStreamHandler {

    S3Operations s3Operations;
    ObjectMapper mapper;
    String netcdfKeyName;
    String ncTmpFile;
    String headerTxtKey;
    String metadataKey;
    String publicWebsiteUrl;
    Map<String, String> responseHeaders;

    public Handler(Config config) {

        ncTmpFile = "/tmp/downloaded.nc";
        headerTxtKey = "header.txt";
        metadataKey = "metadata.json";

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

        publicWebsiteUrl = System.getenv("publicWebsiteUrl") != null
                ? System.getenv("publicWebsiteUrl")
                : config.environmentVariables.publicWebsiteUrl;

        this.mapper = new ObjectMapper();

        this.responseHeaders = new HashMap<String, String>();
        responseHeaders.put("Access-Control-Allow-Origin", "*");
        responseHeaders.put("ContentType", "application/json");
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

    private Boolean allObjectsExist(List<String> keys) {

        for (String key : keys) {
            if(!s3Operations.objectExists(key)) {
                return false;
            }
        }

        return true;
    }

    private String fqdn(String key) {
        return this.publicWebsiteUrl + "/" + key;
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


        String encUrl = enc(event.url);
        String[] urlSplit = event.url.split("/");

        String netcdfKey = encUrl + "/" + urlSplit[urlSplit.length-1];
        String headerKey = encUrl + "/" + this.headerTxtKey;
        String metadataKey = encUrl + "/" + this.metadataKey;


        if (!allObjectsExist(Arrays.asList(netcdfKey, metadataKey, headerKey)) || !event.cache) {

            this.s3Operations.urlToS3(netcdfKey, event.url);

            String tmpFile = this.s3Operations.downloadObjectFromS3(netcdfKey, this.ncTmpFile);
            String ncHeader = NetCDF.read(tmpFile);

            this.s3Operations.stringToS3(headerKey, ncHeader);

            DateTime dt = new DateTime();

            NetCDFMetadata netcdfMetadata = new NetCDFMetadata(
                    new File(tmpFile).length(),
                    ISODateTimeFormat.dateTime().print(dt));

            try {
                this.s3Operations.stringToS3(metadataKey, mapper.writeValueAsString(netcdfMetadata));
            } catch(com.fasterxml.jackson.core.JsonProcessingException e) {
                System.out.println(e);
            }
        }

        ResponseBody body = new ResponseBody(new Locations(
                fqdn(netcdfKey),
                fqdn(metadataKey),
                fqdn(headerKey)));

        LambdaProxyResponse response = new LambdaProxyResponse(body, 200, responseHeaders);

    }
}
