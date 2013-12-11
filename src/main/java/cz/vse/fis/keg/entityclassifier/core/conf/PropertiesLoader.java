/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.vse.fis.keg.entityclassifier.core.conf;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author milan
 */
public class PropertiesLoader {
    
    public Properties getProperties() {
        
        try {
            
            Properties prop = new Properties();
//            URL url =  this.getClass().getResource("/resources/settings-prod.ini");
            URL url =  this.getClass().getResource("/resources/settings-dev.ini");
            prop.load(new FileInputStream(new File(url.getFile())));

            return prop;
        
        } catch (IOException ex) {
            Logger.getLogger(PropertiesLoader.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
}
