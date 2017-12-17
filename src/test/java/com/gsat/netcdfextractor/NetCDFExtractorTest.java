package com.gsat.netcdfextractor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gsat.netcdfextractor.aws.DownloadFailedException;
import com.gsat.netcdfextractor.aws.S3Module;
import com.gsat.netcdfextractor.core.NetCDF;
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
    NetCDF netCDF;

    @Before
    public void setupMocks()
    {
        s3Module = Mockito.mock(S3Module.class);
        netCDF =  Mockito.mock(NetCDF.class);
    }

    @Test
    public void shouldProcessNetCDF()  {

        S3Module s3Module = Mockito.mock(S3Module.class);
        when(s3Module.objectExists(anyString())).thenReturn(false);
        when(s3Module.downloadObjectFromS3(anyString(), anyString())).thenReturn(NetCDFExtractor.NC_TMP_FILE);
        when(netCDF.read(anyString())).thenReturn(anyString());

        NetCDFExtractor netCDFExtractor = new NetCDFExtractor(
                publicWebsiteUrl,
                s3Module,
                new ObjectMapper(),
                netCDF);

        NetCDFExtractorEvent event = new NetCDFExtractorEvent();
        event.url = remoteNetCDFUrl;
        event.cache = true;

        NetCDFExtractorResult result = netCDFExtractor.handleEvent(event);

        verify(s3Module, times(1)).objectExists(anyString());

        try {
            verify(s3Module, times(1)).urlToS3(anyString(), anyString());
        } catch (com.gsat.netcdfextractor.aws.DownloadFailedException e) {
           //
        }
        verify(s3Module, times(1)).downloadObjectFromS3(anyString(), anyString());
        verify(s3Module, times(2)).stringToS3(anyString(), anyString());
        verify(netCDF, times(1)).read(anyString());

        assertEquals("download", result.source);
        assertEquals("https://public.website/https%3A%2F%2Fsome.remote.resource.nc/header.txt", result.locations.header);
        assertEquals("https://public.website/https%3A%2F%2Fsome.remote.resource.nc/metadata.json", result.locations.metadata);
        assertEquals("https://public.website/https%3A%2F%2Fsome.remote.resource.nc/some.remote.resource.nc", result.locations.netcdf);
    }

    @Test
    public void shouldNotDownloadNetCDF() {

        S3Module s3Module = Mockito.mock(S3Module.class);
        when(s3Module.objectExists(anyString())).thenReturn(true);

        NetCDFExtractor netCDFExtractor = new NetCDFExtractor(
                publicWebsiteUrl,
                s3Module,
                new ObjectMapper(),
                netCDF);

        NetCDFExtractorEvent event = new NetCDFExtractorEvent();
        event.url = remoteNetCDFUrl;
        event.cache = true;

        NetCDFExtractorResult result = netCDFExtractor.handleEvent(event);

        verify(netCDF, times(0)).read(anyString());
        assertEquals("cache", result.source);
        assertEquals("https://public.website/https%3A%2F%2Fsome.remote.resource.nc/header.txt", result.locations.header);
        assertEquals("https://public.website/https%3A%2F%2Fsome.remote.resource.nc/metadata.json", result.locations.metadata);
        assertEquals("https://public.website/https%3A%2F%2Fsome.remote.resource.nc/some.remote.resource.nc", result.locations.netcdf);
    }

    @Test
    public void shouldReturnErrorOnBadUrl() throws DownloadFailedException {

        S3Module s3Module = Mockito.mock(S3Module.class);
        when(s3Module.objectExists(anyString())).thenReturn(false);
        doThrow(new DownloadFailedException("download failed")).when(s3Module).urlToS3(anyString(), anyString());

        NetCDFExtractor netCDFExtractor = new NetCDFExtractor(
                publicWebsiteUrl,
                s3Module,
                new ObjectMapper(),
                netCDF);

        NetCDFExtractorEvent event = new NetCDFExtractorEvent();
        event.url = remoteNetCDFUrl;
        event.cache = true;

        NetCDFExtractorResult result = netCDFExtractor.handleEvent(event);

        verify(s3Module, times(1)).objectExists(anyString());
        verify(s3Module, times(1)).urlToS3(anyString(), anyString());
        verify(netCDF, times(0)).read(anyString());

        assertEquals("download failed", result.errors.get(0));
    }
}
