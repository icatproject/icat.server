/*
 * ThreadLocalResource.java
 *
 * Created on 21 November 2006, 09:11
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.util;

/**
 *
 * @author gjd37
 */
public abstract class ThreadLocalResource<T> {
    
    private final ThreadLocal<T> resource = new ThreadLocal<T>( );
    
    public void set( T em ) {
        resource.set(em);
    }
    
    public T get( ) {
        T tclass = resource.get();
        if(tclass == null) throw new NullPointerException("Resource not been set in "+this.getClass());
        else return tclass;
    }
}