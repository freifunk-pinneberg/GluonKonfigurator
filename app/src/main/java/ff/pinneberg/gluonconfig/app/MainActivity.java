package ff.pinneberg.gluonconfig.app;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.StrictMode;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class MainActivity extends ActionBarActivity {


    //Groups
    List<String> groupHeaders = new ArrayList<String>(){{
        add(Core.getResource().getString(R.string.location));
        add(Core.getResource().getString(R.string.mesh));
        add(Core.getResource().getString(R.string.wifi));
    }};

    //Group Content
    ArrayList<HashMap<String,String >> location = new ArrayList<HashMap<String, String>>(){{
        add(new HashMap<String, String>(){{
               put(KEY_HEADER, Core.getResource().getString(R.string.latitude));
               put(KEY_COMMAND,"gluon-node-info.@location[0].latitude");
        }});
        add(new HashMap<String, String>(){{
            put(KEY_HEADER,Core.getResource().getString(R.string.longitude));
            put(KEY_COMMAND,"gluon-node-info.@location[0].longitude");
        }});
        add(new HashMap<String, String>(){{
            put(KEY_HEADER,Core.getResource().getString(R.string.share_location));
            put(KEY_COMMAND,"gluon-node-info.@location[0].share_location");
        }});

    }};

    ArrayList<HashMap<String,String >> mesh= new ArrayList<HashMap<String, String>>(){{
        add(new HashMap<String, String>(){{
            put(KEY_HEADER, Core.getResource().getString(R.string.mesh_on_WAN));
            put(KEY_COMMAND,"network.mesh_wan.auto");
        }});
        add(new HashMap<String, String>(){{
            put(KEY_HEADER,Core.getResource().getString(R.string.mesh_on_LAN));
            put(KEY_COMMAND,"network.mesh_lan.auto");
            put(KEY_COMMAND2_ENABLE,"network.client.ifname=\"bat0\"");
            put(KEY_COMMAND2_DISABLE,"network.client.ifname=\"bat0 $(cat /lib/gluon/core/sysconfig/lan_ifname)\"");
        }});

    }};

    ArrayList<HashMap<String,String >> wifi= new ArrayList<HashMap<String, String>>(){{
        add(new HashMap<String, String>(){{
            put(KEY_HEADER, Core.getResource().getString(R.string.transmitting_power));
            put(KEY_COMMAND,"wireless.radio0.txpower");
            put(KEY_SELECT_VALUES,"iwinfo client0 txpower");
        }});

    }};


    //SuperList of all Children
    ArrayList<ArrayList<HashMap<String,String>>> superList = new ArrayList<ArrayList<HashMap<String, String>>>(){{
        add(location);
        add(mesh);
        add(wifi);
    }};

    //dBm Conversion Table


    public static String gluon_get = "uci get ";
    public static String gluon_set = "uci set ";

    public static String KEY_HEADER = "header";
    public static String KEY_COMMAND = "command";
    public static String KEY_COMMAND2_ENABLE = "command2_enable";
    public static String KEY_COMMAND2_DISABLE = "command2_disable";
    public static String KEY_SELECT_VALUES = "select_values";
    public static String KEY_VALUE = "value";
    ExpandableListAdapter expandableListAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);
        setContentView(R.layout.activity_main);
        initVariables();
        initUI();
        Intent i = new Intent(MainActivity.this,SSHHelper.class);
        i.putExtra("data", superList);


    }


    /*private void showLoadingAnimation(){
        ProgressDialog dialog = new ProgressDialog(MainActivity.this);
        dialog.setCancelable(true);

        dialog.setTitle("Loading");
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setIndeterminate(true);
        dialog.setIndeterminateDrawable(Core.getResource().getDrawable(R.drawable.clockwise_rotate));
        dialog.show();
    }*/

    private void initUI(){
        final ExpandableListView listView = (ExpandableListView) findViewById(R.id.mainListView);

        expandableListAdapter = new ExpandableListAdapter(getApplicationContext(),groupHeaders,superList);
        listView.setAdapter(expandableListAdapter);
        listView.setOnChildClickListener((parent, v, groupPosition, childPosition, id) -> {

            TextView textField = (TextView) v.findViewById(R.id.list_itemvalue);
            String textvalue = textField.getText().toString();
            HashMap<String, String> info = (HashMap<String, String>) expandableListAdapter.getChild(groupPosition, childPosition);
            if (textvalue.equals(Core.getResource().getString(R.string.enabled))) {
                changeSetting(info.get(KEY_COMMAND2_DISABLE), info.get(KEY_COMMAND) + "=0");

                textField.setText(Core.getResource().getString(R.string.disabled));
            } else if (textvalue.equals(Core.getResource().getString(R.string.disabled))) {
                changeSetting(info.get(KEY_COMMAND2_ENABLE), info.get(KEY_COMMAND) + "=1");
                textField.setText(Core.getResource().getString(R.string.enabled));

            } else if (textvalue.equals(Core.getResource().getString(R.string.not_available))) {
                Core.toastError(Core.getResource().getString(R.string.error_not_available), getApplicationContext());
            } else {
                if (info.containsKey(KEY_SELECT_VALUES)) {
                    editNumberPickerDialog(info, textvalue, textField);
                } else {
                    editDialog(info, textvalue);
                }
            }

            return true;
        });

    }

    private void initVariables(){
        Intent i = new Intent(MainActivity.this,LocationService.class);
        startService(i);

    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent i = new Intent(getApplicationContext(),Settings.class);
            startActivity(i);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void changeSetting(String... commands){

        for(String command:commands) {
            Core.sshHelper.executeCommandThread(gluon_set + command);
        }

    }




    private void editDialog(final HashMap<String,String> info,String curValue){
        // get prompts.xml view
        LayoutInflater layoutInflater = (LayoutInflater) MainActivity.this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View promptsView = layoutInflater.inflate(R.layout.alertdialog_edit,null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                MainActivity.this);

        alertDialogBuilder.setView(promptsView);

        final EditText userInput = (EditText) promptsView.findViewById(R.id.alertEditText);


        final TextView header = (TextView) promptsView.findViewById(R.id.alertTitle);
        header.setText(info.get(KEY_HEADER));
        userInput.setText(curValue);

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton(Core.getResource().getString(R.string.ok),
                        (dialog, id) -> {
                            changeSetting(info.get(KEY_COMMAND),userInput.getText().toString());
                            InputMethodManager imm =
                                    (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                            dialog.dismiss();

                        })
                .setNegativeButton(Core.getResource().getString(R.string.cancel),
                        (dialog, id) -> {
                            InputMethodManager imm =
                                    (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY,0);
                            dialog.dismiss();
                        });

        // create alert dialog
        final AlertDialog alertDialog = alertDialogBuilder.create();


        // show it
        alertDialog.show();
        //Force Keyboard to be shown
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
    }


    private void editNumberPickerDialog(final HashMap<String,String> info,String curValue, final TextView valueField){
        // get prompts.xml view
        LayoutInflater layoutInflater = (LayoutInflater) MainActivity.this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View promptsView = layoutInflater.inflate(R.layout.alertdialog_numberpicker,null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                MainActivity.this);

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);

        final NumberPicker numberPicker = (NumberPicker) promptsView.findViewById(R.id.alertNumberPicker);


        final TextView header = (TextView) promptsView.findViewById(R.id.alertTitle);
        header.setText(info.get(KEY_HEADER));


        Core.sshHelper.populateNumberPicker(numberPicker,curValue);

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton(Core.getResource().getString(R.string.ok),
                        (dialog, id) -> {
                            // get user input and set it to result
                            // edit text
                            //changeSetting(info.get(KEY_COMMAND),userInput.getText().toString());
                            changeSetting(info.get(KEY_COMMAND)+"="+String.valueOf(numberPicker.getValue()));
                            valueField.setText(String.valueOf(numberPicker.getValue()));
                            dialog.dismiss();
                        })
                .setNegativeButton(Core.getResource().getString(R.string.cancel),
                        (dialog, id) -> {
                            dialog.dismiss();
                        });

        // create alert dialog
        final AlertDialog alertDialog = alertDialogBuilder.create();


        // show it
        alertDialog.show();
    }




}
