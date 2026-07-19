package Benchmark.Scoring;

import Benchmark.BenchmarkGroup;
import Benchmark.BenchmarkOperation;
import Benchmark.BenchmarkSummary;
import Cryptography.AlgorithmName;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Combines measured performance with explicit theoretical assessments. Every
 * criterion uses a 1-5 scale and every category has the same number of
 * criteria, so no hidden weighting is introduced.
 */
public final class ScoringModel {

    private static final List<AlgorithmName> HASHING_ALGORITHMS = Arrays.asList(
            AlgorithmName.BCRYPT,
            AlgorithmName.PBKDF2,
            AlgorithmName.ARGON2ID
    );
    private static final List<AlgorithmName> ENCRYPTION_ALGORITHMS = Arrays.asList(
            AlgorithmName.AES_GCM,
            AlgorithmName.AES_CBC_HMAC,
            AlgorithmName.CHACHA20_POLY1305
    );

    private final EvaluationCriteriaCatalog catalog;

    public ScoringModel(EvaluationCriteriaCatalog catalog) {
        this.catalog = catalog;
    }

    public ScoringReport evaluate(List<BenchmarkSummary> summaries) {
        requireCompleteBenchmark(summaries);
        List<CriterionScore> scores = new ArrayList<>();
        addHashingPerformance(scores, summaries);
        addHashingTheoreticalScores(scores, summaries);
        addEncryptionPerformance(scores, summaries);
        addEncryptionTheoreticalScores(scores, summaries);
        validateAllCriteriaAreScored(scores);
        return new ScoringReport(scores);
    }

    private void addHashingPerformance(List<CriterionScore> scores, List<BenchmarkSummary> summaries) {
        double fastestHash = minimumAverageMedian(summaries, HASHING_ALGORITHMS, BenchmarkOperation.HASH);
        double fastestVerify = minimumAverageMedian(summaries, HASHING_ALGORITHMS, BenchmarkOperation.VERIFY);

        for (AlgorithmName algorithm : HASHING_ALGORITHMS) {
            double hashMilliseconds = averageMedianMilliseconds(summaries, algorithm, BenchmarkOperation.HASH);
            double verifyMilliseconds = averageMedianMilliseconds(summaries, algorithm, BenchmarkOperation.VERIFY);
            add(scores, algorithm, "H-P1", relativePerformanceScore(hashMilliseconds, fastestHash),
                    milliseconds(hashMilliseconds),
                    "Ocena je izvedena iz odnosa prema najbržoj izmerenoj medijani hashovanja.");
            add(scores, algorithm, "H-P2", relativePerformanceScore(verifyMilliseconds, fastestVerify),
                    milliseconds(verifyMilliseconds),
                    "Ocena je izvedena iz odnosa prema najbržoj izmerenoj medijani verifikacije.");
        }

        add(scores, AlgorithmName.BCRYPT, "H-P3", 5,
                memoryEvidence(summaries, AlgorithmName.BCRYPT, "mali CPU-oriented state"),
                "Mali memorijski zahtev je dobar za resurse aplikacije, ali nije bezbednosna prednost protiv GPU napada.");
        add(scores, AlgorithmName.PBKDF2, "H-P3", 5,
                memoryEvidence(summaries, AlgorithmName.PBKDF2, "bez memory cost parametra"),
                "PBKDF2 zahteva malo memorije; to olakšava legitimno izvršavanje i paralelizaciju napada.");
        add(scores, AlgorithmName.ARGON2ID, "H-P3", 1,
                memoryEvidence(summaries, AlgorithmName.ARGON2ID, "konfigurisano 65536 KiB"),
                "Veliki memorijski zahtev je nameran bezbednosni trošak, pa dobija nižu ocenu samo u kategoriji performansi.");

        add(scores, AlgorithmName.BCRYPT, "H-P4", 3, "jedan logaritamski cost=10",
                "Cost je jednostavan za povećavanje, ali ne odvaja vreme, memoriju i paralelizam.");
        add(scores, AlgorithmName.PBKDF2, "H-P4", 4, "iterations=200000; PRF i dužina ključa su podesivi",
                "Broj iteracija se lako kalibriše, ali algoritam nema zaseban memory cost.");
        add(scores, AlgorithmName.ARGON2ID, "H-P4", 5, "memory=64 MiB; iterations=3; parallelism=1",
                "Vreme, memorija i paralelizam mogu se podešavati odvojeno prema ciljnom hardveru.");
    }

    private void addHashingTheoreticalScores(List<CriterionScore> scores, List<BenchmarkSummary> summaries) {
        add(scores, AlgorithmName.BCRYPT, "H-S1", 4, "adaptivan cost; USENIX bcrypt rad",
                "Namerno je spor i zreo, ali nije memory-hard kao Argon2id.");
        add(scores, AlgorithmName.PBKDF2, "H-S1", 3, "HMAC-SHA256; 200000 iteracija",
                "Iteracije usporavaju napad, ali trenutni cost je ispod savremene OWASP preporuke za PBKDF2-HMAC-SHA256.");
        add(scores, AlgorithmName.ARGON2ID, "H-S1", 5, "Argon2id; 64 MiB; 3 prolaza",
                "Kombinuje vremenski i memorijski trošak namenjen odbrani od specijalizovanog hardvera.");

        add(scores, AlgorithmName.BCRYPT, "H-S2", 2, "CPU-oriented; nema podesiv memory cost",
                "Bcrypt ima skuplji key schedule, ali ne obezbeđuje savremeno memory-hard ponašanje.");
        add(scores, AlgorithmName.PBKDF2, "H-S2", 1, "CPU-hard; nema memory cost",
                "Mali memorijski zahtev omogućava efikasniju masovnu paralelizaciju napada.");
        add(scores, AlgorithmName.ARGON2ID, "H-S2", 5, "RFC 9106 memory-hard konstrukcija",
                "Visok memorijski zahtev povećava cenu paralelnih GPU/ASIC pokušaja.");

        for (AlgorithmName algorithm : HASHING_ALGORITHMS) {
            add(scores, algorithm, "H-S3", 5, storedHashEvidence(summaries, algorithm),
                    "Strategija generiše novi nasumični salt i čuva ga u hash formatu, čime se sprečavaju rainbow-table prečice.");
        }

        add(scores, AlgorithmName.BCRYPT, "H-S4", 3, "zreo algoritam; OWASP ga usmerava na legacy sisteme",
                "Dugogodišnje je analiziran, ali savremene smernice daju prednost memory-hard algoritmima.");
        add(scores, AlgorithmName.PBKDF2, "H-S4", 4, "NIST SP 800-132; široka standardizacija",
                "Veoma je interoperabilan, ali cost mora redovno da se povećava i nema memory-hard zaštitu.");
        add(scores, AlgorithmName.ARGON2ID, "H-S4", 5, "RFC 9106; OWASP primarni izbor",
                "Savremeno je projektovan baš za password hashing i aktuelne smernice ga favorizuju.");

        addHashingImplementationScores(scores);
        addHashingSuitabilityScores(scores);
    }

    private void addHashingImplementationScores(List<CriterionScore> scores) {
        add(scores, AlgorithmName.BCRYPT, "H-I1", 5, "jBCrypt hashpw/checkpw",
                "API je kratak i direktno mapira hashovanje i proveru.");
        add(scores, AlgorithmName.PBKDF2, "H-I1", 4, "Password4j + eksplicitan format zapisa",
                "Kripto poziv je kratak, ali aplikacija sama sklapa i parsira format parametara.");
        add(scores, AlgorithmName.ARGON2ID, "H-I1", 4, "Password4j Argon2Function",
                "Biblioteka skriva kriptografske detalje, dok aplikacija i dalje bira više cost parametara.");

        add(scores, AlgorithmName.BCRYPT, "H-I2", 5, "jedan cost parametar",
                "Jednostavno podešavanje smanjuje prostor za konfiguracionu grešku.");
        add(scores, AlgorithmName.PBKDF2, "H-I2", 4, "PRF, iteracije, salt i dužina ključa",
                "Parametri su standardni i jasni, ali zahtevaju sopstveni format i validaciju.");
        add(scores, AlgorithmName.ARGON2ID, "H-I2", 3, "memorija, iteracije, paralelizam, salt i output",
                "Fleksibilnost je velika, ali pravilna kalibracija zahteva više odluka.");

        add(scores, AlgorithmName.BCRYPT, "H-I3", 5, "lokalna čista Java implementacija",
                "Lako se pokreće bez native zavisnosti, uz cenu oslanjanja na stariju implementaciju.");
        add(scores, AlgorithmName.PBKDF2, "H-I3", 5, "Password4j; PBKDF2 postoji i u standardnom JCA",
                "Algoritam je široko dostupan i lako prenosiv između platformi.");
        add(scores, AlgorithmName.ARGON2ID, "H-I3", 3, "Password4j/Bouncy Castle zavisnost",
                "Pouzdana biblioteka postoji, ali Argon2id nije osnovni JCA algoritam na svim JDK okruženjima.");

        add(scores, AlgorithmName.BCRYPT, "H-I4", 5, "$2a$ zapis sadrži cost, salt i hash",
                "Za proveru nisu potrebne dodatne kolone baze.");
        add(scores, AlgorithmName.PBKDF2, "H-I4", 5, "format sadrži PRF, iteracije, dužinu, salt i hash",
                "Svi potrebni parametri nalaze se u password_hash vrednosti.");
        add(scores, AlgorithmName.ARGON2ID, "H-I4", 5, "standardni $argon2id$ format",
                "Zapis je samodovoljan i biblioteka može iz njega rekonstruisati parametre.");
    }

    private void addHashingSuitabilityScores(List<CriterionScore> scores) {
        add(scores, AlgorithmName.BCRYPT, "H-M1", 4, "izmerena login verifikacija u benchmark-summary.csv",
                "Cost=10 daje prihvatljiv interaktivni tok, ali merenje treba ponoviti na ciljnom računaru.");
        add(scores, AlgorithmName.PBKDF2, "H-M1", 4, "izmerena login verifikacija u benchmark-summary.csv",
                "Latencija je pogodna za desktop prijavu, uz potrebu za jačim cost-om prema savremenim smernicama.");
        add(scores, AlgorithmName.ARGON2ID, "H-M1", 4, "izmerena login verifikacija u benchmark-summary.csv",
                "Veća potrošnja je namerna, a 64 MiB/3 prolaza ostaju praktični za jednu desktop prijavu.");

        add(scores, AlgorithmName.BCRYPT, "H-M2", 4, "salt + adaptivan CPU cost",
                "Dobro usporava offline pogađanje, ali napadač ne plaća veliki memorijski trošak.");
        add(scores, AlgorithmName.PBKDF2, "H-M2", 3, "salt + 200000 HMAC-SHA256 iteracija",
                "Štiti od precompute napada, ali je povoljniji za paralelni hardver od Argon2id-a.");
        add(scores, AlgorithmName.ARGON2ID, "H-M2", 5, "salt + 64 MiB memory cost",
                "Najviše povećava cenu svakog offline pokušaja u scenariju ukradene baze.");

        add(scores, AlgorithmName.BCRYPT, "H-M3", 4, "cost je ugrađen u svaki hash",
                "Novi nalozi mogu dobiti veći cost, a stari zapisi ostaju proverljivi.");
        add(scores, AlgorithmName.PBKDF2, "H-M3", 5, "iteracije i PRF su ugrađeni u zapis",
                "Format omogućava postepeno povećavanje parametara bez dodatnih DB kolona.");
        add(scores, AlgorithmName.ARGON2ID, "H-M3", 5, "svi Argon2 parametri su u zapisu",
                "Omogućava preciznu buduću kalibraciju memorije, vremena i paralelizma.");

        add(scores, AlgorithmName.BCRYPT, "H-M4", 3, "tipično ograničenje ulaza 72 bajta",
                "Ograničenje mora biti poznato UI-ju i dokumentaciji da duga master lozinka ne izgubi entropiju.");
        add(scores, AlgorithmName.PBKDF2, "H-M4", 5, "široka standardizacija; nema kratkog bcrypt limita",
                "Dobar je za interoperabilnost i dugačke master lozinke.");
        add(scores, AlgorithmName.ARGON2ID, "H-M4", 4, "savremen format; zahteva Argon2 biblioteku",
                "Nema praktično kratko ograničenje lozinke, ali interoperabilnost zavisi od dostupnosti biblioteke.");
    }

    private void addEncryptionPerformance(List<CriterionScore> scores, List<BenchmarkSummary> summaries) {
        double fastestEncrypt = minimumAverageMedian(summaries, ENCRYPTION_ALGORITHMS, BenchmarkOperation.ENCRYPT);
        double fastestDecrypt = minimumAverageMedian(summaries, ENCRYPTION_ALGORITHMS, BenchmarkOperation.DECRYPT);
        double fastestLargeInput = minimumLargestInputCombinedMedian(summaries);
        double smallestOverhead = minimumStorageOverhead(summaries);

        for (AlgorithmName algorithm : ENCRYPTION_ALGORITHMS) {
            double encrypt = averageMedianMilliseconds(summaries, algorithm, BenchmarkOperation.ENCRYPT);
            double decrypt = averageMedianMilliseconds(summaries, algorithm, BenchmarkOperation.DECRYPT);
            double largeInput = largestInputCombinedMedianMilliseconds(summaries, algorithm);
            double overhead = averageStorageOverheadBytes(summaries, algorithm);

            add(scores, algorithm, "E-P1", relativePerformanceScore(encrypt, fastestEncrypt),
                    milliseconds(encrypt),
                    "Ocena poredi prosečnu medijanu šifrovanja kroz iste veličine ulaza.");
            add(scores, algorithm, "E-P2", relativePerformanceScore(decrypt, fastestDecrypt),
                    milliseconds(decrypt),
                    "Ocena poredi prosečnu medijanu dešifrovanja kroz iste veličine ulaza.");
            add(scores, algorithm, "E-P3", relativePerformanceScore(largeInput, fastestLargeInput),
                    milliseconds(largeInput) + " za najveći ulaz (encrypt + decrypt)",
                    "Ocena poredi kombinovano vreme na najvećoj testiranoj veličini.");
            add(scores, algorithm, "E-P4", relativePerformanceScore(overhead, smallestOverhead),
                    decimal(overhead) + " B prosečnog dodatnog prostora",
                    "Overhead uključuje Base64 proširenje ciphertext-a i tekstualne DB metapodatke.");
        }
    }

    private void addEncryptionTheoreticalScores(List<CriterionScore> scores, List<BenchmarkSummary> summaries) {
        for (AlgorithmName algorithm : ENCRYPTION_ALGORITHMS) {
            add(scores, algorithm, "E-S1", 5, "256-bit ključ i standardna kriptografska konstrukcija",
                    "Uz tajan ispravno izveden ključ, svi kandidati pružaju jaku poverljivost password polja.");
            add(scores, algorithm, "E-S2", 5, "JUnit tamper i wrong-key testovi prolaze",
                    "AEAD tag ili Encrypt-then-MAC HMAC pouzdano odbijaju izmenjene podatke.");
        }

        add(scores, AlgorithmName.AES_GCM, "E-S3", 3, "nasumični 96-bitni nonce; ponavljanje je kritično",
                "Implementacija koristi SecureRandom, ali GCM zahteva strogu jedinstvenost nonce-a za isti ključ.");
        add(scores, AlgorithmName.AES_CBC_HMAC, "E-S3", 4, "nasumični 128-bitni IV autentifikovan HMAC-om",
                "IV mora biti nepredvidiv; HMAC obuhvata IV i sprečava neprimećenu manipulaciju.");
        add(scores, AlgorithmName.CHACHA20_POLY1305, "E-S3", 3, "nasumični 96-bitni nonce; ponavljanje je kritično",
                "RFC 8439 zahteva različit nonce za svako šifrovanje istim ključem.");

        add(scores, AlgorithmName.AES_GCM, "E-S4", 5, "NIST SP 800-38D AEAD",
                "Standardna AEAD konstrukcija smanjuje broj ručnih kriptografskih koraka.");
        add(scores, AlgorithmName.AES_CBC_HMAC, "E-S4", 4, "AES-CBC + HMAC-SHA256 Encrypt-then-MAC",
                "Komponente su zrele, ali bezbednost zavisi od pravilne kompozicije i odvojenih ključeva.");
        add(scores, AlgorithmName.CHACHA20_POLY1305, "E-S4", 5, "RFC 8439 AEAD",
                "Standardizovana moderna konstrukcija sa jednim integrisanim autentifikacionim tagom.");

        addEncryptionImplementationScores(scores);
        addEncryptionSuitabilityScores(scores, summaries);
    }

    private void addEncryptionImplementationScores(List<CriterionScore> scores) {
        add(scores, AlgorithmName.AES_GCM, "E-I1", 5, "JCA AES/GCM/NoPadding",
                "Direktno je podržan standardnim Java provider-om.");
        add(scores, AlgorithmName.AES_CBC_HMAC, "E-I1", 5, "JCA AES/CBC/PKCS5Padding + HmacSHA256",
                "Obe primitive su standardno dostupne, iako se kompozicija piše u aplikaciji.");
        add(scores, AlgorithmName.CHACHA20_POLY1305, "E-I1", 4, "JCA ChaCha20-Poly1305",
                "Dostupan je na modernom JDK-u, ali manje univerzalno na starijim Java okruženjima.");

        add(scores, AlgorithmName.AES_GCM, "E-I2", 5, "jedan AEAD Cipher poziv",
                "Enkripcija i autentifikacija se obavljaju u istoj standardnoj operaciji.");
        add(scores, AlgorithmName.AES_CBC_HMAC, "E-I2", 2, "derive subkeys + CBC + HMAC + verify-before-decrypt",
                "Najviše ručnih koraka i najveći prostor za grešku u redosledu operacija.");
        add(scores, AlgorithmName.CHACHA20_POLY1305, "E-I2", 5, "jedan AEAD Cipher poziv",
                "API je sličan GCM-u i nema ručnu MAC kompoziciju.");

        add(scores, AlgorithmName.AES_GCM, "E-I3", 5, "jedan 256-bitni AES ključ",
                "Nema dodatnog ključa za autentifikaciju.");
        add(scores, AlgorithmName.AES_CBC_HMAC, "E-I3", 2, "odvojeni encryption i MAC podključevi",
                "Implementacija mora deterministički i bezbedno razdvojiti namene ključeva.");
        add(scores, AlgorithmName.CHACHA20_POLY1305, "E-I3", 5, "jedan 256-bitni ChaCha20 ključ",
                "Jedan ključ se prosleđuje standardnoj AEAD konstrukciji.");

        add(scores, AlgorithmName.AES_GCM, "E-I4", 5, "nonce + 128-bitni tag",
                "Metapodaci su mali i direktno mapirani na EncryptedData model.");
        add(scores, AlgorithmName.AES_CBC_HMAC, "E-I4", 3, "128-bitni IV + 256-bitni HMAC + opis kompozicije",
                "Veći tag i složeniji parametri povećavaju storage i implementacioni overhead.");
        add(scores, AlgorithmName.CHACHA20_POLY1305, "E-I4", 5, "nonce + 128-bitni tag",
                "Metapodaci su po strukturi slični AES-GCM zapisu.");
    }

    private void addEncryptionSuitabilityScores(List<CriterionScore> scores, List<BenchmarkSummary> summaries) {
        double fastestShort = minimumShortInputCombinedMedian(summaries);
        for (AlgorithmName algorithm : ENCRYPTION_ALGORITHMS) {
            double shortTime = shortInputCombinedMedianMilliseconds(summaries, algorithm);
            add(scores, algorithm, "E-M1", relativePerformanceScore(shortTime, fastestShort),
                    milliseconds(shortTime) + " za najmanji ulaz (encrypt + decrypt)",
                    "Ocena koristi tipičan kratak password payload; veoma male razlike možda nemaju praktičan UI značaj.");
            add(scores, algorithm, "E-M2", 5, "ciphertext bez ključa; algoritam i IV/nonce nisu tajni",
                    "Napadač sa bazom ne može dobiti password polje bez ključa izvedenog iz master lozinke.");
            add(scores, algorithm, "E-M3", 5, "tamper testovi za ciphertext, tag i IV/nonce",
                    "Izmenjeni entry se odbija umesto vraćanja neautentičnog plaintext-a.");
        }

        add(scores, AlgorithmName.AES_GCM, "E-M4", 4, "odličan uz AES hardversku akceleraciju",
                "Veoma je rasprostranjen, ali relativna brzina može zavisiti od AES instrukcija procesora.");
        add(scores, AlgorithmName.AES_CBC_HMAC, "E-M4", 4, "široko dostupne AES i HMAC primitive",
                "Prenosiv je, ali uvek zadržava dodatni HMAC i key-separation rad.");
        add(scores, AlgorithmName.CHACHA20_POLY1305, "E-M4", 5, "dobre softverske performanse bez AES instrukcija",
                "Posebno je koristan kao alternativa na hardveru bez ubrzanog AES-a.");
    }

    private void requireCompleteBenchmark(List<BenchmarkSummary> summaries) {
        for (AlgorithmName algorithm : HASHING_ALGORITHMS) {
            requireSummary(summaries, algorithm, BenchmarkOperation.HASH);
            requireSummary(summaries, algorithm, BenchmarkOperation.VERIFY);
        }
        for (AlgorithmName algorithm : ENCRYPTION_ALGORITHMS) {
            requireSummary(summaries, algorithm, BenchmarkOperation.ENCRYPT);
            requireSummary(summaries, algorithm, BenchmarkOperation.DECRYPT);
        }
    }

    private void requireSummary(
            List<BenchmarkSummary> summaries,
            AlgorithmName algorithm,
            BenchmarkOperation operation) {
        boolean exists = summaries.stream()
                .anyMatch(summary -> summary.getAlgorithm() == algorithm && summary.getOperation() == operation);
        if (!exists) {
            throw new IllegalArgumentException("Missing benchmark summary for " + algorithm + " / " + operation);
        }
    }

    private void validateAllCriteriaAreScored(List<CriterionScore> scores) {
        for (EvaluationCriterion criterion : catalog.getAll()) {
            List<AlgorithmName> algorithms = criterion.getGroup() == BenchmarkGroup.HASHING
                    ? HASHING_ALGORITHMS : ENCRYPTION_ALGORITHMS;
            for (AlgorithmName algorithm : algorithms) {
                long count = scores.stream()
                        .filter(score -> score.getAlgorithm() == algorithm)
                        .filter(score -> score.getCriterion().getCode().equals(criterion.getCode()))
                        .count();
                if (count != 1) {
                    throw new IllegalStateException(
                            "Criterion " + criterion.getCode() + " has " + count + " scores for " + algorithm);
                }
            }
        }
    }

    private void add(
            List<CriterionScore> target,
            AlgorithmName algorithm,
            String criterionCode,
            int score,
            String evidence,
            String comment) {
        target.add(new CriterionScore(algorithm, catalog.getByCode(criterionCode), score, evidence, comment));
    }

    private double minimumAverageMedian(
            List<BenchmarkSummary> summaries,
            List<AlgorithmName> algorithms,
            BenchmarkOperation operation) {
        return algorithms.stream()
                .mapToDouble(algorithm -> averageMedianMilliseconds(summaries, algorithm, operation))
                .min()
                .orElseThrow();
    }

    private double averageMedianMilliseconds(
            List<BenchmarkSummary> summaries,
            AlgorithmName algorithm,
            BenchmarkOperation operation) {
        return summaries.stream()
                .filter(summary -> summary.getAlgorithm() == algorithm)
                .filter(summary -> summary.getOperation() == operation)
                .mapToDouble(BenchmarkSummary::getMedianMilliseconds)
                .average()
                .orElseThrow();
    }

    private double minimumLargestInputCombinedMedian(List<BenchmarkSummary> summaries) {
        return ENCRYPTION_ALGORITHMS.stream()
                .mapToDouble(algorithm -> largestInputCombinedMedianMilliseconds(summaries, algorithm))
                .min()
                .orElseThrow();
    }

    private double largestInputCombinedMedianMilliseconds(
            List<BenchmarkSummary> summaries,
            AlgorithmName algorithm) {
        int largest = summaries.stream()
                .filter(summary -> summary.getAlgorithm() == algorithm)
                .mapToInt(BenchmarkSummary::getInputSizeBytes)
                .max()
                .orElseThrow();
        return combinedMedianForInput(summaries, algorithm, largest);
    }

    private double minimumShortInputCombinedMedian(List<BenchmarkSummary> summaries) {
        return ENCRYPTION_ALGORITHMS.stream()
                .mapToDouble(algorithm -> shortInputCombinedMedianMilliseconds(summaries, algorithm))
                .min()
                .orElseThrow();
    }

    private double shortInputCombinedMedianMilliseconds(
            List<BenchmarkSummary> summaries,
            AlgorithmName algorithm) {
        int smallest = summaries.stream()
                .filter(summary -> summary.getAlgorithm() == algorithm)
                .mapToInt(BenchmarkSummary::getInputSizeBytes)
                .min()
                .orElseThrow();
        return combinedMedianForInput(summaries, algorithm, smallest);
    }

    private double combinedMedianForInput(
            List<BenchmarkSummary> summaries,
            AlgorithmName algorithm,
            int inputSize) {
        return summaries.stream()
                .filter(summary -> summary.getAlgorithm() == algorithm)
                .filter(summary -> summary.getInputSizeBytes() == inputSize)
                .filter(summary -> summary.getOperation() == BenchmarkOperation.ENCRYPT
                        || summary.getOperation() == BenchmarkOperation.DECRYPT)
                .mapToDouble(BenchmarkSummary::getMedianMilliseconds)
                .sum();
    }

    private double minimumStorageOverhead(List<BenchmarkSummary> summaries) {
        return ENCRYPTION_ALGORITHMS.stream()
                .mapToDouble(algorithm -> averageStorageOverheadBytes(summaries, algorithm))
                .min()
                .orElseThrow();
    }

    private double averageStorageOverheadBytes(
            List<BenchmarkSummary> summaries,
            AlgorithmName algorithm) {
        return summaries.stream()
                .filter(summary -> summary.getAlgorithm() == algorithm)
                .filter(summary -> summary.getOperation() == BenchmarkOperation.ENCRYPT)
                .mapToDouble(summary -> summary.getAverageOutputSizeBytes()
                        + summary.getAverageMetadataSizeBytes()
                        - summary.getInputSizeBytes())
                .average()
                .orElseThrow();
    }

    private int relativePerformanceScore(double value, double bestValue) {
        if (bestValue <= 0) {
            return 5;
        }
        double ratio = value / bestValue;
        if (ratio <= 1.10) {
            return 5;
        }
        if (ratio <= 1.35) {
            return 4;
        }
        if (ratio <= 1.75) {
            return 3;
        }
        if (ratio <= 2.50) {
            return 2;
        }
        return 1;
    }

    private String memoryEvidence(
            List<BenchmarkSummary> summaries,
            AlgorithmName algorithm,
            String configuredCost) {
        double average = summaries.stream()
                .filter(summary -> summary.getAlgorithm() == algorithm)
                .filter(summary -> summary.getAverageEstimatedPeakMemoryBytes() != null)
                .mapToLong(BenchmarkSummary::getAverageEstimatedPeakMemoryBytes)
                .average()
                .orElse(0);
        return configuredCost + "; procenjeni heap peak delta=" + decimal(average / (1024.0 * 1024.0)) + " MiB";
    }

    private String storedHashEvidence(List<BenchmarkSummary> summaries, AlgorithmName algorithm) {
        double bytes = summaries.stream()
                .filter(summary -> summary.getAlgorithm() == algorithm)
                .filter(summary -> summary.getOperation() == BenchmarkOperation.HASH)
                .mapToDouble(BenchmarkSummary::getAverageOutputSizeBytes)
                .average()
                .orElseThrow();
        return "samodovoljan salted hash zapis; prosečna veličina=" + decimal(bytes) + " B";
    }

    private String milliseconds(double value) {
        return decimal(value) + " ms po operaciji";
    }

    private String decimal(double value) {
        return String.format(Locale.ROOT, "%.4f", value);
    }
}
