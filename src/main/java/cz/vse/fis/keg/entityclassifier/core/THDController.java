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

import cz.vse.fis.keg.entityclassifier.core.conf.Settings;
import gate.CreoleRegister;
import gate.Gate;
import gate.util.GateException;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
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
            Settings.getInstance();
            initGATE();
            initPool();
            poolInitialized = true;
        }
        return pool.take();
    }
    
    private void initPool() {
        pool = new LinkedBlockingQueue<THDWorker>();
        for(int i = 0; i < Settings.POOL_SIZE; i++) {
            pool.add(new THDWorker());
            Logger.getLogger(THDController.class.getName()).log(Level.INFO, "Spawned Entityclassifier.eu worker instance #" + pool.size());
        }
        Logger.getLogger(THDController.class.getName()).log(Level.INFO, "Spawned " + Settings.POOL_SIZE + " Entityclassifier.eu worker instances.");
    }

    private void initGATE() throws MalformedURLException, GateException {
        System.out.println("here 111111");
        File gateHomeFile = new File(Settings.GATE_HOME);
        System.out.println("here 111");
        Gate.setGateHome(gateHomeFile);
        System.out.println("here 11");
        File pluginsHome = new File(Settings.PLUGINS_HOME);
        System.out.println("here 2");
        Gate.setPluginsHome(pluginsHome);
        System.out.println("here 3");
        Gate.setUserConfigFile(new File(Settings.GATE_HOME, "user-gate.xml"));
        System.out.println("here 4");
        URL annieHome = null;
        URL taggerHome = null;
        annieHome = new File(pluginsHome, "ANNIE").toURL();
        System.out.println("here 5");
        taggerHome = new File(pluginsHome, "Tagger_Framework").toURL();
        System.out.println("here 6");
        Gate.init();
        System.out.println("here 7");
        CreoleRegister register = Gate.getCreoleRegister();
        System.out.println("here 8");
        register.registerDirectories(annieHome);
        System.out.println("here 9");
        register.registerDirectories(taggerHome);
        System.out.println("here 10");
        Logger.getLogger(THDController.class.getName()).log(Level.INFO, "Entityclassifier.eu was initialized successfully.");            
    }

    void returnWorker(THDWorker worker) {
        pool.add(worker);
    }

    public String getNumberOfFreeWorkers() {
        return pool.size()+"";
    }
}
