/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clientserver;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author duarteduarte
 */
public class PropertiesHandler {
    
    Properties prop = null;
    
    public PropertiesHandler(){
        try {
            prop = new Properties();
            InputStream inputStream = PropertiesHandler.class.getClassLoader().getResourceAsStream("config.properties");
            if (inputStream!=null){
                prop.load(inputStream);
            }
        } catch (IOException ex) {
            Logger.getLogger(PropertiesHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public Properties getProperties(){
        return this.prop;
    }
    
}
