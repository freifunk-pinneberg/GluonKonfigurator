package ff.pinneberg.gluonconfig.app;

import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.*;
import android.util.Log;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.IOUtils;
import net.schmizz.sshj.connection.ConnectionException;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.transport.TransportException;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import net.schmizz.sshj.userauth.keyprovider.KeyProvider;

import javax.xml.datatype.DatatypeConfigurationException;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.security.Security;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by xilent on 13.08.15.
 */
public class SSHHelper{
    Context context;
    SSHClient sshClient;

    SharedPreferences sp;

    boolean connectingInProgress = false;

    final int connectingFinished = -1;
    final int errorOccured =-2;


    //Replace Bouncecastle with Spongycastle
    static {
        Security.insertProviderAt(new org.spongycastle.jce.provider.BouncyCastleProvider(), 1);
    }

    public SSHHelper( Context ctx){
        context = ctx;
        sp = PreferenceManager.getDefaultSharedPreferences(context);
    }


    private void connect() {


        connectingInProgress= true;
        sshClient = new SSHClient();
        //Do not verfiy any hosts. Maybe bad
        sshClient.addHostKeyVerifier(new PromiscuousVerifier());
        new Thread(() -> {

            //Needs better error handling
            String username = sp.getString("auth_username","root");

            try {
                String host = sp.getString("auth_host", "");
                if (host.length() < 1) {
                    toastError(Core.getResource().getString(R.string.error_no_host));
                } else {
                    String auth_method = sp.getString("auth_method", "");
                    if (auth_method.length() > 1) {
                        if (auth_method.equals("password")) {
                            String password = sp.getString("auth_password", "");
                            if (password.length() > 1) {
                                sshClient.connect(sp.getString("host", ""));
                                sshClient.authPassword(username, password);

                                Message msg = Message.obtain();
                                msg.what = connectingFinished;
                                handler.sendMessage(msg);
                            }else{
                                toastError(Core.getResource().getString(R.string.error_no_auth_password));
                            }
                        } else if (auth_method.equals("key")) {
                            String keypath = sp.getString(Settings.KEY_PATH, "");
                            if (keypath.length() > 1) {
                                File private_key = new File(keypath);
                                if (private_key.exists()) {
                                    KeyProvider keys = sshClient.loadKeys(private_key.getPath());
                                    sshClient.connect(host);
                                    sshClient.authPublickey(username, keys);

                                    Message msg = Message.obtain();
                                    msg.what = connectingFinished;
                                    handler.sendMessage(msg);
                                }else{
                                    toastError(Core.getResource().getString(R.string.error_key_not_exist));
                                }
                            }else{
                                toastError(Core.getResource().getString(R.string.error_no_auth_key));
                            }
                        }
                    }else{
                        toastError(Core.getResource().getString(R.string.error_no_auth_method));
                    }

                }

            }catch (IOException e){
                connectingInProgress = false;
                Message msg = Message.obtain();
                msg.what = errorOccured;
                msg.obj = e.getMessage();
                handler.sendMessage(msg);
                e.printStackTrace();
            }

        }).start();



    }

    public void disconnect(){
        if(sshClient != null && sshClient.isConnected()){
            try {
                sshClient.disconnect();
            }catch (IOException e){
                e.printStackTrace();
            }

        }
    }


    private void checkSSHClient(){

        if(sshClient == null){
            sshClient = new SSHClient();
        }
        if(!sshClient.isConnected() && !connectingInProgress){
            connect();
        }
    }

    private void toastError(String errorMessage){
        Toast.makeText(context,errorMessage,Toast.LENGTH_SHORT).show();
    }

    private void toastErrorLong(String errorMessage){
        Toast.makeText(context,errorMessage,Toast.LENGTH_LONG).show();
    }

    public String executeCommand(String command){
        checkSSHClient();
        return executeCommandwithoutCorrection(command).replace("\n", "");
    }

    public String executeCommandwithoutCorrection(String command){
        checkSSHClient();
        String returnval = "";
        while(connectingInProgress){
            try {
                Thread.sleep(50);
            }catch (InterruptedException e){
                e.printStackTrace();
            }
        }
        if(sshClient != null && sshClient.isConnected()) {
            try {
                Session session = sshClient.startSession();
                Session.Command cmd = session.exec(command);
                returnval = IOUtils.readFully(cmd.getInputStream()).toString();
                cmd.join();
                session.close();
            } catch (IOException e) {
                Message msg = Message.obtain();
                msg.what = errorOccured;
                msg.obj = e.getMessage();
                handler.sendMessage(msg);
                e.printStackTrace();
            }
        }


        return returnval;
    }

    public void executeCommandThread(final String command){
        checkSSHClient();
        new Thread(() -> {
            executeCommand(command);
        }).start();
    }

    public void setText(final TextView textView, final String command){
        final Handler handler = new Handler();
        checkSSHClient();
        new Thread(() -> {
            final String response = executeCommand(command);

            handler.post(() -> {
                switch (response) {
                    case "1":
                        textView.setText(Core.getResource().getString(R.string.enabled));
                        break;
                    case "0":
                        textView.setText(Core.getResource().getString(R.string.disabled));
                        break;
                    default:
                        if (response.length() < 1) {
                            textView.setText(Core.getResource().getString(R.string.not_available));
                        } else {
                            textView.setText(response);
                        }
                        break;

                }
                /*if (response.equals("1")) {

                } else if (response.equals("0")) {
                    textView.setText(Core.getResource().getString(R.string.disabled));
                } else if (response.length() < 1) {

                }else{
                    textView.setText(response);
                }*/

            });
        }).start();

    }

    public void populateNumberPicker(final NumberPicker numberPicker,final String command, final String currentval){
        final Handler handler = new Handler();
        checkSSHClient();
        new Thread(() -> {

            if(isInteger(currentval)) {
                int iterations = Integer.parseInt(currentval);
                if (iterations < 20) {
                    iterations = 20;
                }
                ArrayList<String> selectable_values = new ArrayList<>();
                for(int i= 1; i<=iterations;i++){
                    selectable_values.add(i +" dBm (" +ConversionHelper.dmtomw_int(i)+ " mW )");
                }

                //final String[] select_vals = removeSpaces(executeCommandwithoutCorrection(command));
                final String[] select_vals = new String[selectable_values.size()];
                selectable_values.toArray(select_vals);



                handler.post(() -> {

                    numberPicker.setDisplayedValues(select_vals);
                    numberPicker.setMinValue(0);
                    numberPicker.setMaxValue(select_vals.length - 1);
                    numberPicker.setValue(Integer.parseInt(currentval));
                });
            }
        }).start();
    }

    private String[] removeSpaces(String input){
        ArrayList<String> lines = new ArrayList<>();
        String[] splitLines = input.split("\n");
        for(String eachline:splitLines){
            String[] splitSpaces = eachline.split("\\s+");
            StringBuilder sb = new StringBuilder();
            for(String eachString:splitSpaces){
                sb.append(eachString);
                sb.append(" ");
            }
            lines.add(sb.toString());
        }
        return lines.toArray(new String[lines.size()]);
    }

    public static boolean isInteger(String s) {
        return isInteger(s,10);
    }

    public static boolean isInteger(String s, int radix) {
        if(s.isEmpty()) return false;
        for(int i = 0; i < s.length(); i++) {
            if(i == 0 && s.charAt(i) == '-') {
                if(s.length() == 1) return false;
                else continue;
            }
            if(Character.digit(s.charAt(i),radix) < 0) return false;
        }
        return true;
    }

    private Handler handler = new Handler(msg -> {
        switch(msg.what){
            case connectingFinished:

                connectingInProgress = false;
                return true;
            case errorOccured:
                toastErrorLong((String) msg.obj);
                break;


        }
        return false;
    });


}
