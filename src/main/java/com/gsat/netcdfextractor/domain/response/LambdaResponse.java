package com.gsat.netcdfextractor.domain.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gsat.netcdfextractor.domain.netcdf.NetCDFExtractorResult;

import java.util.Map;

public class LambdaResponse {

    @JsonProperty("body")
    public String body;

    @JsonProperty("statusCode")
    public int statusCode;

    @JsonProperty("headers")
    public Map<String, String> headers;

    public LambdaResponse(String body, int statusCode, Map<String, String> headers) {
        this.body = body;
        this.statusCode = statusCode;
        this.headers = headers;
    }

    public LambdaResponse() {

    }
}
