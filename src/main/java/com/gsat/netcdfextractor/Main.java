package com.gsat.netcdfextractor;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.gsat.netcdfextractor.domain.request.LambdaRequest;
import com.gsat.netcdfextractor.utils.Utils;
import org.apache.commons.io.FileUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;

public class Main {

    public static String jsonFromFile(String fileName) {

        try {
            return FileUtils.readFileToString(new File(fileName), "utf-8");
        } catch (java.io.IOException e) {
            System.out.println(fileName);
        }

        return null;
    }

    public static void main(String[] args) {
        System.out.println("starting");
        String requestFileName = "requestJSON/request1.json";
        ObjectMapper mapper = new ObjectMapper();

        HashMap envVars = new HashMap<String, String>();
        envVars.put("s3Store", "netcdf-test");
        envVars.put("publicWebsiteUrl", "https://public.website");
        envVars.put("aws_profile", "aws-gsat");
        envVars.put("aws_region", "ap-southeast-2");


        try {
            Utils.setEnvironmentVariables(envVars);
        } catch (java.lang.Exception e) {

        }

        Handler handler = new Handler();

        LambdaRequest lambdaRequest = new LambdaRequest(jsonFromFile(requestFileName));
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();

        try {
            ByteArrayInputStream inStream = new ByteArrayInputStream(mapper.writeValueAsBytes(lambdaRequest));
            handler.handleRequest(inStream, outStream, null);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            System.out.println(e);
        }

        System.out.println(outStream.toString());

    }
}
