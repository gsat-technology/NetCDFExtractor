package com.gsat.netcdfextractor.domain.netcdf;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;


public class NetCDFExtractorEvent {

    @JsonProperty("url")
    public String url;

    @JsonProperty("cache")
    public Boolean cache;

    public NetCDFExtractorEvent() {
        cache = true;
    }
}

