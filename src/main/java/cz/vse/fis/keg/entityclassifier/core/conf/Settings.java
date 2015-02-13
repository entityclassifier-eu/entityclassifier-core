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
package cz.vse.fis.keg.entityclassifier.core.conf;

/**
 *
 * @author Milan
 */
public class Settings {
    
    public static String GATE_HOME;
    public static String PLUGINS_HOME;
    
    public static String EN_ENTITY_EXTRACTION_GRAMMAR;
    public static String DE_ENTITY_EXTRACTION_GRAMMAR;
    public static String NL_ENTITY_EXTRACTION_GRAMMAR;

    public static String EN_HYPERNYM_EXTRACTION_GRAMMAR;
    public static String DE_HYPERNYM_EXTRACTION_GRAMMAR;
    public static String NL_HYPERNYM_EXTRACTION_GRAMMAR;

    public static String NL_TAGGER_BINARY;
    public static String DE_TAGGER_BINARY;
    
    public static String EN_WIKIPEDIA_LOCAL_EXPORT;
    public static String DE_WIKIPEDIA_LOCAL_EXPORT;
    public static String NL_WIKIPEDIA_LOCAL_EXPORT;

    public static String EN_WIKIPEDIA_LIVE_EXPORT;
    public static String DE_WIKIPEDIA_LIVE_EXPORT;
    public static String NL_WIKIPEDIA_LIVE_EXPORT;
    
    public static String EN_WIKIPEDIA_LOCAL_API;
    public static String DE_WIKIPEDIA_LOCAL_API;
    public static String NL_WIKIPEDIA_LOCAL_API;

    public static String EN_WIKIPEDIA_LIVE_API;
    public static String DE_WIKIPEDIA_LIVE_API;
    public static String NL_WIKIPEDIA_LIVE_API;
    
    public static String EN_LUCENE;
    public static String DE_LUCENE;
    public static String NL_LUCENE;    
    
    public static String SALIENCE_DATASET;
    
    public static String EN_DBPEDIA_DISAMBIGUATION_DATASET;
    public static String DE_DBPEDIA_DISAMBIGUATION_DATASET;
    public static String NL_DBPEDIA_DISAMBIGUATION_DATASET;
    
    public static String SEMITAGS_SPOTTING_ENDPOINT;
    public static String SEMITAGS_LINKING_ENDPOINT;
    
    public static int POOL_SIZE;
}
