package com.example.demo.fileaccess;

import com.example.demo.domain.CurrencyRate;
import com.example.demo.domain.DataGenerator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RandomAccessFileServiceTest {

    private RandomAccessFileService service;
    private String testFilePath;
    private List<CurrencyRate> testData;

    @BeforeEach
    void setUp() throws IOException {
        service = new RandomAccessFileService();
        testFilePath = "test_random_access.dat";
        testData = DataGenerator.generateCurrencyRates(100);
    }

    @AfterEach
    void tearDown() {
        File file = new File(testFilePath);
        if (file.exists()) {
            file.delete();
        }
    }

    @Test
    void testWriteAndReadSequential() throws IOException {
        service.writeRecords(testData, testFilePath);
        List<CurrencyRate> readData = service.readSequential(testFilePath);

        assertEquals(testData.size(), readData.size());
        for (int i = 0; i < testData.size(); i++) {
            assertEquals(testData.get(i).getCurrencyCode(), readData.get(i).getCurrencyCode());
            assertEquals(testData.get(i).getRate(), readData.get(i).getRate(), 0.000001);
        }
    }

    @Test
    void testReadRandom() throws IOException {
        service.writeRecords(testData, testFilePath);

        CurrencyRate randomRecord = service.readRandom(testFilePath, 50);
        assertNotNull(randomRecord);

        // Test out of bounds
        assertThrows(IllegalArgumentException.class, () -> service.readRandom(testFilePath, 1000));
    }

    @Test
    void testGetFileSize() throws IOException {
        service.writeRecords(testData, testFilePath);
        long expectedSize = (long) testData.size() * CurrencyRate.RECORD_SIZE;
        long actualSize = service.getFileSize(testFilePath);

        assertEquals(expectedSize, actualSize);
    }
}