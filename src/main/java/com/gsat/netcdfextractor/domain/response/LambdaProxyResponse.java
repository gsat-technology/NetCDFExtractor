package com.gsat.netcdfextractor.domain.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public class LambdaProxyResponse {

    @JsonProperty("body")
    public ResponseBody body;

    @JsonProperty("status_code")
    public int statusCode;

    @JsonProperty("headers")
    public Map<String, String> headers;

    public LambdaProxyResponse(ResponseBody body, int statusCode, Map<String, String> headers) {
        this.body = body;
        this.statusCode = statusCode;
        this.headers = headers;
    }
}
