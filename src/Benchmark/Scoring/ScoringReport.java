package Benchmark.Scoring;

import Benchmark.BenchmarkGroup;
import Cryptography.AlgorithmName;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class ScoringReport {

    private final List<CriterionScore> criterionScores;
    private final List<CategoryScore> categoryScores;
    private final List<AlgorithmScoreSummary> algorithmSummaries;

    public ScoringReport(List<CriterionScore> criterionScores) {
        this.criterionScores = new ArrayList<>(criterionScores);
        this.criterionScores.sort(Comparator
                .comparing((CriterionScore score) -> score.getCriterion().getGroup())
                .thenComparing(CriterionScore::getAlgorithm)
                .thenComparing(score -> score.getCriterion().getCategory())
                .thenComparing(score -> score.getCriterion().getCode()));
        this.categoryScores = calculateCategoryScores();
        this.algorithmSummaries = calculateAlgorithmSummaries();
    }

    private List<CategoryScore> calculateCategoryScores() {
        List<CategoryScore> result = new ArrayList<>();
        for (BenchmarkGroup group : BenchmarkGroup.values()) {
            for (AlgorithmName algorithm : AlgorithmName.values()) {
                if (!belongsToGroup(algorithm, group)) {
                    continue;
                }
                for (EvaluationCategory category : EvaluationCategory.values()) {
                    List<CriterionScore> matching = criterionScores.stream()
                            .filter(score -> score.getCriterion().getGroup() == group)
                            .filter(score -> score.getAlgorithm() == algorithm)
                            .filter(score -> score.getCriterion().getCategory() == category)
                            .toList();
                    if (matching.isEmpty()) {
                        throw new IllegalStateException("Missing scores for " + algorithm + " / " + category);
                    }
                    double average = matching.stream().mapToInt(CriterionScore::getScore).average().orElseThrow();
                    result.add(new CategoryScore(group, algorithm, category, matching.size(), average));
                }
            }
        }
        return result;
    }

    private List<AlgorithmScoreSummary> calculateAlgorithmSummaries() {
        List<AlgorithmScoreSummary> result = new ArrayList<>();
        Map<AlgorithmName, Map<EvaluationCategory, Double>> scoresByAlgorithm = new EnumMap<>(AlgorithmName.class);
        for (CategoryScore score : categoryScores) {
            scoresByAlgorithm
                    .computeIfAbsent(score.getAlgorithm(), unused -> new EnumMap<>(EvaluationCategory.class))
                    .put(score.getCategory(), score.getAverageScore());
        }

        for (Map.Entry<AlgorithmName, Map<EvaluationCategory, Double>> entry : scoresByAlgorithm.entrySet()) {
            AlgorithmName algorithm = entry.getKey();
            Map<EvaluationCategory, Double> scores = entry.getValue();
            result.add(new AlgorithmScoreSummary(
                    algorithm.isHashingAlgorithm() ? BenchmarkGroup.HASHING : BenchmarkGroup.ENCRYPTION,
                    algorithm,
                    scores.get(EvaluationCategory.PERFORMANCE),
                    scores.get(EvaluationCategory.SECURITY),
                    scores.get(EvaluationCategory.IMPLEMENTATION_PRACTICALITY),
                    scores.get(EvaluationCategory.PASSWORD_MANAGER_SUITABILITY),
                    conclusionFor(algorithm)
            ));
        }
        result.sort(Comparator
                .comparing(AlgorithmScoreSummary::getGroup)
                .thenComparing(AlgorithmScoreSummary::getAlgorithm));
        return result;
    }

    private boolean belongsToGroup(AlgorithmName algorithm, BenchmarkGroup group) {
        return group == BenchmarkGroup.HASHING
                ? algorithm.isHashingAlgorithm()
                : algorithm.isEncryptionAlgorithm();
    }

    private String conclusionFor(AlgorithmName algorithm) {
        switch (algorithm) {
            case BCRYPT:
                return "Zrela i jednostavna opcija, ali bez memory-hard zaštite i sa ograničenjem ulaza.";
            case PBKDF2:
                return "Prenosiv standard, ali trenutni cost treba posmatrati u odnosu na savremene smernice.";
            case ARGON2ID:
                return "Najbolji bezbednosni profil za master lozinku uz namerno veći memorijski trošak.";
            case AES_GCM:
                return "Jednostavna i efikasna AEAD opcija kada je nonce jedinstven.";
            case AES_CBC_HMAC:
                return "Bezbedna Encrypt-then-MAC konstrukcija, ali složenija i sa većim overhead-om.";
            case CHACHA20_POLY1305:
                return "Moderna AEAD alternativa sa dobrim softverskim performansama i jasnim API-jem.";
            default:
                throw new IllegalArgumentException("Unsupported algorithm: " + algorithm);
        }
    }

    public List<CriterionScore> getCriterionScores() {
        return new ArrayList<>(criterionScores);
    }

    public List<CategoryScore> getCategoryScores() {
        return new ArrayList<>(categoryScores);
    }

    public List<AlgorithmScoreSummary> getAlgorithmSummaries() {
        return new ArrayList<>(algorithmSummaries);
    }
}
