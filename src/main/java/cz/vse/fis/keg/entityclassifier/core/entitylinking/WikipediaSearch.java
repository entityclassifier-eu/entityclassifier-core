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
    
    public LinkedEntity findWikipediaArticle(String query, String lang, String kb){
        
        URL url = null;
        LinkedEntity linkedEntity = null;
       
        try {
            query = URLEncoder.encode(query, "UTF-8");
            
            if(lang.equals("en") && kb.equals("local")){
//                new url: returns better results for queries such as Martena Museum
                url = new URL(Settings.EN_WIKIPEDIA_LOCAL_API + "?action=query&format=xml&list=search&srlimit=1&srsearch="+query);
//                old url
//                url = new URL(Settings.EN_WIKIPEDIA_LOCAL_API + "?action=query&list=search&srwhat=nearmatch&srlimit=1&srsearch="+query+"&titles="+query+"&redirects&format=xml");

            } else if(lang.equals("de") && kb.equals("local")) {
//                new url: returns better results for queries such as Martena Museum
                url = new URL(Settings.DE_WIKIPEDIA_LOCAL_API + "?action=query&format=xml&list=search&srlimit=1&srsearch="+query);
//                old url
//                url = new URL(Settings.DE_WIKIPEDIA_LOCAL_API + "?action=query&list=search&srwhat=nearmatch&srlimit=1&srsearch="+query+"&titles="+query+"&redirects&format=xml");
            
            } else if(lang.equals("nl") && kb.equals("local")){
//                new url: returns better results for queries such as Martena Museum
                url = new URL(Settings.NL_WIKIPEDIA_LOCAL_API + "?action=query&format=xml&list=search&srlimit=1&srsearch="+query);
//                old url
//                url = new URL(Settings.NL_WIKIPEDIA_LOCAL_API + "?action=query&list=search&srwhat=nearmatch&srlimit=1&srsearch="+query+"&titles="+query+"&redirects&format=xml");
            
            } else if(lang.equals("en") && kb.equals("live")){
//                new url: returns better results for queries such as Martena Museum
                url = new URL(Settings.EN_WIKIPEDIA_LIVE_API + "?action=query&format=xml&list=search&srlimit=1&srsearch="+query);
//                old url
//                url = new URL(Settings.EN_WIKIPEDIA_LIVE_API + "?action=query&list=search&srwhat=nearmatch&srlimit=1&srsearch="+query+"&titles="+query+"&redirects&format=xml");

            }else if(lang.equals("de") && kb.equals("live")){
//                new url: returns better results for queries such as Martena Museum
                url = new URL(Settings.DE_WIKIPEDIA_LIVE_API + "?action=query&format=xml&list=search&srlimit=1&srsearch="+query);
//                old url
//                url = new URL(Settings.DE_WIKIPEDIA_LIVE_API + "?action=query&list=search&srwhat=nearmatch&srlimit=1&srsearch="+query+"&titles="+query+"&redirects&format=xml");
            
            } else if(lang.equals("nl") && kb.equals("live")){
//                new url: returns better results for queries such as Martena Museum
                url = new URL(Settings.NL_WIKIPEDIA_LIVE_API + "?action=query&format=xml&list=search&srlimit=1&srsearch="+query);
//                old url
//                url = new URL(Settings.NL_WIKIPEDIA_LIVE_API + "?action=query&list=search&srwhat=nearmatch&srlimit=1&srsearch="+query+"&titles="+query+"&redirects&format=xml");
            
            } else {
                return null;
            }
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
            Pattern articleTitle = Pattern.compile("title=\"(.*?)\"", Pattern.DOTALL);
            Matcher titleMatcher = articleTitle.matcher(buffer.toString());
            while (titleMatcher.find()) {
                linkedEntity = new LinkedEntity();
                linkedEntity.setPageTitle(titleMatcher.group(1));
                double conf = -1.0;
                linkedEntity.setConfidence(conf);
            }
            return linkedEntity;
        } catch (IOException ex) {
            Logger.getLogger(WikipediaSearch.class.getName()).log(Level.SEVERE, null, ex);
        }
        return linkedEntity;
    }
}
