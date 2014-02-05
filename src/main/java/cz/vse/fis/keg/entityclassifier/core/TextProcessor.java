/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.vse.fis.keg.entityclassifier.core;

import cz.vse.fis.keg.entityclassifier.core.vao.Entity;
import cz.vse.fis.keg.entityclassifier.core.vao.Hypernym;
import gate.util.GateException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Milan Dojčinovski 
 * <dojcinovski.milan (at) gmail.com> 
 * Twitter: @m1ci 
 * www: http://dojchinovski.mk 
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
    
    public ArrayList<Hypernym> processText_MT(String query, String lang, String entity_type, String knowledge_base, String[] provenance, boolean priorityEntityLinking){
        
        ArrayList<Hypernym> result = new ArrayList<Hypernym>();
        THDWorker worker = null;
        
        try {
            worker = THDController.getInstance().getTHDWorker();
            result = worker.processText_MT(query, lang, entity_type, knowledge_base, provenance, priorityEntityLinking);
        } catch (InterruptedException ex) {
            Logger.getLogger(TextProcessor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MalformedURLException ex) {
            Logger.getLogger(TextProcessor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (GateException ex) {        
            Logger.getLogger(TextProcessor.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if(worker != null){
                THDController.getInstance().returnWorker(worker);            
            }
            return result;        
        }
    }

    public ArrayList<Entity> processTextAPI_MT(String query, String lang, String entity_type, String knowledge_base, String[] provenance, boolean priorityEntityLinking) {
        
       ArrayList<Entity> result = new ArrayList<Entity>();
       THDWorker worker = null;
       try {           
           worker = THDController.getInstance().getTHDWorker();
           result = worker.processTextAPI_MT(query, lang, entity_type, knowledge_base, provenance, priorityEntityLinking);
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