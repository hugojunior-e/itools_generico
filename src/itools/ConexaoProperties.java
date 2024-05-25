package itools;

import java.net.URL;
import java.util.Properties;

public class ConexaoProperties {
    public String url;
    public String user;
    public String pass;
    public boolean other;

    public ConexaoProperties(String p_base) throws Exception {
        Properties prop = getProp();
        Class.forName(prop.getProperty("prop." + p_base.toLowerCase() + ".driver"));

        this.url = prop.getProperty("prop." + p_base.toLowerCase());
        this.user = prop.getProperty("prop." + p_base.toLowerCase() + ".usuario");
        this.pass = prop.getProperty("prop." + p_base.toLowerCase() + ".senha");
        this.other = prop.containsKey("prop." + p_base.toLowerCase() + ".other");
    }

    public Properties getProp() {

        Properties props = new Properties();
        try {
            String fileName = UUtils.LOGIN_PROPERTIES;
            System.out.println(fileName);
            URL url = new URL(fileName);
            props.load(url.openStream());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return props;
    }
}
