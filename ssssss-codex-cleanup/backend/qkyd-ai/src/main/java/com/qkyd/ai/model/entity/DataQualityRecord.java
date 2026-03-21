package com.qkyd.ai.model.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Data Quality Record Entity
 * corresponds to table ai_data_quality_record
 */
public class DataQualityRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    /** ID */
    private Long id;

    /** User ID */
    private Long userId;

    /** Quality Score (0-100) */
    private Integer qualityScore;

    /** Missing Rate */
    private Double missingRate;

    /** Outlier Rate */
    private Double outlierRate;

    /** Fill Strategy */
    private String fillStrategy;

    /** Suggestion */
    private String suggestion;

    /** Analysis Time */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date analysisTime;

    /** Create Time */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Integer getQualityScore() {
        return qualityScore;
    }

    public void setQualityScore(Integer qualityScore) {
        this.qualityScore = qualityScore;
    }

    public Double getMissingRate() {
        return missingRate;
    }

    public void setMissingRate(Double missingRate) {
        this.missingRate = missingRate;
    }

    public Double getOutlierRate() {
        return outlierRate;
    }

    public void setOutlierRate(Double outlierRate) {
        this.outlierRate = outlierRate;
    }

    public String getFillStrategy() {
        return fillStrategy;
    }

    public void setFillStrategy(String fillStrategy) {
        this.fillStrategy = fillStrategy;
    }

    public String getSuggestion() {
        return suggestion;
    }

    public void setSuggestion(String suggestion) {
        this.suggestion = suggestion;
    }

    public Date getAnalysisTime() {
        return analysisTime;
    }

    public void setAnalysisTime(Date analysisTime) {
        this.analysisTime = analysisTime;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
