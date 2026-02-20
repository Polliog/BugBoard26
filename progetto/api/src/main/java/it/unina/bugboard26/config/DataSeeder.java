package it.unina.bugboard26.config;

import it.unina.bugboard26.model.User;
import it.unina.bugboard26.model.enums.GlobalRole;
import it.unina.bugboard26.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Inizializza il database con l'account admin di default.
 * RF01 - Account admin: admin@bugboard.com / admin123.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            User admin = new User(
                    "admin@bugboard.com",
                    passwordEncoder.encode("admin123"),
                    "Admin",
                    GlobalRole.ADMIN
            );
            userRepository.save(admin);
            log.info("Account admin di default creato: admin@bugboard.com");
        }
    }
}
