/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.vse.fis.keg.entityclassifier.core.vao;

import java.util.ArrayList;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Milan Dojchinovski <milan@dojchinovski.mk>
 */
public class Entity {

    private Long startOffset;
    private Long endOffset;
    private String underlyingString;
    private String entityType;
    
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

}
