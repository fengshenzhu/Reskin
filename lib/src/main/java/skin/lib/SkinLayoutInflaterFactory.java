package skin.lib;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import skin.lib.item.ImageViewSrcItem;
import skin.lib.item.TextViewTextColorItem;
import skin.lib.item.ViewBackgroundItem;

/**
 * 实现换肤的核心类
 * 实现LayoutInflater.Factory，通过实现其onCreateView()方法，手动创建好布局文件中的View。
 * 遍历自行实例化的View可记录需要修改主题的View及其属性，修改这些View的属性值即可实现换肤。
 * <p/>
 * Created by fengshzh on 1/20/16.
 */
class SkinLayoutInflaterFactory implements LayoutInflater.Factory {
    private static final String TAG = "SkinLayoutInflaterFactory";

    /**
     * 换肤支持的资源类型
     */
    private static final String RES_DRAWABLE = "drawable";
    private static final String RES_COLOR = "color";

    /**
     * 换肤支持的属性名
     */
    private static final String ATTR_VIEW_BACKGROUND = "background";
    private static final String ATTR_TEXTVIEW_TEXTCOLOR = "textColor";
    private static final String ATTR_IMAGEVIEW_SRC = "src";

    /**
     * 换肤支持的属性id
     */
    private final static int[] ATTRS_VIEW = new int[]{
            android.R.attr.background
    };
    private final static int[] ATTRS_TEXTVIEW = new int[]{
            android.R.attr.textColor
    };
    private final static int[] ATTRS_IMAGEVIEW = new int[]{
            android.R.attr.src
    };

    /**
     * 这几个前缀在xml布局文件中申明View时可省略，但是实例化View要使用Java反射机制调用其构造函数，需要补全类名
     * 前三个来自PhoneLayoutInflater，第四个来自LayoutInflater
     */
    private static final String[] sClassPrefixList = {
            "android.widget.",
            "android.webkit.",
            "android.app.",
            "android.view."
    };

    /**
     * 最小的自定义资源id,区别于系统资源id.大于0x70000000的是自定义资源
     */
    private static final int MIN_CUSTOM_RESOURCE_ID = 0x70000000;

    private Context mContext;
    private LayoutInflater mLayoutInflater;

    /**
     * 换肤支持的各类型item列表，创建View时添加到列表，换肤时遍历列表修改属性值
     */
    private List<ViewBackgroundItem> mViewBackgroundItems = new ArrayList<>();
    private List<TextViewTextColorItem> mTextViewTextColorItems = new ArrayList<>();
    private List<ImageViewSrcItem> mImageViewSrcItems = new ArrayList<>();

    /**
     * 自定义View列表
     */
    private List<WeakReference<ICustomSkinView>> mCustomSkinViews = new ArrayList<>();

    SkinLayoutInflaterFactory(Activity activity) {
        mContext = activity;
        mLayoutInflater = activity.getLayoutInflater();
    }

    /**
     * Hook you can supply that is called when inflating from a LayoutInflater.
     * You can use this to customize the tag names available in your XML
     * layout files.
     * <p/>
     * <p/>
     * Note that it is good practice to prefix these custom names with your
     * package (i.e., com.coolcompany.apps) to avoid conflicts with system
     * names.
     *
     * @param name    Tag name to be inflated.
     * @param context The context the view is being created in.
     * @param attrs   Inflation attributes as specified in XML file.
     * @return View Newly created view. Return null for the default
     * behavior.
     */
    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {
        // fragment交由系统处理,调用到Fragment.onCreateView()
        if ("fragment".equals(name)) {
            return null;
        }

        View view = createView(name, attrs);

        if (view != null) {
            addSkinViewIfNecessary(view, attrs);
        } else {
            L.e(TAG, "Dangerous!!! You miss view " + name);
        }

        return view;
    }


    /**
     * 手动创建View
     * 只有返回的View不为空，才能保证记录到需要修改主题的View
     *
     * @param name  Tag name to be inflated.
     * @param attrs Inflation attributes as specified in XML file.
     * @return View Newly created view. Return null for the default behavior.
     */
    private View createView(String name, AttributeSet attrs) {
        View view = null;
        // from PhoneLayoutInflater
        for (String prefix : sClassPrefixList) {
            try {
                view = mLayoutInflater.createView(name, prefix, attrs);
            } catch (Exception e) {
            }
        }

        if (view == null) {
            try {
                view = mLayoutInflater.createView(name, null, attrs);
            } catch (Exception e) {
            }
        }

        return view;
    }

    /**
     * 记录需要修改主题的View及其属性,支持style属性
     *
     * @param view 布局xml中需要修改主题的View
     * @param set  需要修改主题的View的属性
     */
    private void addSkinViewIfNecessary(View view, AttributeSet set) {
        // View
        int[] viewResIds = getResIds(set, ATTRS_VIEW);
        for (int i = 0; i < viewResIds.length; i++) {
            if (viewResIds[i] > MIN_CUSTOM_RESOURCE_ID) {
                switch (i) {
                    case 0:
                        addSkinView(view, ATTR_VIEW_BACKGROUND, viewResIds[i]);
                        break;
                }
            }
        }

        // TextView
        if (view instanceof TextView) {
            int[] textViewResIds = getResIds(set, ATTRS_TEXTVIEW);
            for (int i = 0; i < textViewResIds.length; i++) {
                if (textViewResIds[i] > MIN_CUSTOM_RESOURCE_ID) {
                    switch (i) {
                        case 0:
                            addSkinView(view, ATTR_TEXTVIEW_TEXTCOLOR, textViewResIds[i]);
                            break;
                    }
                }
            }
        }

        //ImageView
        if (view instanceof ImageView) {
            int[] imageViewResIds = getResIds(set, ATTRS_IMAGEVIEW);
            for (int i = 0; i < imageViewResIds.length; i++) {
                if (imageViewResIds[i] > MIN_CUSTOM_RESOURCE_ID) {
                    switch (i) {
                        case 0:
                            addSkinView(view, ATTR_IMAGEVIEW_SRC, imageViewResIds[i]);
                            break;
                    }
                }
            }
        }
    }

    private int[] getResIds(AttributeSet set, int[] attrs) {
        int[] resIds = new int[attrs.length];
        for (int i = 0; i < resIds.length; i++) {
            resIds[i] = -1;
        }

        TypedArray typedArray = mContext.obtainStyledAttributes(set, attrs);
        TypedValue value;
        for (int i = 0; i < resIds.length; i++) {
            value = typedArray.peekValue(i);
            if (value != null) {
                resIds[i] = value.resourceId;
            }
        }
        typedArray.recycle();

        return resIds;
    }

    /**
     * 记录需要修改主题的动态添加View及其属性
     *
     * @param view  动态添加的需要修改主题的View
     * @param attrs 需要修改主题的View的属性
     */
    void addSkinViewIfNecessary(View view, List<DynamicViewAttribute> attrs) {
        for (DynamicViewAttribute attr : attrs) {
            addSkinView(view, attr.attrName, attr.resId);
        }
    }

    /**
     * 添加View到换肤管理列表
     *
     * @param view     view
     * @param attrName 换肤属性
     * @param resId    默认资源id
     */
    private void addSkinView(View view, String attrName, int resId) {
        switch (attrName) {
            case ATTR_VIEW_BACKGROUND:
                String typeName = mContext.getResources().getResourceTypeName(resId);
                mViewBackgroundItems.add(new ViewBackgroundItem(view, resId, typeName));
                break;
            case ATTR_TEXTVIEW_TEXTCOLOR:
                mTextViewTextColorItems.add(new TextViewTextColorItem((TextView) view, resId));
                break;
            case ATTR_IMAGEVIEW_SRC:
                mImageViewSrcItems.add(new ImageViewSrcItem((ImageView) view, resId));
                break;
        }
    }

    /**
     * 记录需要修改主题的View及其属性
     *
     * @param view 自定义的View
     */
    void addCustomView(ICustomSkinView view) {
        mCustomSkinViews.add(new WeakReference<>(view));
    }

    /**
     * 移除换肤列表中自定义View
     *
     * @param view 自定义View
     */
    void removeCustomView(ICustomSkinView view) {
        for (WeakReference<ICustomSkinView> ref : mCustomSkinViews) {
            if (ref.get() != null && ref.get() == view) {
                mCustomSkinViews.remove(ref);
                return;
            }
        }
    }

    /**
     * 修改主题
     */
    void reSkin(SkinTheme theme) {
        for (TextViewTextColorItem item : mTextViewTextColorItems) {
            TextView textView = item.view.get();
            if (textView != null) {
                try {
                    textView.setTextColor(theme.getColor(item.resId));
                } catch (Resources.NotFoundException e) {
                    // 找不到主题资源不改变属性值，异常不处理。以下异常捕捉处同。
                }
            }
        }

        for (ImageViewSrcItem item : mImageViewSrcItems) {
            ImageView imageView = item.view.get();
            if (imageView != null) {
                try {
                    imageView.setImageDrawable(theme.getDrawable(item.resId));
                } catch (Resources.NotFoundException e) {
                }
            }
        }

        for (ViewBackgroundItem item : mViewBackgroundItems) {
            View view = item.view.get();
            if (view != null) {
                if (item.typeName.equals(RES_COLOR)) {
                    try {
                        view.setBackgroundColor(theme.getColor(item.resId));
                    } catch (Resources.NotFoundException e) {
                    }
                } else if (item.typeName.equals(RES_DRAWABLE)) {
                    try {
                        view.setBackgroundDrawable(theme.getDrawable(item.resId));
                    } catch (Resources.NotFoundException e) {
                    }
                }
            }
        }

        for (WeakReference<ICustomSkinView> ref : mCustomSkinViews) {
            if (ref.get() != null) {
                ref.get().reSkin(theme);
            }
        }

    }

    /**
     * 清空记录换肤item的列表
     */
    void clear() {
        mViewBackgroundItems.clear();
        mTextViewTextColorItems.clear();
        mImageViewSrcItems.clear();

        mCustomSkinViews.clear();
    }
}