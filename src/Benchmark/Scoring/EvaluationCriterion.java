package Benchmark.Scoring;

import Benchmark.BenchmarkGroup;

public final class EvaluationCriterion {

    private final BenchmarkGroup group;
    private final EvaluationCategory category;
    private final String code;
    private final String name;
    private final EvidenceType evidenceType;
    private final String description;

    public EvaluationCriterion(
            BenchmarkGroup group,
            EvaluationCategory category,
            String code,
            String name,
            EvidenceType evidenceType,
            String description) {
        this.group = group;
        this.category = category;
        this.code = code;
        this.name = name;
        this.evidenceType = evidenceType;
        this.description = description;
    }

    public BenchmarkGroup getGroup() {
        return group;
    }

    public EvaluationCategory getCategory() {
        return category;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public EvidenceType getEvidenceType() {
        return evidenceType;
    }

    public String getDescription() {
        return description;
    }
}
