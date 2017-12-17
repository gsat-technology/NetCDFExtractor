package com.gsat.netcdfextractor.domain.netcdf;

import com.fasterxml.jackson.annotation.JsonProperty;

public class NetCDFExtractorLocations {

    @JsonProperty("netcdf")
    public String netcdf;

    @JsonProperty("metadata")
    public String metadata;

    @JsonProperty("header")
    public String header;

    public NetCDFExtractorLocations(String netcdf, String metadata, String header) {
        this.netcdf = netcdf;
        this.metadata = metadata;
        this.header = header;
    }
}
