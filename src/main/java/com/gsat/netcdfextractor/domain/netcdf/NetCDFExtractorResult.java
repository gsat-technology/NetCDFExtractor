package com.gsat.netcdfextractor.domain.netcdf;

import com.fasterxml.jackson.annotation.JsonProperty;

public class NetCDFExtractorResult {
    @JsonProperty("source")
    public String source;

    @JsonProperty("locations")
    public Locations locations;

    public NetCDFExtractorResult(String source, Locations locations) {
        this.source = source;
        this.locations = locations;
    }
}
