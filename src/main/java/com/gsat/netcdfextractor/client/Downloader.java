package com.gsat.netcdfextractor.client;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;

public class Downloader implements DownloadInterface {

    public InputStream urlToInputStream(String targetUrl, int maxSize) throws DownloadFailedException {

        InputStream in = null;
        URL url;
        URLConnection conn;
        int size;

        try {

            url = new URL(targetUrl);
            conn = url.openConnection();
            size = conn.getContentLength();

            if (size > maxSize) {
              throw new DownloadFailedException("download forbidden by client - resource file size too big");
            }

            in = url.openStream();
        } catch (final IOException e) {
            throw new DownloadFailedException("could not download: " + e.toString());
        }

        return in;
    }
}
