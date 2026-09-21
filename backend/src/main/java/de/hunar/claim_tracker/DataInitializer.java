package de.hunar.claim_tracker;
import de.hunar.claim_tracker.entity.Claim;
import de.hunar.claim_tracker.service.ClaimService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

    @Component
    @Profile("dev")
    @RequiredArgsConstructor
    public class DataInitializer implements CommandLineRunner {

        private final ClaimService claimService;

        @Override
        public void run(String... args) {
            Claim c1 = new Claim();
            c1.setCustomerNumber("K-2026-4711");
            c1.setClaimType("KFZ");
            c1.setDescription("Auffahrunfall A2");
            c1.setAmount(new BigDecimal("3200.50"));
            claimService.createClaim(c1);

            Claim c2 = new Claim();
            c2.setCustomerNumber("K-2026-4712");
            c2.setClaimType("Haftpflicht");
            c2.setDescription("Wasserschaden Nachbar");
            c2.setAmount(new BigDecimal("850.00"));
            claimService.createClaim(c2);

            Claim c3 = new Claim();
            c3.setCustomerNumber("K-2026-4713");
            c3.setClaimType("KFZ");
            c3.setDescription("Parkschaden Supermarkt");
            c3.setAmount(new BigDecimal("1200.00"));
            claimService.createClaim(c3);

            System.out.println("=== 3 Test-Claims wurden erstellt ===");
        }
    }

