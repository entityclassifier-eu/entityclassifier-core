/*
 * #%L
 * Entityclassifier.eu NER CORE v3.9
 * %%
 * Copyright (C) 2015 Knowledge Engineering Group (KEG) and Web Intelligence Research Group (WIRG) - Milan Dojchinovski (milan.dojchinovski@fit.cvut.cz)
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
package cz.vse.fis.keg.entityclassifier.exporter;

import cz.vse.fis.keg.entityclassifier.core.vao.Confidence;
import cz.vse.fis.keg.entityclassifier.core.vao.Entity;
import cz.vse.fis.keg.entityclassifier.core.vao.Salience;
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

    public String toXMLOneEntity(List<Entity> entities) {
        
        String xmlResult = "";
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.newDocument();
            
            // create root element "<entities>"
            Element entityEl = doc.createElement("entity");
            doc.appendChild(entityEl);
            
            if(!entities.isEmpty()) {
                Entity e = entities.get(0);
                // create element <entity>

                // create element <underlyingString>
                Element underlyingStringEl = doc.createElement("underlyingString");
                underlyingStringEl.appendChild(doc.createTextNode(e.getUnderlyingString()));
                entityEl.appendChild(underlyingStringEl);

                // create element <types>
                Element typesEl = doc.createElement("types");
                ArrayList<Type> types = e.getTypes();                
                
                if(types != null ) {
                    
                        for(Type t : types) {

                            // create element <type>
                            Element typeEl = doc.createElement("type");

                            // create element <typeLabel>
                            Element typeLabelEl = doc.createElement("typeLabel");
                            String tLabel = t.getTypeLabel();
                            if(tLabel != null){
                                typeLabelEl.appendChild(doc.createTextNode(t.getTypeLabel()));
                                typeEl.appendChild(typeLabelEl);
                            }

                            // create element <typeURI>
                            Element typeURIEl = doc.createElement("typeURI");
                            String tURI = t.getTypeURI();
                            if(tURI != null){
                                typeURIEl.appendChild(doc.createTextNode(t.getTypeURI()));
                                typeEl.appendChild(typeURIEl);
                            }

                            // create element <entityLabel>
                            Element entityLabelEl = doc.createElement("entityLabel");
                            entityLabelEl.appendChild(doc.createTextNode(t.getEntityLabel()));
                            typeEl.appendChild(entityLabelEl);

                            // create element <entityURI>            
                            Element entityURIEl = doc.createElement("entityURI");
                            entityURIEl.appendChild(doc.createTextNode(t.getEntityURI()));
                            typeEl.appendChild(entityURIEl);

                            // create element <confidence>
                            Confidence classificationConf = t.getClassificationConfidence();
                            if(classificationConf != null) {
                                Element confidenceEl = doc.createElement("confidence");
                                confidenceEl.appendChild(doc.createTextNode(classificationConf.getValue()+""));
                                if(classificationConf.getType() != null) {
                                    confidenceEl.setAttribute("type", classificationConf.getType());
                                }
                                typeEl.appendChild(confidenceEl);
                            }

                            // create element <provenance>
                            Element provenanceEl = doc.createElement("provenance");
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
            return xmlResult;
        
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(XMLExporter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TransformerConfigurationException ex) {
            Logger.getLogger(XMLExporter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TransformerException ex) {
            Logger.getLogger(XMLExporter.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
        }
        return "problem";
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
                    
                    for(Type t : types) {

                        // create element <type>
                        Element typeEl = doc.createElement("type");

                        // create element <typeLabel>
                        Element typeLabelEl = doc.createElement("typeLabel");
                        String tLabel = t.getTypeLabel();
                        if(tLabel != null){
                            typeLabelEl.appendChild(doc.createTextNode(t.getTypeLabel()));
                            typeEl.appendChild(typeLabelEl);
                        }

                        // create element <typeURI>
                        Element typeURIEl = doc.createElement("typeURI");
                        String tURI = t.getTypeURI();
                        if(tURI != null){
                            typeURIEl.appendChild(doc.createTextNode(t.getTypeURI()));
                            typeEl.appendChild(typeURIEl);
                        }

                        // create element <entityLabel>
                        Element entityLabelEl = doc.createElement("entityLabel");
                        entityLabelEl.appendChild(doc.createTextNode(t.getEntityLabel()));
                        typeEl.appendChild(entityLabelEl);

                        // create element <entityURI>            
                        Element entityURIEl = doc.createElement("entityURI");
                        entityURIEl.appendChild(doc.createTextNode(t.getEntityURI()));
                        typeEl.appendChild(entityURIEl);

                        // create element <confidence>
                        Confidence classificationConf = t.getClassificationConfidence();
                        if(classificationConf != null) {
                            Element confidenceEl = doc.createElement("confidence");
                            confidenceEl.appendChild(doc.createTextNode(classificationConf.getValue()+""));
                            if(classificationConf.getType() != null) {
                                confidenceEl.setAttribute("type", classificationConf.getType());
                            }
                            typeEl.appendChild(confidenceEl);
                        }
                        
                        // create element <confidence>
                        Confidence linkingConf = t.getLinkingConfidence();
                        if(linkingConf != null) {
                            Element confidenceEl = doc.createElement("confidence");
                            confidenceEl.appendChild(doc.createTextNode(linkingConf.getValue()+""));
                            if(linkingConf.getType() != null) {
                                confidenceEl.setAttribute("type", linkingConf.getType());
                            }
                            typeEl.appendChild(confidenceEl);
                        }
                        
                        Salience s = t.getSalience();
                        if(s != null) {
                            Element salienceEl = doc.createElement("salience");
                            
                            Element scoreEl = doc.createElement("score");
                            scoreEl.appendChild(doc.createTextNode(s.getScore()+""));
                            salienceEl.appendChild(scoreEl);
                            
                            Element confidenceEl = doc.createElement("confidence");
                            confidenceEl.appendChild(doc.createTextNode(s.getConfidence()+""));
                            salienceEl.appendChild(confidenceEl);

                            Element classLabelEl = doc.createElement("class");
                            classLabelEl.appendChild(doc.createTextNode(s.getClassLabel()+""));
                            salienceEl.appendChild(classLabelEl);
                            
                            typeEl.appendChild(salienceEl);
                        }

                        // create element <provenance>
                        Element provenanceEl = doc.createElement("provenance");
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
            return xmlResult;
        
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(XMLExporter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TransformerConfigurationException ex) {
            Logger.getLogger(XMLExporter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TransformerException ex) {
            Logger.getLogger(XMLExporter.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
        }
        return "problem";
    }    
}
