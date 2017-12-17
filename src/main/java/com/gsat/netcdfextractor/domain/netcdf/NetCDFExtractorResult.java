package com.gsat.netcdfextractor.domain.netcdf;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NetCDFExtractorResult {
    @JsonProperty("source")
    public String source;

    @JsonProperty("locations")
    public NetCDFExtractorLocations locations;

    @JsonProperty("error")
    public ArrayList<String> errors;

    public NetCDFExtractorResult() {

    }
}
