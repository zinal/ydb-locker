package tech.ydb.locker;

import java.util.Properties;

/**
 *
 * @author mzinal
 */
public class YdbConfig {

    private String connectionString;
    private YdbAuthMode authMode = YdbAuthMode.NONE;
    private String saKeyFile;
    private String staticLogin;
    private String staticPassword;
    private String tlsCertificateFile;
    private int poolSize = 2 * (1 + Runtime.getRuntime().availableProcessors());

    public YdbConfig() {
    }

    public YdbConfig(Properties props) {
        this(props, "ydb.");
    }

    public YdbConfig(Properties props, String prefix) {
        this.connectionString = props.getProperty(prefix + "url");
        this.authMode = YdbAuthMode.valueOf(props.getProperty(prefix + "auth.mode", "NONE"));
        this.saKeyFile = props.getProperty(prefix + "auth.sakey");
        this.staticLogin = props.getProperty(prefix + "auth.username");
        this.staticPassword = props.getProperty(prefix + "auth.password");
        this.tlsCertificateFile = props.getProperty(prefix + "cafile");
        String spool = props.getProperty(prefix + "poolSize");
        if (spool!=null && spool.length() > 0) {
            poolSize = Integer.parseInt(spool);
        }
    }

    public String getConnectionString() {
        return connectionString;
    }

    public void setConnectionString(String connectionString) {
        this.connectionString = connectionString;
    }

    public YdbAuthMode getAuthMode() {
        return authMode;
    }

    public void setAuthMode(YdbAuthMode authMode) {
        if (authMode==null) {
            authMode = YdbAuthMode.NONE;
        }
        this.authMode = authMode;
    }

    public String getSaKeyFile() {
        return saKeyFile;
    }

    public void setSaKeyFile(String saKeyFile) {
        this.saKeyFile = saKeyFile;
    }

    public String getStaticLogin() {
        return staticLogin;
    }

    public void setStaticLogin(String staticLogin) {
        this.staticLogin = staticLogin;
    }

    public String getStaticPassword() {
        return staticPassword;
    }

    public void setStaticPassword(String staticPassword) {
        this.staticPassword = staticPassword;
    }

    public String getTlsCertificateFile() {
        return tlsCertificateFile;
    }

    public void setTlsCertificateFile(String tlsCertificateFile) {
        this.tlsCertificateFile = tlsCertificateFile;
    }

    public int getPoolSize() {
        return poolSize;
    }

    public void setPoolSize(int poolSize) {
        if (poolSize <=0) {
            poolSize = 2 * (1 + Runtime.getRuntime().availableProcessors());
        }
        this.poolSize = poolSize;
    }

    public static enum YdbAuthMode {

        NONE,
        ENV,
        STATIC,
        METADATA,
        SAKEY

    }
}
