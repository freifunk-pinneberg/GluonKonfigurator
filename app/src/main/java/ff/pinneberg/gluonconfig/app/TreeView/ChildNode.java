package ff.pinneberg.gluonconfig.app.TreeView;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import com.unnamed.b.atv.model.TreeNode;
import ff.pinneberg.gluonconfig.app.Core;
import ff.pinneberg.gluonconfig.app.MainActivity;
import ff.pinneberg.gluonconfig.app.R;

import java.util.HashMap;

/**
 * Created by xilent on 12.09.15.
 */
public class ChildNode extends TreeNode.BaseNodeViewHolder<ChildNode.ChildNodeData> {


    public ChildNode(Context context) {
        super(context);

    }

    @Override
    public View createNodeView(TreeNode node, ChildNodeData value) {
        final LayoutInflater inflater = LayoutInflater.from(context);
        final View view = inflater.inflate(R.layout.childnode, null, false);
        TextView header = (TextView) view.findViewById(R.id.childNode_itemheader);
        header.setTypeface(null, Typeface.BOLD);
        TextView description = (TextView) view.findViewById(R.id.childNode_itemvalue);
        header.setText(value.data.get(MainActivity.KEY_HEADER));
        if(value.data.get(MainActivity.KEY_CONTENT_TYPE).equals(MainActivity.CONTENT_TEXT) || value.data.get(MainActivity.KEY_CONTENT_TYPE).equals(MainActivity.CONTENT_MULTISELECTLIST) ){
            Core.sshHelper.setText(description,value.data.get(MainActivity.KEY_COMMAND),value.hostinfo.get(MainActivity.KEY_IPADRESS),false);
        }else {
            Core.sshHelper.setText(description, MainActivity.gluon_get + value.data.get(MainActivity.KEY_COMMAND), value.hostinfo.get(MainActivity.KEY_IPADRESS),true);
        }

        return view;
    }

    public static class ChildNodeData {
        public HashMap<String,String> data;
        public HashMap<String,String> hostinfo;

        public ChildNodeData(HashMap<String,String> childNodeData, HashMap<String,String> host){
            data = childNodeData;
            hostinfo=host;
        }
    }

}