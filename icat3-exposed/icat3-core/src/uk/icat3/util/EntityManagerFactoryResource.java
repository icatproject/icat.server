/*
 * EntityManagerResource.java
 *
 * Created on 21 November 2006, 09:10
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.util;

import javax.persistence.EntityManagerFactory;

/**
 *
 * @author gjd37
 */

public class EntityManagerFactoryResource extends ThreadLocalResource<EntityManagerFactory> {
    
    private static EntityManagerResource instance = null;
    
    public static EntityManagerResource getInstance( ) {
        if( instance == null ) {
            synchronized( EntityManagerResource.class ) {
                if( instance == null ) {
                    instance = new EntityManagerResource( );
                }
            }
        }
        return instance;
    }
    
    protected EntityManagerFactoryResource() {
    }
}



