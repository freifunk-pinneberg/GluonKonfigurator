package ff.pinneberg.gluonconfig.app;


import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import android.os.Handler;
import android.os.Message;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.*;
import java.util.Scanner;

public class Settings extends SettingsActivity {

    public static final int FILE_SELECT_CODE = 201;
    public static final String KEY_PATH = "auth_key";
    SharedPreferences sp;
    ProgressDialog pd;


    final int KeyCopyFinished = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.prefrences);
        initUI();
    }


    private void initUI(){
        sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        final ListPreference authMethod = (ListPreference) findPreference("auth_method");
        final EditTextPreference authPassword = (EditTextPreference) findPreference("auth_password");

        final Preference authKey =  findPreference("auth_key");

        String authMethodSetting = sp.getString("auth_method","");
        if(authMethodSetting.length() < 1){
            authPassword.setEnabled(false);
            authKey.setEnabled(false);
        }else if(authMethodSetting.equals("password")){
            authPassword.setEnabled(true);
            authKey.setEnabled(false);
        }else{
            authKey.setEnabled(true);
            authPassword.setEnabled(false);
        }

        authMethod.setOnPreferenceChangeListener((preference, o) -> {
            final String val = o.toString();
            int index = authMethod.findIndexOfValue(val);
            switch (index) {
                case 0:
                    authPassword.setEnabled(true);
                    authKey.setEnabled(false);
                    break;
                case 1:
                    authKey.setEnabled(true);
                    authPassword.setEnabled(false);
                    break;
                default:
                    break;
            }
            return true;
        });

        authKey.setOnPreferenceClickListener(preference -> {
            String Keypath = sp.getString(KEY_PATH, "");
            if (Keypath.length() > 1) {
                alertDialogalreadyExists(Keypath);

            } else {
                fileSelectIntent();
            }

            return false;
        });
    }

    private void fileSelectIntent(){
        //Workaround for Samsung kitkat devices
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                Intent intent = new Intent();
                intent.setType("file/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, FILE_SELECT_CODE);

            } else {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("file/*");
                startActivityForResult(intent, FILE_SELECT_CODE);
            }
        }catch (ActivityNotFoundException e){
            Core.toastError("There is no application to select a file from sdcard",Settings.this);
        }
    }

    private void alertDialogalreadyExists(final String Keypath){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
               Settings.this,R.style.MyAlertDialogStyle);

        // set title
        alertDialogBuilder.setTitle("Error");

        // set dialog message
        alertDialogBuilder
                .setMessage("You have already copied a key. Should this key be deleted?")
                .setCancelable(false)
                .setPositiveButton("Yes", (dialog, id) -> {
                    // if this button is clicked, close
                    // current activity
                    File Keyfile = new File(Keypath);
                    Keyfile.delete();
                    sp.edit().putString(KEY_PATH,"").apply();
                    fileSelectIntent();
                    dialog.dismiss();

                })
                .setNegativeButton("No", (dialog, id) -> {
                    // if this button is clicked, just close
                    // the dialog box and do nothing
                    dialog.dismiss();
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }


    private void copyKey(final String path){
        pd = new ProgressDialog(Settings.this);
        pd.setMessage("Copy key to internal data partition");
        pd.setCancelable(false);
        pd.setIndeterminate(true);
        pd.show();


        new Thread(() -> {
            copyKeyToInternalStorage(path);
            Message msg = Message.obtain();
            msg.what = KeyCopyFinished;
            handler.sendMessage(msg);
        }).start();



    }

    private Handler handler = new Handler(new Handler.Callback(){
        @Override
        public boolean handleMessage(Message msg) {
            switch(msg.what){
                case KeyCopyFinished:

                    pd.dismiss();
                    return true;

            }
            return false;
        }
    });

    private boolean copyKeyToInternalStorage(String FilePath){
        File sourceFile = new File(FilePath);
        if(sourceFile.exists()){

            try {
                final Scanner scanner = new Scanner(sourceFile);
                boolean noPassword=true;
                while (scanner.hasNextLine()) {
                    final String lineFromFile = scanner.nextLine();
                    if(lineFromFile.contains("ENCRYPTED")) {
                        Core.toastError("This app does not support encrypted SSH Keys at the moment", Settings.this);
                        noPassword = false;
                        break;
                    }
                }
                if(noPassword) {
                    File destinationFile = new File(getFilesDir() + FilePath.substring(FilePath.lastIndexOf("/") + 1));
                    BufferedInputStream is = new BufferedInputStream(new FileInputStream(sourceFile));
                    BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(destinationFile));

                    byte[] buff = new byte[8096];
                    int len;
                    while ((len = is.read(buff)) > 0) {
                        os.write(buff, 0, len);
                    }
                    is.close();
                    os.close();
                    sp.edit().putString(KEY_PATH, destinationFile.getAbsolutePath()).apply();
                }
            }catch (IOException e){
                e.printStackTrace();
                return false;
            }

            return true;
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        switch(requestCode){
            case FILE_SELECT_CODE:
                if(resultCode==RESULT_OK){
                    String FilePath = data.getData().getPath();
                    copyKey(FilePath);
                }else{
                    Toast.makeText(getApplicationContext(),"No file selected",Toast.LENGTH_SHORT).show();
                }
                break;

        }
    }




}
