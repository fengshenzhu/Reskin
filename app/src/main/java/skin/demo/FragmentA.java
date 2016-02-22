package skin.demo;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import skin.lib.BaseSkinActivity;
import skin.lib.BaseSkinFragment;
import skin.lib.DynamicViewAttribute;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentA extends BaseSkinFragment {
    private static final String TAG = "FragmentA";


    public FragmentA() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = getActivity().getLayoutInflater().inflate(R.layout.fragment_a, container,
                false);

        rootView.findViewById(R.id.change_theme).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentA.this.getFragmentManager().beginTransaction().remove(FragmentA.this)
                        .replace(R.id.fragment_container, new FragmentB()).commit();
            }
        });

        /** 动态添加系统View */
        LinearLayout linearLayout = (LinearLayout) rootView.findViewById(R.id.root);
        TextView tv = new TextView(getActivity());
        tv.setText("This is a TextView added in code");
        int colorResId = R.color.textColor;
        tv.setTextColor(getResources().getColor(colorResId));
        tv.setBackgroundDrawable(getResources().getDrawable(R.drawable.image));
        linearLayout.addView(tv);

        /** 动态添加系统View不走onCreateView,需要手动添加换肤关注属性 */
        List<DynamicViewAttribute> attrs = new ArrayList<>();
        attrs.add(new DynamicViewAttribute("textColor", colorResId));
        attrs.add(new DynamicViewAttribute("background", R.drawable.image));
        ((BaseSkinActivity) getActivity()).addSkinView(tv, attrs);

        /** 动态添加自定义View,换肤由自定义View自实现,Activity不需额外管理 */
        SkinnableView dCView = new SkinnableView(getActivity());
        linearLayout.addView(dCView);
        return rootView;
    }

}