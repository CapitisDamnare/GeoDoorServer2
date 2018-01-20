package tapsi.com.visuserver;

import javafx.util.Pair;
import tapsi.com.data.Client;
import tapsi.com.data.XMLWriter;

import java.util.List;
import java.util.ListIterator;

public class VisuSocketObject {

    private static List<Client> clients = null;
    private String message = null;


    public VisuSocketObject(List<Client> clients) {
        this.clients = clients;
        message = null;
    }

    public VisuSocketObject(String message) {
        this.message = message;
        clients = null;
    }

    public VisuSocketObject(List<Client> clients, String message) {
        this.clients = clients;
        this.message = message;
    }

    public VisuSocketObject(String message, List<List<String>> listClients) {
        this.message = message;
       ListIterator iterator = listClients.listIterator();
       while (iterator.hasNext()) {
           List<String> list = (List<String>) iterator.next();
           this.clients.add(new Client(list));
       }
    }

    public static List<Client> getClients() {
        return clients;
    }

    public void setClients(List<Client> clients) {
        this.clients = clients;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void addClient(Client client) {
        clients.add(client);
    }

    public Pair<String,String> getContainer () {
        return new Pair<>(this.message, XMLWriter.getXml());
    }

    public void printAll() {
        if (this.clients != null){
            ListIterator<Client> iterator = this.clients.listIterator();
            while (iterator.hasNext()) {
                iterator.next().printData();
            }
        }
    }
}
