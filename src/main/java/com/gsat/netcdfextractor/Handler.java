package com.gsat.netcdfextractor;


import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.gsat.netcdfextractor.domain.netcdf.NetCDFExtractorEvent;
import com.gsat.netcdfextractor.domain.netcdf.NetCDFExtractorResult;
import com.gsat.netcdfextractor.domain.request.LambdaRequest;
import com.gsat.netcdfextractor.domain.response.LambdaResponse;
import com.gsat.netcdfextractor.guice.GuiceModule;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Handler implements RequestStreamHandler {

    private final Map<String, String> responseHeaders;
    private final NetCDFExtractor netCDFExtractor;
    private final ObjectMapper mapper;

    public Handler() {
        Map<String, String> map = new HashMap<>();
        map.put("Access-Control-Allow-Origin", "*");
        map.put("ContentType", "application/json");
        this.responseHeaders = Collections.unmodifiableMap(map);

        AmazonS3 s3Client = (System.getenv("aws_region") != null && System.getenv("aws_profile") != null) ? AmazonS3ClientBuilder.standard()
                .withCredentials(new ProfileCredentialsProvider(System.getenv("aws_profile")))
                .withRegion(System.getenv("aws_region"))
                .build() : AmazonS3ClientBuilder.standard().build();

        Injector injector = Guice.createInjector(
                new GuiceModule(System.getenv("s3Store"), System.getenv("publicWebsiteUrl"), s3Client)
        );
        this.netCDFExtractor = injector.getInstance(NetCDFExtractor.class);

        this.mapper = new ObjectMapper();
    }

    public void handleRequest(InputStream inputStream, OutputStream outStream, Context context) {

        NetCDFExtractorEvent event = null;

        try {
            LambdaRequest lambdaRequest = this.mapper.readValue(inputStream, LambdaRequest.class);

            if (lambdaRequest.body != null) {

                event = this.mapper.readValue(lambdaRequest.body, NetCDFExtractorEvent.class);
                NetCDFExtractorResult netCDFExtractorResult = null;

                if (event != null) {
                    netCDFExtractorResult = netCDFExtractor.handleEvent(event);
                }

                LambdaResponse response = new LambdaResponse(mapper.writeValueAsString(netCDFExtractorResult), 200, this.responseHeaders);

                try {
                    this.mapper.writeValue(outStream, response);
                } catch (java.io.IOException e) {
                    System.out.println(e);
                }
            } else {
                this.mapper.writeValue(outStream, "bad request - could not parse post body");
            }

        } catch (java.io.IOException e) {
            LambdaResponse response = new LambdaResponse("bad request", 400, this.responseHeaders);
        }
    }
}
