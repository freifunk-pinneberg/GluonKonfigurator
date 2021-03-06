package ff.pinneberg.gluonconfig.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.unnamed.b.atv.model.TreeNode;
import com.unnamed.b.atv.view.AndroidTreeView;
import ff.pinneberg.gluonconfig.app.TreeView.ChildNode;
import ff.pinneberg.gluonconfig.app.TreeView.ChildNodeNoDetail;
import ff.pinneberg.gluonconfig.app.TreeView.HeaderNode;
import ff.pinneberg.gluonconfig.app.TreeView.SubHeaderNode;
import ff.pinneberg.gluonconfig.app.helper.Utils;

import java.lang.reflect.Type;
import java.util.*;


public class MainActivity extends ActionBarActivity {


    //Groups
    List<String> groupHeaders = new ArrayList<String>(){{
        add(Core.getResource().getString(R.string.info));
        add(Core.getResource().getString(R.string.general));
        add(Core.getResource().getString(R.string.auto_updater));
        add(Core.getResource().getString(R.string.location));
        add(Core.getResource().getString(R.string.mesh));
        add(Core.getResource().getString(R.string.internet));
        add(Core.getResource().getString(R.string.wifi));
        add(Core.getResource().getString(R.string.expert));
    }};

    //Group Content

    ArrayList<HashMap<String,String >> info = new ArrayList<HashMap<String, String>>(){{
        add(new HashMap<String, String>(){{
            put(KEY_HEADER, Core.getResource().getString(R.string.connected_clients));
            put(KEY_CONTENT_TYPE,CONTENT_TEXT);
            put(KEY_COMMAND,"grep -cEo \"\\[.*W.*\\]+\" /sys/kernel/debug/batman_adv/bat0/transtable_local");
        }});

        add(new HashMap<String, String>(){{
            put(KEY_HEADER, Core.getResource().getString(R.string.community_version));
            put(KEY_CONTENT_TYPE,CONTENT_TEXT);
            put(KEY_COMMAND,"cat /lib/gluon/release");
        }});

        add(new HashMap<String, String>(){{
            put(KEY_HEADER, Core.getResource().getString(R.string.gluon_version));
            put(KEY_CONTENT_TYPE,CONTENT_TEXT);
            put(KEY_COMMAND,"cat /lib/gluon/gluon-version");
        }});



        add(new HashMap<String, String>(){{
            put(KEY_HEADER, Core.getResource().getString(R.string.reboot_to_config));
            put(KEY_CONTENT_TYPE,CONTENT_NOTEXT);
            put(KEY_COMMAND, "uci set gluon-setup-mode.@setup_mode[0].enabled='1';" +
                    "uci commit gluon-setup-mode;" +
                    "reboot;");
        }});

    }};

    ArrayList<HashMap<String,String >> general = new ArrayList<HashMap<String, String>>(){{
        add(new HashMap<String, String>(){{
            put(KEY_HEADER, Core.getResource().getString(R.string.contact));
            put(KEY_CONTENT_TYPE,CONTENT_TEXT_EDIT);
            put(KEY_COMMAND,"gluon-node-info.@owner[0].contact");
        }});
        add(new HashMap<String, String>(){{
            put(KEY_HEADER,Core.getResource().getString(R.string.hostname));
            put(KEY_CONTENT_TYPE,CONTENT_TEXT_EDIT);
            put(KEY_COMMAND,"system.@system[0].hostname");
        }});

    }};

    ArrayList<HashMap<String,String >> auto_updater = new ArrayList<HashMap<String, String>>(){{
        add(new HashMap<String, String>(){{
            put(KEY_HEADER,Core.getResource().getString(R.string.status));
            put(KEY_CONTENT_TYPE,CONTENT_SWITCH);
            put(KEY_COMMAND,"autoupdater.settings.enabled");
        }});

        add(new HashMap<String, String>(){{
            put(KEY_HEADER,Core.getResource().getString(R.string.branch));
            put(KEY_CONTENT_TYPE,CONTENT_NUMBERPICKER);
            put(KEY_COMMAND,"autoupdater.settings.branch");
            put(KEY_SELECT_VALUES,"uci show | grep =branch | cut -d. -f2  | cut -d\"=\" -f1");
        }});

        add(new HashMap<String, String>(){{
            put(KEY_HEADER, Core.getResource().getString(R.string.force_update));
            put(KEY_CONTENT_TYPE,CONTENT_NOTEXT);
            put(KEY_COMMAND, "autoupdater -f -b $(uci show | grep autoupdater.settings.branch | cut -d\"=\" -f2)");
        }});

    }};



    ArrayList<HashMap<String,String >> location = new ArrayList<HashMap<String, String>>(){{
        add(new HashMap<String, String>(){{
            put(KEY_HEADER, Core.getResource().getString(R.string.latitude));
            put(KEY_CONTENT_TYPE,CONTENT_TEXT_EDIT);
            put(KEY_COMMAND,"gluon-node-info.@location[0].latitude");
        }});
        add(new HashMap<String, String>(){{
            put(KEY_HEADER,Core.getResource().getString(R.string.longitude));
            put(KEY_CONTENT_TYPE,CONTENT_TEXT_EDIT);
            put(KEY_COMMAND,"gluon-node-info.@location[0].longitude");
        }});
        add(new HashMap<String, String>(){{
            put(KEY_HEADER,Core.getResource().getString(R.string.share_location));
            put(KEY_CONTENT_TYPE,CONTENT_SWITCH);
            put(KEY_COMMAND,"gluon-node-info.@location[0].share_location");
        }});

    }};

    ArrayList<HashMap<String,String >> mesh= new ArrayList<HashMap<String, String>>(){{
        add(new HashMap<String, String>(){{
            put(KEY_HEADER, Core.getResource().getString(R.string.mesh_on_WAN));
            put(KEY_CONTENT_TYPE,CONTENT_SWITCH);
            put(KEY_COMMAND,"network.mesh_wan.auto");
        }});
        add(new HashMap<String, String>(){{
            put(KEY_HEADER,Core.getResource().getString(R.string.mesh_on_LAN));
            put(KEY_COMMAND,"network.mesh_lan.auto");
            put(KEY_CONTENT_TYPE,CONTENT_SWITCH);
            put(KEY_COMMAND2_ENABLE,"network.client.ifname=\"bat0\"");
            put(KEY_COMMAND2_DISABLE,"network.client.ifname=\"bat0 $(cat /lib/gluon/core/sysconfig/lan_ifname)\"");
        }});

    }};

    ArrayList<HashMap<String,String >> internet= new ArrayList<HashMap<String, String>>(){{
        add(new HashMap<String, String>(){{
            put(KEY_HEADER, Core.getResource().getString(R.string.limit_bandwith));
            put(KEY_CONTENT_TYPE,CONTENT_SWITCH);
            put(KEY_COMMAND,"gluon-simple-tc.mesh_vpn.enabled");
        }});

        add(new HashMap<String, String>(){{
            put(KEY_HEADER, Core.getResource().getString(R.string.limit_bandwith_upload));
            put(KEY_CONTENT_TYPE,CONTENT_TEXT_EDIT);
            put(KEY_COMMAND,"gluon-simple-tc.mesh_vpn.limit_egress");
        }});

        add(new HashMap<String, String>(){{
            put(KEY_HEADER, Core.getResource().getString(R.string.limit_bandwith_download));
            put(KEY_CONTENT_TYPE,CONTENT_TEXT_EDIT);
            put(KEY_COMMAND,"gluon-simple-tc.mesh_vpn.limit_ingress");
        }});

    }};

    ArrayList<HashMap<String,String >> wifi= new ArrayList<HashMap<String, String>>(){{
        add(new HashMap<String, String>(){{
            put(KEY_HEADER, Core.getResource().getString(R.string.transmitting_power));
            put(KEY_CONTENT_TYPE,CONTENT_NUMBERPICKER);
            put(KEY_COMMAND,"wireless.radio0.txpower");
            put(KEY_SELECT_VALUES,"iwinfo client0 txpower");
        }});


    }};

    ArrayList<HashMap<String,String >> expert = new ArrayList<HashMap<String, String>>(){{
        add(new HashMap<String, String>(){{
            put(KEY_HEADER, Core.getResource().getString(R.string.gateways));
            put(KEY_CONTENT_TYPE,CONTENT_MULTISELECTLIST);
            put(KEY_MULTILIST_CHANGE,"fastd.mesh_vpn_backbone_peer_{value}.enabled");
            put(KEY_COMMAND,"uci show| grep fastd.mesh_vpn_backbone_peer | grep enabled=1 | sed s'/[_=]/./g' | cut -d. -f 6| tr '\\n' ','");
            put(KEY_SELECT_VALUES,"uci show| grep fastd.mesh_vpn_backbone_peer | grep enabled | sed s'/[_=]/./g' | cut -d. -f 6,8");
            put(KEY_FINISH_COMMAND,"/etc/init.d/fastd restart");
        }});

        add(new HashMap<String, String>(){{
            put(KEY_HEADER,Core.getResource().getString(R.string.client_network));
            put(KEY_COMMAND,"wireless.client_radio0.disabled");
            put(KEY_CONTENT_TYPE,CONTENT_SWITCH);
            put(KEY_COMMAND2_ENABLE,"/etc/init.d/network restart");
            put(KEY_COMMAND2_DISABLE,"/etc/init.d/network restart");
        }});

        add(new HashMap<String, String>(){{
            put(KEY_HEADER,Core.getResource().getString(R.string.fastd_publ_key));
            put(KEY_COMMAND,"/etc/init.d/fastd show_key mesh_vpn");
            put(KEY_CONTENT_TYPE,CONTENT_TEXT);
        }});

    }};


    //SuperList of all Children
    ArrayList<ArrayList<HashMap<String,String>>> superList = new ArrayList<ArrayList<HashMap<String, String>>>(){{
        add(info);
        add(general);
        add(auto_updater);
        add(location);
        add(mesh);
        add(internet);
        add(wifi);
        add(expert);
    }};


    public static String gluon_get = "uci get ";
    public static String gluon_set = "uci set ";
    public static String gluon_commit = "uci commit ";

    public static String KEY_HEADER = "header";
    public static String KEY_CONTENT_TYPE = "content";
    public static String KEY_COMMAND = "command";
    public static String KEY_COMMAND2_ENABLE = "command2_enable";
    public static String KEY_COMMAND2_DISABLE = "command2_disable";
    public static String KEY_SELECT_VALUES = "select_values";
    public static String KEY_MULTILIST_CHANGE= "multilistchange";
    public static String KEY_EXECUTE = "execute";
    public static String KEY_FINISH_COMMAND = "finishcommand";
    public static String KEY_EXPERT = "expertonly";

    public static String KEY_HOSTNAME = "hostname";
    public static String KEY_IPADRESS = "ipadress";


    //Content Type
    public static String CONTENT_TEXT = "1";
    public static String CONTENT_TEXT_EDIT = "2";
    public static String CONTENT_NOTEXT = "3";
    public static String CONTENT_NUMBERPICKER = "4";
    public static String CONTENT_LIST = "5";
    public static String CONTENT_MULTISELECTLIST = "6";
    public static String CONTENT_SWITCH = "7";

    
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
                if (groupHeaders.get(i).equals(Core.getResource().getString(R.string.expert)) && sp.getBoolean("expert_mode", false) || !groupHeaders.get(i).equals(Core.getResource().getString(R.string.expert))) {
                    TreeNode subCategory = new TreeNode(new SubHeaderNode.SubHeaderText(groupHeaders.get(i))).setViewHolder(new SubHeaderNode(MainActivity.this));
                    for (HashMap<String, String> each : superList.get(i)) {


                        switch (each.get(KEY_CONTENT_TYPE)) {
                            case "1": // CONTENT_TEXT
                                subCategory.addChild(new TreeNode(new ChildNode.ChildNodeData(each, eachHost)).setViewHolder(new ChildNode(MainActivity.this)));
                                break;

                            case "2": //CONTENT_TEXT_EDIT
                                subCategory.addChild(new TreeNode(new ChildNode.ChildNodeData(each, eachHost)).setViewHolder(new ChildNode(MainActivity.this))
                                        .setClickListener((treeNode, o) -> {
                                                    ChildNode.ChildNodeData nodeData = (ChildNode.ChildNodeData) o;

                                                    TextView textField = (TextView) treeNode.getViewHolder().getView().findViewById(R.id.childNode_itemvalue);
                                                    String textvalue = textField.getText().toString();
                                                    if (textvalue.equals(Core.getResource().getString(R.string.not_connected))) {
                                                        Toast.makeText(MainActivity.this, Core.getResource().getString(R.string.not_connected_change), Toast.LENGTH_LONG).show();
                                                    } else {
                                                        editDialog(nodeData.data, textvalue, nodeData.hostinfo.get(KEY_IPADRESS), textField);
                                                    }
                                                }

                                        ));
                                break;

                            case "3": //CONTENT_NOTEXT
                                subCategory.addChild(new TreeNode(new ChildNodeNoDetail.ChildNodeData(each, eachHost)).setViewHolder(new ChildNodeNoDetail(MainActivity.this))
                                                .setClickListener((treeNode, o) -> {
                                                    ChildNodeNoDetail.ChildNodeData nodeData = (ChildNodeNoDetail.ChildNodeData) o;
                                                    executeCommands(nodeData.hostinfo.get(KEY_IPADRESS), nodeData.data.get(KEY_COMMAND));
                                                })
                                );
                                break;

                            case "4": //CONTENT_NUMBERPICKER
                                subCategory.addChild(new TreeNode(new ChildNode.ChildNodeData(each, eachHost)).setViewHolder(new ChildNode(MainActivity.this))
                                        .setClickListener((treeNode, o) -> {
                                            ChildNode.ChildNodeData nodeData = (ChildNode.ChildNodeData) o;

                                            TextView textField = (TextView) treeNode.getViewHolder().getView().findViewById(R.id.childNode_itemvalue);
                                            String textvalue = textField.getText().toString();
                                            if (textvalue.equals(Core.getResource().getString(R.string.not_connected))) {
                                                Toast.makeText(MainActivity.this, Core.getResource().getString(R.string.not_connected_change), Toast.LENGTH_LONG).show();
                                            } else {
                                                editNumberPickerDialog(nodeData.data, textvalue, textField, nodeData.hostinfo.get(KEY_IPADRESS));
                                            }
                                        }));
                                break;

                            case "5": //CONTENT_LIST
                                break;

                            case "6": //CONTENT_MULTISELECTLIST
                                subCategory.addChild(new TreeNode(new ChildNode.ChildNodeData(each, eachHost)).setViewHolder(new ChildNode(MainActivity.this))
                                        .setClickListener((treeNode, o) -> {
                                                    ChildNode.ChildNodeData nodeData = (ChildNode.ChildNodeData) o;

                                                    TextView textField = (TextView) treeNode.getViewHolder().getView().findViewById(R.id.childNode_itemvalue);
                                                    String textvalue = textField.getText().toString();
                                                    if (textvalue.equals(Core.getResource().getString(R.string.not_connected))) {
                                                        Toast.makeText(MainActivity.this, Core.getResource().getString(R.string.not_connected_change), Toast.LENGTH_LONG).show();
                                                    } else {
                                                        editMultiSelectListDialog(nodeData.data, textField, nodeData.hostinfo.get(KEY_IPADRESS));
                                                    }
                                                }


                                        ));
                                break;

                            case "7": //CONTENT_SWITCH
                                subCategory.addChild(new TreeNode(new ChildNode.ChildNodeData(each, eachHost)).setViewHolder(new ChildNode(MainActivity.this))
                                        .setClickListener((treeNode, o) -> {
                                            ChildNode.ChildNodeData nodeData = (ChildNode.ChildNodeData) o;

                                            TextView textField = (TextView) treeNode.getViewHolder().getView().findViewById(R.id.childNode_itemvalue);

                                            String textvalue = textField.getText().toString();

                                            if (textvalue.equals(Core.getResource().getString(R.string.enabled))) {
                                                changeSetting(nodeData.hostinfo.get(KEY_IPADRESS), nodeData.data.get(KEY_COMMAND2_DISABLE), nodeData.data.get(KEY_COMMAND) + "=0");

                                                textField.setText(Core.getResource().getString(R.string.disabled));
                                            } else if (textvalue.equals(Core.getResource().getString(R.string.disabled))) {
                                                changeSetting(nodeData.hostinfo.get(KEY_IPADRESS), nodeData.data.get(KEY_COMMAND2_ENABLE), nodeData.data.get(KEY_COMMAND) + "=1");
                                                textField.setText(Core.getResource().getString(R.string.enabled));

                                            } else if (textvalue.equals(Core.getResource().getString(R.string.not_connected))) {
                                                Toast.makeText(MainActivity.this, Core.getResource().getString(R.string.not_connected_change), Toast.LENGTH_LONG).show();
                                            }
                                        }));
                                break;


                            default:
                                break;
                        }
                    }
                    parent.addChild(subCategory);
                }
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
                Log.d(getClass().getSimpleName(), gluon_set + command);
                Core.sshHelper.executeCommandThread(gluon_set + command, ipadress);
            }
        }

        for(String module:commit_modules){
            Core.sshHelper.executeCommandThread(gluon_commit+module,ipadress);
        }

    }

    private void executeCommands(String ipadress,String... commands){
        for(String command:commands){
            Core.sshHelper.executeCommandThread(command,ipadress);
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


        deleteHostDialog.setTitle(Core.getResource().getString(R.string.delete_node));


        deleteHostDialog.setMessage(Core.getResource().getString(R.string.delete_node_text));




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

        Core.sshHelper.populateNumberPicker(numberPicker,curValue,ipadress,info.get(KEY_SELECT_VALUES));

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton(Core.getResource().getString(R.string.ok),
                        (dialog, id) -> {
                            String display_value = numberPicker.getDisplayedValues()[numberPicker.getValue()];
                            if(display_value.contains("mW")) {
                                changeSetting(ipadress, info.get(KEY_COMMAND) + "=" + String.valueOf(numberPicker.getValue()));
                                valueField.setText(String.valueOf(numberPicker.getValue()));
                            }else{
                                changeSetting(ipadress, info.get(KEY_COMMAND) + "=" + display_value);
                                valueField.setText(display_value);
                            }
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


    private void editMultiSelectListDialog(final HashMap<String,String> info, final TextView valueField,String ipadress){

        LayoutInflater layoutInflater = (LayoutInflater) MainActivity.this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View promptsView = layoutInflater.inflate(R.layout.alertdialog_listview,null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                MainActivity.this);
        alertDialogBuilder.setView(promptsView);

        final ListView listView = (ListView) promptsView.findViewById(R.id.alertListView);
        final TextView header = (TextView) promptsView.findViewById(R.id.alertTitle);
        header.setText(info.get(KEY_HEADER));


        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_multiple_choice);

        listView.setAdapter(adapter);

        Core.sshHelper.populateArrayAdapter(listView,adapter,ipadress,info.get(KEY_SELECT_VALUES));

        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton(Core.getResource().getString(R.string.ok),
                        (dialog, id) -> {

                            SparseBooleanArray checked = listView.getCheckedItemPositions();

                            for(int i=0;i<checked.size();i++){
                                if(checked.get(i)){
                                    changeSetting(ipadress,info.get(KEY_MULTILIST_CHANGE).replace("{value}",adapter.getItem(i))+"=1");
                                }else{
                                    changeSetting(ipadress,info.get(KEY_MULTILIST_CHANGE).replace("{value}",adapter.getItem(i))+"=0");
                                }
                            }

                            if(info.containsKey(KEY_FINISH_COMMAND)){
                                Core.sshHelper.executeCommandThread(info.get(KEY_FINISH_COMMAND),ipadress);
                            }

                            Core.sshHelper.setText(valueField,info.get(KEY_COMMAND),ipadress,false);


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
