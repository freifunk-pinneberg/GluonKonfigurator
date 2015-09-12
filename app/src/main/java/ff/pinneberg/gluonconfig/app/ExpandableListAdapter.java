package ff.pinneberg.gluonconfig.app;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by xilent on 15.08.15.
 */
public class ExpandableListAdapter extends BaseExpandableListAdapter {


    private Context _context;
    private List<String> _listDataHeader; // header titles
    private ArrayList<ArrayList<HashMap<String, String>>> _listDataChild;

    public ExpandableListAdapter(Context context, List<String> listDataHeader,
                                 ArrayList<ArrayList<HashMap<String, String>>> listChildData) {
        this._context = context;
        this._listDataHeader = listDataHeader;
        this._listDataChild = listChildData;
    }

    @Override
    public Object getChild(int groupPosition, int childPosititon) {
        return this._listDataChild.get(groupPosition)
                .get(childPosititon);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    private static class ViewHolderChild {
        public TextView headline;
        public TextView value;
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {

        ViewHolderChild viewHolderChild;

        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this._context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.listitem, parent, false);
            TextView header = (TextView)convertView.findViewById(R.id.list_itemheader);
            header.setTypeface(null, Typeface.BOLD);
            TextView description = (TextView)convertView.findViewById(R.id.list_itemvalue);
            viewHolderChild = new ViewHolderChild();
            viewHolderChild.headline = header;
            viewHolderChild.value = description;
            convertView.setTag(viewHolderChild);
        }else{
            viewHolderChild = (ViewHolderChild) convertView.getTag();
        }

        HashMap<String, String> info = _listDataChild.get(groupPosition).get(childPosition);


        // Setting all values in listview
        viewHolderChild.headline.setText(info.get(MainActivity.KEY_HEADER));

        Core.sshHelper.setText(viewHolderChild.value, MainActivity.gluon_get + info.get(MainActivity.KEY_COMMAND));


        return convertView;
    }




    @Override
    public int getChildrenCount(int groupPosition) {
        return this._listDataChild.get(groupPosition)
                .size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this._listDataHeader.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return this._listDataHeader.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    private static class ViewHolderGroup {
        public TextView headline;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        ViewHolderGroup viewHolderGroup;
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this._context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.listgroup,parent,false);
            viewHolderGroup = new ViewHolderGroup();
            TextView header = (TextView) convertView.findViewById(R.id.list_groupheader);
            header.setTypeface(null, Typeface.BOLD);
            viewHolderGroup.headline = header;
            convertView.setTag(viewHolderGroup);
        }else{
            viewHolderGroup = (ViewHolderGroup) convertView.getTag();
        }

        viewHolderGroup.headline.setText(this._listDataHeader.get(groupPosition));
        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}