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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import cz.vse.fis.keg.entityclassifier.core.conf.Settings;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Milan Dojchinovski
 * <milan (at) dojchinovski (dot) mk>
 * Twitter: @m1ci
 * www: http://dojchinovski.mk
 */
public class LuceneSearch {
    
    private static LuceneSearch wikiSearch = null;

    /**
     * @return the wikiSearch
     */
    public static LuceneSearch getWikiSearch() {
        return wikiSearch;
    }

    /**
     * @param aWikiSearch the wikiSearch to set
     */
    public static void setWikiSearch(LuceneSearch aWikiSearch) {
        wikiSearch = aWikiSearch;
    }

    public static LuceneSearch getInstance(){
        if(getWikiSearch() == null){
            setWikiSearch(new LuceneSearch());
        }
        return getWikiSearch();
    }
    
    public LinkedEntity findWikipediaArticleSkipDisambiguationPage(String query, String lang, String kb){
        URL url = null;
        LinkedEntity linkedEntity = null;
        try {
            String path = "";
            query = URLEncoder.encode(query, "UTF-8");
            switch(lang){
                case "en" :
                    path = Settings.EN_LUCENE+query+"?limit=5";
                    break;
                case "de" : 
                    path = Settings.DE_LUCENE+query+"?limit=5";
                    break;
                case "nl" : 
                    path = Settings.NL_LUCENE+query+"?limit=5";
                    break;
                    
            }
            path = path.replace("+", "%20");
            url = new URL(path);
            
            URLConnection connection;
            connection = url.openConnection();
            connection.setConnectTimeout(60000);
            connection.setReadTimeout(60000);
            
            InputStreamReader ins = new InputStreamReader(connection.getInputStream());
            BufferedReader in = new BufferedReader(ins);
            String inputLine;
            int counter = 1;
            
            while ((inputLine = in.readLine()) != null){
                if(!inputLine.startsWith("#") && counter != 1 ) {
                    String title = inputLine.substring(inputLine.lastIndexOf(" ")+1);
                    String dbpediaURI = "";
                    switch (lang) {
                        case "en": 
                            dbpediaURI = "http://dbpedia.org/resource/" + title;
                            break;
                        case "de": 
                            dbpediaURI = "http://de.dbpedia.org/resource/" + title;
                            break;
                        case "nl": 
                            dbpediaURI = "http://nl.dbpedia.org/resource/" + title;
                            break;
                        // Should never fall in the default case.
                        default:
                            dbpediaURI = "http://dbpedia.org/resource/" + title;
                            
                    }
                                
                    if(!DisambiguationPageValidator.getInstance().isDisambiguationResource(dbpediaURI)) {
                        double confidence = Double.parseDouble(inputLine.substring(0,inputLine.indexOf(" ")))/200000;

                        if(confidence>1.0) {
                            confidence = 1.0;
                        }
                        linkedEntity = new LinkedEntity();
                        linkedEntity.setPageTitle(URLDecoder.decode(title.replaceAll("_", " "), "UTF-8"));
                        linkedEntity.setConfidence(confidence);
                        break;
                    }
                }else{
                    counter++;
                }
            }
            in.close();
            ins.close();

            return linkedEntity;
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(LuceneSearch.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MalformedURLException ex) {
            Logger.getLogger(LuceneSearch.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(LuceneSearch.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public LinkedEntity findWikipediaArticle(String query, String lang, String kb){
        URL url = null;
        LinkedEntity linkedEntity = null;
        try {
            String path = "";
            query = URLEncoder.encode(query, "UTF-8");
            switch(lang){
                case "en" :
                    path = Settings.EN_LUCENE+query+"?limit=1";
                    break;
                case "de" : 
                    path = Settings.DE_LUCENE+query+"?limit=1";
                    break;
                case "nl" : 
                    path = Settings.NL_LUCENE+query+"?limit=1";
                    break;
                    
            }
            path = path.replace("+", "%20");
            url = new URL(path);
            
            URLConnection connection;
            connection = url.openConnection();
            connection.setConnectTimeout(60000);
            connection.setReadTimeout(60000);
            
            InputStreamReader ins = new InputStreamReader(connection.getInputStream());
            BufferedReader in = new BufferedReader(ins);
            String inputLine;
            int counter = 1;
            
            while ((inputLine = in.readLine()) != null){
                if(!inputLine.startsWith("#") && counter != 1 ) {
                    String title = inputLine.substring(inputLine.lastIndexOf(" ")+1);
                    double confidence = Double.parseDouble(inputLine.substring(0,inputLine.indexOf(" ")))/200000;
                    
                    if(confidence>1.0) {
                        confidence = 1.0;
                    }
                        
                    if(confidence > 1) {
                    }
                    linkedEntity = new LinkedEntity();
                    linkedEntity.setPageTitle(URLDecoder.decode(title.replaceAll("_", " "), "UTF-8"));
                    linkedEntity.setConfidence(confidence);
                    
                }else{
                    counter++;
                }
            }
            in.close();
            ins.close();

            return linkedEntity;
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(LuceneSearch.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MalformedURLException ex) {
            Logger.getLogger(LuceneSearch.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(LuceneSearch.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
}
