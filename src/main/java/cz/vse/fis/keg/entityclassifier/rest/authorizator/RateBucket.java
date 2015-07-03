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
package cz.vse.fis.keg.entityclassifier.rest.authorizator;

/**
 *
 * @author Milan Dojchinovski <milan.dojchinovski@fit.cvut.cz>
 * http://dojchinovski.mk
 */
public class RateBucket {
    
    private boolean isKeyValid;
    private boolean isAuthorized;
    private int     intervalLimit;      // X-Rate-Limit-Limit - Num of allowed requests per hour. 
    private int     leftRequests;       // X-Rate-Limit-Remaining - The number of requests remaining in the current rate limit window.
    private int     timeUntilReset;     // X-Rate-Limit-Reset - The time at which the current rate limit window resets.

    
    public RateBucket() {}
    
    public RateBucket(int intervalLimit, int leftRequests, int timeUntilReset, boolean isAuthorized, boolean isKeyValid) {
        this.intervalLimit  = intervalLimit;
        this.leftRequests   = leftRequests;
        this.timeUntilReset = timeUntilReset;
        this.isAuthorized   = isAuthorized;
        this.isKeyValid     = isKeyValid;
    }
    
    /**
     * @return the intervalLimit
     */
    public int getIntervalLimit() {
        return intervalLimit;
    }

    /**
     * @param intervalLimit the intervalLimit to set
     */
    public void setIntervalLimit(int intervalLimit) {
        this.intervalLimit = intervalLimit;
    }

    /**
     * @return the leftRequests
     */
    public int getLeftRequests() {
        return leftRequests;
    }

    /**
     * @param leftRequests the leftRequests to set
     */
    public void setLeftRequests(int leftRequests) {
        this.leftRequests = leftRequests;
    }

    /**
     * @return the timeUntilReset
     */
    public int getTimeUntilReset() {
        return timeUntilReset;
    }

    /**
     * @param timeUntilReset the timeUntilReset to set
     */
    public void setTimeUntilReset(int timeUntilReset) {
        this.timeUntilReset = timeUntilReset;
    }

    /**
     * @return the isAuthorized
     */
    public boolean isIsAuthorized() {
        return isAuthorized;
    }

    /**
     * @param isAuthorized the isAuthorized to set
     */
    public void setIsAuthorized(boolean isAuthorized) {
        this.isAuthorized = isAuthorized;
    }

    /**
     * @return the isKeyValid
     */
    public boolean getIsKeyValid() {
        return isKeyValid;
    }

    /**
     * @param isKeyValid the isKeyValid to set
     */
    public void setIsKeyValid(boolean isKeyValid) {
        this.isKeyValid = isKeyValid;
    }
   
    
    
}
