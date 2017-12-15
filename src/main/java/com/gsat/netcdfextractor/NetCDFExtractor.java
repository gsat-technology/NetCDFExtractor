package com.gsat.netcdfextractor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.gsat.netcdfextractor.aws.S3Module;
import com.gsat.netcdfextractor.core.NetCDF;
import com.gsat.netcdfextractor.domain.netcdf.Locations;
import com.gsat.netcdfextractor.domain.netcdf.NetCDFExtractorEvent;
import com.gsat.netcdfextractor.domain.netcdf.NetCDFExtractorResult;
import com.gsat.netcdfextractor.domain.netcdf.NetCDFMetadata;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

import java.io.File;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;

public class NetCDFExtractor {

    private static String NC_TMP_FILE = "/tmp/downloaded.nc";
    private static String HEADER_TXT_KEY = "header.txt";
    private static String METADATA_KEY = "metadata.json";

    private String publicWebsiteUrl;
    private S3Module s3module;
    private ObjectMapper mapper;

    @Inject
    public NetCDFExtractor(@Named("publicWebsiteUrl") String publicWebsiteUrl, S3Module s3module, ObjectMapper mapper) {
        this.publicWebsiteUrl = publicWebsiteUrl;
        this.s3module = s3module;
        this.mapper = mapper;
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

        System.out.println("url: " + event.url);
        System.out.println("cache: " + event.cache);

        String encUrl = enc(event.url);
        String[] urlSplit = event.url.split("/");

        String netcdfKey = encUrl + "/" + urlSplit[urlSplit.length-1];
        String headerKey = encUrl + "/" + HEADER_TXT_KEY;
        String metadataKey = encUrl + "/" + METADATA_KEY;

        String source = "cache";

        if (!allObjectsExist(Arrays.asList(netcdfKey, metadataKey, headerKey)) || !event.cache) {
            System.out.println("1 or more objects not found in cache. downloading instead.");
            this.s3module.urlToS3(netcdfKey, event.url);

            String tmpFile = this.s3module.downloadObjectFromS3(netcdfKey, NC_TMP_FILE);
            String ncHeader = NetCDF.read(tmpFile);

            this.s3module.stringToS3(headerKey, ncHeader);

            DateTime dt = new DateTime();

            NetCDFMetadata netcdfMetadata = new NetCDFMetadata(
                    new File(tmpFile).length(),
                    ISODateTimeFormat.dateTime().print(dt));

            try {
                this.s3module.stringToS3(metadataKey, this.mapper.writeValueAsString(netcdfMetadata));
            } catch(com.fasterxml.jackson.core.JsonProcessingException e) {
                System.out.println(e);
            }

            source = "download";
        }

        NetCDFExtractorResult result = new NetCDFExtractorResult(
                source,
                new Locations(
                        fqdn(netcdfKey),
                        fqdn(metadataKey),
                        fqdn(headerKey)));

        return result;
    }
}
