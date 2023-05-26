package telegramBot.SpringSwapBot.Repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import telegramBot.SpringSwapBot.Model.HistoryOpiration;

import java.util.List;

@Repository
@Transactional
public interface HistoryRepository extends JpaRepository<HistoryOpiration,Long> {
    List<HistoryOpiration> findAllByChatId(Long id);
}
