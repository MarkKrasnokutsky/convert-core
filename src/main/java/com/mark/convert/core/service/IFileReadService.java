package com.mark.convert.core.service;

import java.io.IOException;

public interface IFileReadService {

    byte[] downloadFileAsBytes(String bucketName, String path) throws IOException;

    void uploadBytesAsPdf(String bucketName, String filePath, byte[] pdfBytes);

}
