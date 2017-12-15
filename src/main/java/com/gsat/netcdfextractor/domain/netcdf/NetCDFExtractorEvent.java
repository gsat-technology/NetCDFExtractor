package com.gsat.netcdfextractor.domain.netcdf;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NetCDFExtractorEvent {

    @JsonProperty("url")
    public String url;

    @JsonProperty("cache")
    public Boolean cache;

    public NetCDFExtractorEvent(String url, Boolean cache) {
        this.url = url;

        if (cache == null) {
            this.cache = true;
        } else {
            this.cache = cache;
        }
    }
}

