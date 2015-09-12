package ff.pinneberg.gluonconfig.app;

/**
 * Created by xilent on 12.09.15.
 */
public class ConversionHelper {

    public static double dbmtomw(int dbm){
        return Math.pow(10,(dbm/10));
    }

    public static double mwtodbm(double mw){
        return 10* Math.log10(mw);
    }

    public static int dmtomw_int(int dbm){
        return (int) dbmtomw(dbm);
    }

    public static int mwtodbm_int(int mw){
        return (int) mwtodbm((double) mw);
    }
}
