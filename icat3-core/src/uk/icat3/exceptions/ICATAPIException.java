package uk.icat3.exceptions;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import javax.ejb.ApplicationException;

/*
 * ICATAPIException.java
 *
 * Created on 13 February 2007, 10:20
 *
 * Core ICAT3API Exception class that provides nested exceptions in a string 
 * representation and also generates a unique id for each exception that is 
 * thrown.  All other ICAT3API exceptions should extend this class.
 * 
 * @author df01
 * @version 1.0
 */
//means that the transaction rolls back on this application exception (default is false)
@ApplicationException(rollback=true)
public class ICATAPIException extends java.lang.Exception {
    
    /** A wrapped Throwable */
    protected Throwable cause;
    String uniqueId;
    
    /**
     * Creates a new instance of <code>ICATAPIException</code> without detail message.
     */
    public ICATAPIException() {
        super("Error occurred in application.");
        uniqueId = this.generateExceptionId();
    }
    
    
    /**
     * Constructs an instance of <code>ICATAPIException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public ICATAPIException(String msg) {
        super(msg);
        uniqueId = this.generateExceptionId();
    }
    
    public ICATAPIException(String message, Throwable cause)  {
        super(message);
        this.cause = cause;
        uniqueId = this.generateExceptionId();
    }    

    private String generateExceptionId() {
        String exceptionId = null;
        try {
            exceptionId = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            exceptionId = "....";
        }//end try/catch
        
        exceptionId += System.currentTimeMillis();
        return exceptionId;
    }//end method

    //Created to match the JDK 1.4 Throwable method.
    public Throwable initCause(Throwable cause)  {
         this.cause = cause;
         return cause;
    }

    private static Throwable getNestedException(Throwable parent) {        
        return parent.getCause();        
    }

     public String getMessage() {
         // Get this exception's message.
         String msg = super.getMessage();
  
         Throwable parent = this;
         Throwable child;
  
         // Look for nested exceptions.
         while((child = getNestedException(parent)) != null) {
             // Get the child's message.
             String msg2 = child.getMessage();
  
             // If we found a message for the child exception, 
             // we append it.
             if (msg2 != null) {
                 if (msg != null) {
                     msg += ": " + msg2;
                 } else {
                     msg = msg2;
                 }
             }
  
             // Any nested ApplicationException will append its own
             // children, so we need to break out of here.
             if (child instanceof ICATAPIException) {
                 break;
             }
             parent = child;
         }
  
         // Return the completed message.
         return msg;
     }
       
       public String getStackTraceAsString() {
          StringWriter sw = new StringWriter();
          PrintWriter pw = new PrintWriter(sw, true);
          this.printStackTrace(pw);
          pw.flush();
          sw.flush();
          String errorMsg = "Unique Id: " + uniqueId;
          errorMsg += "\t\n" + sw.toString();
          return errorMsg;
      }


       public void printStackTrace() {
           // Print the stack trace for this exception.
           super.printStackTrace();

           Throwable parent = this;
           Throwable child;

           // Print the stack trace for each nested exception.
           while((child = getNestedException(parent)) != null) {
               if (child != null) {
                   System.err.print("Caused by: ");
                   child.printStackTrace();

                   if (child instanceof ICATAPIException) {
                       break;
                   }
                   parent = child;
               }
           }
       }

       public void printStackTrace(PrintStream s) {
           // Print the stack trace for this exception.
           super.printStackTrace(s);

           Throwable parent = this;
           Throwable child;

           // Print the stack trace for each nested exception.
           while((child = getNestedException(parent)) != null) {
               if (child != null) {
                   s.print("Caused by: ");
                   child.printStackTrace(s);

                   if (child instanceof ICATAPIException) {
                       break;
                   }
                   parent = child;
               }
           }
       }

       public void printStackTrace(PrintWriter w) {
           // Print the stack trace for this exception.
           super.printStackTrace(w);

           Throwable parent = this;
           Throwable child;

           // Print the stack trace for each nested exception.
           while((child = getNestedException(parent)) != null) {
               if (child != null) {
                   w.print("Caused by: ");
                   child.printStackTrace(w);

                   if (child instanceof ICATAPIException) {
                       break;
                   }
                   parent = child;
               }
           }
       }

       public Throwable getCause()  {
           return cause;
       }

  public String getUniqueId() {
    return uniqueId;
  }

  public void setUniqueId(String uniqueId) {
    this.uniqueId = uniqueId;
  }
    
}
