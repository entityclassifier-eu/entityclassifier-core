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
package cz.vse.fis.keg.entityclassifier.core.salience;

import java.util.ArrayList;

/**
 *
 * @author Milan Dojchinovski <milan.dojchinovski@fit.cvut.cz>
 * http://dojchinovski.mk
 */
public class SEntity {
    
    private ArrayList<String> urls = new ArrayList<String>();
    private int numOccurrences;
    private int numMentions;
    private String entityType;
    private int beginIndex;
    private double salienceConfidence;
    private double salienceScore;
    private String salienceClass;

    /**
     * @return the urls
     */
    public ArrayList<String> getUrls() {
        return urls;
    }

    /**
     * @param urls the urls to set
     */
    public void setUrls(ArrayList<String> urls) {
        this.urls = urls;
    }

    /**
     * @return the numOccurrences
     */
    public int getNumOccurrences() {
        return numOccurrences;
    }

    /**
     * @param numOccurrences the numOccurrences to set
     */
    public void setNumOccurrences(int numOccurrences) {
        this.numOccurrences = numOccurrences;
    }

    /**
     * @return the numMentions
     */
    public int getNumMentions() {
        return numMentions;
    }

    /**
     * @param numMentions the numMentions to set
     */
    public void setNumMentions(int numMentions) {
        this.numMentions = numMentions;
    }

    /**
     * @return the entityType
     */
    public String getEntityType() {
        return entityType;
    }

    /**
     * @param entityType the entityType to set
     */
    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    /**
     * @return the beginIndex
     */
    public int getBeginIndex() {
        return beginIndex;
    }

    /**
     * @param beginIndex the beginIndex to set
     */
    public void setBeginIndex(int beginIndex) {
        this.beginIndex = beginIndex;
    }

    /**
     * @return the salienceConfidence
     */
    public double getSalienceConfidence() {
        return salienceConfidence;
    }

    /**
     * @param salienceConfidence the salienceConfidence to set
     */
    public void setSalienceConfidence(double salienceConfidence) {
        this.salienceConfidence = salienceConfidence;
    }

    /**
     * @return the salienceScore
     */
    public double getSalienceScore() {
        return salienceScore;
    }

    /**
     * @param salienceScore the salienceScore to set
     */
    public void setSalienceScore(double salienceScore) {
        this.salienceScore = salienceScore;
    }

    /**
     * @return the salienceClass
     */
    public String getSalienceClass() {
        return salienceClass;
    }

    /**
     * @param salienceClass the salienceClass to set
     */
    public void setSalienceClass(String salienceClass) {
        this.salienceClass = salienceClass;
    }
}
