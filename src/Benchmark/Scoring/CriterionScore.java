package Benchmark.Scoring;

import Cryptography.AlgorithmName;

public final class CriterionScore {

    private final AlgorithmName algorithm;
    private final EvaluationCriterion criterion;
    private final int score;
    private final String evidence;
    private final String comment;

    public CriterionScore(
            AlgorithmName algorithm,
            EvaluationCriterion criterion,
            int score,
            String evidence,
            String comment) {
        if (score < 1 || score > 5) {
            throw new IllegalArgumentException("Criterion score must be between 1 and 5.");
        }
        this.algorithm = algorithm;
        this.criterion = criterion;
        this.score = score;
        this.evidence = evidence;
        this.comment = comment;
    }

    public AlgorithmName getAlgorithm() {
        return algorithm;
    }

    public EvaluationCriterion getCriterion() {
        return criterion;
    }

    public int getScore() {
        return score;
    }

    public String getEvidence() {
        return evidence;
    }

    public String getComment() {
        return comment;
    }
}
