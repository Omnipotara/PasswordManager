# TASKS.md

Ovaj fajl prati rad na projektu "PasswordManager" u okviru diplomskog rada:

**Projektovanje i implementacija sistema za bezbedno upravljanje lozinkama uz komparativnu analizu kriptografskih algoritama**

Projekat ostaje Java desktop aplikacija sa trenutnom NetBeans/Ant strukturom i izvorima u `src/...`.

## Status oznake

- `TODO` - zadatak je definisan, ali nije započet.
- `IN_PROGRESS` - zadatak je trenutno u radu.
- `DONE` - zadatak je završen i proveren.
- `BLOCKED` - zadatak čeka odluku, podatak, biblioteku, konfiguraciju ili spoljašnji uslov.

## Pravila rada

- Ne prelaziti na Maven i ne uvoditi `src/main/java` strukturu.
- Ne menjati originalni Word dokument diplomskog rada bez eksplicitnog zahteva.
- Ne praviti velike refaktore bez prethodnog ažuriranja ovog fajla i `DECISIONS.md`.
- Posle svake veće implementacione faze ažurirati status zadataka.
- Kriptografske algoritme koristiti kroz proverene biblioteke ili Java Cryptography Architecture, ne implementirati ih ručno.
- Algoritam za hashing se čuva uz korisnika, a algoritam za enkripciju uz konkretan password entry.

---

## Faza 0: Inicijalna analiza i projektni kontekst

### TASK 0.1 - Analiza trenutnog projekta

Status: DONE

Opis:
Pregledana je trenutna struktura projekta, Git stanje, NetBeans konfiguracija, SQL dump, modeli, Swing forme, database sloj i postojeća kriptografska logika.

Fajlovi:
- `src/Singletons/Controller.java`
- `src/Database/DBConnection.java`
- `src/Database/DBBroker.java`
- `src/Cryptography/HashUtils.java`
- `src/Cryptography/CryptoUtils.java`
- `src/Model/User.java`
- `src/Model/PasswordEntry.java`
- `src/Model/EntryTableModel.java`
- `src/View/LoginForm.java`
- `src/View/RegistrationForm.java`
- `src/View/MainForm.java`
- `src/View/InsertForm.java`
- `passwordmanager_db.sql`
- `nbproject/project.properties`
- `build.xml`

Kriterijum završetka:
Utvrđeno je da projekat već koristi Swing, MySQL, BCrypt, PBKDF2 derivaciju ključa i AES-GCM enkripciju password entry lozinke.

### TASK 0.2 - Usklađivanje `CODEX_CONTEXT.md` sa realnom strukturom projekta

Status: DONE

Opis:
Iz kontekst dokumenta uklonjene su smernice koje traže Maven, `pom.xml` i `src/main/java`. Dokument je usklađen sa odlukom da projekat ostaje NetBeans/Ant aplikacija sa postojećom `src/...` strukturom.

Fajlovi:
- `context/CODEX_CONTEXT.md`

Kriterijum završetka:
U dokumentu više nema pomena `Maven`, `pom.xml` ni `src/main/java`.

### TASK 0.3 - Analiza započetog diplomskog rada

Status: DONE

Opis:
DOCX fajl diplomskog rada pročitan je strukturno bez izmene originalnog dokumenta. Identifikovana su popunjena poglavlja, placeholder-i, tabele i mesta koja treba uskladiti sa implementacijom projekta.

Fajlovi:
- `D:\Fakultet\DIPLOMSKI\Diplomski rad.docx`

Kriterijum završetka:
Utvrđeno je da su apstrakt, uvod i teorijske osnove uglavnom popunjeni, dok poglavlja 3-8 čekaju konkretne rezultate projektovanja, implementacije, testiranja, benchmark-a i bezbednosne analize.

### TASK 0.4 - Kreiranje inicijalnih fajlova `TASKS.md` i `DECISIONS.md`

Status: DONE

Opis:
Napraviti početne fajlove za praćenje rada i tehničkih odluka.

Fajlovi:
- `TASKS.md`
- `DECISIONS.md`

Kriterijum završetka:
Oba fajla postoje u root-u projekta i sadrže faze rada, zadatke, odluke i otvorena pitanja.

---

## Faza 1: Repository hygiene i stabilna početna tačka

### TASK 1.1 - Rešiti case-duplikat `PasswordEntry.java` / `passwordEntry.java`

Status: DONE

Opis:
Git indeks je sadržao i `src/Model/PasswordEntry.java` i `src/Model/passwordEntry.java`, što je rizično na Windows sistemu zbog `core.ignorecase=true`. Lowercase zapis je uklonjen iz Git indeksa, a kanonski fajl ostaje `src/Model/PasswordEntry.java`.

Fajlovi:
- `src/Model/PasswordEntry.java`
- Git indeks

Kriterijum završetka:
Git prati samo jedan fajl za klasu `PasswordEntry`, ime fajla odgovara imenu javne klase, a lowercase duplikat je označen za uklanjanje iz repozitorijuma bez brisanja lokalnog kanonskog fajla.

### TASK 1.2 - Definisati šta se ignoriše u Git-u

Status: DONE

Opis:
Proširen je `.gitignore` i iz Git indeksa su skinuti već praćeni `build/classes` i `nbproject/private` fajlovi. Lokalni fajlovi nisu obrisani; samo više neće ulaziti u commitove.

Fajlovi:
- `.gitignore`
- `build/classes/...`
- `nbproject/private/...`

Kriterijum završetka:
Postoji jasno pravilo šta ostaje u repozitorijumu, šta se ignoriše i šta je skinuto iz Git indeksa bez brisanja lokalnih fajlova.

### TASK 1.3 - Proveriti build baseline

Status: DONE

Opis:
Pre refaktorisanja je provereno da li se projekat može kompajlirati u trenutnom stanju. Java 23 je dostupna, MySQL connector na trenutno podešenoj apsolutnoj putanji postoji, a NetBeans Ant build prolazi.

Fajlovi:
- `nbproject/project.properties`
- `build.xml`
- `src/...`

Kriterijum završetka:
Build komanda `C:\Program Files\NetBeans-24\netbeans\extide\ant\bin\ant.bat clean jar` završava uspešno i generiše `dist/PasswordManager.jar`.

---

## Faza 2: Package organizacija i kriptografska arhitektura

### TASK 2.1 - Predložiti novu organizaciju paketa unutar `src/...`

Status: DONE

Opis:
Predložena je postepena organizacija paketa koja odgovara postojećem NetBeans/Ant projektu, bez nasilnog pomeranja svega odjednom. Nova struktura uvodi jasne zone za konfiguraciju, kriptografiju, servise, MFA i benchmark, dok postojeći `Database`, `Model`, `Singletons` i `View` paketi ostaju stabilni.

Dogovoreni ciljni paketi:
- `Config`
- `Cryptography`
- `Cryptography.Hashing`
- `Cryptography.Encryption`
- `Cryptography.KeyDerivation`
- `Cryptography.Model`
- `Cryptography.Factory`
- `Database`
- `Model`
- `Service`
- `MFA`
- `Benchmark`
- `Benchmark.Hashing`
- `Benchmark.Encryption`
- `Singletons`
- `View`

Fajlovi:
- `TASKS.md`
- `DECISIONS.md`

Kriterijum završetka:
U `DECISIONS.md` je zabeležena nova paketna organizacija, a refaktorisanje je podeljeno u male korake. U ovom task-u nisu premeštane postojeće Java klase.

### TASK 2.2 - Uvesti zajedničke nazive algoritama

Status: DONE

Opis:
Definisani su stabilni interni nazivi algoritama koji se čuvaju u bazi i koriste u factory/resolver klasama.

Usvojeni nazivi:
- `BCRYPT`
- `PBKDF2`
- `ARGON2ID`
- `AES_GCM`
- `AES_CBC_HMAC`
- `CHACHA20_POLY1305`

Fajlovi:
- `src/Cryptography/AlgorithmName.java`

Kriterijum završetka:
Nazivi algoritama nisu rasuti kao ručni stringovi po UI, DB i kriptografskim klasama. `AlgorithmName` razlikuje hashing i encryption algoritme i ume da mapira vrednost pročitanu iz baze.

### TASK 2.3 - Uvesti izuzetke za nepodržane algoritme i kripto greške

Status: DONE

Opis:
Dodati su jasni izuzeci za slučaj da baza sadrži nepoznat algoritam ili da kriptografska operacija ne uspe.

Fajlovi:
- `src/Cryptography/CryptoException.java`
- `src/Cryptography/UnsupportedAlgorithmException.java`
- `src/Cryptography/CryptoOperationException.java`
- `src/Cryptography/AlgorithmName.java`

Kriterijum završetka:
Factory/resolver i servisni sloj mogu jasno da razlikuju nepodržan algoritam od neuspele kriptografske operacije. `AlgorithmName.fromDatabaseValue(...)` baca `UnsupportedAlgorithmException` za nepoznate vrednosti.

---

## Faza 3: Hashing strategije za master lozinku

### TASK 3.1 - Uvesti `HashingStrategy` interfejs

Status: TODO

Opis:
Napraviti zajednički interfejs za algoritme koji štite master lozinku korisnika.

Fajlovi:
- Novi fajl: `src/Cryptography/Hashing/HashingStrategy.java`

Kriterijum završetka:
Interfejs definiše metode za hash, verify i naziv algoritma, uz tipove koji ne forsiraju čuvanje plain text lozinke duže nego što je potrebno.

### TASK 3.2 - Prebaciti postojeći BCrypt kod u `BCryptStrategy`

Status: TODO

Opis:
Postojeći `HashUtils` trenutno direktno koristi BCrypt. Logiku treba preseliti ili obmotati kroz `BCryptStrategy`, uz očuvanje kompatibilnosti sa postojećim korisnicima u bazi.

Fajlovi:
- `src/Cryptography/HashUtils.java`
- Novi fajl: `src/Cryptography/Hashing/BCryptStrategy.java`
- `src/Database/DBBroker.java`

Kriterijum završetka:
BCrypt hash i verify rade preko `HashingStrategy`, a postojeći BCrypt hash-evi ostaju validni.

### TASK 3.3 - Implementirati `PBKDF2HashingStrategy`

Status: TODO

Opis:
Dodati PBKDF2 strategiju za zaštitu master lozinke. Ovu strategiju razlikovati od postojeće PBKDF2 derivacije encryption ključa.

Fajlovi:
- Novi fajl: `src/Cryptography/Hashing/PBKDF2HashingStrategy.java`

Kriterijum završetka:
Strategija generiše salt, čuva parametre i verifikuje lozinku koristeći isti format zapisa.

### TASK 3.4 - Implementirati `Argon2idStrategy`

Status: TODO

Opis:
Dodati Argon2id strategiju pomoću proverene biblioteke. Ne implementirati Argon2 ručno.

Fajlovi:
- Novi fajl: `src/Cryptography/Hashing/Argon2idStrategy.java`
- Bouncy Castle JAR u `lib/`
- NetBeans/Ant classpath

Kriterijum završetka:
Argon2id hash i verify rade sa dokumentovanim parametrima, a biblioteka je jasno zabeležena u `DECISIONS.md`.

### TASK 3.5 - Uvesti `HashingStrategyFactory`

Status: TODO

Opis:
Dodati resolver koji na osnovu naziva algoritma vraća odgovarajuću hashing strategiju.

Fajlovi:
- Novi fajl: `src/Cryptography/Factory/HashingStrategyFactory.java`

Kriterijum završetka:
Business logika nema `if/else` grananje za svaki hashing algoritam.

### TASK 3.6 - Dodati izbor hashing algoritma pri registraciji

Status: TODO

Opis:
Registration forma treba da omogući izbor hashing algoritma za novog korisnika.

Fajlovi:
- `src/View/RegistrationForm.java`
- `src/View/RegistrationForm.form`
- `src/Database/DBBroker.java`
- `src/Model/User.java`

Kriterijum završetka:
Novi korisnik se registruje sa izabranim algoritmom, a algoritam i parametri se čuvaju uz korisnika.

---

## Faza 4: Korisnik, email identifikator i baza

### TASK 4.1 - Proširiti model `User`

Status: TODO

Opis:
Model korisnika treba da podrži email, hash algoritam, parametre hashing-a i MFA status.

Fajlovi:
- `src/Model/User.java`

Kriterijum završetka:
`User` više ne zavisi samo od `username/password/salt` modela, već može da predstavlja novo stanje baze.

### TASK 4.2 - Zameniti username email adresom u UI i logici

Status: TODO

Opis:
Login i registration forme treba da koriste email kao korisnički identifikator.

Fajlovi:
- `src/View/LoginForm.java`
- `src/View/LoginForm.form`
- `src/View/RegistrationForm.java`
- `src/View/RegistrationForm.form`
- `src/Database/DBBroker.java`
- `src/Model/User.java`

Kriterijum završetka:
Korisnik se registruje i prijavljuje email adresom, uz osnovnu validaciju email formata.

### TASK 4.3 - Ukloniti account enumeration iz login poruka

Status: TODO

Opis:
Poruke o neuspešnoj prijavi ne smeju otkrivati da li email postoji.

Fajlovi:
- `src/View/LoginForm.java`
- `src/Database/DBBroker.java`

Kriterijum završetka:
Za pogrešan email i pogrešnu lozinku korisnik dobija istu bezbednosno neutralnu poruku.

### TASK 4.4 - Definisati i napisati SQL migraciju za tabelu `users`

Status: TODO

Opis:
Napraviti migraciju koja uvodi email, hash metadata i MFA polja.

Moguća polja:
- `email`
- `password_hash`
- `hash_algorithm`
- `hash_salt`
- `hash_parameters`
- `mfa_enabled`
- `created_at`
- `updated_at`

Fajlovi:
- Novi direktorijum/fajl: `db/migrations/...`
- `passwordmanager_db.sql`, ako se ažurira referentni dump

Kriterijum završetka:
Postoji SQL migracija i dokumentovana odluka o migraciji postojećih korisnika.

---

## Faza 5: Encryption strategije i metapodaci za password entry

### TASK 5.1 - Izdvojiti derivaciju ključa u posebnu komponentu

Status: TODO

Opis:
Postojeća PBKDF2 derivacija ključa nalazi se u `CryptoUtils`. Treba je odvojiti od same enkripcije.

Fajlovi:
- `src/Cryptography/CryptoUtils.java`
- Novi fajl: `src/Cryptography/KeyDerivation/KeyDerivationService.java`

Kriterijum završetka:
Hashing master lozinke, derivacija encryption ključa i encryption strategije su jasno odvojene.

### TASK 5.2 - Uvesti `EncryptedData` model

Status: TODO

Opis:
Napraviti model koji objedinjuje ciphertext, IV/nonce, authentication tag ako se čuva odvojeno, salt/parametre ako su potrebni i naziv algoritma.

Fajlovi:
- Novi fajl: `src/Cryptography/Model/EncryptedData.java`

Kriterijum završetka:
Encryption strategije vraćaju strukturirane podatke umesto ad hoc Base64 stringova.

### TASK 5.3 - Uvesti `EncryptionStrategy` interfejs

Status: TODO

Opis:
Napraviti zajednički interfejs za algoritme koji šifruju i dešifruju password entry podatke.

Fajlovi:
- Novi fajl: `src/Cryptography/Encryption/EncryptionStrategy.java`

Kriterijum završetka:
AES-GCM, AES-CBC-HMAC i ChaCha20-Poly1305 mogu da se koriste kroz isti interfejs.

### TASK 5.4 - Prebaciti postojeći AES-GCM kod u `AESGCMStrategy`

Status: TODO

Opis:
Postojeći AES-GCM kod iz `CryptoUtils` treba pretvoriti u strategiju, uz očuvanje kompatibilnosti sa starim zapisima koji imaju IV i ciphertext spojene u jednom Base64 stringu.

Fajlovi:
- `src/Cryptography/CryptoUtils.java`
- Novi fajl: `src/Cryptography/Encryption/AESGCMStrategy.java`
- `src/Database/DBBroker.java`

Kriterijum završetka:
Novi AES-GCM zapisi se čuvaju sa metapodacima, a stari zapisi se mogu pročitati ili migrirati.

### TASK 5.5 - Implementirati `AESCBCStrategy` sa HMAC zaštitom integriteta

Status: TODO

Opis:
AES-CBC se uvodi isključivo radi komparativne analize i mora imati HMAC ili ekvivalentnu proveru integriteta.

Fajlovi:
- Novi fajl: `src/Cryptography/Encryption/AESCBCStrategy.java`

Kriterijum završetka:
Izmena ciphertext-a, IV-a ili HMAC-a dovodi do neuspešne dekripcije.

### TASK 5.6 - Implementirati `ChaCha20Poly1305Strategy`

Status: TODO

Opis:
Dodati ChaCha20-Poly1305 kao AEAD strategiju kroz Java Cryptography Architecture ili proverenu biblioteku.

Fajlovi:
- Novi fajl: `src/Cryptography/Encryption/ChaCha20Poly1305Strategy.java`
- NetBeans classpath / `lib` direktorijum, ako bude potreban

Kriterijum završetka:
ChaCha20-Poly1305 encryption/decryption rade i ponašaju se ispravno pri izmenjenom ciphertext-u ili nonce-u.

### TASK 5.7 - Uvesti `EncryptionStrategyFactory`

Status: TODO

Opis:
Dodati resolver koji na osnovu naziva algoritma iz baze vraća odgovarajuću encryption strategiju.

Fajlovi:
- Novi fajl: `src/Cryptography/Factory/EncryptionStrategyFactory.java`

Kriterijum završetka:
Dekripcija se oslanja na algoritam sačuvan uz entry, a ne na trenutno izabran algoritam u formi.

### TASK 5.8 - Proširiti model `PasswordEntry`

Status: TODO

Opis:
Model treba da podrži service, username/email za servis, password, description i encryption metapodatke.

Fajlovi:
- `src/Model/PasswordEntry.java`

Kriterijum završetka:
Model može da predstavlja podatke potrebne za samostalnu dekripciju entry-ja.

### TASK 5.9 - Definisati i napisati SQL migraciju za `password_entries`

Status: TODO

Opis:
Dodati polja za encryption algoritam i metapodatke.

Moguća polja:
- `encryption_algorithm`
- `iv`
- `nonce`
- `authentication_tag`
- `encryption_salt`
- `encryption_parameters`
- `created_at`
- `updated_at`

Fajlovi:
- Novi fajl: `db/migrations/...`
- `passwordmanager_db.sql`, ako se ažurira referentni dump

Kriterijum završetka:
Baza sadrži dovoljno informacija da se svaki password entry dekriptuje algoritmom kojim je napravljen.

### TASK 5.10 - Dodati izbor encryption algoritma u `InsertForm`

Status: TODO

Opis:
Forma za unos treba da omogući izbor algoritma za konkretan entry. Detalji postojećeg entry-ja treba da prikažu korišćeni algoritam.

Fajlovi:
- `src/View/InsertForm.java`
- `src/View/InsertForm.form`
- `src/View/MainForm.java`
- `src/Database/DBBroker.java`

Kriterijum završetka:
Korisnik može da ima više zapisa šifrovanih različitim algoritmima, a svi ostaju čitljivi.

---

## Faza 6: Konfiguracija aplikacije

### TASK 6.1 - Uvesti `application.example.properties`

Status: TODO

Opis:
Dodati primer konfiguracije za bazu i eventualno email/MFA parametre. Stvarni `application.properties` mora biti ignorisan.

Fajlovi:
- Novi fajl: `application.example.properties`
- `.gitignore`

Kriterijum završetka:
Repozitorijum sadrži primer konfiguracije, ali ne sadrži stvarne lokalne kredencijale.

### TASK 6.2 - Uvesti `ConfigLoader`

Status: TODO

Opis:
Dodati komponentu za učitavanje i validaciju konfiguracije.

Fajlovi:
- Novi fajl: `src/Config/ConfigLoader.java`
- Novi fajl: `src/Config/AppConfig.java`

Kriterijum završetka:
Aplikacija jasno prijavljuje grešku ako konfiguracija nedostaje ili nije validna.

### TASK 6.3 - Prebaciti `DBConnection` na konfiguracioni fajl

Status: TODO

Opis:
Ukloniti hard-coded DB URL, username i password iz Java koda.

Fajlovi:
- `src/Database/DBConnection.java`
- `src/Config/...`
- `application.example.properties`

Kriterijum završetka:
Promena DB parametara ne zahteva izmenu Java klase.

---

## Faza 7: Email OTP MFA

### TASK 7.1 - Definisati MFA model i tok

Status: TODO

Opis:
Definisati kako se generiše OTP, gde se čuva, koliko traje, koliko pokušaja je dozvoljeno i kako se vezuje za korisnika.

Fajlovi:
- `DECISIONS.md`
- Novi fajlovi u `src/MFA/...`

Kriterijum završetka:
Postoji dokumentovana odluka o MFA toku pre pisanja UI i DB koda.

### TASK 7.2 - Implementirati OTP generator i verifikator

Status: TODO

Opis:
Dodati generisanje jednokratnog koda, hashovanje koda pre čuvanja i proveru isteka.

Fajlovi:
- Novi fajlovi u `src/MFA/...`

Kriterijum završetka:
OTP se ne čuva kao plain text ako se perzistira, a istekao ili pogrešan kod se odbija.

### TASK 7.3 - Implementirati email slanje OTP koda

Status: TODO

Opis:
Dodati servis za slanje email poruka putem realne SMTP konfiguracije. JavaMail/Gmail SMTP primer korisnika koristiti kao smernicu, ali stvarne kredencijale učitavati iz konfiguracije i ne hardkodovati ih.

Fajlovi:
- Novi fajlovi u `src/MFA/...` ili `src/Service/...`
- `application.example.properties`
- `lib/...` za mail biblioteku ako bude potrebna

Kriterijum završetka:
OTP kod može da se isporuči korisniku preko SMTP-a, a stvarni email nalog i lozinka/app-password nisu commitovani.

### TASK 7.4 - Ugraditi MFA u login tok

Status: TODO

Opis:
Nakon ispravne master lozinke, ako je MFA uključen, prikazati dijalog/formu za unos OTP koda.

Fajlovi:
- `src/View/LoginForm.java`
- Novi UI fajl ako bude potreban
- `src/Singletons/Controller.java`
- `src/Database/DBBroker.java`

Kriterijum završetka:
Korisnik ne ulazi u `MainForm` dok ne prođe OTP proveru kada je MFA uključen.

---

## Faza 8: Testiranje

### TASK 8.1 - Pripremiti JUnit u NetBeans/Ant projektu

Status: TODO

Opis:
Dodati potrebne JUnit dependency-je i proveriti da testovi mogu da se pokrenu u postojećoj strukturi.

Fajlovi:
- `nbproject/project.properties`
- `test/...`
- Eventualni `lib/...`

Kriterijum završetka:
Postoji minimalan test koji se uspešno pokreće.

### TASK 8.2 - Unit testovi za hashing strategije

Status: TODO

Opis:
Testirati BCrypt, PBKDF2 i Argon2id za uspešnu i neuspešnu proveru master lozinke.

Fajlovi:
- `test/...`
- `src/Cryptography/Hashing/...`

Kriterijum završetka:
Svaka hashing strategija ima test za ispravnu i pogrešnu lozinku.

### TASK 8.3 - Unit testovi za encryption strategije

Status: TODO

Opis:
Testirati AES-GCM, AES-CBC-HMAC i ChaCha20-Poly1305 za encryption/decryption i detekciju izmena.

Fajlovi:
- `test/...`
- `src/Cryptography/Encryption/...`

Kriterijum završetka:
Pogrešan ključ, izmenjen ciphertext, IV/nonce ili tag/HMAC dovode do neuspešne dekripcije.

### TASK 8.4 - Testovi za strategy factory/resolver klase

Status: TODO

Opis:
Proveriti da poznati algoritmi vraćaju ispravne strategije, a nepoznati algoritmi bacaju jasan izuzetak.

Fajlovi:
- `test/...`
- `src/Cryptography/Factory/...`

Kriterijum završetka:
Factory klase su pokrivene pozitivnim i negativnim testovima.

### TASK 8.5 - Integration testovi za korisnički tok

Status: TODO

Opis:
Testirati registraciju, prijavu, dodavanje entry-ja, čitanje, izmenu i brisanje. Posebno testirati više entry-ja šifrovanih različitim algoritmima.

Fajlovi:
- `test/...`
- `src/Database/...`
- `src/Singletons/Controller.java`

Kriterijum završetka:
Osnovni tok aplikacije radi kroz bazu bez ručnog kliktanja UI-ja.

### TASK 8.6 - Security testovi za MFA i account enumeration

Status: TODO

Opis:
Testirati pogrešan OTP, istekao OTP, previše pokušaja ako se uvede ograničenje i neutralne login poruke.

Fajlovi:
- `test/...`
- `src/MFA/...`
- `src/View/LoginForm.java`

Kriterijum završetka:
Bezbednosni scenariji iz diplomskog imaju test pokriće.

---

## Faza 9: Benchmark framework

### TASK 9.1 - Definisati benchmark model rezultata

Status: TODO

Opis:
Dodati model koji čuva naziv algoritma, parametre, veličinu ulaza, iteraciju, vreme izvršavanja i memorijsku potrošnju ako se meri.

Fajlovi:
- Novi fajlovi u `src/Benchmark/...`

Kriterijum završetka:
Hashing i encryption benchmark mogu da koriste isti ili srodan model rezultata.

### TASK 9.2 - Implementirati CSV export benchmark rezultata

Status: TODO

Opis:
Rezultate benchmark-a izvoziti u CSV format pogodan za tabele i grafikone u diplomskom.

Fajlovi:
- Novi fajlovi u `src/Benchmark/...`
- Mogući output folder: `benchmark-results/`

Kriterijum završetka:
Benchmark generiše CSV sa stabilnim kolonama.

### TASK 9.3 - Implementirati hashing benchmark

Status: TODO

Opis:
Meriti BCrypt, PBKDF2 i Argon2id sa različitim parametrima i brojem ponavljanja.

Fajlovi:
- Novi fajlovi u `src/Benchmark/Hashing/...`

Kriterijum završetka:
Postoje CSV rezultati za hashing algoritme koji se mogu koristiti u poglavlju 6 diplomskog.

### TASK 9.4 - Implementirati encryption benchmark

Status: TODO

Opis:
Meriti AES-GCM, AES-CBC-HMAC i ChaCha20-Poly1305 za šifrovanje i dešifrovanje ulaza različitih veličina.

Fajlovi:
- Novi fajlovi u `src/Benchmark/Encryption/...`

Kriterijum završetka:
Postoje CSV rezultati za encryption algoritme koji se mogu koristiti u poglavlju 6 diplomskog.

---

## Faza 10: Dokumentacija, dijagrami i diplomski rad

### TASK 10.1 - Pripremiti tehnički opis trenutne arhitekture

Status: TODO

Opis:
Napisati tekst koji može da se koristi za poglavlja 3 i 4 diplomskog rada: akteri, slučajevi korišćenja, komponente, modeli i tokovi.

Fajlovi:
- Novi markdown fajl po dogovoru ili tekst za kasniju DOCX doradu

Kriterijum završetka:
Postoji tehnički opis usklađen sa stvarnim kodom.

### TASK 10.2 - Pripremiti listu dijagrama

Status: TODO

Opis:
Definisati potrebne UML/sekvencne/dijagrame napada koji su već označeni u diplomskom.

Fajlovi:
- `DECISIONS.md`
- Mogući direktorijum za izvore dijagrama

Kriterijum završetka:
Za svaki placeholder dijagrama zna se šta treba da prikazuje i iz kojih klasa/podataka se izvodi.

### TASK 10.3 - Uskladiti tekst diplomskog sa implementacijom

Status: TODO

Opis:
Kasnije, nakon implementacije, uskladiti apstrakt i poglavlja 3-8 sa stvarnim tehničkim rešenjem.

Fajlovi:
- `D:\Fakultet\DIPLOMSKI\Diplomski rad.docx`

Kriterijum završetka:
Tekst rada ne tvrdi funkcionalnosti koje projekat nema i ne opisuje globalni izbor algoritama ako se algoritmi čuvaju uz podatke.

---

## Otvorena pitanja za korisnika

1. RESOLVED: Uvodimo lokalni `lib/` direktorijum sa spoljnim JAR bibliotekama, da projekat ne zavisi od apsolutnih putanja na disku.
2. RESOLVED: Za Argon2id koristimo Bouncy Castle JAR kao proverenu biblioteku.
3. RESOLVED: Password entry šifruje samo lozinku kao sada. Service, username/email i description ostaju čitljivi metapodaci.
4. RESOLVED: Email OTP odmah koristi realan SMTP. Konfiguracija ide u `application.properties`, a primer u `application.example.properties`.
5. RESOLVED: Za završnu verziju pravi se nova čista šema baze, bez migracije postojećih demo podataka.
