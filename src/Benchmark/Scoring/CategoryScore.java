package Benchmark.Scoring;

import Benchmark.BenchmarkGroup;
import Cryptography.AlgorithmName;

public final class CategoryScore {

    private final BenchmarkGroup group;
    private final AlgorithmName algorithm;
    private final EvaluationCategory category;
    private final int criterionCount;
    private final double averageScore;

    public CategoryScore(
            BenchmarkGroup group,
            AlgorithmName algorithm,
            EvaluationCategory category,
            int criterionCount,
            double averageScore) {
        this.group = group;
        this.algorithm = algorithm;
        this.category = category;
        this.criterionCount = criterionCount;
        this.averageScore = averageScore;
    }

    public BenchmarkGroup getGroup() {
        return group;
    }

    public AlgorithmName getAlgorithm() {
        return algorithm;
    }

    public EvaluationCategory getCategory() {
        return category;
    }

    public int getCriterionCount() {
        return criterionCount;
    }

    public double getAverageScore() {
        return averageScore;
    }
}
