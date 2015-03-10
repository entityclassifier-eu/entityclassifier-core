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
package cz.vse.fis.keg.entityclassifier.core.entitylinking;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.SimpleSelector;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.FileManager;
import cz.vse.fis.keg.entityclassifier.core.conf.Settings;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Milan Dojchinovski <milan.dojchinovski@fit.cvut.cz>
 * http://dojchinovski.mk
 */
public class DisambiguationPageValidator {
    
    private static DisambiguationPageValidator instance = null;
    private Model model = ModelFactory.createDefaultModel();
    private boolean linksLoaded = false;
    
    public static DisambiguationPageValidator getInstance(){
        if(instance == null) {
            instance = new DisambiguationPageValidator();
        }
        return instance;
    }
    
    
    public boolean isDisambiguationResource(String uri) {
        
        if(!linksLoaded){
            System.out.println(Settings.EN_DBPEDIA_DISAMBIGUATION_DATASET);
            System.out.println(Settings.DE_DBPEDIA_DISAMBIGUATION_DATASET);
            System.out.println(Settings.NL_DBPEDIA_DISAMBIGUATION_DATASET);
            InputStream in1 = FileManager.get().open( Settings.EN_DBPEDIA_DISAMBIGUATION_DATASET );
            InputStream in2 = FileManager.get().open( Settings.DE_DBPEDIA_DISAMBIGUATION_DATASET );
            InputStream in3 = FileManager.get().open( Settings.NL_DBPEDIA_DISAMBIGUATION_DATASET );
            model.read(in1, null, "N-TRIPLES");
            System.out.println("Loaded English disambiguation dataset.");
            model.read(in2, null, "N-TRIPLES");
            System.out.println("Loaded German disambiguation dataset.");
            model.read(in3, null, "N-TRIPLES");
            System.out.println("Loaded Dutch disambiguation dataset.");
            linksLoaded = true;
        }
        
        StmtIterator iter = model.listStatements( new SimpleSelector(
                ResourceFactory.createResource(uri), 
                ResourceFactory.createProperty("http://dbpedia.org/ontology/wikiPageDisambiguates"), 
                        (RDFNode)null));
        
        return iter.hasNext();
    }
    
    public boolean processTxt(String path){
        OutputStream os = null;
        boolean isDisambiguationPage = false;
        try {
            URL url = new URL(path);
            StringBuffer buffer = new StringBuffer();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-type", "text/html; charset=UTF-8");
            connection.setDoOutput(true);
            connection.setDoInput(true);
            os = connection.getOutputStream();
            os.flush();
            os.close();
            InputStream is = connection.getInputStream();
            Reader isr = new InputStreamReader(is,"UTF-8");
            Reader in = new BufferedReader(isr);
            int ch;
            while ((ch = in.read()) > -1) {
                buffer.append((char) ch);
            }
            in.close();
            isr.close();
            String result = buffer.toString();
            if(result.contains("Category:Disambiguation pages")){
                isDisambiguationPage = true;
            } else {
                isDisambiguationPage = false;
            }
        } catch (IOException ex) {
            Logger.getLogger(DisambiguationPageValidator.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                os.close();
            } catch (IOException ex) {
                Logger.getLogger(DisambiguationPageValidator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }        
        return isDisambiguationPage;
    }
}
