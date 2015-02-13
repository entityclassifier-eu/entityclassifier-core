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

import java.util.ArrayList;

/**
 *
 * @author Milan Dojchinovski <milan@dojchinovski.mk>
 */
public class Entity {

    private Long startOffset;
    private Long endOffset;
    private String underlyingString;
    private String entityType;
    private double linkingConfidence;
    private String entityLink;
            
    private ArrayList<Type> types;
    
    public Entity(){};
    public Entity(String underlyingString, Long startOffset, Long endOffset, String entityType) {
        
        this.underlyingString = underlyingString;
        this.startOffset = startOffset;
        this.endOffset = endOffset;
        this.entityType = entityType;
        this.types = null;
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
     * @return the types
     */
    
    public ArrayList<Type> getTypes() {
        return types;
    }

    /**
     * @param types the types to set
     */
    public void setTypes(ArrayList<Type> types) {
        this.types = types;
    }

    /**
     * @return the label
     */
    public String getUnderlyingString() {
        return underlyingString;
    }

    /**
     * @param label the label to set
     */
    public void setUnderlyingString(String underlyingString) {
        this.underlyingString = underlyingString;
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
     * @return the linkingConfidence
     */
    public double getLinkingConfidence() {
        return linkingConfidence;
    }

    /**
     * @param linkingConfidence the linkingConfidence to set
     */
    public void setLinkingConfidence(double linkingConfidence) {
        this.linkingConfidence = linkingConfidence;
    }

    /**
     * @return the entityLink
     */
    public String getEntityLink() {
        return entityLink;
    }

    /**
     * @param entityLink the entityLink to set
     */
    public void setEntityLink(String entityLink) {
        this.entityLink = entityLink;
    }

}
