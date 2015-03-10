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

import cz.vse.fis.keg.entityclassifier.core.conf.Settings;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONObject;

/**
 *
 * @author Milan Dojchinovski <milan.dojchinovski@fit.cvut.cz>
 http://dojchinovski.mk
 */
public class SFISearch {
    
    private static SFISearch instance = null;

    public static SFISearch getInstance(){
        if(instance == null){
            instance = new SFISearch();
        }
        return instance;
    }
    
    public LinkedEntity findWikipediaArticle(String mention, String lang) {
        
        LinkedEntity linkedEntity = null;
        OutputStream os = null;
        try {            
            String url = Settings.SEMITAGS_LINKING_ENDPOINT;
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            String data = "lang="+lang+"&surfaceForm="+mention;
            con.setRequestMethod("POST");
            con.setRequestProperty("Accept", "application/json");
            con.setDoOutput(true);
            os = con.getOutputStream();
            os.write(data.getBytes("UTF-8"));
            os.flush();
            os.close();

            int responseCode = con.getResponseCode();
            
            int code = con.getResponseCode();
            
            if(code == 500) {
                return linkedEntity;
            }
            StringBuffer buffer = new StringBuffer();
            InputStream is = con.getInputStream();
            Reader isr = new InputStreamReader(is,"UTF-8");
            Reader in = new BufferedReader(isr);
            int ch;
            while ((ch = in.read()) > -1) {
                buffer.append((char) ch);
            }
            in.close();
            isr.close();
            String result = buffer.toString();            
            
            Object linkObj = null;
            Object scoreObj = null;
            String link = "";
            double score;
            JSONObject jsonObj = new JSONObject(result);
            
            linkObj = jsonObj.get("link");
            scoreObj = jsonObj.get("socre");
            if(!linkObj.toString().equals("null")) {
                link = jsonObj.getString("link");
                score = jsonObj.getDouble("socre");            
                linkedEntity = new LinkedEntity();
                linkedEntity.setPageTitle(link.split("/")[link.split("/").length-1].replace("_", " "));
                linkedEntity.setConfidence(score);
                return linkedEntity;
            } else {
                return null;
            }
        } catch (MalformedURLException ex) {
            Logger.getLogger(SFISearch.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(SFISearch.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return linkedEntity;        
    }
}
