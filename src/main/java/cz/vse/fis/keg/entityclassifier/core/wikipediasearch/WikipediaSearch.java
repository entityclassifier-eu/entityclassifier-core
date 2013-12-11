package cz.vse.fis.keg.entityclassifier.core.wikipediasearch;

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
public class WikipediaSearch {
    
    private static WikipediaSearch wikiSearch = null;

    /**
     * @return the wikiSearch
     */
    public static WikipediaSearch getWikiSearch() {
        return wikiSearch;
    }

    /**
     * @param aWikiSearch the wikiSearch to set
     */
    public static void setWikiSearch(WikipediaSearch aWikiSearch) {
        wikiSearch = aWikiSearch;
    }

    public static WikipediaSearch getInstance(){
        if(getWikiSearch() == null){
            setWikiSearch(new WikipediaSearch());
        }
        return getWikiSearch();
    }
    
    public String findWikipediaArticle(String query, String lang, String kb){
        

//        long estimatedTotal = 0L;
//        long start = System.currentTimeMillis();
        
//        System.out.println("query: " + query);
        URL url = null;
        String result = null;
       
        String query2 = query;
        try {
            query = URLEncoder.encode(query, "UTF-8");
            
            if(lang.equals("en") && kb.equals("local")){
                url = new URL(Settings.EN_WIKIPEDIA_LOCAL_API + "?action=query&list=search&srwhat=nearmatch&srlimit=1&srsearch="+query+"&titles="+query+"&redirects&format=xml");
            
            }else if(lang.equals("de") && kb.equals("local")){
                url = new URL(Settings.DE_WIKIPEDIA_LOCAL_API + "?action=query&list=search&srwhat=nearmatch&srlimit=1&srsearch="+query+"&titles="+query+"&redirects&format=xml");
            
            } else if(lang.equals("nl") && kb.equals("local")){
                url = new URL(Settings.NL_WIKIPEDIA_LOCAL_API + "?action=query&list=search&srwhat=nearmatch&srlimit=1&srsearch="+query+"&titles="+query+"&redirects&format=xml");
            
            } else if(lang.equals("en") && kb.equals("live")){
                url = new URL(Settings.EN_WIKIPEDIA_LIVE_API + "?action=query&list=search&srwhat=nearmatch&srlimit=1&srsearch="+query+"&titles="+query+"&redirects&format=xml");

            }else if(lang.equals("de") && kb.equals("live")){
                url = new URL(Settings.DE_WIKIPEDIA_LIVE_API + "?action=query&list=search&srwhat=nearmatch&srlimit=1&srsearch="+query+"&titles="+query+"&redirects&format=xml");
            
            } else if(lang.equals("nl") && kb.equals("live")){
                url = new URL(Settings.NL_WIKIPEDIA_LIVE_API + "?action=query&list=search&srwhat=nearmatch&srlimit=1&srsearch="+query+"&titles="+query+"&redirects&format=xml");
            
            } else {
                //System.out.println("Not supported language");
                return result;
            }
//            System.out.println(url.toString());
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(WikipediaSearch.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MalformedURLException ex) {
            Logger.getLogger(WikipediaSearch.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            StringBuffer buffer = new StringBuffer();
            URLConnection connection = url.openConnection();
            InputStream is = connection.getInputStream();
            Reader isr = new InputStreamReader(is,"UTF-8");
            Reader in = new BufferedReader(isr);
            int ch;
            
            while ((ch = in.read()) > -1) {
                buffer.append((char) ch);
            }
            in.close();
            isr.close();
            
            Pattern searchElm = Pattern.compile("<search>(.*?)</search>", Pattern.DOTALL);
            Matcher searchElmMatcher = searchElm.matcher(buffer.toString());
            if(!searchElmMatcher.find()){
                return result;
            }
            
            Pattern pagesElm = Pattern.compile("<pages>(.*?)</pages>", Pattern.DOTALL);
            Matcher pageMatcher = pagesElm.matcher(buffer.toString());            
            Pattern titleAttr = Pattern.compile("title=\"(.*?)\"");
            
            if (pageMatcher.find()) {
                String DataElements = pageMatcher.group(1);
                Matcher titleMatcher = titleAttr.matcher(DataElements);
                if (titleMatcher.find()) {
                    result = titleMatcher.group(1);
                } 
            }
            return result;
        } catch (IOException ex) {
            Logger.getLogger(WikipediaSearch.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }
}
