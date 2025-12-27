package com.example.demo.core.ports.output;

import com.example.demo.core.domain.AnalysisResult;

public interface ResultPublisherPort {
    void publishResult(AnalysisResult result);
    void saveResult(AnalysisResult result);
    AnalysisResult getLastResult();
}