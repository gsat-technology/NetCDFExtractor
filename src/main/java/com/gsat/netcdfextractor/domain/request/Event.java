package com.gsat.netcdfextractor.domain.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Event {
    @JsonProperty("url")
    public String url;

    @JsonProperty("cache")
    public Boolean cache;

    public Event() {
        cache = true;
    }
}
