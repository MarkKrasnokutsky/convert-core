package com.mark.convert.core.service;

public interface IConvertService {

    byte[] convertToPdf(byte[] fileContent);
    String getSupportedFormat();

}
