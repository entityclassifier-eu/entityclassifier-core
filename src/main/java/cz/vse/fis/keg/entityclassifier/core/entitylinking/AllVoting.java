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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author Milan Dojchinovski
 * <milan (at) dojchinovski (dot) mk>
 * Twitter: @m1ci
 * www: http://dojchinovski.mk
 */
public class AllVoting {
    
    private static AllVoting wikiSearch = null;

    private final double LuceneSearchScore = 0.279;
    private final double SFIScore = 0.390;
    private final double WikipediaSearchScore = 0.287;
    
    /**
     * @return the wikiSearch
     */
    public static AllVoting getWikiSearch() {
        return wikiSearch;
    }

    /**
     * @param aWikiSearch the wikiSearch to set
     */
    public static void setWikiSearch(AllVoting aWikiSearch) {
        wikiSearch = aWikiSearch;
    }

    public static AllVoting getInstance(){
        if(getWikiSearch() == null){
            setWikiSearch(new AllVoting());
        }
        return getWikiSearch();
    }
        
    public LinkedEntity findWikipediaArticle(String query, String lang, String kb){
        
        LinkedEntity linkedEntity = null;
        double confidence = 0;
        
        LinkedEntity linkedEntityLucene = LuceneSearch.getInstance().findWikipediaArticleSkipDisambiguationPage(query, lang, "local");
        LinkedEntity linkedEntitySFI = SFISearch.getInstance().findWikipediaArticle(query, lang);
        LinkedEntity linkedEntityWikipediaSearch = WikipediaSearch.getInstance().findWikipediaArticle(query, lang, kb);        
        
        HashMap<String,Double> hm = new HashMap();
        HashMap<String,Double> hmConf = new HashMap();
        
        if(linkedEntityLucene != null) {
            
            if(hm.containsKey(linkedEntityLucene.getPageTitle())) {
                double currentScore = hm.get(linkedEntityLucene.getPageTitle());
                currentScore += LuceneSearchScore;
                hm.put(linkedEntityLucene.getPageTitle(),currentScore);                
            } else {
                hm.put(linkedEntityLucene.getPageTitle(),LuceneSearchScore);
                hmConf.put(linkedEntityLucene.getPageTitle(), linkedEntityLucene.getConfidence());
            }
        }
        
        if(linkedEntitySFI != null) {            
            if(hm.containsKey(linkedEntitySFI.getPageTitle())) {
                double currentScore = hm.get(linkedEntitySFI.getPageTitle());
                currentScore += SFIScore;
                hm.put(linkedEntitySFI.getPageTitle(),currentScore);
            } else {
                hm.put(linkedEntitySFI.getPageTitle(),SFIScore);
                hmConf.put(linkedEntitySFI.getPageTitle(), linkedEntitySFI.getConfidence());
            }
        }
        
        if(linkedEntityWikipediaSearch != null) {
            if(hm.containsKey(linkedEntityWikipediaSearch.getPageTitle())) {
                double currentScore = hm.get(linkedEntityWikipediaSearch.getPageTitle());
                currentScore += WikipediaSearchScore;
                hm.put(linkedEntityWikipediaSearch.getPageTitle(),currentScore);
            } else {
                hm.put(linkedEntityWikipediaSearch.getPageTitle(),WikipediaSearchScore);
                hmConf.put(linkedEntityWikipediaSearch.getPageTitle(), linkedEntityWikipediaSearch.getConfidence());
            }            
        }
        
        double max = 0;
        String winner = null;
        Iterator<Entry<String,Double>> it = hm.entrySet().iterator();
        while (it.hasNext()) {
            Entry<String,Double> pairs = (Map.Entry)it.next();
            double score = pairs.getValue();
            if(score > max) {
                max = score;
                winner = pairs.getKey();
                confidence = hmConf.get(winner);
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
