package telegramBot.SpringSwapBot.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import telegramBot.SpringSwapBot.Config.BotConfig;
import telegramBot.SpringSwapBot.Model.HistoryOpiration;
import telegramBot.SpringSwapBot.Model.Rate;
import telegramBot.SpringSwapBot.Repository.HistoryRepository;
import telegramBot.SpringSwapBot.Repository.RateRepository;

import java.util.ArrayList;
import java.util.List;

@Component
public class TelegramBot extends TelegramLongPollingBot {
    @Autowired
    private RateRepository rateRepository;
    @Autowired
    private HistoryRepository historyRepository;
    final BotConfig config;
    public TelegramBot(BotConfig config){
        this.config=config;
        List<BotCommand> listOfCommands=new ArrayList<>();
        listOfCommands.add(new BotCommand("/start", "get a welcome message"));
        listOfCommands.add(new BotCommand("/new", "start to new operation"));
        listOfCommands.add(new BotCommand("/help","info how to use this bot"));
        try {
            this.execute(new SetMyCommands(listOfCommands,new BotCommandScopeDefault(), null));
        }catch (TelegramApiException e){
            e.printStackTrace();
        }
    }
    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()){
            String messageText=update.getMessage().getText();
            Long chatId=update.getMessage().getChatId();
            switch (messageText){
                case "/start":
                    startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
                    break;
                case "/new":
                    newOperation(chatId, 0,5);
                    break;
                default:
                    List<HistoryOpiration> historyOpirations=historyRepository.findAllByChatId(chatId);
                    HistoryOpiration historyOpiration=historyOpirations.get(historyOpirations.size()-1);
                    if (historyOpiration.getConvertFrom()!=null &&historyOpiration.getConvertTo()!=null){
                        String nameFrom=historyOpiration.getConvertFrom().substring(5);
                        String nameTo=historyOpiration.getConvertTo().substring(3);
                        Rate from=rateRepository.findByName(nameFrom);
                        Rate to=rateRepository.findByName(nameTo);
                        Double resultFrom=Double.parseDouble(from.getValue())/from.getNominal();
                        Double To=Double.parseDouble(to.getValue())/to.getNominal();
                        Double textSum=Double.parseDouble(update.getMessage().getText().replaceAll(" ",""));
                        Double answer=(resultFrom/To)*textSum;
                        historyOpiration.setSum(answer);
                        historyRepository.save(historyOpiration);
                        sendMessage(chatId,"Итого: "+historyOpiration.getSum()+" "+to.getName()+
                                ". Для того чтобы начать новую операцию введите /start, затем /new для создания новой операции");
                    }
            }
        }else if (update.hasCallbackQuery()){
            String callBackData=update.getCallbackQuery().getData();
            Long chatId=update.getCallbackQuery().getMessage().getChatId();
            List<HistoryOpiration> historyOpirations=historyRepository.findAllByChatId(chatId);
            HistoryOpiration historyOpiration=historyOpirations.get(historyOpirations.size()-1);
            if (callBackData.equals("More") && historyOpiration.getConvertFrom()==null){
                Integer pos=historyOpiration.getPosition();
                if (pos<40){
                    pressButtenMoreFrom(chatId,pos,pos+5);
                    historyOpiration.setPosition(pos+5);
                    historyRepository.save(historyOpiration);
                }else if (pos==40){
                    pressButtenMoreFrom(chatId,pos,pos+1);
                    historyOpiration.setPosition(pos+1);
                    historyRepository.save(historyOpiration);
                }else if (pos==41){
                    pressButtenMoreFrom(chatId,0,5);
                    historyOpiration.setPosition(5);
                    historyRepository.save(historyOpiration);
                }
            }if (callBackData.equals("More") && historyOpiration.getConvertFrom()!=null){
                Integer pos=historyOpiration.getPosition();
                if (pos<40){
                    choseRate(chatId,pos,pos+5);
                    historyOpiration.setPosition(pos+5);
                    historyRepository.save(historyOpiration);
                }else if (pos==40){
                    choseRate(chatId,pos,pos+1);
                    historyOpiration.setPosition(pos+1);
                    historyRepository.save(historyOpiration);
                }else if (pos==41){
                    choseRate(chatId,0,5);
                    historyOpiration.setPosition(5);
                    historyRepository.save(historyOpiration);
                }
            }else if (callBackData.startsWith("From_")){
                String convertFrom=callBackData;
                historyOpiration.setConvertFrom(convertFrom);
                historyOpiration.setPosition(5);
                historyRepository.save(historyOpiration);
                choseRate(chatId,0,historyOpiration.getPosition());
            }else if (callBackData.startsWith("To_")){
                String convertTo=callBackData;
                historyOpiration.setConvertTo(convertTo);
                historyRepository.save(historyOpiration);
                SendMessage sendMessage=new SendMessage();
                sendMessage.setChatId(chatId);
                sendMessage.setText("Введите сумму.(В ответ не должн содержать специальные символы!)");
                try{
                    execute(sendMessage);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }
    private void startCommandReceived(Long chatId, String name){
        HistoryOpiration historyOpiration=new HistoryOpiration();
        historyOpiration.setChatId(chatId);
        historyOpiration.setPosition(5);
        historyOpiration.setUserName(name);
        historyRepository.save(historyOpiration);
        String answer="Здравствуйте, "+name+" введите /new для выполнения операции.";
        sendMessage(chatId, answer);
    }
    private void newOperation(Long id, Integer start, Integer count){
        SendMessage sendMessage=new SendMessage();
        sendMessage.setChatId(id);
        sendMessage.setText("Выбирите валюту с которой хотите произвести перевод:");
        InlineKeyboardMarkup markup=new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline=new ArrayList<>();
        List<InlineKeyboardButton> rowInline=new ArrayList<>();
        List<Rate> rates=rateRepository.findAll();
        for (int i=start;i<count;i++){
            List<InlineKeyboardButton> rowInlineRate=new ArrayList<>();
            var button=new InlineKeyboardButton();
            button.setText(rates.get(i).getName());
            button.setCallbackData("From_"+rates.get(i).getName());
            rowInlineRate.add(button);
            rowsInline.add(rowInlineRate);
        }
        var buttonMore=new InlineKeyboardButton();
        buttonMore.setText("More");
        buttonMore.setCallbackData("More");
        rowInline.add(buttonMore);
        rowsInline.add(rowInline);
        markup.setKeyboard(rowsInline);
        sendMessage.setReplyMarkup(markup);
        try {
            execute(sendMessage);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    private void pressButtenMoreFrom(Long id, Integer start, Integer count){
        SendMessage sendMessage=new SendMessage();
        sendMessage.setChatId(id);
        sendMessage.setText("Выбирите валюту с которой хотите произвести перевод:");
        InlineKeyboardMarkup markup=new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline=new ArrayList<>();
        List<InlineKeyboardButton> rowInline=new ArrayList<>();
        List<Rate> rates=rateRepository.findAll();
        for (int i=start;i<count;i++){
            List<InlineKeyboardButton> rowInlineRate=new ArrayList<>();
            var button=new InlineKeyboardButton();
            button.setText(rates.get(i).getName());
            button.setCallbackData("From_"+rates.get(i).getName());
            rowInlineRate.add(button);
            rowsInline.add(rowInlineRate);
        }
        var buttonMore=new InlineKeyboardButton();
        buttonMore.setText("More");
        buttonMore.setCallbackData("More");
        rowInline.add(buttonMore);
        rowsInline.add(rowInline);
        markup.setKeyboard(rowsInline);
        sendMessage.setReplyMarkup(markup);
        try {
            execute(sendMessage);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    private void choseRate(Long id, Integer start, Integer count){
        SendMessage sendMessage=new SendMessage();
        sendMessage.setChatId(id);
        sendMessage.setText("Выбирите валюту в которую вы хотите произвести перевод:");
        InlineKeyboardMarkup markup=new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline=new ArrayList<>();
        List<InlineKeyboardButton> rowInline=new ArrayList<>();
        List<Rate> rates=rateRepository.findAll();
        for (int i=start;i<count;i++){
            List<InlineKeyboardButton> rowInlineRate=new ArrayList<>();
            var button=new InlineKeyboardButton();
            button.setText(rates.get(i).getName());
            button.setCallbackData("To_"+rates.get(i).getName());
            rowInlineRate.add(button);
            rowsInline.add(rowInlineRate);
        }
        var buttonMore=new InlineKeyboardButton();
        buttonMore.setText("More");
        buttonMore.setCallbackData("More");
        rowInline.add(buttonMore);
        rowsInline.add(rowInline);
        markup.setKeyboard(rowsInline);
        sendMessage.setReplyMarkup(markup);
        try {
            execute(sendMessage);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    private void sendMessage(Long chatId, String textToSend){
        SendMessage message=new SendMessage();
        message.setChatId(chatId);
        message.setText(textToSend);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
    @Override
    public String getBotUsername() {
        return config.getBotName();
    }
    @Override
    public String getBotToken() {
        return config.getToken();
    }
}
