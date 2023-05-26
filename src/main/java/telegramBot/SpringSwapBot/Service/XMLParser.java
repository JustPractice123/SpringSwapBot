package telegramBot.SpringSwapBot.Service;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import telegramBot.SpringSwapBot.Model.Rate;
import telegramBot.SpringSwapBot.Repository.RateRepository;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
@Service
@Getter
@Setter
public class XMLParser {
    @Autowired
    public RateRepository rateRepository;
    public static InputStream getXMLfromURL(String url) throws Exception{
        URL apiURL=new URL(url);
        HttpURLConnection connection=(HttpURLConnection)apiURL.openConnection();
        connection.setRequestMethod("GET");
        connection.connect();
        int responseCode= connection.getResponseCode();
        if (responseCode==HttpURLConnection.HTTP_OK){
            return connection.getInputStream();
        }else {
            throw new Exception("Error retrieving XML data. Response code: " + responseCode);
        }
    }
    public static Document parseXML(InputStream inputStream) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document =builder.parse(inputStream);
        return document;
    }
    public void updateCurrency(){
        try {
            String name="";
            Integer nominal=0;
            String url="http://www.cbr.ru/scripts/XML_daily.asp";
            InputStream inputStream= XMLParser.getXMLfromURL(url);
            Document doc=XMLParser.parseXML(inputStream);
            Element element=doc.getDocumentElement();
            NodeList nodeList=element.getChildNodes();
            for (int i=0;i< nodeList.getLength();i++){
                if (nodeList.item(i) instanceof Element){
                    if (nodeList.item(i).hasChildNodes()){
                        NodeList firstChild=nodeList.item(i).getChildNodes();
                        for (int j=0;j<firstChild.getLength();j++){
                            if (firstChild.item(j) instanceof Element){
                                if (((Element) firstChild.item(j)).getTagName().equals("Nominal")){
                                    nominal=Integer.parseInt(firstChild.item(j).getTextContent());
                                }if (((Element) firstChild.item(j)).getTagName().equals("Name")){
                                    name=firstChild.item(j).getTextContent();
                                    if (name.toCharArray().length>30){
                                        break;
                                    }
                                }if (((Element) firstChild.item(j)).getTagName().equals("Value")){
                                    String value=firstChild.item(j).getTextContent().replace(",",".");
                                    Rate rate=new Rate();
                                    rate.setNominal(nominal);
                                    rate.setName(name);
                                    rate.setValue(value);
                                    rateRepository.save(rate);
                                }
                            }
                        }
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
