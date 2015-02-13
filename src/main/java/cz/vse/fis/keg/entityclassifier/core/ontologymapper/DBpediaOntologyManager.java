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
            record.setUri(iter1.next().getObject().toString());
            iter2 = model.listStatements( new SimpleSelector(ResourceFactory.createResource(record.getUri()), ResourceFactory.createProperty("http://www.w3.org/2000/01/rdf-schema#label"),  (RDFNode)null));
            
            while(iter2.hasNext()){
                Literal res = (Literal) iter2.next().getObject();                
                String tmpLang = res.getLanguage();
                
                if( tmpLang.equals("en") ){
                    record.setLabel(res.getString());
                    return record;
                    
                }
            }
        }
        return null;        
    }
}
