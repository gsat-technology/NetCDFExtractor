package com.gsat.netcdfextractor.domain.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ResponseBody {

    @JsonProperty("locations")
    public Locations locations;

    public ResponseBody(Locations locations) {
        this.locations = locations;
    }
}
