package Benchmark;

import Benchmark.Scoring.AlgorithmScoreSummary;
import Benchmark.Scoring.ScoringReport;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Produces a compact human-readable companion to the CSV files. */
public final class BenchmarkReportWriter {

    public void write(
            Path path,
            Map<String, String> environment,
            List<BenchmarkSummary> summaries,
            ScoringReport scoringReport) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            line(writer, "PASSWORD MANAGER - BENCHMARK IZVEŠTAJ");
            line(writer, "=====================================");
            line(writer, "");
            line(writer, "Run ID: " + environment.get("run_id"));
            line(writer, "Sistem: " + environment.get("os_name") + " " + environment.get("os_version"));
            line(writer, "Procesor: " + environment.get("processor"));
            line(writer, "Java/JVM: " + environment.get("java_version") + " / " + environment.get("jvm_name"));
            line(writer, "");

            line(writer, "METODOLOGIJA");
            line(writer, "- Hashovanje i verifikacija mere se kao pojedinačne operacije.");
            line(writer, "- Encryption operacije mere se u batch-evima, a rezultat se deli brojem operacija.");
            line(writer, "- Svi algoritmi koriste isti broj zagrevanja i merenih uzoraka unutar svoje grupe.");
            line(writer, "- Redosled algoritama rotira se između uzoraka radi smanjenja order bias-a.");
            line(writer, "- Key derivation se izvršava jednom pre encryption merenja i nije uključena u rezultat.");
            line(writer, "- Storage overhead koristi realnu UTF-8 veličinu Base64 polja i kripto metapodataka.");
            line(writer, "- Pre memorijskog uzorka zahteva se GC; zatim se vršni rast Java heap-a uzorkuje na 1 ms.");
            line(writer, "");

            line(writer, "SAŽETAK MERENJA (medijana ms po operaciji)");
            line(writer, "Grupa | Algoritam | Operacija | Ulaz B | Medijana ms | P95 ms | Heap procena B | Metadata B");
            for (BenchmarkSummary summary : summaries) {
                line(writer, String.format(Locale.ROOT,
                        "%s | %s | %s | %d | %.6f | %.6f | %s | %.2f",
                        summary.getGroup(),
                        summary.getAlgorithm().getDatabaseValue(),
                        summary.getOperation(),
                        summary.getInputSizeBytes(),
                        summary.getMedianMilliseconds(),
                        summary.getPercentile95Milliseconds(),
                        summary.getAverageEstimatedPeakMemoryBytes() == null
                                ? "nije mereno" : summary.getAverageEstimatedPeakMemoryBytes(),
                        summary.getAverageMetadataSizeBytes()));
            }
            line(writer, "");

            line(writer, "OCENE PO KATEGORIJAMA (1-5, bez težinskih koeficijenata)");
            line(writer, "Algoritam | Performanse | Bezbednost | Praktičnost | Pogodnost | Prosek");
            for (AlgorithmScoreSummary score : scoringReport.getAlgorithmSummaries()) {
                line(writer, String.format(Locale.ROOT,
                        "%s | %.2f | %.2f | %.2f | %.2f | %.2f",
                        score.getAlgorithm().getDatabaseValue(),
                        score.getPerformanceScore(),
                        score.getSecurityScore(),
                        score.getImplementationPracticalityScore(),
                        score.getPasswordManagerSuitabilityScore(),
                        score.getOverallAverage()));
                line(writer, "  Zaključak: " + score.getConclusion());
            }
            line(writer, "");

            line(writer, "OGRANIČENJA TUMAČENJA");
            line(writer, "- Rezultati važe za zabeleženi hardver, JDK, JVM i trenutno opterećenje sistema.");
            line(writer, "- Kraće vreme password hashovanja nije automatski bezbednosna prednost.");
            line(writer, "- Heap procena nije isto što i ukupna procesna ili native memorija.");
            line(writer, "- Male razlike encryption algoritama na kratkim unosima mogu biti bez praktičnog značaja.");
            line(writer, "- Teorijske ocene nisu rezultat brzinskog testa; njihov dokaz i komentar su u algorithm-criterion-scores.csv.");
            line(writer, "");

            line(writer, "IZLAZNI FAJLOVI");
            line(writer, "- benchmark-raw.csv: svaki pojedinačni mereni uzorak");
            line(writer, "- benchmark-summary.csv: statistika za tabele i grafikone");
            line(writer, "- benchmark-environment.csv: hardver, JVM i parametri eksperimenta");
            line(writer, "- evaluation-criteria.csv: kompletan katalog kriterijuma");
            line(writer, "- algorithm-criterion-scores.csv: ocena, dokaz i komentar za svaki kriterijum");
            line(writer, "- algorithm-category-scores.csv: proseci detaljnih kriterijuma po kategoriji");
            line(writer, "- algorithm-score-summary.csv: završni pregled algoritama");
            line(writer, "");

            line(writer, "REFERENCE ZA TEORIJSKU ANALIZU");
            for (Map.Entry<String, String> reference : BenchmarkReferences.all().entrySet()) {
                line(writer, "- " + reference.getKey() + ": " + reference.getValue());
            }
        }
    }

    private void line(BufferedWriter writer, String value) throws IOException {
        writer.write(value);
        writer.newLine();
    }
}
