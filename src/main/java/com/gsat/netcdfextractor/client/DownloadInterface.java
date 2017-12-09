package com.gsat.netcdfextractor.client;

import java.io.InputStream;

public interface DownloadInterface {

    public InputStream urlToInputStream(String url);
}
