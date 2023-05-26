package telegramBot.SpringSwapBot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import telegramBot.SpringSwapBot.Service.XMLParser;

@SpringBootApplication
public class SpringSwapBotApplication implements ApplicationListener<ApplicationReadyEvent> {
	private final XMLParser xmlParser;

	public SpringSwapBotApplication(XMLParser xmlParser) {
		this.xmlParser = xmlParser;
	}

	public static void main(String[] args) {
		SpringApplication.run(SpringSwapBotApplication.class, args);
	}
	@Override
	public void onApplicationEvent(ApplicationReadyEvent event) {
		xmlParser.updateCurrency();
	}
}
