package Benchmark;

import java.util.LinkedHashMap;
import java.util.Map;

/** Primary and authoritative references used for theoretical scoring. */
public final class BenchmarkReferences {

    private BenchmarkReferences() {
    }

    public static Map<String, String> all() {
        Map<String, String> references = new LinkedHashMap<>();
        references.put("Argon2 / RFC 9106", "https://www.rfc-editor.org/rfc/rfc9106.html");
        references.put("PBKDF2 / NIST SP 800-132", "https://csrc.nist.gov/pubs/sp/800/132/final");
        references.put("bcrypt original paper", "https://www.usenix.org/conference/1999-usenix-annual-technical-conference/future-adaptable-password-scheme");
        references.put("Password Storage / OWASP", "https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html");
        references.put("AES-GCM / NIST SP 800-38D", "https://csrc.nist.gov/pubs/sp/800/38/d/final");
        references.put("AES-CBC / NIST SP 800-38A", "https://csrc.nist.gov/pubs/sp/800/38/a/final");
        references.put("Encrypt-then-MAC / RFC 7366", "https://www.rfc-editor.org/rfc/rfc7366.html");
        references.put("ChaCha20-Poly1305 / RFC 8439", "https://www.rfc-editor.org/rfc/rfc8439.html");
        references.put("Java security algorithms", "https://docs.oracle.com/en/java/javase/23/docs/specs/security/standard-names.html");
        return references;
    }
}
