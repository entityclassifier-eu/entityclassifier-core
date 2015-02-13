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
package cz.vse.fis.keg.entityclassifier.core.vao;

/**
 *
 * @author Milan Dojčinovski 
 * <dojcinovski.milan (at) gmail.com> 
 * Twitter: @m1ci 
 * www: http://dojchinovski.mk 
 */
public class Hypernym {
    
    private String type;
    private Long startOffset;
    private Long endOffset;
    private String typeURL;
    private String entity;
    private String underlyingEntityText;
    private String entityURL;
    private String origin;
    private String accuracy;
    private String bounds;
    
    @Override
    public Hypernym clone() throws CloneNotSupportedException {
        return (Hypernym)super.clone();
    }
    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * @return the startOffset
     */
    public Long getStartOffset() {
        return startOffset;
    }

    /**
     * @param startOffset the startOffset to set
     */
    public void setStartOffset(Long startOffset) {
        this.startOffset = startOffset;
    }

    /**
     * @return the endOffset
     */
    public Long getEndOffset() {
        return endOffset;
    }

    /**
     * @param endOffset the endOffset to set
     */
    public void setEndOffset(Long endOffset) {
        this.endOffset = endOffset;
    }

    /**
     * @return the typeURL
     */
    public String getTypeURL() {
        return typeURL;
    }

    /**
     * @param typeURL the typeURL to set
     */
    public void setTypeURL(String typeURL) {
        this.typeURL = typeURL;
    }

    /**
     * @return the entity
     */
    public String getEntity() {
        return entity;
    }

    /**
     * @param entity the entity to set
     */
    public void setEntity(String entity) {
        this.entity = entity;
    }

    /**
     * @return the entityURL
     */
    public String getEntityURL() {
        return entityURL;
    }

    /**
     * @param entityURL the entityURL to set
     */
    public void setEntityURL(String entityURL) {
        this.entityURL = entityURL;
    }

    /**
     * @return the origin
     */
    public String getOrigin() {
        return origin;
    }

    /**
     * @param origin the origin to set
     */
    public void setOrigin(String origin) {
        this.origin = origin;
    }

    /**
     * @return the underlyingEntityText
     */
    public String getUnderlyingEntityText() {
        return underlyingEntityText;
    }

    /**
     * @param underlyingEntityText the underlyingEntityText to set
     */
    public void setUnderlyingEntityText(String underlyingEntityText) {
        this.underlyingEntityText = underlyingEntityText;
    }

    /**
     * @return the accuracy
     */
    public String getAccuracy() {
        return accuracy;
    }

    /**
     * @param accuracy the accuracy to set
     */
    public void setAccuracy(String accuracy) {
        this.accuracy = accuracy;
    }

    /**
     * @return the bounds
     */
    public String getBounds() {
        return bounds;
    }

    /**
     * @param bounds the bounds to set
     */
    public void setBounds(String bounds) {
        this.bounds = bounds;
    }
    
    @Override 
    public int hashCode()
    {
        return
                this.getEntityURL().hashCode() +
                this.getTypeURL().hashCode() + 
                this.getOrigin().hashCode();
    }

    @Override
    public boolean equals(Object objc) {

        Hypernym obj = (Hypernym)objc;
        if (
                obj.getTypeURL().equals(this.getTypeURL()) &&
                obj.getEntityURL().equals(this.getEntityURL()) &&
                obj.getOrigin().equals(this.getOrigin())
                ) {
            return true;
        } else {
            return false;
        }
    }

}
