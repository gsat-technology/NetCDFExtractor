package com.gsat.netcdfextractor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gsat.netcdfextractor.aws.S3Module;
import com.gsat.netcdfextractor.domain.netcdf.NetCDFExtractorEvent;
import com.gsat.netcdfextractor.domain.netcdf.NetCDFExtractorResult;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.*;

import static org.junit.Assert.*;


public class NetCDFExtractorTest {

    String publicWebsiteUrl = "https://public.website";
    String remoteNetCDFUrl = "https://some.remote.resource.nc";
    S3Module s3Module;

    @Before
    public void setupMocks()
    {
        s3Module = Mockito.mock(S3Module.class);
    }

    @Test
    public void shouldDownloadNetCDF()  {
        System.out.println("shouldDownloadNetCDF");

        S3Module s3Module = Mockito.mock(S3Module.class);
        when(s3Module.objectExists("fdsa")).thenReturn(false);
        when(s3Module.downloadObjectFromS3(anyString(), anyString())).thenReturn("abcd");

        NetCDFExtractor netCDFExtractor = new NetCDFExtractor(
                publicWebsiteUrl,
                s3Module,
                new ObjectMapper());

        NetCDFExtractorEvent event = new NetCDFExtractorEvent(remoteNetCDFUrl, true);
        NetCDFExtractorResult result = netCDFExtractor.handleEvent(event);

        verify(s3Module, times(1)).objectExists(anyString());
        verify(s3Module, times(1)).urlToS3(anyString(), anyString());
        verify(s3Module, times(1)).downloadObjectFromS3(anyString(), anyString());
        verify(s3Module, times(2)).stringToS3(anyString(), anyString());

        assertEquals(result.source, "download");
        assertEquals(result.locations.header, "https://public.website/https%3A%2F%2Fsome.remote.resource.nc/header.txt");
        assertEquals(result.locations.metadata, "https://public.website/https%3A%2F%2Fsome.remote.resource.nc/metadata.json");
        assertEquals(result.locations.netcdf, "https://public.website/https%3A%2F%2Fsome.remote.resource.nc/some.remote.resource.nc");

    }
}