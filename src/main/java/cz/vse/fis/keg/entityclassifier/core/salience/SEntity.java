/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
