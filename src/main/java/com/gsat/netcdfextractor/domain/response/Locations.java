package com.gsat.netcdfextractor.domain.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Locations {

    @JsonProperty("netcdf")
    public String netcdf;

    @JsonProperty("metadata")
    public String metadata;

    @JsonProperty("header")
    public String header;

    public Locations(String netcdf, String metadata, String header) {
        this.netcdf = netcdf;
        this.metadata = metadata;
        this.header = header;
    }
}