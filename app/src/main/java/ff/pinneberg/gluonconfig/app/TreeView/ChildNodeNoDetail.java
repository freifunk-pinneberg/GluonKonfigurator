package ff.pinneberg.gluonconfig.app.TreeView;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import com.unnamed.b.atv.model.TreeNode;
import ff.pinneberg.gluonconfig.app.MainActivity;
import ff.pinneberg.gluonconfig.app.R;

import java.util.HashMap;

/**
 * Created by xilent on 12.09.15.
 */
public class ChildNodeNoDetail extends TreeNode.BaseNodeViewHolder<ChildNodeNoDetail.ChildNodeData> {


    public ChildNodeNoDetail(Context context) {
        super(context);

    }

    @Override
    public View createNodeView(TreeNode node, ChildNodeData value) {
        final LayoutInflater inflater = LayoutInflater.from(context);
        final View view = inflater.inflate(R.layout.childnodenodetail, null, false);
        TextView header = (TextView) view.findViewById(R.id.childNode_itemheader);
        header.setTypeface(null, Typeface.BOLD);
        header.setText(value.data.get(MainActivity.KEY_HEADER));

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