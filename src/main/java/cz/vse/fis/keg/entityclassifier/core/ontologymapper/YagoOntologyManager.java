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
package cz.vse.fis.keg.entityclassifier.core.ontologymapper;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.SimpleSelector;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import cz.vse.fis.keg.entityclassifier.core.vao.Hypernym;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author milan
 */
public class YagoOntologyManager {
    
    private static YagoOntologyManager manager = null;
    private static Model model = null;
    private static String yagoOntologyLocation = null;
    private static MongoClient mongoClient;
    private static DB db = null;
    
    
    public static YagoOntologyManager getInstance(){
    
        if(manager == null){
            
            try {
                
                mongoClient = new MongoClient( "localhost" , 27017 );
                db = mongoClient.getDB( "thddb" );
                
                model = ModelFactory.createDefaultModel();
                BufferedReader br = null;
            
                br = new BufferedReader(new FileReader(yagoOntologyLocation));
                String line;
                String[] stm;
                
                while ((line = br.readLine()) != null) {
                    
                    stm = line.split("\\t");
                    
                    if(stm[2].equals("rdfs:subClassOf")){
                        
                        if(stm[3].startsWith("<")){
                            model.add(ResourceFactory.createResource("http://yago-knowledge.org/resource/"+stm[1].substring(1, stm[1].length()-1)), RDFS.subClassOf, ResourceFactory.createResource("http://yago-knowledge.org/resource/"+stm[3].substring(1, stm[3].length()-1)));
                        } else {
                            model.add(ResourceFactory.createResource("http://yago-knowledge.org/resource/"+stm[1].substring(1, stm[1].length()-1)), RDFS.subClassOf, ResourceFactory.createResource("http://yago-knowledge.org/resource/"+stm[3]));                        
                        }
                    }
                }
                
                manager = new YagoOntologyManager();
            } catch (FileNotFoundException ex) {
                Logger.getLogger(YagoOntologyManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(YagoOntologyManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return manager;
    }
    
    public HashSet getYagoHypernyms(String entityTitle, String entityURI, String lang, String origin){
        
        HashSet hypernymsList = new HashSet();
        try {
        DBCursor cursor = db.getCollection("entities_yago").find(new BasicDBObject().append("uri", entityURI));
        Model mainModel = ModelFactory.createDefaultModel();
        
        while(cursor.hasNext()){
        
            DBObject resObj = cursor.next();
            
            BasicDBList types = (BasicDBList) resObj.get("types");
            
            if(types != null){
                
                for(int i=0; i<types.size(); i++){
                    
                    DBObject type = (DBObject) types.get(i); // yago type (pointer in yago taxonomy)
                    
                    Hypernym hyp1 = new Hypernym();
                    hyp1.setEntityURL(entityURI);
                    hyp1.setEntity(entityTitle);
                    hyp1.setTypeURL(type.get("uri").toString());
                    hyp1.setType(type.get("label").toString());
                    hyp1.setOrigin(origin);
                    hyp1.setAccuracy("-1.0");
                    hypernymsList.add(hyp1);

                    mainModel.union(getHierarchyModel(type.get("uri").toString()));

                }
            }
        }
        
        StmtIterator iter = mainModel.listStatements( new SimpleSelector((Resource)null, null,  (RDFNode)null));
                    
        while(iter.hasNext()){
                        
            Statement stm = iter.next();
                    
            Hypernym hyp = new Hypernym();
            hyp.setEntityURL(entityURI);
            hyp.setEntity(entityTitle);
            hyp.setTypeURL(stm.getObject().toString());
            hyp.setOrigin(origin);
            hyp.setAccuracy("-1.0");
            
            String typeLabel = getYagoTypeLabel(stm.getObject().toString());
            
            if(typeLabel != null){
                hyp.setType(typeLabel);
                hypernymsList.add(hyp);        
            }
        }
        }catch(Exception ex){
            Logger.getLogger(YagoOntologyManager.class.getName()).log(Level.SEVERE, "Problem with the mongodb client.", ex);            
        }
        return hypernymsList;
    }
    
    public String getYagoTypeLabel(String uri){
        
        DBCursor cursor = db.getCollection("entities_yago").find(new BasicDBObject().append("uri", uri));
        
        if(cursor.size() > 0){
            DBObject tmp = cursor.next();
            BasicDBList labels = (BasicDBList)tmp.get("labels");
            
            if(labels != null){
                DBObject tmp2 = (DBObject)labels.get(0);
                return tmp2.get("label").toString();
            }
        }
        
        return null;
    
    }
    
    public Model getHierarchyModel(String uri){
        
        // returns all subclasses for given URI
        Model m = ModelFactory.createDefaultModel();
        OntoRecord initRecord = new OntoRecord();
        initRecord.setUri(uri);
        
        while(initRecord !=null){
            
            initRecord = getSuperclass(initRecord.getUri());
            
            if(initRecord != null){
                StmtIterator iter1 = model.listStatements( new SimpleSelector(ResourceFactory.createResource(uri), RDFS.subClassOf,  (RDFNode)null));                
                m.add(iter1);
            }
        }
         
        return m;
    }
    
    public Model processEntity(String entityTitle){
        
        DBCursor cursor = db.getCollection("entities_yago").find(new BasicDBObject().append("uri", "http://yago-knowledge.org/resource/"+entityTitle.replaceAll(" ", "_")));
        
        while(cursor.hasNext()) {
            
            DBObject resObj = cursor.next();
            
            BasicDBList types = (BasicDBList) resObj.get("types");
            
            if(types != null){
                for(int i=0; i<types.size(); i++){
                    DBObject type = (DBObject) types.get(i);
                    Hypernym h = new Hypernym();
                    h.setEntity(entityTitle);
                    h.setEntityURL(resObj.get("uri").toString());
                    h.setType(type.get("label").toString());
                    h.setTypeURL(type.get("uri").toString());
                    h.setOrigin("thd-derived");
                    
                    OntoRecord initRecord = new OntoRecord();
                    initRecord.setUri(type.get("uri").toString());
                    
                    while(initRecord != null){
                        
                        initRecord = getSuperclass(initRecord.getUri());
                            
                            if(initRecord != null){
                                
                                Hypernym hypernymDerived = new Hypernym();
                                hypernymDerived.setEntity(entityTitle);
                                hypernymDerived.setEntityURL(resObj.get("uri").toString());
                                hypernymDerived.setType(initRecord.getLabel());
                                hypernymDerived.setTypeURL(initRecord.getUri());
                                hypernymDerived.setOrigin("thd-derived");
                            }
                    }                    
                }
            }            
        }
        return null;
    }
        
    public static void setYagoOntologyFileLocation(String loc){
        yagoOntologyLocation = loc;
    }
    
    public void test3(){
        
        StmtIterator iter = model.listStatements( new SimpleSelector(ResourceFactory.createResource("http://yago-knowledge.org/resource/wikicategory_Category:Music_competitions"), (Property) RDFS.subClassOf,  (RDFNode)null));
        while (iter.hasNext()) {
        }            
        
    }
    
    public void test(){
    
        StmtIterator iter = model.listStatements( new SimpleSelector(ResourceFactory.createResource("http://dbpedia.org/ontology/Stadium"), null,  (RDFNode)null));
        while (iter.hasNext()) {
        }    
    }
    
    public OntoRecord getSuperclass(String resourceURI){
        
        StmtIterator iter1 = model.listStatements( new SimpleSelector(ResourceFactory.createResource(resourceURI), RDFS.subClassOf,  (RDFNode)null));
        OntoRecord record = new OntoRecord();
        
        while(iter1.hasNext()) {
            record.setUri(iter1.next().getObject().toString());
            record.setLabel("Test label");
            return record;
        }
        return null;
    }
    
    public static void test2(){
    
        StmtIterator iter1 = model.listStatements( new SimpleSelector(ResourceFactory.createResource("http://dbpedia.org/ontology/RouteOfTransportation"), ResourceFactory.createProperty("http://www.w3.org/2000/01/rdf-schema#label"),  (RDFNode)null));
        Literal res = (Literal)iter1.next().getObject();
        if(res.getLanguage().equals("de")){
        }
    }
}
