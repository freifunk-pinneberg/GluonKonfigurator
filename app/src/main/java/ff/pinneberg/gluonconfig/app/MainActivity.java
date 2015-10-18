package ff.pinneberg.gluonconfig.app;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.unnamed.b.atv.model.TreeNode;
import com.unnamed.b.atv.view.AndroidTreeView;
import ff.pinneberg.gluonconfig.app.TreeView.ChildNode;
import ff.pinneberg.gluonconfig.app.TreeView.HeaderNode;
import ff.pinneberg.gluonconfig.app.TreeView.SubHeaderNode;
import ff.pinneberg.gluonconfig.app.helper.Utils;


import java.lang.reflect.Type;
import java.util.*;


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
    public static String gluon_commit = "uci commit ";

    public static String KEY_HEADER = "header";
    public static String KEY_COMMAND = "command";
    public static String KEY_COMMAND2_ENABLE = "command2_enable";
    public static String KEY_COMMAND2_DISABLE = "command2_disable";
    public static String KEY_SELECT_VALUES = "select_values";
    public static String KEY_VALUE = "value";


    public static String KEY_HOSTNAME = "hostname";
    public static String KEY_IPADRESS = "ipadress";

    ExpandableListAdapter expandableListAdapter;
    SharedPreferences sp;
    ArrayList<HashMap<String,String>> hosts;
    AndroidTreeView tView;
    RelativeLayout rootLayout;
    TreeNode root;


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

    private void initUI(){
        rootLayout = (RelativeLayout) findViewById(R.id.RootLayout);
        initTreeView();

    }

    private void initTreeView(){

        root = TreeNode.root();


        Type type = new TypeToken<ArrayList<HashMap<String,String>>>(){}.getType();
        hosts = new Gson().fromJson(sp.getString("hosts", ""), type);
        if(hosts == null){
            hosts = new ArrayList<>();
        }


        for(HashMap<String,String> eachHost: hosts){

            TreeNode parent = new TreeNode(new HeaderNode.HeaderText(eachHost.get(KEY_HOSTNAME))).setViewHolder(new HeaderNode(MainActivity.this));


            parent.setLongClickListener((treeNode, o) -> {
                deleteHostDialog(eachHost.get(KEY_HOSTNAME));
                return false;
            });


            for (int i = 0; i < superList.size(); i++) {
                TreeNode subCategory = new TreeNode(new SubHeaderNode.SubHeaderText(groupHeaders.get(i))).setViewHolder(new SubHeaderNode(MainActivity.this));
                for (HashMap<String, String> each : superList.get(i)) {
                    TreeNode child = new TreeNode(new ChildNode.ChildNodeData(each,eachHost)).setViewHolder(new ChildNode(MainActivity.this));
                    child.setClickListener((treeNode, o) -> {
                        ChildNode.ChildNodeData nodeData = (ChildNode.ChildNodeData) o;

                        TextView textField = (TextView) treeNode.getViewHolder().getView().findViewById(R.id.childNode_itemvalue);

                        String textvalue = textField.getText().toString();
                        if (textvalue.equals(Core.getResource().getString(R.string.enabled))) {
                            changeSetting(nodeData.hostinfo.get(KEY_IPADRESS),nodeData.data.get(KEY_COMMAND2_DISABLE), nodeData.data.get(KEY_COMMAND) + "=0");

                            textField.setText(Core.getResource().getString(R.string.disabled));
                        } else if (textvalue.equals(Core.getResource().getString(R.string.disabled))) {
                            changeSetting(nodeData.hostinfo.get(KEY_IPADRESS),nodeData.data.get(KEY_COMMAND2_ENABLE), nodeData.data.get(KEY_COMMAND) + "=1");
                            textField.setText(Core.getResource().getString(R.string.enabled));

                        }else if(textvalue.equals(Core.getResource().getString(R.string.not_connected))) {
                            Toast.makeText(MainActivity.this,Core.getResource().getString(R.string.not_connected_change),Toast.LENGTH_LONG).show();
                        }
                        else {
                            if (nodeData.data.containsKey(KEY_SELECT_VALUES)) {
                                editNumberPickerDialog(nodeData.data, textvalue, textField,nodeData.hostinfo.get(KEY_IPADRESS));
                            } else {
                                editDialog(nodeData.data, textvalue,nodeData.hostinfo.get(KEY_IPADRESS),textField);
                            }
                        }
                    });
                    subCategory.addChild(child);
                }
                parent.addChild(subCategory);
            }

            root.addChild(parent);
        }



            tView = new AndroidTreeView(MainActivity.this, root);
            rootLayout.addView(tView.getView());


    }

    private void rebuildTreeView(){

        rootLayout.removeAllViews();
        initTreeView();

    }

    private void initVariables(){
        Intent i = new Intent(MainActivity.this,LocationService.class);
        startService(i);
        sp = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
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
        if(id == R.id.action_add_host){
            addHostDialog();
        }

        return super.onOptionsItemSelected(item);
    }

    private void changeSetting(String ipadress,String... commands){

        Set<String> commit_modules = new HashSet<>();
        for(String command:commands) {
            if(command != null) {
                commit_modules.add(command.split("\\.")[0]);
                Core.sshHelper.executeCommandThread(gluon_set + command, ipadress);
            }
        }

        for(String module:commit_modules){
            Core.sshHelper.executeCommandThread(gluon_commit+module,ipadress);
        }

    }


    private void addHostDialog(){
        // get prompts.xml view
        LayoutInflater layoutInflater = (LayoutInflater) MainActivity.this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View promptsView = layoutInflater.inflate(R.layout.add_hosts,null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                MainActivity.this);

        alertDialogBuilder.setView(promptsView);

        final EditText hostname = (EditText) promptsView.findViewById(R.id.add_host_name);
        final EditText ipaddress = (EditText) promptsView.findViewById(R.id.add_host_ip);



        alertDialogBuilder
                .setCancelable(true)
                .setPositiveButton(Core.getResource().getString(R.string.ok),
                        null)
                .setNegativeButton(Core.getResource().getString(R.string.cancel),
                        null);

        final AlertDialog alertDialog = alertDialogBuilder.create();


        alertDialog.setOnShowListener(dialogInterface -> {
            Button positive = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positive.setOnClickListener(view -> {
                if (Utils.isIpAddress(ipaddress.getText().toString())) {

                    Gson gson = new Gson();
                    hosts = gson.fromJson(sp.getString("hosts", ""), ArrayList.class);

                    HashMap<String, String> newHost = new HashMap<>();
                    newHost.put(KEY_HOSTNAME, hostname.getText().toString());
                    newHost.put(KEY_IPADRESS, ipaddress.getText().toString());
                    if (hosts == null) {
                        hosts = new ArrayList<>();
                    }
                    hosts.add(newHost);

                    sp.edit().putString("hosts", gson.toJson(hosts)).apply();
                    rebuildTreeView();

                    InputMethodManager imm =
                            (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                    alertDialog.dismiss();
                } else {
                    Toast.makeText(MainActivity.this, Core.getResource().getString(R.string.invalid_ipadress), Toast.LENGTH_SHORT).show();
                }

            });

            Button negative = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
            negative.setOnClickListener(view -> {
                InputMethodManager imm =
                        (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                alertDialog.dismiss();
            });
        });
        // show it
        alertDialog.show();

        //Force Keyboard to be shown
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }


    private void deleteHostDialog(String hostName){
        AlertDialog.Builder deleteHostDialog = new AlertDialog.Builder(
                MainActivity.this);


        deleteHostDialog.setTitle(Core.getResource().getString(R.string.delete_host));


        deleteHostDialog.setMessage(Core.getResource().getString(R.string.delete_host_text));




        deleteHostDialog.setPositiveButton(Core.getResource().getString(R.string.yes),
                (dialog, which) -> {
                    Type type = new TypeToken<ArrayList<HashMap<String,String>>>(){}.getType();
                    hosts = new Gson().fromJson(sp.getString("hosts", ""), type);
                    if(hosts == null){
                        hosts = new ArrayList<>();
                    }

                    int index=-1;

                    for(int i=0;i<hosts.size();i++){
                        if(hosts.get(i).get(KEY_HOSTNAME).equals(hostName)){
                            index =i;
                            break;
                        }
                    }

                    if(index >-1) {
                        hosts.remove(index);
                        sp.edit().putString("hosts", new Gson().toJson(hosts)).apply();
                        rebuildTreeView();
                    }

                    dialog.dismiss();
                });

        deleteHostDialog.setNegativeButton(Core.getResource().getString(R.string.no),
                (dialog, which) -> {
                    // Write your code here to execute after dialog

                    dialog.cancel();
                });

        deleteHostDialog.show();
    }


    private void editDialog(final HashMap<String,String> info,String curValue,String ipadress,TextView textView){
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
                            changeSetting(ipadress,info.get(KEY_COMMAND)+"="+userInput.getText().toString());
                            textView.setText(userInput.getText().toString());
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


    private void editNumberPickerDialog(final HashMap<String,String> info,String curValue, final TextView valueField,String ipadress){
        LayoutInflater layoutInflater = (LayoutInflater) MainActivity.this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View promptsView = layoutInflater.inflate(R.layout.alertdialog_numberpicker,null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                MainActivity.this);
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
