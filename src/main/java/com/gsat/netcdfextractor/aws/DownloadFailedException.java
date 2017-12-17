package com.gsat.netcdfextractor.aws;

public class DownloadFailedException extends Exception
{
    public DownloadFailedException(String message)
    {
        super(message);
    }
}
