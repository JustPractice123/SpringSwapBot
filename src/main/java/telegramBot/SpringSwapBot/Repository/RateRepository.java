package telegramBot.SpringSwapBot.Repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import telegramBot.SpringSwapBot.Model.Rate;

import java.util.Optional;

@Repository
@Transactional
public interface RateRepository extends JpaRepository<Rate, Long> {
    Rate findByName(String name);
}
