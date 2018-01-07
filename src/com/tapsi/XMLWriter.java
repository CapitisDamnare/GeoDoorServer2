package com.tapsi;

import java.io.StringWriter;
import java.util.List;
import java.util.ListIterator;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartDocument;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

public class XMLWriter {
    private static String xml;

    public static String getXml() {
        return xml;
    }

    public static void saveConfig() throws Exception {
        StringWriter stringOut = new StringWriter();

        // create an XMLOutputFactory
        XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
        // create XMLEventWriter
        XMLEventWriter eventWriter = outputFactory
                .createXMLEventWriter(stringOut);
        // create an EventFactory
        XMLEventFactory eventFactory = XMLEventFactory.newInstance();
        XMLEvent end = eventFactory.createDTD("\n");
        // create and write Start Tag
        StartDocument startDocument = eventFactory.createStartDocument();
        eventWriter.add(startDocument);

        // create config open tag
        StartElement configStartElement = eventFactory.createStartElement("",
                "", "data");
        eventWriter.add(end);
        eventWriter.add(configStartElement);
        eventWriter.add(end);


        try {
            createNodesFromClients(eventWriter, eventFactory);
        } catch (XMLStreamException e) {
            LogHandler.handleError(e);
        }

        eventWriter.add(eventFactory.createEndElement("", "", "data"));
        eventWriter.add(end);
        eventWriter.add(eventFactory.createEndDocument());

        xml = stringOut.toString();
        stringOut.close();
        eventWriter.close();
    }

    private static void createNodesFromClients(XMLEventWriter eventWriter, XMLEventFactory eventFactory) throws XMLStreamException {
        XMLEvent end = eventFactory.createDTD("\n");
        XMLEvent tab = eventFactory.createDTD("\t");

        List<Client> clients = VisuSocketObject.getClients();
        ListIterator<Client> iterator = clients.listIterator();
        while (iterator.hasNext()) {
            Client client = iterator.next();

            StartElement clientStartElement = eventFactory.createStartElement("",
                    "", "client");
            eventWriter.add(tab);
            eventWriter.add(clientStartElement);
            eventWriter.add(end);
            createNode(eventWriter, "id", Integer.toString(client.getId()));
            createNode(eventWriter, "name", client.getName());
            createNode(eventWriter, "phoneID", client.getPhoneID());
            createNode(eventWriter, "threadID", client.getThreadID());
            createNode(eventWriter, "allowed", Integer.toString(client.getAllowed()));
            createNode(eventWriter, "lastConnection", client.getLastConnection());
            eventWriter.add(tab);
            eventWriter.add(eventFactory.createEndElement("", "", "client"));
            eventWriter.add(end);
        }
    }

    private static void createNode(XMLEventWriter eventWriter, String name,
                            String value) throws XMLStreamException {

        if (value == null)
            return;
        XMLEventFactory eventFactory = XMLEventFactory.newInstance();
        XMLEvent end = eventFactory.createDTD("\n");
        XMLEvent tab = eventFactory.createDTD("\t");
        // create Start node
        StartElement sElement = eventFactory.createStartElement("", "", name);
        eventWriter.add(tab);
        eventWriter.add(tab);
        eventWriter.add(sElement);
        // create Content
        Characters characters = eventFactory.createCharacters(value);
        eventWriter.add(characters);
        // create End node
        EndElement eElement = eventFactory.createEndElement("", "", name);
        eventWriter.add(eElement);
        eventWriter.add(end);
    }
}