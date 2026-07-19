package Benchmark.Scoring;

import Benchmark.BenchmarkGroup;
import Cryptography.AlgorithmName;

public final class AlgorithmScoreSummary {

    private final BenchmarkGroup group;
    private final AlgorithmName algorithm;
    private final double performanceScore;
    private final double securityScore;
    private final double implementationPracticalityScore;
    private final double passwordManagerSuitabilityScore;
    private final double overallAverage;
    private final String conclusion;

    public AlgorithmScoreSummary(
            BenchmarkGroup group,
            AlgorithmName algorithm,
            double performanceScore,
            double securityScore,
            double implementationPracticalityScore,
            double passwordManagerSuitabilityScore,
            String conclusion) {
        this.group = group;
        this.algorithm = algorithm;
        this.performanceScore = performanceScore;
        this.securityScore = securityScore;
        this.implementationPracticalityScore = implementationPracticalityScore;
        this.passwordManagerSuitabilityScore = passwordManagerSuitabilityScore;
        this.overallAverage = (performanceScore
                + securityScore
                + implementationPracticalityScore
                + passwordManagerSuitabilityScore) / 4.0;
        this.conclusion = conclusion;
    }

    public BenchmarkGroup getGroup() {
        return group;
    }

    public AlgorithmName getAlgorithm() {
        return algorithm;
    }

    public double getPerformanceScore() {
        return performanceScore;
    }

    public double getSecurityScore() {
        return securityScore;
    }

    public double getImplementationPracticalityScore() {
        return implementationPracticalityScore;
    }

    public double getPasswordManagerSuitabilityScore() {
        return passwordManagerSuitabilityScore;
    }

    public double getOverallAverage() {
        return overallAverage;
    }

    public String getConclusion() {
        return conclusion;
    }
}
