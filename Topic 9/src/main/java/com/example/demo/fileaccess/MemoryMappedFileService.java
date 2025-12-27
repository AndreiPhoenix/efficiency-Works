package com.example.demo.fileaccess;

import com.example.demo.domain.CurrencyRate;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class MemoryMappedFileService implements FileAccessService {

    @Override
    public void writeRecords(List<CurrencyRate> records, String filePath) throws IOException {
        Path path = Path.of(filePath);

        try (FileChannel channel = FileChannel.open(
                path,
                StandardOpenOption.CREATE,
                StandardOpenOption.WRITE,
                StandardOpenOption.READ,
                StandardOpenOption.TRUNCATE_EXISTING)) {

            long fileSize = (long) CurrencyRate.RECORD_SIZE * records.size();
            MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_WRITE, 0, fileSize);

            for (CurrencyRate record : records) {
                byte[] bytes = record.toBytes();
                buffer.put(bytes);
            }

            buffer.force(); // Force OS to write to disk

            log.info("Written {} records using Memory-Mapped File to {}", records.size(), filePath);
        }
    }

    @Override
    public List<CurrencyRate> readSequential(String filePath) throws IOException {
        List<CurrencyRate> records = new ArrayList<>();
        Path path = Path.of(filePath);

        try (FileChannel channel = FileChannel.open(path, StandardOpenOption.READ)) {
            long fileSize = channel.size();
            long recordCount = fileSize / CurrencyRate.RECORD_SIZE;

            MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, fileSize);

            for (int i = 0; i < recordCount; i++) {
                byte[] bytes = new byte[CurrencyRate.RECORD_SIZE];
                buffer.position(i * CurrencyRate.RECORD_SIZE);
                buffer.get(bytes);

                CurrencyRate record = CurrencyRate.fromBytes(bytes);
                if (record != null) {
                    records.add(record);
                }
            }
        }

        log.info("Read {} records sequentially using Memory-Mapped File from {}", records.size(), filePath);
        return records;
    }

    @Override
    public CurrencyRate readRandom(String filePath, long position) throws IOException {
        Path path = Path.of(filePath);

        try (FileChannel channel = FileChannel.open(path, StandardOpenOption.READ)) {
            long fileSize = channel.size();
            long offset = position * CurrencyRate.RECORD_SIZE;

            if (offset >= fileSize) {
                throw new IllegalArgumentException("Position out of bounds");
            }

            MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY, offset, CurrencyRate.RECORD_SIZE);
            byte[] bytes = new byte[CurrencyRate.RECORD_SIZE];
            buffer.get(bytes);

            return CurrencyRate.fromBytes(bytes);
        }
    }

    @Override
    public long getFileSize(String filePath) throws IOException {
        return Files.size(Path.of(filePath));
    }
}