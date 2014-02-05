/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.vse.fis.keg.entityclassifier.exporter;

import cz.vse.fis.keg.entityclassifier.core.vao.Confidence;
import cz.vse.fis.keg.entityclassifier.core.vao.Entity;
import cz.vse.fis.keg.entityclassifier.core.vao.Type;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author Milan
 */
public class XMLExporter {
    
    private static XMLExporter instance = null;
    
    public static XMLExporter getInstance(){
        if(instance == null){
            instance = new XMLExporter();
        }
        return instance;
    }
    
    public String toXML(List<Entity> entities) {
        String xmlResult = "";
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.newDocument();
            
            // create root element "<entities>"
            Element entitiesEl = doc.createElement("entities");
            doc.appendChild(entitiesEl);
            
            for(Entity e : entities){
                
                // create element <entity>
                Element entityEl = doc.createElement("entity");
                entitiesEl.appendChild(entityEl);

                // create element <entityType>
                Element entityTypeEl = doc.createElement("entityType");
                entityTypeEl.appendChild(doc.createTextNode(e.getEntityType()));
                entityEl.appendChild(entityTypeEl);

                // create element <underlyingString>
                Element underlyingStringEl = doc.createElement("underlyingString");
                underlyingStringEl.appendChild(doc.createTextNode(e.getUnderlyingString()));
                entityEl.appendChild(underlyingStringEl);

                // create element <startOffset>
                Element startOffsetEl = doc.createElement("startOffset");
                startOffsetEl.appendChild(doc.createTextNode(e.getStartOffset().toString()));
                entityEl.appendChild(startOffsetEl);

                // create element <endOffset>
                Element endOffsetEl = doc.createElement("endOffset");
                endOffsetEl.appendChild(doc.createTextNode(e.getEndOffset().toString()));
                entityEl.appendChild(endOffsetEl);

                // create element <types>
                Element typesEl = doc.createElement("types");
                ArrayList<Type> types = e.getTypes();
                
                
                if(types != null) {
                    
                    System.out.println(e.getTypes().size());
                    
                    for(Type t : types) {

                        // create element <type>
                        Element typeEl = doc.createElement("type");

                        // create element <typeLabel>
                        Element typeLabelEl = doc.createElement("typeLabel");
                        String tLabel = t.getTypeLabel();
                        System.out.println("type label" + t.getTypeLabel());
                        if(tLabel != null){
                            typeLabelEl.appendChild(doc.createTextNode(t.getTypeLabel()));
                        }
                        typeEl.appendChild(typeLabelEl);

                        // create element <typeURI>
                        Element typeURIEl = doc.createElement("typeURI");
                        System.out.println("type URI" + t.getTypeURI());
                        String tURI = t.getTypeURI();
                        if(tURI != null){
                            typeURIEl.appendChild(doc.createTextNode(t.getTypeURI()));
                            typeEl.appendChild(typeURIEl);
                        }

                        // create element <entityLabel>
                        Element entityLabelEl = doc.createElement("entityLabel");
                        System.out.println("entity label" + t.getEntityLabel());
                        entityLabelEl.appendChild(doc.createTextNode(t.getEntityLabel()));
                        typeEl.appendChild(entityLabelEl);

                        // create element <entityURI>            
                        Element entityURIEl = doc.createElement("entityURI");
                        System.out.println("entity URI" + t.getEntityURI());
                        entityURIEl.appendChild(doc.createTextNode(t.getEntityURI()));
                        typeEl.appendChild(entityURIEl);

                        // create element <confidence>
                        Confidence conf = t.getConfidence();
                        if(conf != null) {
                            Element confidenceEl = doc.createElement("confidence");
                            System.out.println("conf val" + conf.getValue());
                            confidenceEl.appendChild(doc.createTextNode(conf.getValue()));
                            System.out.println("conf type" + conf.getType());
                            confidenceEl.setAttribute("type", conf.getType());
                            System.out.println("conf bounds" + conf.getBounds());
                            confidenceEl.setAttribute("bounds", conf.getBounds());
                            typeEl.appendChild(confidenceEl);
                        }

                        // create element <provenance>
                        Element provenanceEl = doc.createElement("provenance");
                        System.out.println(t.getProvenance());
                        provenanceEl.appendChild(doc.createTextNode(t.getProvenance()));
                        typeEl.appendChild(provenanceEl);
                        
                        typesEl.appendChild(typeEl);
                    }
                }
                entityEl.appendChild(typesEl);

            }

            
            StringWriter sw = new StringWriter();
            StreamResult result = new StreamResult(sw);
            
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            transformer.setOutputProperty(OutputKeys.METHOD,"xml");
            
            DOMSource source = new DOMSource(doc);
            transformer.transform(source, result);

            xmlResult = sw.toString();
            System.out.println(xmlResult);
            return xmlResult;
        
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(XMLExporter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TransformerConfigurationException ex) {
            Logger.getLogger(XMLExporter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TransformerException ex) {
            Logger.getLogger(XMLExporter.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
//            return result;
        }
        return "problem";
    }
    
}
