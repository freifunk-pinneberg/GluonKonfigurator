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
public class SubHeaderNode extends TreeNode.BaseNodeViewHolder<SubHeaderNode.SubHeaderText> {

    private PrintView arrowView;


    public SubHeaderNode(Context context) {
        super(context);

    }

    @Override
    public View createNodeView(TreeNode node, SubHeaderText value) {
        final LayoutInflater inflater = LayoutInflater.from(context);
        final View view = inflater.inflate(R.layout.subheadernode, null, false);
        TextView tvValue = (TextView) view.findViewById(R.id.tree_nodeheader);
        tvValue.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        tvValue.setTypeface(null, Typeface.BOLD);
        tvValue.setText(value.text);


        arrowView = (PrintView) view.findViewById(R.id.arrow_icon);
        if (node.isLeaf()) {
            arrowView.setVisibility(View.INVISIBLE);
        }

        arrowView.setIconText(context.getResources().getString(R.string.ic_keyboard_arrow_right));

        return view;
    }

    public static class SubHeaderText {
        public String text;

        public SubHeaderText(String SubheaderText){
            text = SubheaderText;
        }
    }

    @Override
    public void toggle(boolean active) {
        arrowView.setIconText(context.getResources().getString(active ? R.string.ic_keyboard_arrow_down : R.string.ic_keyboard_arrow_right));
    }


}