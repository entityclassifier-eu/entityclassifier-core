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
            URL url =  this.getClass().getResource("/resources/settings-prod.ini");
//            URL url =  this.getClass().getResource("/resources/settings-dev.ini");
//            URL url =  this.getClass().getResource("/resources/settings-ner-dev.ini");
            prop.load(new FileInputStream(new File(url.getFile())));
            return prop;
        
        } catch (IOException ex) {
            Logger.getLogger(PropertiesLoader.class.getName()).log(Level.INFO, ex.toString());
        }
        return null;
    }
}
