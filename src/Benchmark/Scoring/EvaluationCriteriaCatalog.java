package Benchmark.Scoring;

import Benchmark.BenchmarkGroup;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** The complete, stable list of criteria used by the scoring report. */
public final class EvaluationCriteriaCatalog {

    private final List<EvaluationCriterion> criteria;
    private final Map<String, EvaluationCriterion> criteriaByCode;

    public EvaluationCriteriaCatalog() {
        List<EvaluationCriterion> definitions = new ArrayList<>();
        addHashingCriteria(definitions);
        addEncryptionCriteria(definitions);
        criteria = Collections.unmodifiableList(definitions);

        Map<String, EvaluationCriterion> byCode = new LinkedHashMap<>();
        for (EvaluationCriterion criterion : criteria) {
            if (byCode.put(criterion.getCode(), criterion) != null) {
                throw new IllegalStateException("Duplicate evaluation criterion: " + criterion.getCode());
            }
        }
        criteriaByCode = Collections.unmodifiableMap(byCode);
    }

    private void addHashingCriteria(List<EvaluationCriterion> target) {
        add(target, BenchmarkGroup.HASHING, EvaluationCategory.PERFORMANCE, "H-P1",
                "Vreme hashovanja", EvidenceType.MEASURED,
                "Medijana vremena potrebnog za pravljenje novog hash zapisa.");
        add(target, BenchmarkGroup.HASHING, EvaluationCategory.PERFORMANCE, "H-P2",
                "Vreme verifikacije", EvidenceType.MEASURED,
                "Medijana vremena provere master lozinke pri prijavi.");
        add(target, BenchmarkGroup.HASHING, EvaluationCategory.PERFORMANCE, "H-P3",
                "Memorijski zahtev", EvidenceType.MIXED,
                "Konfigurisani memorijski cost i procenjeni vršni rast JVM heap memorije.");
        add(target, BenchmarkGroup.HASHING, EvaluationCategory.PERFORMANCE, "H-P4",
                "Skalabilnost cost parametara", EvidenceType.THEORETICAL,
                "Koliko precizno se CPU, memorija i paralelizam mogu prilagoditi hardveru.");

        add(target, BenchmarkGroup.HASHING, EvaluationCategory.SECURITY, "H-S1",
                "Otpornost na offline pogađanje", EvidenceType.THEORETICAL,
                "Trošak koji algoritam nameće napadaču sa ukradenim hash zapisima.");
        add(target, BenchmarkGroup.HASHING, EvaluationCategory.SECURITY, "H-S2",
                "Memory-hard svojstvo", EvidenceType.THEORETICAL,
                "Otpornost na masovno paralelno pogađanje pomoću GPU/ASIC hardvera.");
        add(target, BenchmarkGroup.HASHING, EvaluationCategory.SECURITY, "H-S3",
                "Salt i rainbow-table zaštita", EvidenceType.MIXED,
                "Generisanje jedinstvenog salta i njegovo čuvanje u samom hash zapisu.");
        add(target, BenchmarkGroup.HASHING, EvaluationCategory.SECURITY, "H-S4",
                "Zrelost i savremene smernice", EvidenceType.THEORETICAL,
                "Standardizovanost, istorija analize i položaj algoritma u aktuelnim smernicama.");

        add(target, BenchmarkGroup.HASHING, EvaluationCategory.IMPLEMENTATION_PRACTICALITY, "H-I1",
                "Jednostavnost API-ja", EvidenceType.THEORETICAL,
                "Količina aplikacionog koda potrebna za hashovanje i proveru.");
        add(target, BenchmarkGroup.HASHING, EvaluationCategory.IMPLEMENTATION_PRACTICALITY, "H-I2",
                "Upravljanje parametrima", EvidenceType.THEORETICAL,
                "Jasnoća izbora i validacije cost, salt i output parametara.");
        add(target, BenchmarkGroup.HASHING, EvaluationCategory.IMPLEMENTATION_PRACTICALITY, "H-I3",
                "Prenosivost i zavisnosti", EvidenceType.THEORETICAL,
                "Dostupnost pouzdanih Java implementacija i dodatnih biblioteka.");
        add(target, BenchmarkGroup.HASHING, EvaluationCategory.IMPLEMENTATION_PRACTICALITY, "H-I4",
                "Samodovoljan hash format", EvidenceType.MIXED,
                "Da li zapis sadrži algoritam, salt i parametre potrebne za buduću proveru.");

        add(target, BenchmarkGroup.HASHING, EvaluationCategory.PASSWORD_MANAGER_SUITABILITY, "H-M1",
                "Balans login latencije", EvidenceType.MIXED,
                "Da li je legitimna prijava prihvatljivo brza uz namerno skup napad.");
        add(target, BenchmarkGroup.HASHING, EvaluationCategory.PASSWORD_MANAGER_SUITABILITY, "H-M2",
                "Zaštita pri kompromitovanoj bazi", EvidenceType.THEORETICAL,
                "Pogodnost za scenario u kom napadač dobije hash, salt i parametre.");
        add(target, BenchmarkGroup.HASHING, EvaluationCategory.PASSWORD_MANAGER_SUITABILITY, "H-M3",
                "Buduće podešavanje i migracija", EvidenceType.THEORETICAL,
                "Mogućnost povećanja cost-a i provere starijih zapisa tokom migracije.");
        add(target, BenchmarkGroup.HASHING, EvaluationCategory.PASSWORD_MANAGER_SUITABILITY, "H-M4",
                "Ograničenja lozinke i interoperabilnost", EvidenceType.THEORETICAL,
                "Uticaj ograničenja ulaza i dostupnosti implementacija na realnu aplikaciju.");
    }

    private void addEncryptionCriteria(List<EvaluationCriterion> target) {
        add(target, BenchmarkGroup.ENCRYPTION, EvaluationCategory.PERFORMANCE, "E-P1",
                "Vreme šifrovanja", EvidenceType.MEASURED,
                "Prosečna medijana šifrovanja kroz sve testirane veličine ulaza.");
        add(target, BenchmarkGroup.ENCRYPTION, EvaluationCategory.PERFORMANCE, "E-P2",
                "Vreme dešifrovanja", EvidenceType.MEASURED,
                "Prosečna medijana dešifrovanja kroz sve testirane veličine ulaza.");
        add(target, BenchmarkGroup.ENCRYPTION, EvaluationCategory.PERFORMANCE, "E-P3",
                "Skaliranje sa veličinom ulaza", EvidenceType.MEASURED,
                "Kombinovano vreme šifrovanja i dešifrovanja najvećeg ulaza.");
        add(target, BenchmarkGroup.ENCRYPTION, EvaluationCategory.PERFORMANCE, "E-P4",
                "Storage overhead", EvidenceType.MEASURED,
                "Dodatni UTF-8 prostor za Base64 ciphertext i kriptografske metapodatke.");

        add(target, BenchmarkGroup.ENCRYPTION, EvaluationCategory.SECURITY, "E-S1",
                "Poverljivost", EvidenceType.THEORETICAL,
                "Kriptografska snaga zaštite sadržaja password entry-ja.");
        add(target, BenchmarkGroup.ENCRYPTION, EvaluationCategory.SECURITY, "E-S2",
                "Integritet i autentičnost", EvidenceType.MIXED,
                "Detekcija izmenjenog ciphertext-a, taga i IV/nonce vrednosti.");
        add(target, BenchmarkGroup.ENCRYPTION, EvaluationCategory.SECURITY, "E-S3",
                "Bezbedno upravljanje IV/nonce vrednošću", EvidenceType.THEORETICAL,
                "Posledice ponavljanja IV/nonce vrednosti i zahtev za njihovom jedinstvenošću.");
        add(target, BenchmarkGroup.ENCRYPTION, EvaluationCategory.SECURITY, "E-S4",
                "Zrelost konstrukcije", EvidenceType.THEORETICAL,
                "Standardizovanost i količina prostora za grešku pri pravilnoj kompoziciji.");

        add(target, BenchmarkGroup.ENCRYPTION, EvaluationCategory.IMPLEMENTATION_PRACTICALITY, "E-I1",
                "Dostupnost Java API-ja", EvidenceType.THEORETICAL,
                "Podrška kroz standardni JCA/JCE bez dodatne kriptografske implementacije.");
        add(target, BenchmarkGroup.ENCRYPTION, EvaluationCategory.IMPLEMENTATION_PRACTICALITY, "E-I2",
                "Složenost implementacije", EvidenceType.THEORETICAL,
                "Broj kriptografskih koraka i mogućnost greške u redosledu operacija.");
        add(target, BenchmarkGroup.ENCRYPTION, EvaluationCategory.IMPLEMENTATION_PRACTICALITY, "E-I3",
                "Upravljanje ključevima", EvidenceType.THEORETICAL,
                "Potreba za jednim ključem ili bezbednim razdvajanjem encryption i MAC ključa.");
        add(target, BenchmarkGroup.ENCRYPTION, EvaluationCategory.IMPLEMENTATION_PRACTICALITY, "E-I4",
                "Upravljanje metapodacima", EvidenceType.MIXED,
                "Količina i složenost IV/nonce, tag i parameter podataka uz ciphertext.");

        add(target, BenchmarkGroup.ENCRYPTION, EvaluationCategory.PASSWORD_MANAGER_SUITABILITY, "E-M1",
                "Efikasnost za kratke tajne", EvidenceType.MIXED,
                "Ponašanje na veličinama tipičnim za lozinke, a ne velike fajlove.");
        add(target, BenchmarkGroup.ENCRYPTION, EvaluationCategory.PASSWORD_MANAGER_SUITABILITY, "E-M2",
                "Zaštita pri kompromitovanoj bazi", EvidenceType.THEORETICAL,
                "Zaštita password polja kada napadač poseduje ciphertext i metapodatke, ali ne ključ.");
        add(target, BenchmarkGroup.ENCRYPTION, EvaluationCategory.PASSWORD_MANAGER_SUITABILITY, "E-M3",
                "Detekcija manipulacije", EvidenceType.MIXED,
                "Pouzdano odbijanje entry-ja koji je izmenjen u bazi.");
        add(target, BenchmarkGroup.ENCRYPTION, EvaluationCategory.PASSWORD_MANAGER_SUITABILITY, "E-M4",
                "Prenosivost na različit hardver", EvidenceType.THEORETICAL,
                "Očekivano ponašanje sa i bez specijalizovane AES hardverske podrške.");
    }

    private void add(
            List<EvaluationCriterion> target,
            BenchmarkGroup group,
            EvaluationCategory category,
            String code,
            String name,
            EvidenceType evidenceType,
            String description) {
        target.add(new EvaluationCriterion(group, category, code, name, evidenceType, description));
    }

    public List<EvaluationCriterion> getAll() {
        return criteria;
    }

    public EvaluationCriterion getByCode(String code) {
        EvaluationCriterion criterion = criteriaByCode.get(code);
        if (criterion == null) {
            throw new IllegalArgumentException("Unknown evaluation criterion: " + code);
        }
        return criterion;
    }
}
