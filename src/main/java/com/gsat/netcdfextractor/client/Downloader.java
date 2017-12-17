package com.gsat.netcdfextractor.client;

import java.io.*;
import java.net.URL;

public class Downloader implements DownloadInterface {

    public InputStream urlToInputStream(String url) {

        InputStream in = null;

        try {
            in = new URL(url).openStream();
        } catch (final IOException e) {
            System.out.println(e);
        }

        return in;
    }
}
