/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.vse.fis.keg.entityclassifier.core.conf;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
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
//            System.out.println(this.getClass().getName());
//            System.out.println(this.getClass().getCanonicalName());
//            System.out.println(this.getClass().getResource("/resources/settings-dev.ini"));
//            URL url =  this.getClass().getResource("/cz/vse/fis/keg/entityclassifier/core/conf/settings-dev.ini");
//            System.out.println(url.toURI().toString());
//            System.out.println(url.getFile());
//            URL url =  this.getClass().getResource("/Users/Milan/Documents/research/repositories/entityclassifier-core/src/main/java/cz/vse/fis/keg/entityclassifier/core/conf/settings-dev.ini");
//            System.out.println(url.getFile());
            URL url =  this.getClass().getResource("/resources/settings-dev.ini");
//            URL url =  this.getClass().getResource("/resources/settings-ner-dev.ini");
//            System.out.println(url.toString());
            prop.load(new FileInputStream(new File(url.getFile())));

            return prop;
//            prop.load(new FileInputStream(new File("/Users/Milan/Documents/research/repositories/entityclassifier-core/src/main/java/cz/vse/fis/keg/entityclassifier/core/conf/settings-dev.ini")));

//            return prop;
        
        } catch (IOException ex) {
            System.out.println("problem"+ex.toString());
            Logger.getLogger(PropertiesLoader.class.getName()).log(Level.INFO, ex.toString());
//        } catch (URISyntaxException ex) {
//            System.out.println("problem"+ex.toString());
//            Logger.getLogger(PropertiesLoader.class.getName()).log(Level.INFO, null, ex);
        }
        return null;
    }
}
