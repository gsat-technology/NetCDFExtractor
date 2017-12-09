package com.gsat.netcdfextractor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.gsat.netcdfextractor.domain.configuration.Config;
import com.gsat.netcdfextractor.domain.request.LambdaProxyRequest;
import org.apache.commons.io.FileUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;

public class Main {

    public static String jsonFromFile(String fileName) {

        try {
            return FileUtils.readFileToString(new File(fileName), "utf-8");
        } catch (java.io.IOException e) {
            System.out.println(fileName);
        }

        return null;
    }

    public static Config loadConfigFromFile(String filename) {

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

        try {
            return mapper.readValue(new File(filename), Config.class);
        } catch(java.io.IOException e) {
            return null;
        }
    }


    public static void main(String[] args) {
        System.out.println("starting");
        String requestFileName = "requestJSON/request1.json";

        ObjectMapper mapper = new ObjectMapper();
        Handler h = new Handler(loadConfigFromFile("conf/localEnvironment.yml"));

        LambdaProxyRequest lambdaProxyRequest = new LambdaProxyRequest(jsonFromFile(requestFileName));
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();

        try {
            ByteArrayInputStream inStream = new ByteArrayInputStream(mapper.writeValueAsBytes(lambdaProxyRequest));
            h.handleRequest(inStream, outStream, null);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            System.out.println(e);
        }

        System.out.println(outStream.toString());
    }
}
