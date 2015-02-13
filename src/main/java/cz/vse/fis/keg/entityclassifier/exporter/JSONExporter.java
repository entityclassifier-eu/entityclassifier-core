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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author Milan
 */
public class JSONExporter {
    
    private static JSONExporter instance = null;
    
    public static JSONExporter getInstance(){
        if(instance == null){
            instance = new JSONExporter();
        }
        return instance;
    }
    
    public String toJSONOneEntity(List<Entity> entities) {
        String jsonResult = "";
        try {
            
            JSONObject jsonE = new JSONObject();
            if(!entities.isEmpty()) {
                
                Entity e = entities.get(0);
                
                jsonE.put("underlyingString", e.getUnderlyingString());
                ArrayList<Type> types = e.getTypes();
                
                JSONArray typesJ = new JSONArray();                
                
                if(types != null) {
                    for(Type t : types) {
                        
                        JSONObject typeJ = new JSONObject();

                        String tLabel = t.getTypeLabel();
                        if(tLabel != null){
                            typeJ.put("typeLabel", t.getTypeLabel());
                        } else {
                            typeJ.put("typeLabel", JSONObject.NULL);
                        }
                        
                        String tURI = t.getTypeURI();
                        if(tURI != null){
                            typeJ.put("typeURI", t.getTypeURI());
                        } else {
                            typeJ.put("typeURI", JSONObject.NULL);
                        }
                        
                        typeJ.put("entityLabel", t.getEntityLabel());
                        typeJ.put("entityURI", t.getEntityURI());
                        
                        Confidence classificationConf = t.getClassificationConfidence();
                        
                        if(classificationConf != null) {
                            
                            JSONObject confValueJ = new JSONObject();
                            confValueJ.put("value", classificationConf.getValue());
                                
                            if(classificationConf.getType() != null) {
                                confValueJ.put("type", classificationConf.getType());
                            }else {
                                confValueJ.put("type", "classification");                            
                            }
                            typeJ.put("classificationConfidence", confValueJ);
                        } else {
                            JSONObject confValueJ = new JSONObject();
                            confValueJ.put("value", -1);
                            confValueJ.put("type", "classification");                            
                            typeJ.put("classificationConfidence", confValueJ);                        
                        }
                                                
                        typeJ.put("provenance", t.getProvenance());
                        typesJ.put(typeJ);
                    }
                    jsonE.put("types", typesJ);
                }
            }

            jsonResult = jsonE.toString();
            return jsonResult;
        
        } catch (Exception ex) {
            Logger.getLogger(JSONExporter.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
        }
        return "problem";
    }
    
    public String toJSON(List<Entity> entities) {
        String jsonResult = "";
        try {
            
            JSONArray jsonEntities = new JSONArray();
            
            for(Entity e : entities){
                
                JSONObject jsonE = new JSONObject();
                jsonE.put("entityType", e.getEntityType());
                jsonE.put("underlyingString", e.getUnderlyingString());
                jsonE.put("startOffset", e.getStartOffset());
                jsonE.put("endOffset", e.getEndOffset());
                ArrayList<Type> types = e.getTypes();
                
                JSONArray typesJ = new JSONArray();                
                
                if(types != null) {
                    for(Type t : types) {
                        
                        JSONObject typeJ = new JSONObject();

                        String tLabel = t.getTypeLabel();
                        if(tLabel != null){
                            typeJ.put("typeLabel", t.getTypeLabel());
                        } else {
                            String tmp = null;
                            typeJ.put("typeLabel", JSONObject.NULL);
                        }
                        
                        String tURI = t.getTypeURI();
                        if(tURI != null){
                            typeJ.put("typeURI", t.getTypeURI());
                        } else {
                            String tmp = null;
                            typeJ.put("typeURI", JSONObject.NULL);
                        }
                        
                        typeJ.put("entityLabel", t.getEntityLabel());
                        typeJ.put("entityURI", t.getEntityURI());
                        
                        Confidence classificationConf = t.getClassificationConfidence();
                        
                        if(classificationConf != null) {
                            
                            JSONObject confValueJ = new JSONObject();
                            confValueJ.put("value", classificationConf.getValue());
                                
                            if(classificationConf.getType() != null) {
                                confValueJ.put("type", classificationConf.getType());
                            }else {
                                confValueJ.put("type", "classification");                            
                            }
                            typeJ.put("classificationConfidence", confValueJ);
                        } else {
                            JSONObject confValueJ = new JSONObject();
                            confValueJ.put("value", -1);
                            confValueJ.put("type", "classification");                            
                            typeJ.put("classificationConfidence", confValueJ);                        
                        }
                        
                        // create element linking confidence
                        Confidence linkingConf = t.getLinkingConfidence();
                        if(linkingConf != null) {
                            JSONObject linkValueJ = new JSONObject();
                            linkValueJ.put("value", linkingConf.getValue());
                            if(linkingConf.getType() != null) {
                                linkValueJ.put("type", linkingConf.getType());
                            }
                            typeJ.put("linkingConfidence", linkValueJ);
                        } else {
                            JSONObject linkValueJ = new JSONObject();
                            linkValueJ.put("value", -1);
                            linkValueJ.put("type", "linking");
                            typeJ.put("linkingConfidence", linkValueJ);                        
                        }
                        
                        
                        Salience s = t.getSalience();
                        if(s != null) {
                            JSONObject salienceJ = new JSONObject();
                            salienceJ.put("score", s.getScore());
                            salienceJ.put("confidence", s.getConfidence());
                            salienceJ.put("classLabel", s.getClassLabel());
                            typeJ.put("salience", salienceJ);                            
                        }
                        
                        typeJ.put("provenance", t.getProvenance());
                        typesJ.put(typeJ);
                    }
                    jsonE.put("types", typesJ);
                }
                jsonEntities.put(jsonE);
            }

            jsonResult = jsonEntities.toString();
            return jsonResult;
        
        } catch (Exception ex) {
            Logger.getLogger(JSONExporter.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
        }
        return "problem";
    }
    
}
