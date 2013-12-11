/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.vse.fis.keg.entityclassifier.core.ontologymapper;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.SimpleSelector;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.vocabulary.RDFS;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Milan Dojchinovski <http://dojchinovski.mk>
 */
public class DBpediaOntologyMapper {
    
    private static DBpediaOntologyMapper instance = null;
    private static Model enModel = null;
    private static Model deModel = null;
    private static Model nlModel = null;
    private static String enMappingsLocation = null;
    private static String deMappingsLocation = null;
    private static String nlMappingsLocation = null;
    
    public static DBpediaOntologyMapper getInstance() {
        if(instance == null) {
            FileInputStream enIn = null;
            FileInputStream deIn = null;
            FileInputStream nlIn = null;
            try {
                enModel = ModelFactory.createDefaultModel();
                deModel = ModelFactory.createDefaultModel();
                nlModel = ModelFactory.createDefaultModel();
                enIn = new FileInputStream(getEnMappingsLocation());                
                deIn = new FileInputStream(getDeMappingsLocation());                
                nlIn = new FileInputStream(getNlMappingsLocation());
                BufferedReader enBr = new BufferedReader(new InputStreamReader(enIn));
                BufferedReader deBr = new BufferedReader(new InputStreamReader(deIn));
                BufferedReader nlBr = new BufferedReader(new InputStreamReader(nlIn));
                String strLine;
                while((strLine = enBr.readLine()) != null) {
                    if(!strLine.startsWith("#")) {
                        String[] all = strLine.split(" ");
                        enModel.createResource(all[0].substring(1, all[0].length()-1)).addProperty(ResourceFactory.createProperty("http://www.w3.org/2000/01/rdf-schema#subClassOf"), all[2].substring(1, all[2].length()-1));
                    }
                }
                while((strLine = enBr.readLine()) != null) {
                    if(!strLine.startsWith("#")) {
                        String[] all = strLine.split(" ");
                        deModel.createResource(all[0].substring(1, all[0].length()-1)).addProperty(ResourceFactory.createProperty("http://www.w3.org/2000/01/rdf-schema#subClassOf"), all[2].substring(1, all[2].length()-1));
                    }
                }
                while((strLine = enBr.readLine()) != null) {
                    if(!strLine.startsWith("#")) {
                        String[] all = strLine.split(" ");
                        nlModel.createResource(all[0].substring(1, all[0].length()-1)).addProperty(ResourceFactory.createProperty("http://www.w3.org/2000/01/rdf-schema#subClassOf"), all[2].substring(1, all[2].length()-1));
                    }
                }
                instance = new DBpediaOntologyMapper();
            } catch (FileNotFoundException ex) {
                Logger.getLogger(DBpediaOntologyMapper.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(DBpediaOntologyMapper.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    enIn.close();
                    deIn.close();
                    nlIn.close();
                } catch (IOException ex) {
                    Logger.getLogger(DBpediaOntologyMapper.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return instance;
    }

    /**
     * @return the enMappingsLocation
     */
    public static String getEnMappingsLocation() {
        return enMappingsLocation;
    }

    /**
     * @param aEnMappingsLocation the enMappingsLocation to set
     */
    public static void setEnMappingsLocation(String aEnMappingsLocation) {
        enMappingsLocation = aEnMappingsLocation;
    }

    /**
     * @return the deMappingsLocation
     */
    public static String getDeMappingsLocation() {
        return deMappingsLocation;
    }

    /**
     * @param aDeMappingsLocation the deMappingsLocation to set
     */
    public static void setDeMappingsLocation(String aDeMappingsLocation) {
        deMappingsLocation = aDeMappingsLocation;
    }

    /**
     * @return the nlMappingsLocation
     */
    public static String getNlMappingsLocation() {
        return nlMappingsLocation;
    }

    /**
     * @param aNlMappingsLocation the nlMappingsLocation to set
     */
    public static void setNlMappingsLocation(String aNlMappingsLocation) {
        nlMappingsLocation = aNlMappingsLocation;
    }


    public String mapEnResource(String h) {
        
        StmtIterator iter = enModel.listStatements( new SimpleSelector(ResourceFactory.createResource(h), ResourceFactory.createProperty("http://www.w3.org/2000/01/rdf-schema#subClassOf"),  (RDFNode)null));
        String ontoType = null;
        while(iter.hasNext()) {
            ontoType = iter.next().getObject().toString();            
        }
        return ontoType;
    }
    
    public String mapDeResource(String h) {
        
        StmtIterator iter = deModel.listStatements( new SimpleSelector(ResourceFactory.createResource(h), ResourceFactory.createProperty("http://www.w3.org/2000/01/rdf-schema#subClassOf"),  (RDFNode)null));
        String ontoType = null;
        while(iter.hasNext()) {
            ontoType = iter.next().getObject().toString();            
        }
        return ontoType;
    }    
    public String mapNlResource(String h) {
        
        StmtIterator iter = nlModel.listStatements( new SimpleSelector(ResourceFactory.createResource(h), ResourceFactory.createProperty("http://www.w3.org/2000/01/rdf-schema#subClassOf"),  (RDFNode)null));
        String ontoType = null;
        while(iter.hasNext()) {
            ontoType = iter.next().getObject().toString();            
        }
        return ontoType;
    }    
}
