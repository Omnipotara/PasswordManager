# DECISIONS.md

Ovaj fajl beleži važne tehničke odluke tokom razvoja projekta "PasswordManager" za diplomski rad.

Svaka odluka treba da sadrži:

- datum;
- kontekst;
- odluku;
- razlog;
- posledice po projekat.

---

## 2026-07-06 - Projekat ostaje desktop Java aplikacija

Kontekst:
Postojeći projekat je Java Swing desktop password manager koji radi sa lokalnom MySQL/MariaDB bazom. Cilj diplomskog rada je kriptografska i bezbednosna analiza, a ne promena tipa aplikacije.

Odluka:
Projekat ostaje desktop aplikacija. Ne uvodi se web platforma, REST API, server-side arhitektura niti distribuirani sistem.

Razlog:
Trenutni kod, UI i baza već odgovaraju lokalnoj desktop aplikaciji. Migracija na drugi tip sistema bi nepotrebno povećala obim rada i skrenula fokus sa kriptografije, Strategy Pattern-a, MFA i benchmark analize.

Posledice:
UI ostaje Java Swing. Baza ostaje lokalna MySQL/MariaDB. Arhitektura se refaktoriše postepeno unutar postojeće aplikacije.

---

## 2026-07-06 - Ne prelazi se na Maven

Kontekst:
Kontekst dokument je prvobitno pominjao Maven, `pom.xml` i `src/main/java`, ali stvarni projekat je NetBeans/Ant projekat sa `build.xml`, `nbproject` i izvorima u `src/...`.

Odluka:
Ne raditi Maven migraciju. Zadržati postojeću NetBeans/Ant strukturu projekta i `src/...` raspored.

Razlog:
Maven migracija nije potrebna za cilj diplomskog rada i otvorila bi dodatni rizik. Postojeći projekat već ima radnu strukturu koju treba nadograditi, ne zameniti.

Posledice:
Nove klase se dodaju u postojeći `src/...` raspored. Dependency-ji se uvode kroz NetBeans classpath, postojeći Ant build ili eventualni lokalni `lib/` direktorijum ako se tako odluči.

---

## 2026-07-06 - Uvodi se lokalni `lib/` direktorijum za spoljne JAR biblioteke

Kontekst:
Postojeći projekat trenutno referencira MySQL connector preko apsolutne putanje u `nbproject/project.properties`. To otežava pokretanje projekta na drugoj mašini i otežava dodavanje novih biblioteka za Argon2id, JUnit ili druge potrebe.

Odluka:
Uvesti lokalni `lib/` direktorijum u okviru projekta za spoljne JAR biblioteke koje su potrebne za build i pokretanje aplikacije.

Razlog:
Projekat treba da bude prenosiviji i manje vezan za lokalne apsolutne putanje. Ovo je posebno važno za diplomski rad, gde projekat treba lako demonstrirati i objasniti.

Posledice:
NetBeans/Ant classpath treba prebaciti da koristi JAR fajlove iz `lib/`. U `lib/` ne treba stavljati osetljive konfiguracione fajlove, već samo biblioteke potrebne za rad projekta.

Azurirano 2026-07-07:
U `lib/` su dodate proverene biblioteke koje pomazu da se algoritmi koriste preko gotovih implementacija, jer cilj diplomskog rada nije implementacija kriptografije od nule, vec poredjenje algoritama i prikaz rada password manager-a:

- `bcprov-jdk18on-1.84.jar` - Bouncy Castle provider za Argon2id, ChaCha20-Poly1305 i dodatnu kripto podrsku.
- `password4j-1.8.4.jar` - biblioteka za password hashing algoritme, korisna za poredjenje i potencijalno krace strategije.
- `jakarta.mail-2.0.5.jar` - Jakarta Mail implementacija za SMTP slanje OTP kodova.
- `angus-activation-2.0.3.jar` - Activation podrska potrebna za Jakarta Mail.
- `mysql-connector-j-9.7.0.jar` - lokalni MySQL JDBC connector umesto apsolutne putanje van projekta.

SHA-256 preuzetih JAR fajlova:

```text
angus-activation-2.0.3.jar A6BD35C538CF90FFF941AD6258C40C08FCA0B5C9C3F536C657114F27CE0527A7
bcprov-jdk18on-1.84.jar 64D6C5A6121FCD927152DD182CBED39AFE0FDA641A970D9BCC0C9CB1858B2731
jakarta.mail-2.0.5.jar F48001755A4A591BB1B602965337FCF316D794C43783052F88EDE05130539358
mysql-connector-j-9.7.0.jar 0353648EAA1C91E0F4020C959ABF756BC866FFD583DF22AE6B6F6E0CBD43EB44
password4j-1.8.4.jar D1406A158533B65BDA7C20CEBF7BF2A9A0626F8931FD1DC3D39243AF5849EDF5
```

Uticaj na vec uradjene strategije:
Dodavanje ovih biblioteka ne menja automatski `BCryptStrategy` i `PBKDF2HashingStrategy`. `BCryptStrategy` ostaje na postojecem jBCrypt kodu zbog kompatibilnosti sa starim hash zapisima. `PBKDF2HashingStrategy` za sada ostaje na JDK `SecretKeyFactory` implementaciji, jer vec ima eksplicitan format zapisa koji cuva algoritam, iteracije, duzinu kljuca, salt i hash. Password4j ostaje dostupna biblioteka za benchmark, poredjenje i moguce kasnije pojednostavljenje strategija ako se pokaze korisnim.

---

## 2026-07-06 - Za Argon2id se koristi Bouncy Castle

Kontekst:
Java standardna biblioteka nema jednostavnu ugrađenu podršku za Argon2id, a kriptografske algoritme ne treba implementirati ručno.

Odluka:
Za implementaciju `Argon2idStrategy` koristi se Bouncy Castle biblioteka, dodata kao JAR u lokalni `lib/` direktorijum.

Razlog:
Bouncy Castle je poznata i proverena Java kriptografska biblioteka. Omogućava implementaciju Argon2id strategije bez ručnog pisanja algoritma.

Posledice:
Potrebno je dodati odgovarajući Bouncy Castle JAR u `lib/`, ažurirati NetBeans/Ant classpath i dokumentovati verziju biblioteke. `Argon2idStrategy`, testovi i benchmark zavise od ove biblioteke.

---

## 2026-07-06 - Algoritmi se čuvaju uz podatke, a ne u globalnoj konfiguraciji

Kontekst:
Sistem treba da podrži više hashing i encryption algoritama. Postoji mogućnost da se algoritam bira globalno, ali to bi moglo da pokvari čitanje starih zapisa kada se izbor promeni.

Odluka:
Hashing algoritam se bira prilikom registracije korisnika i čuva se uz korisnika. Encryption algoritam se bira prilikom kreiranja password entry-ja i čuva se uz taj entry.

Razlog:
Stari zapisi moraju ostati čitljivi čak i kada se kasnije koriste drugi algoritmi za nove korisnike ili nove password entry-je.

Posledice:
Verifikacija master lozinke koristi algoritam sačuvan uz korisnika. Dekripcija password entry-ja koristi algoritam sačuvan uz konkretan entry. Konfiguracioni fajl se ne koristi za globalni izbor algoritama nad korisničkim podacima.

---

## 2026-07-06 - Kod password entry-ja šifruje se samo lozinka

Kontekst:
Postojeći sistem već čuva service, username i description kao čitljive podatke, dok se password čuva šifrovano. Postojala je mogućnost da se šifruju i username/email i description.

Odluka:
U završnoj verziji ovog projekta šifruje se samo password polje unutar password entry-ja. Service, username/email i description ostaju u čitljivom obliku u bazi.

Razlog:
Ova odluka zadržava postojeći funkcionalni model aplikacije, olakšava filtriranje i prikaz zapisa, i smanjuje obim migracije. Kriptografsko poređenje se i dalje može jasno sprovesti nad najosetljivijim poljem: sačuvanom lozinkom.

Posledice:
`EncryptionStrategy` se primenjuje na password vrednost. Bezbednosna analiza mora jasno navesti da metapodaci entry-ja ostaju vidljivi u scenariju kompromitovane baze, dok je sama lozinka kriptografski zaštićena.

---

## 2026-07-06 - Strategy Pattern je centralni obrazac za kriptografske algoritme

Kontekst:
Postojeći sistem trenutno direktno poziva `HashUtils` i `CryptoUtils`, što otežava dodavanje novih algoritama bez menjanja poslovne logike.

Odluka:
Uvesti `HashingStrategy` za zaštitu master lozinke i `EncryptionStrategy` za šifrovanje korisničkih podataka. Uz njih uvesti factory/resolver klase koje biraju strategiju na osnovu naziva algoritma.

Razlog:
Strategy Pattern omogućava proširivost i jasno poređenje algoritama u diplomskom radu.

Posledice:
BCrypt, PBKDF2 i Argon2id postaju odvojene hashing strategije. AES-GCM, AES-CBC-HMAC i ChaCha20-Poly1305 postaju odvojene encryption strategije. `DBBroker`, `Controller` i UI ne treba da sadrže detalje implementacije algoritama.

---

## 2026-07-07 - `HashingStrategy` definiše zajednički ugovor za master lozinku

Kontekst:
Faza 3 uvodi Strategy Pattern za zaštitu master lozinke. Potreban je zajednički interfejs koji će koristiti BCrypt, PBKDF2 i Argon2id implementacije.

Odluka:
Uvesti `Cryptography.Hashing.HashingStrategy` sa metodama:

```java
String hash(String password);
boolean verify(String password, String storedHash);
AlgorithmName getAlgorithmName();
```

Razlog:
`String` se koristi za master lozinku da bi interfejs pratio postojeći stil projekta, jer modeli, forme i trenutna logika već rade sa lozinkama kao stringovima. Bezbednosno ograničenje je svesno prihvaćeno radi jednostavnije integracije u trenutnu aplikaciju. `AlgorithmName` povezuje strategiju sa centralizovanim nazivima algoritama koji se čuvaju u bazi.

Posledice:
BCrypt, PBKDF2 i Argon2id implementacije treba da implementiraju isti interfejs. Postojeći `HashUtils` se još ne menja u ovom koraku; on će biti povezan sa strategijom u narednom task-u.

---

## 2026-07-07 - BCrypt se izdvaja u `BCryptStrategy`

Kontekst:
Postojeća registracija i prijava korisnika koriste `HashUtils`, a `HashUtils` je direktno pozivao jBCrypt.

Odluka:
Uvesti `Cryptography.Hashing.BCryptStrategy` kao implementaciju `HashingStrategy`. `HashUtils` ostaje postojeći statički adapter, ali interno delegira na `BCryptStrategy`.

Razlog:
Ovim se dobija prva konkretna hashing strategija uz postepenu migraciju postojećeg toka aplikacije. Zadržava se isti jBCrypt format hash-a i cost faktor 10, pa postojeći BCrypt hash-evi ostaju proverljivi.

Posledice:
Naredne hashing strategije mogu da se dodaju paralelno. Kasnije će factory/resolver odlučivati koju strategiju koristiti na osnovu algoritma zapisanog uz korisnika.

---

## 2026-07-07 - PBKDF2 hashing strategija cuva parametre u hash zapisu

Kontekst:
Projekat vec koristi PBKDF2 u `CryptoUtils`, ali tamo PBKDF2 sluzi za derivaciju AES kljuca iz master lozinke i korisnickog salta. TASK 3.3 uvodi PBKDF2 kao alternativni algoritam za zastitu master lozinke pri prijavi.

Odluka:
Uvesti `Cryptography.Hashing.PBKDF2HashingStrategy` kao posebnu implementaciju `HashingStrategy`. Format zapisa je:

```text
PBKDF2$PBKDF2WithHmacSHA256$iterations$keyLengthBits$saltBase64$hashBase64
```

Razlog:
Hash zapis mora da nosi sopstvene parametre da bi verifikacija radila i nakon buducih promena broja iteracija ili duzine kljuca. Strategija koristi poseban salt za password hash i ne deli odgovornost sa PBKDF2 derivacijom encryption kljuca.

Posledice:
PBKDF2 za login hash i PBKDF2 za AES key derivation ostaju odvojeni koncepti u kodu i dokumentaciji. `HashingStrategyFactory` ce kasnije moci da izabere ovu strategiju na osnovu `AlgorithmName.PBKDF2`.

---

## 2026-07-07 - Argon2id se implementira preko Bouncy Castle-a

Kontekst:
TASK 3.4 uvodi Argon2id kao trecu hashing strategiju za master lozinku. Argon2 je kompleksan password hashing algoritam i ne treba ga implementirati rucno u okviru diplomskog rada.

Odluka:
Uvesti `Cryptography.Hashing.Argon2idStrategy` kao implementaciju `HashingStrategy`, koristeci Bouncy Castle klase `Argon2BytesGenerator` i `Argon2Parameters`.

Format zapisa je:

```text
ARGON2ID$version$memoryKb$iterations$parallelism$hashLengthBytes$saltBase64$hashBase64
```

Parametri inicijalne implementacije:

- version: `Argon2Parameters.ARGON2_VERSION_13`
- memory: `65536 KB`
- iterations: `3`
- parallelism: `1`
- salt: `16 bytes`
- hash: `32 bytes`

Razlog:
Bouncy Castle daje proverenu implementaciju Argon2id algoritma, dok aplikacija i dalje kontrolise format zapisa i parametre potrebne za kasniju verifikaciju. Cilj projekta je poredjenje algoritama i prikaz rada password manager-a, a ne implementacija kriptografskih primitiva od nule.

Posledice:
Argon2id hash zapis je samodovoljan za verifikaciju i moze se cuvati uz korisnika. Kasniji `HashingStrategyFactory` ce moci da bira ovu strategiju preko `AlgorithmName.ARGON2ID`.

---

## 2026-07-07 - Hashing strategije se biraju preko `HashingStrategyFactory`

Kontekst:
Nakon uvodjenja `BCryptStrategy`, `PBKDF2HashingStrategy` i `Argon2idStrategy`, poslovna logika ne treba da sadrzi `if/else` ili `switch` grananje za izbor konkretnog hashing algoritma.

Odluka:
Uvesti `Cryptography.Factory.HashingStrategyFactory` kao centralno mesto koje mapira `AlgorithmName` ili vrednost procitanu iz baze na konkretnu `HashingStrategy` implementaciju.

Razlog:
Factory izoluje izbor algoritma od registracije, login toka i baze. Kada se kasnije registruje korisnik sa izabranim hashing algoritmom, `DBBroker` ili servisni sloj treba samo da prosledi vrednost algoritma factory klasi.

Posledice:
Dodavanje nove hashing strategije kasnije zahteva promenu factory klase i `AlgorithmName`, a ne rasute izmene kroz UI i poslovnu logiku.

---

## 2026-07-07 - Izbor hashing algoritma cuva se uz korisnika

Kontekst:
TASK 3.6 povezuje hashing strategije sa registracijom i login tokom. Do sada su svi korisnici implicitno koristili BCrypt preko `HashUtils`.

Odluka:
Prosiriti `User` model i tabelu `users` poljem `hashing_algorithm`. Registraciona forma dobija izbor `BCRYPT`, `PBKDF2` ili `ARGON2ID`. `DBBroker` pri registraciji bira strategiju preko `HashingStrategyFactory`, a pri login-u cita algoritam iz baze i koristi odgovarajucu strategiju za proveru master lozinke.

Razlog:
Algoritam mora da bude sacuvan uz korisnika da bi stari i novi korisnici mogli da koegzistiraju cak i kada se za nove naloge izabere drugi hashing algoritam.

Posledice:
`users.password` u SQL dump-u se menja iz `varchar(100)` u `text`, jer PBKDF2 i Argon2id zapisi cuvaju vise parametara i mogu biti duzi od BCrypt hash-a. Postojeci demo korisnici dobijaju `hashing_algorithm = 'BCRYPT'`. Za vec postojecu lokalnu bazu dodat je migration SQL fajl koji dodaje novu kolonu i oznacava postojece korisnike kao `BCRYPT`.

---

## 2026-07-06 - Nazivi algoritama su centralizovani u `AlgorithmName`

Kontekst:
Nazivi algoritama biće čuvani u bazi, prikazivani u UI-ju, korišćeni u factory/resolver klasama i benchmark modulu. Ako se pišu kao ručni stringovi na više mesta, lako može doći do grešaka i neusklađenosti.

Odluka:
Uvesti enum `Cryptography.AlgorithmName` kao centralno mesto za stabilne identifikatore algoritama. Usvojene vrednosti su:

- `BCRYPT`
- `PBKDF2`
- `ARGON2ID`
- `AES_GCM`
- `AES_CBC_HMAC`
- `CHACHA20_POLY1305`

Razlog:
Jedno centralno mesto smanjuje dupliranje i omogućava da baza, UI, strategije, testovi i benchmark koriste iste vrednosti.

Posledice:
Polja u bazi za `hash_algorithm` i `encryption_algorithm` treba da koriste ove vrednosti. Factory klase će kasnije koristiti `AlgorithmName` umesto ručnih stringova. AES-CBC se interno vodi kao `AES_CBC_HMAC`, čime se jasno dokumentuje da se koristi uz zaštitu integriteta.

---

## 2026-07-06 - Kriptografski sloj koristi domenske exception klase

Kontekst:
Factory/resolver klase i kriptografske strategije moraju jasno razlikovati nepoznat algoritam od neuspele kriptografske operacije, kao što su pogrešan ključ, oštećen ciphertext ili neuspešna verifikacija.

Odluka:
Uvesti poseban paket `Cryptography.Exceptions` sa osnovnim `CryptoException`, zatim `UnsupportedAlgorithmException` za nepoznate vrednosti algoritma i `CryptoOperationException` za neuspele kriptografske operacije nakon što je algoritam već izabran.

Razlog:
Domenski izuzeci čine kod čitljivijim i olakšavaju kasnije rukovanje greškama u servisnom sloju i UI-ju. Takođe pomažu testiranju, jer testovi mogu proveriti tačan tip greške.

Posledice:
`AlgorithmName.fromDatabaseValue(...)` baca `UnsupportedAlgorithmException` kada baza ili konfiguracija sadrži nepoznat algoritam. Buduće hashing/encryption strategije treba da koriste `CryptoOperationException` kada operacija ne uspe.

---

## 2026-07-06 - Razdvajaju se hashing, derivacija ključa i encryption

Kontekst:
Postojeći sistem koristi BCrypt za login hash, PBKDF2 za derivaciju AES ključa i AES-GCM za šifrovanje entry lozinke. Ove operacije imaju različite bezbednosne uloge.

Odluka:
Hashing master lozinke, derivacija encryption ključa i encryption/decryption korisničkih podataka treba da budu odvojene komponente.

Razlog:
U diplomskom radu je važno jasno objasniti razliku između autentifikacije korisnika i zaštite sačuvanih kredencijala.

Posledice:
Postojeći `CryptoUtils` treba refaktorisati. PBKDF2 za key derivation ne sme se mešati sa PBKDF2 hashing strategijom za master lozinku.

---

## 2026-07-06 - AES-CBC se koristi samo uz zaštitu integriteta

Kontekst:
AES-CBC sam po sebi obezbeđuje poverljivost, ali ne i integritet/autentičnost šifrovanih podataka.

Odluka:
Ako se implementira AES-CBC strategija, ona mora koristiti HMAC ili ekvivalentan mehanizam autentifikacije poruke. Interni naziv algoritma treba da bude jasan, na primer `AES_CBC_HMAC`.

Razlog:
Bez zaštite integriteta AES-CBC bi bio slabije i potencijalno pogrešno predstavljeno rešenje za password manager.

Posledice:
Benchmark i bezbednosna analiza moraju jasno razlikovati AEAD algoritme od AES-CBC-HMAC kombinacije. Testovi moraju pokriti izmenjen ciphertext, IV i HMAC.

---

## 2026-07-06 - Email zamenjuje username kao korisnički identifikator

Kontekst:
Postojeći sistem koristi username. Za MFA i realističniji password manager potreban je email.

Odluka:
Tokom razvoja zameniti username email adresom u modelu, bazi, login formi i registration formi.

Razlog:
Email je realističniji identifikator korisnika i potreban je za Email OTP MFA.

Posledice:
Potrebna je migracija tabele `users`, validacija email formata i neutralne login poruke koje ne otkrivaju da li korisnik postoji.

---

## 2026-07-06 - Konfiguracioni fajl služi samo za tehničke parametre

Kontekst:
Trenutni `DBConnection` ima hard-coded URL, username i password. Kontekst dokument traži konfiguracioni fajl, ali algoritmi ne treba da budu globalna konfiguracija korisničkih podataka.

Odluka:
Uvesti konfiguraciju za tehničke parametre, pre svega bazu i email/SMTP. Ne koristiti konfiguraciju za globalni izbor hashing ili encryption algoritma nad podacima.

Razlog:
DB kredencijali ne treba da budu u Java kodu. Sa druge strane, algoritmi moraju biti vezani za konkretne zapise zbog kompatibilnosti starih podataka.

Posledice:
Dodaje se `application.example.properties`, a stvarni `application.properties` treba ignorisati. `DBConnection` treba da učitava parametre iz konfiguracije.

---

## 2026-07-06 - Email OTP MFA se uvodi kao dodatni sloj zaštite, ne kao savršeno rešenje

Kontekst:
Diplomski rad predviđa Email OTP MFA nakon provere master lozinke.

Odluka:
Implementirati Email OTP kao dodatni korak prijave kada je MFA uključen za korisnika.

Razlog:
MFA smanjuje rizik od neovlašćene prijave kada je master lozinka kompromitovana, a email je praktičan za lokalnu desktop aplikaciju.

Posledice:
Treba implementirati OTP generisanje, istek, verifikaciju, ograničenje pokušaja ako se uvede, email slanje i UI za unos koda. U radu treba jasno navesti ograničenja Email OTP pristupa.

---

## 2026-07-06 - Email OTP koristi realan SMTP režim

Kontekst:
Za MFA je potreban mehanizam slanja jednokratnog koda korisniku putem emaila. Korisnik je naveo JavaMail/Gmail SMTP primer kao smernicu za implementaciju.

Odluka:
Implementirati realno SMTP slanje OTP koda. SMTP host, port, email nalog, lozinka/app-password i TLS podešavanja učitavaju se iz `application.properties`, a u repozitorijum ide samo `application.example.properties`.

Razlog:
Realno slanje emaila bolje odgovara cilju diplomskog rada i omogućava demonstraciju potpunog MFA toka.

Posledice:
Ne smeju se hardkodovati stvarni email i lozinka u Java kodu. Za generisanje OTP koda treba koristiti `SecureRandom`, a ne `Math.random`. OTP kod ne treba trajno čuvati kao plain text; ako se čuva u bazi, čuva se hash uz vreme isteka i broj pokušaja.

---

## 2026-07-06 - Za završnu verziju pravi se nova čista šema baze

Kontekst:
Postojeći `passwordmanager_db.sql` sadrži staru strukturu i demo podatke zasnovane na username modelu, bez polja za izbor hashing/encryption algoritama i MFA.

Odluka:
Za završnu verziju projekta pravi se nova čista šema baze, umesto migracije postojećih demo podataka.

Razlog:
Nova šema je jednostavnija, čistija i lakša za objašnjenje u diplomskom radu. Izbegava se komplikovano mapiranje starih demo korisnika na email, MFA i nove kriptografske metapodatke.

Posledice:
Potrebno je napraviti SQL skripte za novu šemu i inicijalne demo podatke ako budu potrebni. Stari SQL dump može ostati kao istorijski početni primer, ali završna demonstracija treba da koristi novu šemu.

---

## 2026-07-06 - Originalni DOCX diplomskog rada se ne menja bez posebnog zahteva

Kontekst:
Korisnik je poslao `D:\Fakultet\DIPLOMSKI\Diplomski rad.docx` na analizu i naglasio da fajl ne sme da se menja.

Odluka:
DOCX fajl se koristi samo za čitanje i analizu dok korisnik eksplicitno ne zatraži izmene.

Razlog:
Diplomski rad je važan originalni dokument, a trenutni zadatak je analiza i planiranje implementacije.

Posledice:
Sva planiranja i tehničke beleške za sada idu u markdown fajlove u projektu. Kasnije izmene DOCX-a rade se tek posle posebnog zahteva.

---

## 2026-07-06 - `context` folder se ignoriše u Git-u

Kontekst:
Korisnik želi da `context` folder sadrži radni markdown kontekst i prateće fajlove koji ne ulaze u commit.

Odluka:
Dodato je pravilo `/context/` u `.gitignore`.

Razlog:
Kontekst fajlovi mogu biti lokalni, radni i potencijalno vezani za diplomski materijal koji ne treba commitovati.

Posledice:
Izmene u `context/CODEX_CONTEXT.md` ne ulaze u Git commit osim ako se kasnije ne promeni pravilo ignorisanja.

---

## 2026-07-06 - Generisani build i NetBeans private fajlovi se ne prate u Git-u

Kontekst:
Repozitorijum je pratio `build/classes` artefakte i `nbproject/private` fajlove. Ti fajlovi su lokalni ili generisani i mogu praviti šum u commitovima.

Odluka:
`build/`, `dist/`, `nbproject/private/`, `application.properties`, `benchmark-results/` i `*.class` se ignorišu kroz `.gitignore`. Već praćeni `build/classes` i `nbproject/private` fajlovi skinuti su iz Git indeksa bez brisanja lokalnih fajlova.

Razlog:
Repozitorijum treba da prati izvorni kod, projektne konfiguracije koje su potrebne svima, SQL/migracije, dokumentacione fajlove i relevantne biblioteke, a ne lokalni build output ili privatne IDE postavke.

Posledice:
Build i NetBeans mogu lokalno da regenerišu ove fajlove, ali oni više neće ulaziti u commitove. Git status postaje čistiji i buduće izmene su preglednije.

---

## 2026-07-06 - Build baseline prolazi pre refaktorisanja

Kontekst:
Pre većih izmena bilo je potrebno proveriti da trenutni projekat može da se kompajlira.

Odluka:
Kao baseline build komanda koristi se NetBeans Ant: `C:\Program Files\NetBeans-24\netbeans\extide\ant\bin\ant.bat clean jar`.

Razlog:
`ant` nije dostupan direktno iz PATH-a, ali NetBeans instalacija sadrži Ant koji uspešno gradi projekat.

Posledice:
Trenutni projekat se uspešno kompajlira sa Java 23 i postojećom MySQL connector putanjom. U sledećim fazama svaka veća promena može se proveravati istom build komandom.

---

## 2026-07-06 - Ciljna organizacija paketa ostaje unutar `src/...`

Kontekst:
Postojeći projekat je NetBeans/Ant Java desktop aplikacija. Trenutna struktura paketa je jednostavna: `BCrypt`, `Cryptography`, `Database`, `Model`, `Singletons` i `View`. Projekat treba proširiti Strategy Pattern-om, konfiguracijom, MFA slojem, benchmark modulom i testabilnijom kripto arhitekturom, ali bez Maven migracije i bez prelaska na `src/main/java`.

Odluka:
Nova organizacija paketa uvodi se postepeno unutar postojeće `src/...` strukture. Postojeći paketi se ne premeštaju masovno odjednom. Novi kod se smešta u sledeće ciljane pakete:

```text
src/
    Config/
        AppConfig.java
        ConfigLoader.java

    Cryptography/
        AlgorithmName.java
        Exceptions/
            CryptoException.java
            UnsupportedAlgorithmException.java
            CryptoOperationException.java

        Hashing/
            HashingStrategy.java
            BCryptStrategy.java
            PBKDF2HashingStrategy.java
            Argon2idStrategy.java

        Encryption/
            EncryptionStrategy.java
            AESGCMStrategy.java
            AESCBCStrategy.java
            ChaCha20Poly1305Strategy.java

        KeyDerivation/
            KeyDerivationService.java

        Model/
            EncryptedData.java

        Factory/
            HashingStrategyFactory.java
            EncryptionStrategyFactory.java

    Database/
        DBConnection.java
        DBBroker.java

    Model/
        User.java
        PasswordEntry.java
        EntryTableModel.java

    Service/
        UserService.java
        PasswordEntryService.java
        CryptoService.java

    MFA/
        OtpService.java
        OtpCode.java
        EmailService.java

    Benchmark/
        BenchmarkResult.java
        CsvBenchmarkWriter.java

        Hashing/
            HashingBenchmarkRunner.java

        Encryption/
            EncryptionBenchmarkRunner.java

    Singletons/
        Controller.java

    View/
        LoginForm.java
        RegistrationForm.java
        MainForm.java
        InsertForm.java
```

Razlog:
Ova struktura razdvaja odgovornosti bez nasilnog premeštanja celog projekta. `Cryptography` dobija jasne potpakete za hashing, encryption, key derivation, modele i factory/resolver klase. `Config`, `MFA`, `Service` i `Benchmark` dobijaju sopstvene zone, što će pomoći i implementaciji i pisanju poglavlja o arhitekturi u diplomskom radu.

Posledice:
Refaktorisanje ide malim koracima. Prvo se dodaju novi paketi i klase, zatim se postojeći `HashUtils`, `CryptoUtils`, `DBBroker`, `Controller` i Swing forme povezuju sa novom strukturom. Postojeći `View`, `Database`, `Model` i `Singletons` paketi ostaju na mestu dok ne postoji konkretan razlog za promenu.

---

## Otvorena pitanja

### OQ 1 - Kako uvodimo spoljne JAR biblioteke?

Status: RESOLVED

Pitanje:
Da li u repozitorijum dodajemo lokalni `lib/` direktorijum sa potrebnim JAR fajlovima, ili dependency-ji ostaju lokalno podešeni u NetBeans-u?

Odluka:
Uvodimo lokalni `lib/` direktorijum za potrebne JAR biblioteke.

Uticaj:
Odluka utiče na Argon2id, potencijalno ChaCha20-Poly1305, JUnit i MySQL connector.

### OQ 2 - Koju biblioteku koristiti za Argon2id?

Status: RESOLVED

Pitanje:
Da li je prihvatljivo koristiti Bouncy Castle kao proverenu biblioteku za Argon2id?

Odluka:
Koristiti Bouncy Castle JAR iz lokalnog `lib/` direktorijuma.

Uticaj:
Odluka utiče na `Argon2idStrategy`, testove, benchmark i opis tehnologija u diplomskom.

### OQ 3 - Koja polja password entry-ja se šifruju?

Status: RESOLVED

Pitanje:
Da li šifrovati samo password kao sada, ili i username/email i description?

Odluka:
Šifruje se samo password polje, kao u postojećoj verziji projekta.

Uticaj:
Odluka utiče na bezbednosnu analizu: u database compromise scenariju service, username/email i description ostaju vidljivi, dok je password zaštićen izabranim encryption algoritmom.

### OQ 4 - Kako čuvati OTP kod?

Pitanje:
Da li OTP čuvati privremeno u memoriji ili u bazi kao hash sa istekom i brojem pokušaja?

Preporuka:
Za diplomski je bolje čuvati hash OTP koda u bazi sa vremenom isteka i brojem pokušaja, jer je lakše objasniti i testirati security pravila.

Uticaj:
Odluka utiče na šemu baze, MFA servis i security testove.

### OQ 5 - Kako slati email tokom razvoja?

Status: RESOLVED

Pitanje:
Da li odmah koristiti realan SMTP nalog ili omogućiti development režim u kome se OTP ispisuje u konzoli?

Odluka:
Koristiti realan SMTP režim. SMTP parametri idu u `application.properties`, ne u Java kod.

Uticaj:
Odluka utiče na konfiguracioni fajl, MFA testiranje i demonstraciju aplikacije.

### OQ 6 - Da li čuvamo postojeće demo podatke?

Status: RESOLVED

Pitanje:
Da li postojeće korisnike i password entry-je iz `passwordmanager_db.sql` treba migrirati ili je prihvatljivo napraviti novu čistu šemu?

Odluka:
Pravi se nova čista šema baze za završnu verziju projekta.

Uticaj:
Odluka utiče na SQL migracije, backward compatibility kod i demonstracione podatke.

### OQ 7 - Da li generisane fajlove uklanjamo iz Git-a?

Status: RESOLVED

Pitanje:
Da li iz repozitorijuma treba ukloniti već praćene `build/classes` artefakte i NetBeans private fajlove?

Odluka:
Da. Skinuti su iz Git indeksa bez brisanja lokalnih fajlova i dodati su u `.gitignore`.

Uticaj:
Odluka utiče na čistoću Git statusa i buduće commitove.
