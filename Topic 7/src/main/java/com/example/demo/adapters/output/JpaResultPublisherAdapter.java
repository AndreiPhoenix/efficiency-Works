package com.example.demo.adapters.output;

import com.example.demo.core.domain.AnalysisResult;
import com.example.demo.core.ports.output.ResultPublisherPort;
import org.springframework.stereotype.Component;

@Component
public class JpaResultPublisherAdapter implements ResultPublisherPort {

    private AnalysisResult lastResult;

    @Override
    public void publishResult(AnalysisResult result) {
        // Имитация публикации в очередь
        System.out.println("[JPA Adapter] Publishing result to message queue: " + result.getProductId());
        this.lastResult = result;
    }

    @Override
    public void saveResult(AnalysisResult result) {
        // Имитация сохранения в БД через JPA
        System.out.println("[JPA Adapter] Saving result to database: " +
                result.getProductId() + " - " +
                result.getAverageRating() + " - " +
                result.getOverallSentiment());
        this.lastResult = result;
    }

    @Override
    public AnalysisResult getLastResult() {
        return lastResult;
    }
}