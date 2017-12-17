package com.gsat.netcdfextractor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.gsat.netcdfextractor.aws.DownloadFailedException;
import com.gsat.netcdfextractor.aws.S3Module;
import com.gsat.netcdfextractor.core.NetCDF;
import com.gsat.netcdfextractor.domain.netcdf.NetCDFExtractorLocations;
import com.gsat.netcdfextractor.domain.netcdf.NetCDFExtractorEvent;
import com.gsat.netcdfextractor.domain.netcdf.NetCDFExtractorResult;
import com.gsat.netcdfextractor.domain.netcdf.NetCDFExtractorMetadata;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import sun.nio.ch.Net;

import java.io.File;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NetCDFExtractor {

    public static String NC_TMP_FILE = "/tmp/downloaded.nc";
    public static String HEADER_TXT_KEY = "header.txt";
    public static String METADATA_KEY = "metadata.json";

    private String publicWebsiteUrl;
    private S3Module s3module;
    private ObjectMapper mapper;
    private NetCDF netCDF;

    @Inject
    public NetCDFExtractor(
            @Named("publicWebsiteUrl") String publicWebsiteUrl,
            S3Module s3module,
            ObjectMapper mapper,
            NetCDF netCDF
    ) {
        this.publicWebsiteUrl = publicWebsiteUrl;
        this.s3module = s3module;
        this.mapper = mapper;
        this.netCDF = netCDF;
    }

    private String enc(String in) {
        try {
            return URLEncoder.encode(in, "UTF-8");
        } catch(java.io.UnsupportedEncodingException e) {
            return null;
        }
    }

    private Boolean allObjectsExist(List<String> keys) {

        for (String key : keys) {
            if(!s3module.objectExists(key)) {
                return false;
            }
        }

        return true;
    }

    private String fqdn(String key) {
        return this.publicWebsiteUrl + "/" + key;
    }


    public NetCDFExtractorResult handleEvent(NetCDFExtractorEvent event) {

        NetCDFExtractorResult result = new NetCDFExtractorResult();

        System.out.println("url: " + event.url);
        System.out.println("cache: " + event.cache);

        String encUrl = enc(event.url);
        String[] urlSplit = event.url.split("/");

        String netcdfKey = encUrl + "/" + urlSplit[urlSplit.length-1];
        String headerKey = encUrl + "/" + HEADER_TXT_KEY;
        String metadataKey = encUrl + "/" + METADATA_KEY;

        NetCDFExtractorLocations locations = new NetCDFExtractorLocations(
                fqdn(netcdfKey),
                fqdn(metadataKey),
                fqdn(headerKey)
        );

        ArrayList<String> errors = new ArrayList<>();

        String source = "cache";

        if (!allObjectsExist(Arrays.asList(netcdfKey, metadataKey, headerKey)) || !event.cache) {

            try {
                this.s3module.urlToS3(netcdfKey, event.url);
                String tmpFile = this.s3module.downloadObjectFromS3(netcdfKey, NC_TMP_FILE);
                String ncHeader = netCDF.read(tmpFile);

                this.s3module.stringToS3(headerKey, ncHeader);

                DateTime dt = new DateTime();

                NetCDFExtractorMetadata netcdfMetadata = new NetCDFExtractorMetadata(
                        new File(tmpFile).length(),
                        ISODateTimeFormat.dateTime().print(dt));

                try {
                    this.s3module.stringToS3(metadataKey, this.mapper.writeValueAsString(netcdfMetadata));
                } catch(com.fasterxml.jackson.core.JsonProcessingException e) {
                    System.out.println(e);
                }

                source = "download";

            } catch (DownloadFailedException e) {
                errors.add(e.getMessage());
            }
        }

        if (errors.size() > 0) {
            result.errors = errors;
        } else {
            result.source = source;
            result.locations = locations;
        }

        return result;
    }
}
