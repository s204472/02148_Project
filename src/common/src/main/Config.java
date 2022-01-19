package common.src.main;

public class Config {
    public static final int PORT = 9001;
    public static final String HOST = "localhost";

    public static String getURI(String str){
        return "tcp://" + HOST + ":" + PORT + "/" + str + "?conn";
    }
}
