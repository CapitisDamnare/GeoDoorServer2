package com.tapsi;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class XMLReader {
    static final String CLIENT = "client";
    static final String ID = "id";
    static final String NAME = "name";
    static final String PHONEID = "phoneID";
    static final String THREADID = "threadID";
    static final String ALLOWED = "allowed";
    static final String LASTCONNECTION = "lastConnection";

    @SuppressWarnings({"unchecked", "null"})
    public static List<Client> readConfig(String configFile) {
        List<Client> clients = new ArrayList<Client>();
        try {
            // First, create a new XMLInputFactory
            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            // Setup a new eventReader
            InputStream in = null;
            try {
                in = new ByteArrayInputStream(configFile.getBytes("UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            XMLEventReader eventReader = inputFactory.createXMLEventReader(in);
            // read the XML document
            Client client = null;

            while (eventReader.hasNext()) {
                XMLEvent event = eventReader.nextEvent();

                if (event.isStartElement()) {
                    StartElement startElement = event.asStartElement();
                    // If we have an item element, we create a new item
                    if (startElement.getName().getLocalPart().equals(CLIENT)) {
                        client = new Client();
                        // We read the attributes from this tag and add the date
                        // attribute to our object
                        Iterator<Attribute> attributes = startElement
                                .getAttributes();
                        while (attributes.hasNext()) {
                            Attribute attribute = attributes.next();
                            if (attribute.getName().toString().equals(ID)) {
                                client.setId(Integer.valueOf(attribute.getValue()));
                            }

                        }
                    }

                    if (event.isStartElement()) {
                        if (event.asStartElement().getName().getLocalPart()
                                .equals(ID)) {
                            event = eventReader.nextEvent();
                            client.setId(Integer.valueOf(event.asCharacters().getData()));
                            continue;
                        }
                    }

                    if (event.isStartElement()) {
                        if (event.asStartElement().getName().getLocalPart()
                                .equals(NAME)) {
                            event = eventReader.nextEvent();
                            client.setName(event.asCharacters().getData());
                            continue;
                        }
                    }
                    if (event.asStartElement().getName().getLocalPart()
                            .equals(PHONEID)) {
                        event = eventReader.nextEvent();
                        client.setPhoneID(event.asCharacters().getData());
                        continue;
                    }

                    if (event.asStartElement().getName().getLocalPart()
                            .equals(THREADID)) {
                        event = eventReader.nextEvent();
                        client.setThreadID(event.asCharacters().getData());
                        continue;
                    }

                    if (event.asStartElement().getName().getLocalPart()
                            .equals(ALLOWED)) {
                        event = eventReader.nextEvent();
                        client.setAllowed(Integer.valueOf(event.asCharacters().getData()));
                        continue;
                    }

                    if (event.asStartElement().getName().getLocalPart()
                            .equals(LASTCONNECTION)) {
                        event = eventReader.nextEvent();
                        client.setLastConnection(event.asCharacters().getData());
                        continue;
                    }
                }
                // If we reach the end of an item element, we add it to the list
                if (event.isEndElement()) {
                    EndElement endElement = event.asEndElement();
                    if (endElement.getName().getLocalPart().equals(ID)) {
                        clients.add(client);
                    }
                }

            }
        } catch (XMLStreamException e) {
            LogHandler.handleError(e);
        }
        return clients;
    }
}
