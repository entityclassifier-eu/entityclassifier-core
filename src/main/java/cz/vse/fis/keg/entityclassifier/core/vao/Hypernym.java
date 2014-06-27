/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.vse.fis.keg.entityclassifier.core.vao;

import java.util.Objects;
import javax.xml.bind.annotation.XmlRootElement;

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
    
//    public int compareTo(Object o) {
//        Hypernym o2 = null;
////        
//        if(o instanceof Hypernym) {
//            o2 = (Hypernym) o;
//        }
//        
//        if(
//                this.entity.equals(o2.entity) && 
//                this.entityURL.equals(o2.entityURL) &&
//                this.type.equals(o2.type) &&
//                this.typeURL.equals(o2.typeURL)
//                ) {
//            return 0;
//        } else {
//            return -1;
//        }
//            
//   }

//    @Override
//    public int compareTo(Hypernym o) {
//        
////        System.out.println("== checking");
////        
////        System.out.println(o.getEntity());
////        System.out.println(o.getEntityURL());
////        System.out.println(o.getType());
////        System.out.println(o.getTypeURL());
////        
////        System.out.println("---");
////        
////        System.out.println(this.getEntity());
////        System.out.println(this.getEntityURL());
////        System.out.println(this.getType());
////        System.out.println(this.getTypeURL());
//        
//        if(this.entity.equals(o.getEntity()) &&
//        this.entityURL.equals(o.getEntityURL()) &&
//        this.type.equals(o.getType()) &&
//        this.typeURL.equals(o.getTypeURL())) {
//            System.out.println("SAME");
//            return 0;
//        } else {
//            return -1;
//        }
//    }
    
//    @Override
//    public boolean equals(Object o)
//    {
//        if(o != null && (o instanceof Hypernym)) {
//            Hypernym o2 = (Hypernym)o;
//        if(this.entity.equals(o2.entity))
//            return -1;
//        if(this.entityURL.equals(o.entityURL))
//            return -1;
//        if(this.type.equals(o.type))
//            return -1;
//        if(this.typeURL.equals(o.typeURL))
//            return -1;
//
//        }
//        return false;
//    }
    @Override 
    public int hashCode()
    {
//        System.out.println("hash");
        return
//                entity.hashCode() +
                this.getEntityURL().hashCode() +
//                type.hashCode() +
                this.getTypeURL().hashCode() + 
                this.getOrigin().hashCode();
    }

    @Override
    public boolean equals(Object objc) {

        Hypernym obj = (Hypernym)objc;
        if (
//                obj.getType().equals(this.getType()) &&
                obj.getTypeURL().equals(this.getTypeURL()) &&
//                obj.getEntity().equals(this.getEntity()) &&
                obj.getEntityURL().equals(this.getEntityURL()) &&
                obj.getOrigin().equals(this.getOrigin())
                ) {
            return true;
        } else {
            return false;
        }
    }
}