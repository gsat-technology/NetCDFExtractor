package com.gsat.netcdfextractor.domain.configuration;

public class Config {

    public class AWS {

        public String namedProfile;
        public String region;
    }

    public class EnvironmentVariables {

        public String s3Store;

    }

    public EnvironmentVariables environmentVariables;
    public AWS aws;
}
