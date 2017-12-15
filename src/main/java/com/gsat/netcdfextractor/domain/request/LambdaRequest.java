package com.gsat.netcdfextractor.domain.request;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LambdaRequest {

    @JsonProperty("body")
    @Getter
    @Setter
    public String body;

    public LambdaRequest(@JsonProperty("body") String body) {
        this.body = body;
    }
}
