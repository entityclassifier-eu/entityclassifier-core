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
 * @author Milan Dojchinovski <milan@dojchinovski.mk>
 */
public class Type {
    
    private String typeLabel;
    private String typeURI;
    private String entityURI;
    private String entityLabel;
    private Confidence classificationConfidence;
    private Confidence linkingConfidence;
    private String provenance;
    private Salience salience;
    private int specificity;
    
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
     * @return the classificationConfidence
     */
    public Confidence getClassificationConfidence() {
        return classificationConfidence;
    }

    /**
     * @param classificationConfidence the classificationConfidence to set
     */
 
    public void setClassificationConfidence(Confidence classificationConfidence) {
        this.classificationConfidence = classificationConfidence;
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

    /**
     * @return the salience
     */
    public Salience getSalience() {
        return salience;
    }

    /**
     * @param salience the salience to set
     */
    public void setSalience(Salience salience) {
        this.salience = salience;
    }

    /**
     * @return the linkingConfidence
     */
    public Confidence getLinkingConfidence() {
        return linkingConfidence;
    }

    /**
     * @param linkingConfidence the linkingConfidence to set
     */
    public void setLinkingConfidence(Confidence linkingConfidence) {
        this.linkingConfidence = linkingConfidence;
    }

    /**
     * @return the specificity
     */
    public int getSpecificity() {
        return specificity;
    }

    /**
     * @param specificity the specificity to set
     */
    public void setSpecificity(int specificity) {
        this.specificity = specificity;
    }
}
