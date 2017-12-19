package com.gsat.netcdfextractor.guice;

import com.amazonaws.services.s3.AmazonS3;
import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

public class GuiceModule extends AbstractModule {

    private String s3Store;
    private String publicWebsiteUrl;
    private AmazonS3 s3Client;

    public GuiceModule(String s3Store, String publicWebsiteUrl, AmazonS3 s3Client) {
        this.s3Store = s3Store;
        this.s3Client = s3Client;
        this.publicWebsiteUrl = publicWebsiteUrl;
    }

    protected void configure() {
        bindConstant().annotatedWith(Names.named("s3Store")).to(this.s3Store);
        bindConstant().annotatedWith(Names.named("publicWebsiteUrl")).to(this.publicWebsiteUrl);
        bind(AmazonS3.class).toInstance(s3Client);
    }
}