package com.gsat.netcdfextractor;

import ucar.nc2.NetcdfFile;

import java.io.IOException;

public class NetCDF {


    public static String read(String filename) {

        NetcdfFile ncfile = null;
        String result = null;

        try {
            ncfile = NetcdfFile.open(filename);
            return ncfile.toString();
        } catch (java.io.IOException ioe) {
            System.out.println(ioe);
        } finally {
            if (null != ncfile) try {
                ncfile.close();
            } catch (IOException ioe) {
                System.out.println(ioe);
            }
        }

        return result;
    }

}
