/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.vse.fis.keg.entityclassifier.core.ontologymapper;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.SimpleSelector;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.FileManager;
import java.io.InputStream;

/**
 *
 * @author milan
 */
public class DBpediaOntologyManager {
    
    private static DBpediaOntologyManager manager = null;
    private static Model model = null;
    private static String dbpediaOntologyLocation = null;
    
    public static DBpediaOntologyManager getInstance(){
        
        if(manager == null){
            model = ModelFactory.createDefaultModel();
            InputStream in = FileManager.get().open( dbpediaOntologyLocation );
            model.read(in, null, "RDF/XML");
            manager = new DBpediaOntologyManager();
//            System.out.println("dbpedia ontology loaded");
        }
        return manager;
    }
    
    public static void setDbpediaOntologyFileLocation(String loc){
        dbpediaOntologyLocation = loc;
    }
    
    public OntoRecord getSubclass(String resourceURI, String lang){
        
        StmtIterator iter1 = model.listStatements( new SimpleSelector(ResourceFactory.createResource(resourceURI), ResourceFactory.createProperty("http://www.w3.org/2000/01/rdf-schema#subClassOf"),  (RDFNode)null));
        OntoRecord record = new OntoRecord();
        StmtIterator iter2;
        
        while(iter1.hasNext()) {
            ////System.out.println(iter.next().getObject().toString());
            record.setUri(iter1.next().getObject().toString());
            iter2 = model.listStatements( new SimpleSelector(ResourceFactory.createResource(record.getUri()), ResourceFactory.createProperty("http://www.w3.org/2000/01/rdf-schema#label"),  (RDFNode)null));
            
            while(iter2.hasNext()){
                Literal res = (Literal) iter2.next().getObject();                
                String tmpLang = res.getLanguage();
                
                if(tmpLang.equals("en") && lang.equals("en")){
                    record.setLabel(res.getString());
                    return record;
                } else if(tmpLang.equals("de") && lang.equals("de")){
                    record.setLabel(res.getString());
                    return record;
                } else if(tmpLang.equals("nl") && lang.equals("nl")){
                    record.setLabel(res.getString());
                    return record;
                }                    
            }
        }
//        System.out.println("ending here");
        return null;        
    }
}
