package enginuity.util;

import java.io.File; 
import java.util.StringTokenizer; 

import org.jdesktop.jdic.filetypes.AssociationAlreadyRegisteredException; 
import org.jdesktop.jdic.filetypes.AssociationNotRegisteredException; 
import org.jdesktop.jdic.filetypes.AssociationService; 
import org.jdesktop.jdic.filetypes.Association; 
import org.jdesktop.jdic.filetypes.Action; 
import org.jdesktop.jdic.filetypes.RegisterFailedException; 


public class FileAssociator { 
    
    public static boolean addAssociation(String extension, String command, /*String iconFileName,*/ String description) {
        // Add association 
        // StringTokenizer osName = new StringTokenizer(System.getProperties().getProperty("os.name"));
        
        // remove association if it already exists
        
        System.out.println("Removing 1...\n");
        removeAssociation(extension);
        System.out.println("Removing 2...\n");

        AssociationService serv = new AssociationService(); 
        Association logassoc = new Association(); 

        logassoc.addFileExtension(extension.toUpperCase());    
        logassoc.setDescription(description); 
        logassoc.addAction(new Action("open", command + " %1")); 
        logassoc.setIconFileName(new File("").getAbsolutePath() + "/graphics/enginuity-ico.ico"); 
        
        System.out.println("Adding ...\n" + logassoc + "\n\n\n");

        try {    
            serv.registerUserAssociation(logassoc); 
        } catch (Exception e) {    
            System.err.println(e); 
        }     
        
        return true;
    }
    
    
    public static boolean removeAssociation(String extension) {
        AssociationService serv = new AssociationService(); 
        Association logassoc = serv.getFileExtensionAssociation(extension.toUpperCase()); 
        
        System.out.println("Removing ...\n" + logassoc + "\n\n\n");

        try {    
            serv.unregisterUserAssociation(logassoc); 
        } catch (Exception e) {      
            System.err.println(e); 
        }    
        return true;
}    
}