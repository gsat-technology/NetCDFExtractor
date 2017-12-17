package com.gsat.netcdfextractor;

import com.gsat.netcdfextractor.utils.Utils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;


public class HandlerTest {

    NetCDFExtractor netCDFExtractor;
    OutputStream outputStream;
    Handler handler;

    @Before
    public void setupMocks()
    {
        netCDFExtractor = Mockito.mock(NetCDFExtractor.class);
        outputStream = new ByteArrayOutputStream();

        HashMap envVars = new HashMap<String, String>();
        envVars.put("aws_profile", "aws-gsat");
        envVars.put("aws_region", "ap-southeast-2");
        envVars.put("s3Store", "abc");
        envVars.put("publicWebsiteUrl", "xyz");

        try {
            Utils.setEnvironmentVariables(envVars);
        } catch (java.lang.Exception e) {
            System.out.println("could not set up environment variables");
        }

        handler = new Handler();
    }


    @Test
    public void handlerShouldReturn400()  {

        String badRequest = "{\"test\": \"this is not what a good request looks like\"}";

        try {
            InputStream inputStream = new ByteArrayInputStream(badRequest.getBytes(StandardCharsets.UTF_8.name()));
            handler.handleRequest(inputStream, outputStream, null);
        } catch (java.io.UnsupportedEncodingException e) {

        }

        assertEquals("\"bad request - could not parse post body\"", outputStream.toString());
    }

}
