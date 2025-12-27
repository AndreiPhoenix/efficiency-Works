package com.example.demo.fileaccess;

import com.example.demo.domain.CurrencyRate;

import java.io.IOException;
import java.util.List;

public interface FileAccessService {
    void writeRecords(List<CurrencyRate> records, String filePath) throws IOException;
    List<CurrencyRate> readSequential(String filePath) throws IOException;
    CurrencyRate readRandom(String filePath, long position) throws IOException;
    long getFileSize(String filePath) throws IOException;
}