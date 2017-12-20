package com.gsat.netcdfextractor.client;

import java.io.InputStream;

public interface DownloadInterface {

    InputStream urlToInputStream(String url, int maxSize) throws DownloadFailedException;
}
