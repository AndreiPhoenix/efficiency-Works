package com.example.demo.adapters.output;

import com.example.demo.core.domain.AnalysisResult;
import com.example.demo.core.ports.output.ResultPublisherPort;
import org.springframework.stereotype.Component;

@Component
public class RestResultPublisherAdapter implements ResultPublisherPort {

    private AnalysisResult lastResult;

    @Override
    public void publishResult(AnalysisResult result) {
        // Имитация отправки через REST
        System.out.println("[REST Adapter] Publishing result via REST API: " + result);
        this.lastResult = result;
    }

    @Override
    public void saveResult(AnalysisResult result) {
        // Имитация сохранения через REST
        System.out.println("[REST Adapter] Saving result via REST endpoint: " + result);
        this.lastResult = result;
    }

    @Override
    public AnalysisResult getLastResult() {
        return lastResult;
    }
}