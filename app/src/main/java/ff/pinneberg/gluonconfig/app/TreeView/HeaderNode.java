package ff.pinneberg.gluonconfig.app.TreeView;

import android.content.Context;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import com.github.johnkil.print.PrintView;
import com.unnamed.b.atv.model.TreeNode;
import ff.pinneberg.gluonconfig.app.R;

/**
 * Created by xilent on 12.09.15.
 */
public class HeaderNode extends TreeNode.BaseNodeViewHolder<HeaderNode.HeaderText> {

    private PrintView arrowView;


    public HeaderNode(Context context) {
        super(context);

    }

    @Override
    public View createNodeView(TreeNode node, HeaderText value) {
        final LayoutInflater inflater = LayoutInflater.from(context);
        final View view = inflater.inflate(R.layout.headernode, null, false);
        TextView tvValue = (TextView) view.findViewById(R.id.tree_nodeheader);
        tvValue.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
        tvValue.setTypeface(null, Typeface.BOLD);
        tvValue.setText(value.text);


        arrowView = (PrintView) view.findViewById(R.id.arrow_icon);
        if (node.isLeaf()) {
            arrowView.setVisibility(View.INVISIBLE);
        }

        return view;
    }

    public static class HeaderText {
        public String text;

        public HeaderText(String headerText){
            text = headerText;
        }
    }

    @Override
    public void toggle(boolean active) {
        arrowView.setIconText(context.getResources().getString(active ? R.string.ic_keyboard_arrow_down : R.string.ic_keyboard_arrow_right));
    }


}