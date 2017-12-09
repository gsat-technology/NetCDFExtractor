package com.gsat.netcdfextractor.domain.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ResponseBody {

    @JsonProperty("source")
    public String source;

    @JsonProperty("locations")
    public Locations locations;

    public ResponseBody(String source, Locations locations) {
        this.source = source;
        this.locations = locations;
    }
}
