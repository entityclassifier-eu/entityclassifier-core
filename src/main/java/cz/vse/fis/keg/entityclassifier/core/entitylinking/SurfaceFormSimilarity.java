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

import java.util.HashMap;

/**
 * @author Milan Dojchinovski
 * <milan (at) dojchinovski (dot) mk>
 * Twitter: @m1ci
 * www: http://dojchinovski.mk
 */
public class SurfaceFormSimilarity {
    
    private static SurfaceFormSimilarity wikiSearch = null;
    
    /**
     * @return the wikiSearch
     */
    public static SurfaceFormSimilarity getWikiSearch() {
        return wikiSearch;
    }

    /**
     * @param aWikiSearch the wikiSearch to set
     */
    public static void setWikiSearch(SurfaceFormSimilarity aWikiSearch) {
        wikiSearch = aWikiSearch;
    }

    public static SurfaceFormSimilarity getInstance(){
        if(getWikiSearch() == null){
            setWikiSearch(new SurfaceFormSimilarity());
        }
        return getWikiSearch();
    }
        
    public LinkedEntity findWikipediaArticle(String query, String lang, String kb){
        
        LinkedEntity linkedEntity = null;
        
        LinkedEntity linkedEntityLucene = LuceneSearch.getInstance().findWikipediaArticleSkipDisambiguationPage(query, lang, "local");
        LinkedEntity linkedEntitySFI = SFISearch.getInstance().findWikipediaArticle(query, lang);
        LinkedEntity linkedEntityWikipediaSearch = WikipediaSearch.getInstance().findWikipediaArticle(query, lang, kb);
        
        String winner = null;
        float maxSimScore = 0;
        double confidence = 0;
        
        HashMap<String,Double> hm = new HashMap();
        
        if(linkedEntityLucene != null) {            
            org.apache.lucene.search.spell.JaroWinklerDistance jaroWinkler = new org.apache.lucene.search.spell.JaroWinklerDistance();
            String pageTitle = linkedEntityLucene.getPageTitle();
            float dist = jaroWinkler.getDistance(query, pageTitle);
            if(dist > maxSimScore) {
                maxSimScore = dist;
                winner = pageTitle;
                confidence = linkedEntityLucene.getConfidence();
            }                
        }
        
        if(linkedEntitySFI != null) {            
            org.apache.lucene.search.spell.JaroWinklerDistance jaroWinkler = new org.apache.lucene.search.spell.JaroWinklerDistance();
            String pageTitle = linkedEntitySFI.getPageTitle();
            float dist = jaroWinkler.getDistance(query, pageTitle);
            if(dist > maxSimScore) {
                maxSimScore = dist;
                winner = pageTitle;
                confidence = linkedEntitySFI.getConfidence();
            }            
        }
        
        if(linkedEntityWikipediaSearch != null) {            
            org.apache.lucene.search.spell.JaroWinklerDistance jaroWinkler = new org.apache.lucene.search.spell.JaroWinklerDistance();
            String pageTitle = linkedEntityWikipediaSearch.getPageTitle();
            float dist = jaroWinkler.getDistance(query, pageTitle);
            if(dist > maxSimScore) {
                maxSimScore = dist;
                winner = pageTitle;
                confidence = linkedEntityWikipediaSearch.getConfidence();
            }            
        }
        
        if(winner != null) {
            linkedEntity = new LinkedEntity();
            linkedEntity.setPageTitle(winner);
            linkedEntity.setConfidence(confidence);
        }

        return linkedEntity;
    }
}
