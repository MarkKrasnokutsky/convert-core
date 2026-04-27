package com.mark.convert.core.service;

public interface ConvertService {

    byte[] convertToPdf(byte[] fileContent);
    String getSupportedFormat();

}
