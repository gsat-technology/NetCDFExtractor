package com.gsat.netcdfextractor.domain.request;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LambdaProxyRequest {

    @JsonProperty("body")
    @Getter
    @Setter
    public String body;

    public LambdaProxyRequest(@JsonProperty("body") String body) {
        this.body = body;
    }
}
