package com.example.demo.fileaccess;

import com.example.demo.domain.CurrencyRate;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class RandomAccessFileService implements FileAccessService {

    @Override
    public void writeRecords(List<CurrencyRate> records, String filePath) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(filePath, "rw")) {
            for (CurrencyRate record : records) {
                byte[] bytes = record.toBytes();
                raf.write(bytes);
            }
            log.info("Written {} records using RandomAccessFile to {}", records.size(), filePath);
        }
    }

    @Override
    public List<CurrencyRate> readSequential(String filePath) throws IOException {
        List<CurrencyRate> records = new ArrayList<>();
        try (RandomAccessFile raf = new RandomAccessFile(filePath, "r")) {
            long fileSize = raf.length();
            long recordCount = fileSize / CurrencyRate.RECORD_SIZE;

            byte[] buffer = new byte[CurrencyRate.RECORD_SIZE];
            for (int i = 0; i < recordCount; i++) {
                raf.readFully(buffer);
                CurrencyRate record = CurrencyRate.fromBytes(buffer);
                if (record != null) {
                    records.add(record);
                }
            }
        }
        log.info("Read {} records sequentially using RandomAccessFile from {}", records.size(), filePath);
        return records;
    }

    @Override
    public CurrencyRate readRandom(String filePath, long position) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(filePath, "r")) {
            long offset = position * CurrencyRate.RECORD_SIZE;
            if (offset >= raf.length()) {
                throw new IllegalArgumentException("Position out of bounds");
            }

            raf.seek(offset);
            byte[] buffer = new byte[CurrencyRate.RECORD_SIZE];
            raf.readFully(buffer);

            return CurrencyRate.fromBytes(buffer);
        }
    }

    @Override
    public long getFileSize(String filePath) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(filePath, "r")) {
            return raf.length();
        }
    }
}