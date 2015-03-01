//created by duknust
//find in https://github.com/Duknust

package clientserver;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

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
