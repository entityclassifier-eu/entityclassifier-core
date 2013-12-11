/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.vse.fis.keg.entityclassifier.core.vao;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;


/**
 *
 * @author Milan Dojchinovski <milan@dojchinovski.,mk>
 */
public class Confidence {
    
    protected String value;
    private String confType;
    private String bounds;

    /**
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * @return the confType
     */
    public String getConfType() {
        return confType;
    }

    /**
     * @param confType the confType to set
     */
    public void setConfType(String confType) {
        this.confType = confType;
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
}
