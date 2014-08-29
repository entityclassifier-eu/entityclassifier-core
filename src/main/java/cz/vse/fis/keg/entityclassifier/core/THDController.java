/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.vse.fis.keg.entityclassifier.core;

import cz.vse.fis.keg.entityclassifier.core.conf.Settings;

import cz.vse.fis.keg.entityclassifier.core.conf.PropertiesLoader;
import cz.vse.fis.keg.entityclassifier.core.ontologymapper.DBpediaOntologyManager;
import cz.vse.fis.keg.entityclassifier.core.ontologymapper.DBpediaOntologyMapper;
import cz.vse.fis.keg.entityclassifier.core.ontologymapper.TypeMapper;
import cz.vse.fis.keg.entityclassifier.core.ontologymapper.YagoOntologyManager;
import gate.CreoleRegister;
import gate.Gate;
import gate.util.GateException;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Milan Dojchinovski
 */
public class THDController {
    
    private static THDController thdController = null;
    private boolean poolInitialized = false;
    private BlockingQueue<THDWorker> pool = null;
    
    public static THDController getInstance(){
        if(thdController == null){
            thdController = new THDController();
        }
        return thdController;
    }
    
    public THDWorker getTHDWorker() throws InterruptedException, MalformedURLException, GateException {
        
        if(!poolInitialized) {
            Logger.getLogger(THDController.class.getName()).log(Level.INFO, "========= Initializing THD =========");            
            loadSettings();
            initGATE();
            initPool();
            poolInitialized = true;
        }
        return pool.take();
    }
    
    private void initPool() {
        pool = new LinkedBlockingQueue<THDWorker>();
        for(int i = 0; i < Settings.POOL_SIZE; i++) {
            Logger.getLogger(THDController.class.getName()).log(Level.INFO, "Spawned thread #" + pool.size());
            pool.add(new THDWorker());
        }
        Logger.getLogger(THDController.class.getName()).log(Level.INFO, "Spawned " + Settings.POOL_SIZE + " threads.");
    }

    private void loadSettings() {
        System.out.println("loading properties...");
        Properties prop = new PropertiesLoader().getProperties();
        readSettings(prop);        
    }
    
    private void readSettings(Properties prop) {
        
        Settings.GATE_HOME = prop.getProperty("gateHome");
        Settings.PLUGINS_HOME = prop.getProperty("pluginHome");
        
        Settings.POOL_SIZE = Integer.parseInt(prop.getProperty("workerPoolSize"));
        
        Settings.EN_ENTITY_EXTRACTION_GRAMMAR = prop.getProperty("enEntityExtractionGrammar");
        Settings.NL_ENTITY_EXTRACTION_GRAMMAR = prop.getProperty("nlEntityExtractionGrammar");
        Settings.DE_ENTITY_EXTRACTION_GRAMMAR = prop.getProperty("deEntityExtractionGrammar");

        Settings.EN_HYPERNYM_EXTRACTION_GRAMMAR = prop.getProperty("enHypernymExtractionGrammar");
        Settings.DE_HYPERNYM_EXTRACTION_GRAMMAR = prop.getProperty("deHypernymExtractionGrammar");
        Settings.NL_HYPERNYM_EXTRACTION_GRAMMAR = prop.getProperty("nlHypernymExtractionGrammar");
        
        Settings.NL_TAGGER_BINARY = prop.getProperty("nlTaggerBinary");
        Settings.DE_TAGGER_BINARY = prop.getProperty("deTaggerBinary");
        
        Settings.EN_WIKIPEDIA_LOCAL_EXPORT = prop.getProperty("en_wikipedia_local_export");
        Settings.DE_WIKIPEDIA_LOCAL_EXPORT = prop.getProperty("de_wikipedia_local_export");
        Settings.NL_WIKIPEDIA_LOCAL_EXPORT = prop.getProperty("nl_wikipedia_local_export");
        
        Settings.EN_WIKIPEDIA_LIVE_EXPORT = prop.getProperty("en_wikipedia_live_export");
        Settings.DE_WIKIPEDIA_LIVE_EXPORT = prop.getProperty("de_wikipedia_live_export");
        Settings.NL_WIKIPEDIA_LIVE_EXPORT = prop.getProperty("nl_wikipedia_live_export");
        
        Settings.EN_WIKIPEDIA_LOCAL_API = prop.getProperty("en_wikipedia_local_api");
        Settings.DE_WIKIPEDIA_LOCAL_API = prop.getProperty("de_wikipedia_local_api");
        Settings.NL_WIKIPEDIA_LOCAL_API = prop.getProperty("nl_wikipedia_local_api");
        
        Settings.EN_WIKIPEDIA_LIVE_API = prop.getProperty("en_wikipedia_live_api");
        Settings.DE_WIKIPEDIA_LIVE_API = prop.getProperty("de_wikipedia_live_api");
        Settings.NL_WIKIPEDIA_LIVE_API = prop.getProperty("nl_wikipedia_live_api");
        
        Settings.EN_LUCENE = prop.getProperty("ENLuceneURL");
        Settings.DE_LUCENE = prop.getProperty("DELuceneURL");
        Settings.NL_LUCENE = prop.getProperty("NLLuceneURL");
        
        Settings.SALIENCE_DATASET = prop.getProperty("salience_dataset");
        
        DBpediaOntologyManager.setDbpediaOntologyFileLocation(prop.getProperty("dbpediaOntologyFileLocation"));
        YagoOntologyManager.setYagoOntologyFileLocation(prop.getProperty("yagoOntologyFileLocation"));
                
//        DBpediaOntologyMapper.setEnMappingsLocation(prop.getProperty("en_inferred_mappings"));
//        DBpediaOntologyMapper.setDeMappingsLocation(prop.getProperty("de_inferred_mappings"));
//        DBpediaOntologyMapper.setNlMappingsLocation(prop.getProperty("nl_inferred_mappings"));
        
        
        TypeMapper.setEn_inferred_mappings(prop.getProperty("en_inferred_mappings"));
        TypeMapper.setDe_inferred_mappings(prop.getProperty("de_inferred_mappings"));
        TypeMapper.setNl_inferred_mappings(prop.getProperty("nl_inferred_mappings"));
        
        TypeMapper.getInstance().init();
        
        
        Logger.getLogger(THDController.class.getName()).log(Level.INFO, "Settings loaded.");            
    }

    private void initGATE() throws MalformedURLException, GateException {
        
        File gateHomeFile = new File(Settings.GATE_HOME);
        Gate.setGateHome(gateHomeFile);
        File pluginsHome = new File(Settings.PLUGINS_HOME);
        Gate.setPluginsHome(pluginsHome);
        Gate.setUserConfigFile(new File(Settings.GATE_HOME, "user-gate.xml"));
        URL annieHome = null;
        URL taggerHome = null;
        annieHome = new File(pluginsHome, "ANNIE").toURL();
        taggerHome = new File(pluginsHome, "Tagger_Framework").toURL();
        Gate.init();
        CreoleRegister register = Gate.getCreoleRegister();
        register.registerDirectories(annieHome);
        register.registerDirectories(taggerHome);
        Logger.getLogger(THDController.class.getName()).log(Level.INFO, "GATE was initialized successfully.");            
    }

    void returnWorker(THDWorker worker) {
        pool.add(worker);
    }

    public String getNumberOfFreeWorkers() {
        return pool.size()+"";
    }
}
