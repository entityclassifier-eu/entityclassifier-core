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
package cz.vse.fis.keg.entityclassifier.core;

import cz.vse.fis.keg.entityclassifier.core.vao.Entity;
import gate.util.GateException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Milan Dojčinovski 
 <dojcinovski.milan (at) gmail.com> 
 Twitter: @m1ci 
 www: http://dojchinovski.mk 
 */
public class TextProcessor {
    
    private static TextProcessor txtProcessor = null;
    
    public TextProcessor(){};
    
    public static TextProcessor getInstance() {
        
        if(txtProcessor == null) {
            txtProcessor = new TextProcessor();
        }
        return txtProcessor;
    }
    
//    public ArrayList<Hypernym> processText_MT(String query, String lang, String entity_type, String knowledge_base, String[] provenance, boolean priorityEntityLinking, String typesFilter){
//        
//        ArrayList<Hypernym> result = new ArrayList<Hypernym>();
//        THDWorker worker = null;
//        
//        try {
//            worker = THDController.getInstance().getTHDWorker();
//            result = worker.processText_MT(query, lang, entity_type, knowledge_base, provenance, priorityEntityLinking, typesFilter);
//        } catch (InterruptedException ex) {
//            Logger.getLogger(TextProcessor.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (MalformedURLException ex) {
//            Logger.getLogger(TextProcessor.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (GateException ex) {        
//            Logger.getLogger(TextProcessor.class.getName()).log(Level.SEVERE, null, ex);
//        } finally {
//            if(worker != null){
//                THDController.getInstance().returnWorker(worker);            
//            }
//            return result;        
//        }
//    }

    public ArrayList<Entity> processTextAPI_MT(String query, String lang, String entity_type, String knowledge_base, String[] provenance, boolean priorityEntityLinking, String typesFilter, String spottingMethod, String linkingMethod, String tmpPar) {
        
        ArrayList<Entity> result = new ArrayList<Entity>();
        THDWorker worker = null;
        try {           
            worker = THDController.getInstance().getTHDWorker();
            result = worker.processTextAPI_MT(query, lang, entity_type, knowledge_base, provenance, priorityEntityLinking, typesFilter, spottingMethod, linkingMethod, tmpPar);
        } catch (InterruptedException ex) {
            Logger.getLogger(TextProcessor.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (MalformedURLException ex) {
            Logger.getLogger(TextProcessor.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (GateException ex) {
            Logger.getLogger(TextProcessor.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } finally {
            if(worker != null) {
                THDController.getInstance().returnWorker(worker);
            }
            return result;        
        }
    }
    
    public ArrayList<Entity> classifyEntityAPI_MT(String entityURI, String lang, String knowledge_base, String[] provenance, String typesFilter) {
        ArrayList<Entity> result = new ArrayList<Entity>();
        THDWorker worker = null;
        try {           
            worker = THDController.getInstance().getTHDWorker();
            result = worker.classifyEntityAPI_MT(entityURI, lang, knowledge_base, provenance, typesFilter);
        } catch (InterruptedException ex) {
            Logger.getLogger(TextProcessor.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (MalformedURLException ex) {
            Logger.getLogger(TextProcessor.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (GateException ex) {
            Logger.getLogger(TextProcessor.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } finally {
            if(worker != null) {
                THDController.getInstance().returnWorker(worker);
            }
            return result;        
        }
    }    
}
