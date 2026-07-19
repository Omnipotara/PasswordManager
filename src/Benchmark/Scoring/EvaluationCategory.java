package Benchmark.Scoring;

public enum EvaluationCategory {
    PERFORMANCE("Performanse"),
    SECURITY("Bezbednost"),
    IMPLEMENTATION_PRACTICALITY("Praktičnost implementacije"),
    PASSWORD_MANAGER_SUITABILITY("Pogodnost za password manager");

    private final String displayName;

    EvaluationCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
