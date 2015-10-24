package ff.pinneberg.gluonconfig.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.widget.*;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.IOUtils;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import net.schmizz.sshj.userauth.keyprovider.KeyProvider;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.security.Security;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

/**
 * Created by xilent on 13.08.15.
 */
public class SSHHelper{
    Context context;
    SSHClient sshClient;

    SharedPreferences sp;

    boolean connectingInProgress = false;
    boolean sshConnectionInUse = false;
    boolean connectionFailed = false;

    final int connectingFinished = -1;
    final int errorOccured =-2;

    final int ErrorSSHPassword= 1;


    //Replace Bouncecastle with Spongycastle
    static {
        Security.insertProviderAt(new org.spongycastle.jce.provider.BouncyCastleProvider(), 1);
    }

    public SSHHelper( Context ctx){
        context = ctx;
        sp = PreferenceManager.getDefaultSharedPreferences(context);
    }


    private void connect(String ipadress) {


        connectingInProgress= true;
        sshClient = new SSHClient();
        //Do not verfiy any hosts. Maybe bad
        sshClient.addHostKeyVerifier(new PromiscuousVerifier());
        new Thread(() -> {

            //Needs better error handling and big rework - looks horrible
            String username = sp.getString("auth_username","root");

            try {
                String auth_method = sp.getString("auth_method", "");
                if (auth_method.length() > 1) {
                    if (auth_method.equals("password")) {
                        String password = sp.getString("auth_password", "");
                        if (password.length() > 1) {
                            sshClient.connect(ipadress);
                            sshClient.authPassword(username, password);

                            Message msg = Message.obtain();
                            msg.what = connectingFinished;
                            handler.sendMessage(msg);
                        } else {
                            toastError(Core.getResource().getString(R.string.error_no_auth_password));
                        }
                    } else if (auth_method.equals("key")) {
                        String keypath = sp.getString(Settings.KEY_PATH, "");
                        if (keypath.length() > 1) {
                            File private_key = new File(keypath);
                            if (private_key.exists()) {
                                if (sp.getString("auth_key_password", "").length() > 2) {

                                    KeyProvider keys = sshClient.loadKeys(private_key.getPath(), sp.getString("auth_key_password", ""));
                                    sshClient.connect(ipadress);
                                    sshClient.authPublickey(username, keys);

                                    Message msg = Message.obtain();
                                    msg.what = connectingFinished;
                                    handler.sendMessage(msg);
                                } else {
                                    final Scanner scanner = new Scanner(private_key);
                                    boolean noPassword = true;
                                    while (scanner.hasNextLine()) {
                                        final String lineFromFile = scanner.nextLine();
                                        if (lineFromFile.contains("ENCRYPTED")) {
                                            Message msg = Message.obtain();
                                            msg.what = ErrorSSHPassword;
                                            handler.sendMessage(msg);
                                            noPassword = false;
                                            break;
                                        }
                                    }

                                    if (noPassword) {
                                        KeyProvider keys = sshClient.loadKeys(private_key.getPath());
                                        sshClient.connect(ipadress);
                                        sshClient.authPublickey(username, keys);

                                        Message msg = Message.obtain();
                                        msg.what = connectingFinished;
                                        handler.sendMessage(msg);
                                    }
                                }
                            } else {
                                toastError(Core.getResource().getString(R.string.error_key_not_exist));
                            }
                        } else {
                            toastError(Core.getResource().getString(R.string.error_no_auth_key));
                        }
                    }
                } else {
                    toastError(Core.getResource().getString(R.string.error_no_auth_method));
                }

            }catch (IOException  e){
                connectingInProgress = false;
                connectionFailed = true;
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


    private void toastError(String errorMessage){
        Toast.makeText(context,errorMessage,Toast.LENGTH_SHORT).show();
    }

    private void toastErrorLong(String errorMessage){
        Toast.makeText(context,errorMessage,Toast.LENGTH_LONG).show();
    }

    public String executeCommand(String command,String ipadress){
       // checkSSHClient();
        return executeCommandwithoutCorrection(command,ipadress).replace("\n", "");
    }

    public String executeCommandwithoutCorrection(String command,String ipadress){
        //checkSSHClient();

        //checking may need to be put in other method
        String returnval = "";
        while(connectingInProgress){
            try {
                Thread.sleep(50);
            }catch (InterruptedException e){
                e.printStackTrace();
            }
        }
        while(sshConnectionInUse){
            try {
                Thread.sleep(50);
            }catch (InterruptedException e){
                e.printStackTrace();
            }
        }
        sshConnectionInUse = true;

        if(sshClient != null){
            if(sshClient.isConnected()){

                try {


                    if(!sshClient.getRemoteAddress().equals(InetAddress.getByName(ipadress))){
                        connect(ipadress);
                        while(connectingInProgress){
                            try {
                                Thread.sleep(50);
                            }catch (InterruptedException e){
                                e.printStackTrace();
                            }
                        }
                    }

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
        }else{
            connect(ipadress);
            while(connectingInProgress){
                try {
                    Thread.sleep(50);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }

            try {
                if(sshClient.isConnected()) {
                    Session session = sshClient.startSession();
                    Session.Command cmd = session.exec(command);
                    returnval = IOUtils.readFully(cmd.getInputStream()).toString();
                    cmd.join();
                    session.close();
                }
            } catch (IOException e) {
                Message msg = Message.obtain();
                msg.what = errorOccured;
                msg.obj = e.getMessage();
                handler.sendMessage(msg);
                e.printStackTrace();
            }

        }

        sshConnectionInUse = false;

        return returnval;
    }

    public void executeCommandThread(final String command,final String ipadress){
      //  checkSSHClient();
        new Thread(() -> {
            executeCommand(command,ipadress);
        }).start();
    }


    public void setText(final TextView textView, final String command,String ipadress,boolean correction){
        final Handler handler = new Handler();

        textView.setText(Core.getResource().getString(R.string.loading));

        //checkSSHClient();
        new Thread(() -> {
            final String response = executeCommand(command,ipadress);

            handler.post(() -> {
                if(correction) {
                    switch (response) {
                        case "1":
                            textView.setText(Core.getResource().getString(R.string.enabled));
                            break;
                        case "0":
                            textView.setText(Core.getResource().getString(R.string.disabled));
                            break;
                        default:
                            if (connectionFailed) {
                                textView.setText(Core.getResource().getString(R.string.not_connected));
                            } else {
                                if (response.length() < 1) {
                                    textView.setText(Core.getResource().getString(R.string.not_available));
                                } else {
                                    textView.setText(response);
                                }
                            }
                            break;

                    }
                }else{
                    textView.setText(response);
                }

            });
        }).start();

    }

    public void populateArrayAdapter(final ListView listView,final ArrayAdapter<String> arrayAdapter,String ipadress,String select_value_ssh){
        final Handler handler = new Handler();

        new Thread(() -> {

            String[] values = executeCommandwithoutCorrection(select_value_ssh,ipadress).split("\n");
            String[] selectable_values = new String[values.length];
            String[] enabled = new String[values.length];

            for(int i=0;i < values.length;i++){
                String[] split = values[i].split("\\.");
                selectable_values[i] = split[0];
                enabled[i] = split[1];
            }

            handler.post(() -> {

                for( int j=0;j<enabled.length;j++) {
                    listView.setItemChecked(j,enabled[j].equals("1"));
                }

                arrayAdapter.addAll(selectable_values);
                //arrayAdapter.sort(String::compareToIgnoreCase);

            });

        }).start();
    }

    public void populateNumberPicker(final NumberPicker numberPicker, final String currentval,String ipadress,String select_value_ssh){
        final Handler handler = new Handler();
       // checkSSHClient();
        new Thread(() -> {

            if(isInteger(currentval)) {
                int iterations = Integer.parseInt(currentval);
                if (iterations < 20) {
                    iterations = 20;
                }
                ArrayList<String> selectable_values = new ArrayList<>();
                for(int i= 0; i<=iterations;i++){
                    selectable_values.add(i +" dBm (" +ConversionHelper.dmtomw_int(i)+ " mW )");
                }

                final String[] select_vals = new String[selectable_values.size()];
                selectable_values.toArray(select_vals);



                handler.post(() -> {

                    EditText numberPickerChild = (EditText) numberPicker.getChildAt(0);
                    numberPickerChild.setFocusable(false);
                    numberPickerChild.setInputType(InputType.TYPE_NULL);

                    numberPicker.setDisplayedValues(select_vals);
                    numberPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
                    numberPicker.setMinValue(0);
                    numberPicker.setMaxValue(select_vals.length -1);
                    numberPicker.setValue(Integer.parseInt(currentval));
                });
            }else{
                String[] select_vals = executeCommandwithoutCorrection(select_value_ssh,ipadress).split("\n");
                ArrayList<String> selectable_values = new ArrayList<String>();
                selectable_values.addAll(Arrays.asList(select_vals));


                handler.post(() -> {

                   /* EditText numberPickerChild = (EditText) numberPicker.getChildAt(0);
                    numberPickerChild.setFocusable(false);
                    numberPickerChild.setInputType(InputType.TYPE_NULL);*/

                    numberPicker.setDisplayedValues(select_vals);
                   // numberPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
                    numberPicker.setMinValue(0);
                    numberPicker.setMaxValue(select_vals.length -1);
                    numberPicker.setValue(selectable_values.indexOf(currentval));

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
            case ErrorSSHPassword:
                toastErrorLong("SSH Key is encrypted, but no password is given");


        }
        return false;
    });


}
