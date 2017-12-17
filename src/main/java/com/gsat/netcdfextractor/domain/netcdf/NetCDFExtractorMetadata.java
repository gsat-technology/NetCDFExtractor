package com.gsat.netcdfextractor.domain.netcdf;

import com.fasterxml.jackson.annotation.JsonProperty;

public class NetCDFExtractorMetadata {

    @JsonProperty("filesize")
    String filesize;

    @JsonProperty("created")
    String created;

    public NetCDFExtractorMetadata(Long filesizeBytes, String timestamp) {
        this.filesize = humanReadableByteCount(filesizeBytes);
        this.created = timestamp;
    }

    private String humanReadableByteCount(long bytes) {
        int unit = 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = ("kMGTPE").charAt(exp-1) + ("");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

}
