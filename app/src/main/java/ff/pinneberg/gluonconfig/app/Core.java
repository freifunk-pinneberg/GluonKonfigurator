package ff.pinneberg.gluonconfig.app;

import android.app.Application;
import android.content.Context;
import android.content.res.Resources;
import android.widget.Toast;

/**
 * Created by xilent on 15.08.15.
 */
public class Core extends Application {
    protected static Core instance;

    public static SSHHelper sshHelper;


    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        sshHelper = new SSHHelper(instance);

        //Init Bugreporting

    }

    public static Resources getResource() {
        return instance.getResources();
    }

    public static SSHHelper getSSHHelper(){
        return new SSHHelper(instance);
    }

    public static void toastError(String errorMessage, Context context){
        Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show();
    }
}