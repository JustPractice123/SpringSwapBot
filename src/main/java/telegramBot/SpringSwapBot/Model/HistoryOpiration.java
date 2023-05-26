package telegramBot.SpringSwapBot.Model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(schema = "test",name = "history")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class HistoryOpiration extends BaseEntity{
    String userName;
    Long chatId;
    String ConvertFrom;
    String ConvertTo;
    Double sum;
    Integer position;
}
