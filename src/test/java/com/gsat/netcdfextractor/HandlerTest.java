package com.gsat.netcdfextractor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gsat.netcdfextractor.domain.netcdf.NetCDFExtractorEvent;
import com.gsat.netcdfextractor.domain.netcdf.NetCDFExtractorLocations;
import com.gsat.netcdfextractor.domain.netcdf.NetCDFExtractorResult;
import com.gsat.netcdfextractor.domain.request.LambdaRequest;
import com.gsat.netcdfextractor.domain.response.LambdaResponse;
import com.gsat.netcdfextractor.utils.Utils;
import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.skyscreamer.jsonassert.JSONAssert;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;


public class HandlerTest {

    NetCDFExtractor netCDFExtractor;
    OutputStream outputStream;
    Handler handler;
    ObjectMapper mapper;

    @Before
    public void setupMocks()
    {
        mapper = new ObjectMapper();
        netCDFExtractor = Mockito.mock(NetCDFExtractor.class);
        outputStream = new ByteArrayOutputStream();

        HashMap envVars = new HashMap<String, String>();
        envVars.put("aws_profile", "aws-gsat");
        envVars.put("aws_region", "ap-southeast-2");
        envVars.put("s3Store", "abc");
        envVars.put("publicWebsiteUrl", "xyz");
        envVars.put("maxDownloadByteSize", "0000");

        try {
            Utils.setEnvironmentVariables(envVars);
        } catch (java.lang.Exception e) {
            System.out.println("could not set up environment variables");
        }

        handler = new Handler(netCDFExtractor);
    }

    public static String jsonFromFile(String fileName) {

        try {
            return FileUtils.readFileToString(new File(fileName), "utf-8");
        } catch (java.io.IOException e) {
            System.out.println(fileName);
        }

        return null;
    }


    @Test
    public void handlerShouldReturnSuccess() throws IOException, JSONException {

        LambdaRequest lambdaRequest = new LambdaRequest(jsonFromFile("src/test/java/com/gsat/netcdfextractor/fixtures/request/lambda-success-request.json"));

        NetCDFExtractorResult result = new NetCDFExtractorResult();
        result.source = "download";
        result.locations = new NetCDFExtractorLocations("netcdf", "metadata", "header");
        result.errors = null;

        when(netCDFExtractor.handleEvent(any(NetCDFExtractorEvent.class))).thenReturn(result);

        ByteArrayInputStream inputStream = new ByteArrayInputStream(mapper.writeValueAsBytes(lambdaRequest));
        handler.handleRequest(inputStream, outputStream, null);

        System.out.println(outputStream.toString());
        LambdaResponse response = mapper.readValue(outputStream.toString(), LambdaResponse.class);
        JSONAssert.assertEquals(jsonFromFile("src/test/java/com/gsat/netcdfextractor/fixtures/response/lambda-success-response.json"), response.body, false);
        assertEquals(200, response.statusCode);
    }


    @Test
    public void handlerShouldReturnErrorBadBodyRequest() throws IOException {

        LambdaRequest lambdaRequest = new LambdaRequest(jsonFromFile("src/test/java/com/gsat/netcdfextractor/fixtures/request/lambda-client-error-request.json"));

        ByteArrayInputStream inputStream = new ByteArrayInputStream(mapper.writeValueAsBytes(lambdaRequest));
        handler.handleRequest(inputStream, outputStream, null);

        LambdaResponse response = mapper.readValue(outputStream.toString(), LambdaResponse.class);
        assertEquals("could not parse request body", response.body);
        assertEquals(400, response.statusCode);
    }

    @Test
    public void handlerShouldReturnErrorBadAPIRequest() throws IOException {

        InputStream inputStream = new ByteArrayInputStream("this is a bad lambda proxy request".getBytes(StandardCharsets.UTF_8.name()));
        handler.handleRequest(inputStream, outputStream, null);

        LambdaResponse response = mapper.readValue(outputStream.toString(), LambdaResponse.class);
        assertEquals("could not parse request body", response.body);
        assertEquals(400, response.statusCode);
    }
}
