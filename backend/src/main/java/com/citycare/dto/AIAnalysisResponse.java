package com.citycare.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AIAnalysisResponse {
    private String category;
    private Double confidence;
    private String priority;
    private String department;
    private String engine;
    private Boolean isTrainedModel;

    public AIAnalysisResponse() {}

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Double getConfidence() { return confidence; }
    public void setConfidence(Double confidence) { this.confidence = confidence; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getEngine() { return engine; }
    public void setEngine(String engine) { this.engine = engine; }

    public Boolean getIsTrainedModel() { return isTrainedModel; }
    public void setIsTrainedModel(Boolean isTrainedModel) { this.isTrainedModel = isTrainedModel; }
}
