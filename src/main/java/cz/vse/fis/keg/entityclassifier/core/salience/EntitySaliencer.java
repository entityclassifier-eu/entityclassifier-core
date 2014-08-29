/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.lazy.IBk;
import weka.classifiers.meta.FilteredClassifier;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.neighboursearch.LinearNNSearch;
import weka.filters.unsupervised.attribute.Remove;

/**
 *
 * @author Milan Dojchinovski <milan.dojchinovski@fit.cvut.cz>
 * http://dojchinovski.mk
 */
public class EntitySaliencer {
    
    private static EntitySaliencer instance = null;
    private boolean initialized = false;
//    IBk
    private NaiveBayes classifier = null;
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
            
//            System.out.println("Started computing salience ...");
            
            ArrayList<SEntity> processedEntities = new ArrayList<SEntity>();
            
            for(Entity e : entities) {
                SEntity entityMention = new SEntity();
                entityMention.setBeginIndex(e.getStartOffset().intValue());
                entityMention.setEntityType(e.getEntityType());
//                System.out.println("Process entity mention ...");
                
                ArrayList<Type> types = e.getTypes();
                ArrayList<String> loggedURIs = new ArrayList<String>();
                
                if(types != null) {
                    for(Type t : types) {
                        String entityURI = t.getEntityURI();                    

                        if(!loggedURIs.contains(entityURI)) {
                            loggedURIs.add(entityURI);
//                            System.out.println(entityURI);
                            entityMention.getUrls().add(entityURI);
                        }
                    }
                }
                
                boolean entityAlreadyLogged = false;
                
                for(SEntity sEntity : processedEntities) {
                    boolean isThisEntitySame = false;
                    ArrayList<String> entityURIs1 = sEntity.getUrls();
                    ArrayList<String> entityURIs2 = entityMention.getUrls();
                    
//                    System.out.println("first");
//                    for(String eURI1 : entityURIs1) {
//                        System.out.println("uri: " + eURI1);
//                    }
//                    
//                    System.out.println("second");
//                    for(String eURI2 : entityURIs2) {
//                        System.out.println("uri: " + eURI2);
//                        
//                    }
                    
                    for(String eURI1 : entityURIs1) {
                        for(String eURI2 : entityURIs2) {
//                            System.out.println("comparing: " + eURI1 + " and " + eURI2);                                
                            if(!entityAlreadyLogged) {
                                if(eURI1.equals(eURI2)) {
//                                    System.out.println("now");
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
//                    System.out.println("adding entity");
                    processedEntities.add(entityMention);
                }
            }
            
//            for(SEntity e : processedEntities) {
//                System.out.println("###############");
//                System.out.println(e.getNumOccurrences());
//                for(String uri : e.getUrls()) {
//                    System.out.println(uri);
//                }
//            }
            
            // Preparing the test data container.
            FastVector attributes = new FastVector(6);
            attributes.add(new Attribute("beginIndex"));
            attributes.add(new Attribute("numUniqueEntitiesInDoc"));
            attributes.add(new Attribute("numOfOccurrencesOfEntityInDoc"));
            attributes.add(new Attribute("numOfEntityMentionsInDoc"));
            
            FastVector entityTypeNominalAttVal = new FastVector(2);
            entityTypeNominalAttVal.addElement("named_entity");
            entityTypeNominalAttVal.addElement("common_entity");
            
            Attribute entityTypeAtt = new Attribute("entityType", entityTypeNominalAttVal);
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
                
//                System.out.println(processedEntities.get(i).getEntityType());
                String entityType = "";
                if(processedEntities.get(i).getEntityType().equals("named entity")) {
                    entityType = "named_entity";
                } else if(processedEntities.get(i).getEntityType().equals("common entity")) {
                    entityType = "common_entity";                    
                } else {
                    System.out.println("PROBLEM");
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
//                System.out.println(classLabel + " : " + probability);
//                System.out.println(pred[0]+":"+pred[1]+":"+pred[2]);
                
                double salienceScore = pred[1] * 0.5 + pred[2];
//                sEntity.setSalienceScore(salienceScore);
//                sEntity.setSalienceConfidence(probability);
//                sEntity.setSalienceClass(classLabel);
                
                for(Entity e : entities) {
                    ArrayList<Type> types = e.getTypes();
                    if(types != null) {
                        for(Type t : types) {
                            if(sEntity.getUrls().contains(t.getEntityURI())){
                                Salience s = new Salience();
                                s.setClassLabel(classLabel);
                                DecimalFormat df=new DecimalFormat("0.000");
                                double fProbability = (Double)df.parse(df.format(probability));
                                double fSalience = (Double)df.parse(df.format(salienceScore));
                                s.setConfidence(fProbability);
                                s.setScore(fSalience);
                                t.setSalience(s);
                            }
                        }
                    }
                }
            }
            
//            System.out.println("Finished computing salience ...");            
//            System.out.println("END");
            
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
            
            classifier = new NaiveBayes();
            
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
