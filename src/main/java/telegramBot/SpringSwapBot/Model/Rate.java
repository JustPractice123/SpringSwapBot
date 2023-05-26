package telegramBot.SpringSwapBot.Model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import scala.Int;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(schema = "test",name = "rate")
public class Rate extends BaseEntity{
    private String name;
    private String value;
    private Integer nominal;
}
