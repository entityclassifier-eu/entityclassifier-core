/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.vse.fis.keg.entityclassifier.core.vao;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Milan Dojchinovski <milan@dojchinovski.mk>
 */
public class Type {
    
    private String typeLabel;
    private String typeURI;
    private String entityURI;
    private String entityLabel;
    private Confidence confidence;
    private String provenance;

    /**
     * @return the typeLabel
     */
    public String getTypeLabel() {
        return typeLabel;
    }

    /**
     * @param typeLabel the typeLabel to set
     */
    public void setTypeLabel(String typeLabel) {
        this.typeLabel = typeLabel;
    }

    /**
     * @return the typeURI
     */
    public String getTypeURI() {
        return typeURI;
    }

    /**
     * @param typeURI the typeURI to set
     */
    public void setTypeURI(String typeURI) {
        this.typeURI = typeURI;
    }

    /**
     * @return the entityURI
     */
    public String getEntityURI() {
        return entityURI;
    }

    /**
     * @param entityURI the entityURI to set
     */
    public void setEntityURI(String entityURI) {
        this.entityURI = entityURI;
    }

    /**
     * @return the provenance
     */
    public String getProvenance() {
        return provenance;
    }

    /**
     * @param provenance the provenance to set
     */
    public void setProvenance(String provenance) {
        this.provenance = provenance;
    }

    /**
     * @return the confidence
     */
    public Confidence getConfidence() {
        return confidence;
    }

    /**
     * @param confidence the confidence to set
     */
 
    public void setConfidence(Confidence confidence) {
        this.confidence = confidence;
    }

    /**
     * @return the entityLabel
     */
    public String getEntityLabel() {
        return entityLabel;
    }

    /**
     * @param entityLabel the entityLabel to set
     */
    public void setEntityLabel(String entityLabel) {
        this.entityLabel = entityLabel;
    }
}
