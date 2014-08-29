package cz.vse.fis.keg.entityclassifier.core.entitylinking;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import cz.vse.fis.keg.entityclassifier.core.conf.Settings;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    
    public LinkedEntity findWikipediaArticle(String query, String lang, String kb){
//        System.out.println("Using lucene");
        URL url = null;
        LinkedEntity linkedEntity = null;
        try {
//            System.out.println("query: " + query);
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
//            String path = "http://ner.vse.cz:8123/search/wikimirror/"+query+"?limit=1";
            path = path.replace("+", "%20");
//            System.out.println(path);
            url = new URL(path);
//            StringBuffer buffer = new StringBuffer();
            
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
//                    System.out.println(inputLine.substring(0,inputLine.indexOf(" ")));
                    double confidence = Double.parseDouble(inputLine.substring(0,inputLine.indexOf(" ")))/200000;
                    
                    if(confidence>1.0) {
                        confidence = 1.0;
                    }
                        
//                    System.out.println(entityQuery);
//                    System.out.println(title);
//                    System.out.println(confidence);                    
//                    e.setConfidence(confidence);
                    if(confidence > 1) {
//                        System.out.println("conf: " + confidence);
                    }
//                    e.setConfidence(0.67);
                    linkedEntity = new LinkedEntity();
//                    System.out.println("http://dbpedia.org/resource/"+title);
                    linkedEntity.setPageTitle(URLDecoder.decode(title.replaceAll("_", " "), "UTF-8"));
                    System.out.println("before: " + title.replaceAll("_", " "));
                    System.out.println("after: " + linkedEntity.getPageTitle());
                    linkedEntity.setConfidence(confidence);
//                    e.setUri("http://dbpedia.org/resource/"+title);
                    
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
