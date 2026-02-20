package it.unina.bugboard26.config;

import it.unina.bugboard26.model.Comment;
import it.unina.bugboard26.model.Issue;
import it.unina.bugboard26.model.User;
import it.unina.bugboard26.model.enums.GlobalRole;
import it.unina.bugboard26.model.enums.IssuePriority;
import it.unina.bugboard26.model.enums.IssueStatus;
import it.unina.bugboard26.model.enums.IssueType;
import it.unina.bugboard26.repository.CommentRepository;
import it.unina.bugboard26.repository.IssueRepository;
import it.unina.bugboard26.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository userRepository;
    private final IssueRepository issueRepository;
    private final CommentRepository commentRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository,
                      IssueRepository issueRepository,
                      CommentRepository commentRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.issueRepository = issueRepository;
        this.commentRepository = commentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) return;

        // --- Utenti ---
        User admin = userRepository.save(new User(
                "admin@bugboard.com",
                passwordEncoder.encode("admin123"),
                "Admin",
                GlobalRole.ADMIN
        ));
        log.info("Account admin creato: admin@bugboard.com / admin123");

        User mario = userRepository.save(new User(
                "mario.rossi@bugboard.com",
                passwordEncoder.encode("password"),
                "Mario Rossi",
                GlobalRole.USER
        ));

        User lucia = userRepository.save(new User(
                "lucia.bianchi@bugboard.com",
                passwordEncoder.encode("password"),
                "Lucia Bianchi",
                GlobalRole.USER
        ));

        User cliente = userRepository.save(new User(
                "cliente@esterno.com",
                passwordEncoder.encode("password"),
                "Cliente Esterno",
                GlobalRole.EXTERNAL
        ));
        log.info("Utenti seed creati: mario.rossi, lucia.bianchi, cliente");

        // --- Issue ---
        Issue bug1 = new Issue();
        bug1.setTitle("Login fallisce con email maiuscola");
        bug1.setType(IssueType.BUG);
        bug1.setDescription("Quando un utente inserisce l'email con lettere maiuscole, il login restituisce 401 anche se le credenziali sono corrette. Il confronto email dovrebbe essere case-insensitive.");
        bug1.setPriority(IssuePriority.CRITICA);
        bug1.setStatus(IssueStatus.IN_PROGRESS);
        bug1.setCreatedBy(mario);
        bug1.setAssignedTo(lucia);
        bug1 = issueRepository.save(bug1);

        Issue feature1 = new Issue();
        feature1.setTitle("Aggiungere filtro per data di creazione");
        feature1.setType(IssueType.FEATURE);
        feature1.setDescription("Nella lista issue servirebbe un filtro che permetta di selezionare un intervallo di date per la data di creazione. Utile per i report settimanali.");
        feature1.setPriority(IssuePriority.MEDIA);
        feature1.setStatus(IssueStatus.TODO);
        feature1.setCreatedBy(lucia);
        feature1.setAssignedTo(mario);
        feature1 = issueRepository.save(feature1);

        Issue doc1 = new Issue();
        doc1.setTitle("Documentare le API di export CSV/PDF");
        doc1.setType(IssueType.DOCUMENTATION);
        doc1.setDescription("Le API di export non hanno documentazione. Serve una pagina che descriva i parametri accettati, i filtri applicabili e il formato di output per CSV e PDF.");
        doc1.setPriority(IssuePriority.BASSA);
        doc1.setStatus(IssueStatus.TODO);
        doc1.setCreatedBy(admin);
        doc1.setAssignedTo(mario);
        doc1 = issueRepository.save(doc1);

        Issue bug2 = new Issue();
        bug2.setTitle("Notifica duplicata al cambio stato");
        bug2.setType(IssueType.BUG);
        bug2.setDescription("Quando si cambia lo stato di una issue da TODO a IN_PROGRESS, il creatore riceve due notifiche identiche invece di una sola. Probabilmente il service viene invocato due volte.");
        bug2.setPriority(IssuePriority.ALTA);
        bug2.setStatus(IssueStatus.TODO);
        bug2.setCreatedBy(lucia);
        bug2 = issueRepository.save(bug2);

        Issue question1 = new Issue();
        question1.setTitle("Come gestire le issue archiviate nel report?");
        question1.setType(IssueType.QUESTION);
        question1.setDescription("Nel report PDF, le issue archiviate devono essere incluse di default oppure escluse? Attualmente il parametro includeArchived e' opzionale ma non e' chiaro il comportamento di default.");
        question1.setPriority(IssuePriority.BASSA);
        question1.setStatus(IssueStatus.RISOLTA);
        question1.setCreatedBy(mario);
        question1.setAssignedTo(admin);
        question1 = issueRepository.save(question1);

        Issue feature2 = new Issue();
        feature2.setTitle("Dashboard riepilogativa per admin");
        feature2.setType(IssueType.FEATURE);
        feature2.setDescription("L'admin dovrebbe avere una dashboard con contatori: issue aperte, in progress, risolte, chiuse. Anche un grafico a torta per la distribuzione per tipo e priorita.");
        feature2.setPriority(IssuePriority.ALTA);
        feature2.setStatus(IssueStatus.TODO);
        feature2.setCreatedBy(admin);
        feature2.setAssignedTo(lucia);
        feature2 = issueRepository.save(feature2);

        Issue bug3 = new Issue();
        bug3.setTitle("Errore 500 nell'export PDF con issue senza priorita");
        bug3.setType(IssueType.BUG);
        bug3.setDescription("Se una issue non ha la priorita impostata (campo opzionale), l'export PDF va in NullPointerException nella riga della tabella. Serve un controllo null nella generazione del PDF.");
        bug3.setPriority(IssuePriority.CRITICA);
        bug3.setStatus(IssueStatus.RISOLTA);
        bug3.setCreatedBy(mario);
        bug3.setAssignedTo(lucia);
        bug3.setArchived(true);
        bug3 = issueRepository.save(bug3);

        log.info("Issue seed create: {}", issueRepository.count());

        // --- Commenti ---
        commentRepository.save(new Comment(bug1, lucia, "Confermato: il bug si verifica con 'Admin@BugBoard.com'. Sto lavorando al fix con toLowerCase()."));
        commentRepository.save(new Comment(bug1, mario, "Grazie Lucia, se riesci a fixare entro venerdi sarebbe ottimo."));
        commentRepository.save(new Comment(feature1, admin, "Feature approvata, la inseriamo nello sprint prossimo."));
        commentRepository.save(new Comment(question1, admin, "Le issue archiviate vanno escluse di default. Il parametro includeArchived=true le include esplicitamente."));
        commentRepository.save(new Comment(bug2, mario, "Potrebbe essere legato al doppio evento nel frontend. Verifico."));
        commentRepository.save(new Comment(feature2, lucia, "Ho iniziato il mockup della dashboard, lo condivido domani."));

        log.info("Commenti seed creati: {}", commentRepository.count());
        log.info("Seed completato.");
    }
}
