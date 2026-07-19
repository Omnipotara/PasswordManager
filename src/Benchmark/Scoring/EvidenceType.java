package Benchmark.Scoring;

public enum EvidenceType {
    MEASURED("Izmereno"),
    THEORETICAL("Teorijska analiza"),
    MIXED("Merenje i teorijska analiza");

    private final String displayName;

    EvidenceType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
