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
package cz.vse.fis.keg.entityclassifier.core.salience;

import cz.vse.fis.keg.entityclassifier.core.THDController;
import cz.vse.fis.keg.entityclassifier.core.conf.Settings;
import cz.vse.fis.keg.entityclassifier.core.vao.Entity;
import cz.vse.fis.keg.entityclassifier.core.vao.Salience;
import cz.vse.fis.keg.entityclassifier.core.vao.Type;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import weka.classifiers.trees.RandomForest;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

/**
 *
 * @author Milan Dojchinovski <milan.dojchinovski@fit.cvut.cz>
 * http://dojchinovski.mk
 */
public class EntitySaliencer {
    
    private static EntitySaliencer instance = null;
    private boolean initialized = false;
    
//    private NaiveBayes classifier = null;
    private RandomForest classifier = null;
    
    public static EntitySaliencer getInstance() {
        if(instance == null) {
            instance = new EntitySaliencer();
        }
        return instance;        
    }
    
    public void initialize() {
        trainModel();
    }
    
    public void computeSalience(List<Entity> entities) {
        try {
            if(!initialized){
                initialize();
                initialized = true;
            }
            
            ArrayList<SEntity> processedEntities = new ArrayList<SEntity>();
            
            for(Entity e : entities) {
                SEntity entityMention = new SEntity();
                entityMention.setBeginIndex(e.getStartOffset().intValue());
                entityMention.setEntityType(e.getEntityType());
                
                ArrayList<Type> types = e.getTypes();
                ArrayList<String> loggedURIs = new ArrayList<String>();
                
                if(types != null) {
                    for(Type t : types) {
                        String entityURI = t.getEntityURI();                    

                        if(!loggedURIs.contains(entityURI)) {
                            loggedURIs.add(entityURI);
                            entityMention.getUrls().add(entityURI);
                        }
                    }
                }
                
                boolean entityAlreadyLogged = false;
                
                for(SEntity sEntity : processedEntities) {
                    boolean isThisEntitySame = false;
                    ArrayList<String> entityURIs1 = sEntity.getUrls();
                    ArrayList<String> entityURIs2 = entityMention.getUrls();
                    
                    for(String eURI1 : entityURIs1) {
                        for(String eURI2 : entityURIs2) {
                            if(!entityAlreadyLogged) {
                                if(eURI1.equals(eURI2)) {
                                    entityAlreadyLogged = true;
                                    isThisEntitySame = true;
                                    sEntity.setNumOccurrences(sEntity.getNumOccurrences()+1);
                                }
                            }
                        }
                    }
                    
                    if(isThisEntitySame) {
                        for(String uri : entityMention.getUrls()) {
                            if(!sEntity.getUrls().contains(uri)){
                                sEntity.getUrls().add(uri);
                            }
                        }
                    }
                }
                
                // Entity seen for first time in the document.
                if(!entityAlreadyLogged) {
                    entityMention.setNumOccurrences(1);
                    processedEntities.add(entityMention);
                }
            }
            
            // Preparing the test data container.
            FastVector attributes = new FastVector(6);
            attributes.add(new Attribute("beginIndex"));
            attributes.add(new Attribute("numUniqueEntitiesInDoc"));
            attributes.add(new Attribute("numOfOccurrencesOfEntityInDoc"));
            attributes.add(new Attribute("numOfEntityMentionsInDoc"));
            
            FastVector entityTypeNominalAttVal = new FastVector(2);
            entityTypeNominalAttVal.addElement("named_entity");
            entityTypeNominalAttVal.addElement("common_entity");
            
            Attribute entityTypeAtt = new Attribute("type", entityTypeNominalAttVal);
            attributes.add(entityTypeAtt);
            FastVector classNominalAttVal = new FastVector(3);
            classNominalAttVal.addElement("not_salient");
            classNominalAttVal.addElement("less_salient");
            classNominalAttVal.addElement("most_salient");
            Attribute classAtt = new Attribute("class", classNominalAttVal);
            attributes.add(classAtt);
            Instances evalData = new Instances("MyRelation", attributes, 0);

            evalData.setClassIndex(evalData.numAttributes() - 1);
                        
            for(int i = 0; i < processedEntities.size(); i++) {
                
                String entityType = "";
                if(processedEntities.get(i).getEntityType().equals("named entity")) {
                    entityType = "named_entity";
                } else if(processedEntities.get(i).getEntityType().equals("common entity")) {
                    entityType = "common_entity";                    
                } else {
                }
                Instance inst = new DenseInstance(6);
                inst.setValue(evalData.attribute(0), processedEntities.get(i).getBeginIndex()); // begin index
                inst.setValue(evalData.attribute(1), processedEntities.size()); // num of unique entities in doc
                inst.setValue(evalData.attribute(2), processedEntities.get(i).getNumOccurrences()); // num of entity occurrences in doc
                inst.setValue(evalData.attribute(3), entities.size()); // num of entity mentions in doc
                inst.setValue(evalData.attribute(4), entityType); // type of the entity
                evalData.add(inst);
                
            }
            
            for(int i = 0; i < processedEntities.size(); i++) {
                SEntity sEntity = processedEntities.get(i);
                int classIndex = (int) classifier.classifyInstance(evalData.get(i));
                String classLabel = evalData.firstInstance().classAttribute().value( classIndex);
                double pred[] = classifier.distributionForInstance(evalData.get(i));
                double probability = pred[classIndex];
                
                double salienceScore = pred[1] * 0.5 + pred[2];
                sEntity.setSalienceScore(salienceScore);
                sEntity.setSalienceConfidence(probability);
                sEntity.setSalienceClass(classLabel);
                
                
                for(Entity e : entities) {
                    ArrayList<Type> types = e.getTypes();
                    if(types != null) {
                        for(Type t : types) {
                            if(sEntity.getUrls().contains(t.getEntityURI())){
                                Salience s = new Salience();
                                s.setClassLabel(classLabel);
                                DecimalFormat df = new DecimalFormat("0.000");
                                double fProbability = df.parse(df.format(probability)).doubleValue();
                                double fSalience = df.parse(df.format(salienceScore)).doubleValue();
                                s.setConfidence(fProbability);
                                s.setScore(fSalience);
                                t.setSalience(s);
                            }
                        }
                    }
                }
            }
            
        } catch (Exception ex) {
            Logger.getLogger(EntitySaliencer.class.getName()).log(Level.SEVERE, null, ex);
        }        
    }

    private void trainModel() {
        
        BufferedReader reader = null;
        
        try {
            
            URL fileURL = THDController.getInstance().getClass().getResource(Settings.SALIENCE_DATASET);
            File arrfFile = new File(fileURL.getFile());
            
            reader = new BufferedReader(new FileReader(arrfFile));
            Instances data = new Instances(reader);            
            data.setClassIndex(data.numAttributes() - 1);
            
//            classifier = new NaiveBayes();
            classifier = new RandomForest();
            
            // Train the classifer.
            classifier.buildClassifier(data);

        } catch (FileNotFoundException ex) {
            Logger.getLogger(EntitySaliencer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(EntitySaliencer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(EntitySaliencer.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                reader.close();
                System.out.println("Model was successfully trained.");
            } catch (IOException ex) {
                Logger.getLogger(EntitySaliencer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
