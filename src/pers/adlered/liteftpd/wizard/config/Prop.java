package pers.adlered.liteftpd.wizard.config;

import pers.adlered.liteftpd.logger.Levels;
import pers.adlered.liteftpd.logger.Logger;
import pers.adlered.liteftpd.logger.Types;
import pers.adlered.liteftpd.variable.Variable;

import java.io.*;
import java.lang.reflect.Field;
import java.util.Properties;
import java.util.Set;

/**
 * <h3>LiteFTPD-UNIX</h3>
 * <p>配置文件读写操作类</p>
 *
 * @author : https://github.com/AdlerED
 * @date : 2019-10-03 23:45
 **/
public class Prop {
    private static Properties properties = new Properties();

    private static Prop prop = null;

    private Prop() {
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader("config.prop"));
            properties.load(bufferedReader);
            Logger.log(Types.SYS, Levels.INFO, "Profile \"config.prop\" loaded successfully.");
        } catch (FileNotFoundException FNFE) {
            Logger.log(Types.SYS, Levels.WARN, "Cannot found properties file \"config.prop\" at the root path, re-generating default...");
            try {
                File file = new File("config.prop");
                file.createNewFile();
                // Set default props
                addAnnotation("# ================================================================================================")
                        .addAnnotation("# ")
                        .addAnnotation("# >>> LiteFTPD-UNIX Configure File")
                        .addAnnotation("# ")
                        .addAnnotation("# >> debugLevel")
                        .addAnnotation("#     Too high level can affect performance!")
                        .addAnnotation("#       0: NONE;")
                        .addAnnotation("#       1: INFO;")
                        .addAnnotation("#       2: WARN && INFO;")
                        .addAnnotation("#       3: ERROR && WARN && INFO;")
                        .addAnnotation("#       4: DEBUG && ERROR && WARN && INFO;")
                        .addAnnotation("# >> maxUserLimit")
                        .addAnnotation("#     Set to 0, will be ignore the limit. Too small value may make multi-thread ftp client not working")
                        .addAnnotation("# >> timeout")
                        .addAnnotation("#     Timeout in second.")
                        .addAnnotation("# >> maxTimeout")
                        .addAnnotation("#     On mode timeout when client is on passive or initiative mode. (default: 21600 sec = 6 hrs)")
                        .addAnnotation("# >> smartEncode")
                        .addAnnotation("#     Smart choose transmission encode.")
                        .addAnnotation("# >> defaultEncode")
                        .addAnnotation("#     Set the default translating encode. Unix is UTF-8, Windows is GB2312.")
                        .addAnnotation("# >> port")
                        .addAnnotation("#     FTP Server listening tcp port.")
                        .addAnnotation("# >> welcomeMessage")
                        .addAnnotation("#     Customize welcome message when user visited.")
                        .addAnnotation("# >> minPort && maxPort")
                        .addAnnotation("#     Appoint passive mode port range.")
                        .addAnnotation("#       Recommend 100+ ports in the range to make sure generation have high-performance!")
                        .addAnnotation("# >> user")
                        .addAnnotation("#     Multi users. Format:")
                        .addAnnotation("#       user=[username];[password];[permission];[permitDir];[defaultDir]")
                        .addAnnotation("#       username: User's login name.")
                        .addAnnotation("#       password: User's password.")
                        .addAnnotation("#       permission:")
                        .addAnnotation("#       r = read")
                        .addAnnotation("#       w = write")
                        .addAnnotation("#       d = delete")
                        .addAnnotation("#       c = create")
                        .addAnnotation("#       m = move")
                        .addAnnotation("#       Example: rw means read and write permission.")
                        .addAnnotation("#       permitDir: Set dir that user can access.")
                        .addAnnotation("#       Example: \"/\" means user can access the hole disk; \"/home\" means user can access folder/subFolder/files under \"/home\" directory.")
                        .addAnnotation("#       defaultDir: The default dir will be re-directed when user logged.")
                        .addAnnotation("# >> ipOnlineLimit")
                        .addAnnotation("#     Max connections limit for specify IP Address.")
                        .addAnnotation("#       ipOnlineLimit=[IP];[Limit];[IP];[Limit]; ...")
                        .addAnnotation("#       If you defined IP Address as \"0.0.0.0\", priority will be given to limiting the number of connections per IP address to a specified number (Except for IP Address that have been set up separately).")
                        .addAnnotation("#       \"x\" means \"All\". If you defined \"192.168.x.x\",  that connections from \"192.168.1-255.1-255\" all will be refused.")
                        .addAnnotation("#       BlackList for Ip Address? Set limit to \"0\"!")
                        .addAnnotation("# >> userOnlineLimit")
                        .addAnnotation("#     Max connections limit for specify User.")
                        .addAnnotation("#       userOnlineLimit=[username];[Limit];[username];[Limit]; ...")
                        .addAnnotation("#       If you defined User as \"%\", priority will be given to limiting the number of connections per User to a specified number (Except for users that have been set up separately).")
                        .addAnnotation("#       BlackList for User? Set limit to \"0\"!")
                        .addAnnotation("# ")
                        .addAnnotation("# ================================================================================================")
                        .addAnnotation("# =                                          ↓ CONFIG ↓                                          =")
                        .addAnnotation("# ================================================================================================")
                        .addAnnotation("# ");
                addProperty("user", "anonymous;;r;/;/;admin;123456;r;/;/root;")
                        .addProperty("ipOnlineLimit", "127.x.x.x;100;192.168.1.x;100;0.0.0.0;100;")
                        .addProperty("userOnlineLimit", "anonymous;2;admin;100;%;100;")
                        .addProperty("debugLevel", "3")
                        .addProperty("maxUserLimit", "100")
                        .addProperty("timeout", "100")
                        .addProperty("maxTimeout", "21600")
                        .addProperty("smartEncode", "true")
                        .addProperty("defaultEncode", "UTF-8")
                        .addProperty("port", "21")
                        .addProperty("welcomeMessage", "オレは ルフィ、海賊王になる男だ")
                        .addProperty("minPort", "10240")
                        .addProperty("maxPort", "20480");
                BufferedReader bufferedReader = new BufferedReader(new FileReader("config.prop"));
                properties.load(bufferedReader);
            } catch (IOException IOE) {
                IOE.printStackTrace();
            }
        } catch (IOException IOE) {
            IOE.printStackTrace();
        }
        // 反射并应用配置
        try {
            Class clazz = Variable.class;
            Set<Object> keys = properties.keySet();
            for (Object key : keys) {
                Field field = clazz.getDeclaredField(key.toString());
                switch (field.getType().toString()) {
                    case "int":
                        field.set(clazz, Integer.parseInt(getProperty(key.toString())));
                        break;
                    case "long":
                        field.set(clazz, Long.parseLong(getProperty(key.toString())));
                        break;
                    case "boolean":
                        field.set(clazz, Boolean.parseBoolean(getProperty(key.toString())));
                        break;
                    case "class java.lang.String":
                        field.set(clazz, getProperty(key.toString()));
                        break;
                }
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static Prop getInstance() {
        if (prop == null) {
            prop = new Prop();
        }
        return prop;
    }

    private Prop addAnnotation(String annotation) {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(new File("config.prop"), true);
            fileOutputStream.write((annotation + "\n").getBytes());
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (FileNotFoundException FNFE) {
            FNFE.printStackTrace();
        } catch (IOException IOE) {
            IOE.printStackTrace();
        }
        return this;
    }

    private Prop addProperty(String key, String value) {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(new File("config.prop"), true);
            fileOutputStream.write((key + "=" + value + "\n").getBytes());
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (FileNotFoundException FNFE) {
            FNFE.printStackTrace();
        } catch (IOException IOE) {
            IOE.printStackTrace();
        }
        return this;
    }

    private String getProperty(String key) {
        return properties.getProperty(key);
    }
}
