/*
 * Copyright (C) 2013 Lemon Labs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package john.bacon.expbplus;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.view.ViewHelper;
import com.nineoldandroids.view.ViewPropertyAnimator;

import java.util.ArrayList;
import java.util.List;

import diok.per.expbmenu.R;

@SuppressWarnings("ConstantConditions")
public class ExpandableButtonMenu extends RelativeLayout implements View.OnClickListener {

    /**
     * DEFAULT BUTTON SIZE AND DISTANCE VALUES
     */
    private static final int DEFAULT_NUN_COLUMN = 3;
    private static final int DEFAULT_LINES = 1;
    private static final float DEFAULT_ITEM_SIZE = 100;
    private static final float DEFAULT_BOTTOM_PADDING = 100;
    private static final float DEFAULT_BUTTON_DISTANCE_Y = 0.15f;
    private static final float DEFAULT_BUTTON_DISTANCE_X = 0.27f;


    /**
     * Screen metrics
     */
    protected int sWidth;
    protected int sHeight;

    /**
     * Parent for items
     */
    private RelativeLayout rl_decor;

    /**
     * Close Button
     */
    private ImageButton mCloseBtn;

    private ExpandableMenuOverlay mParent;

    private View mOverlay;

    /**
     * Entity set
     */
    private List<ExpandableButtonEntity> list;
    /**
     * Flag indicating that the menu is expanded or collapsed
     */
    private boolean mExpanded;

    /**
     * Flag indicating if clicking anywhere on the screen collapses the menu
     */
    private boolean mAllowOverlayClose = true;

    /**
     * Flag indicating that menu is being animated
     */
    private boolean mAnimating;

    /**
     * Menu button position variables in % of screen width or height
     */
    protected float bottomPadding = DEFAULT_BOTTOM_PADDING;
    protected float buttonDistanceY = DEFAULT_BUTTON_DISTANCE_Y;
    protected float buttonDistanceX = DEFAULT_BUTTON_DISTANCE_X;
    /**
     * Size for each item
     */
    protected float itemSize = DEFAULT_ITEM_SIZE;
    /**
     * Size for close button
     */
    protected float closeSize = DEFAULT_ITEM_SIZE;
    /**
     * Item num for each column
     */
    protected int numColumns = DEFAULT_NUN_COLUMN;
    /**
     * Line num for each item's text
     */
    protected int lines = DEFAULT_LINES;
    /**
     * Line num for each item's text
     */
    protected ColorStateList textColor;
    /**
     * backgroud color for menu
     */
    protected int backColor;

    /**
     * Button click interface. Use setOnMenuButtonClickListener() to
     * register callbacks
     */
    private OnMenuButtonClick mListener;

    /**
     * index for generating certain entity to indicate current index
     */
    private int currentIndex = 0;


    /**
     * ANIMATION DEFINITIONS
     */

    /**
     * We don't use AnimatorSet so we have our own counter to see whether all animations have ended
     */
    private volatile byte ANIMATION_COUNTER;

    /**
     * Collapse and expand animation duration
     */
    private static final int ANIMATION_DURATION = 300;

    /**
     * Used interpolators
     */
    private static final float INTERPOLATOR_WEIGHT = 3.0f;
    private AnticipateInterpolator anticipation;
    private OvershootInterpolator overshoot;

    /**
     * Translation in Y axis of all three menu buttons
     */
    protected float TRANSLATION_Y;

    /**
     * Translation in X axis for left and right buttons
     */
    protected float TRANSLATION_X;

    public ExpandableButtonMenu(Context context) {
        this(context, null, 0);
    }

    public ExpandableButtonMenu(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ExpandableButtonMenu(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        list = new ArrayList<>();
        inflate();
        parseAttributes(attrs);
        init();
        calculateAnimationProportions();
    }


    /**
     * Set a top level overlay that acts as a proxy to this view. In a
     * a current implementation the content of the expandable menu is
     * set to a dialog. This allows the control over native screen dim
     * and keyboard key callbacks.
     *
     * @param parent
     */
    public void setButtonMenuParentOverlay(ExpandableMenuOverlay parent) {
        mParent = parent;
    }

    /**
     * Set a callback on menu button clicks
     *
     * @param listener
     */
    public void setOnMenuButtonClickListener(OnMenuButtonClick listener) {
        mListener = listener;
    }

    /**
     * Returns the menu button container. The first child of the container is
     * a TextView, the second - an ImageButton
     */
    public View getMenuButton(int pos) {
        return list.get(pos).getContainer();
    }

    /**
     * Set text appearance for button text views
     *
     * @param appearanceResource
     */
    public void setMenuTextAppearance(int appearanceResource) {
        for (ExpandableButtonEntity entity : list) {
            entity.getText().setTextAppearance(getContext(), appearanceResource);
        }
    }

    /**
     * Set image resource for a menu button
     *
     * @param imageResource
     */
    public void setMenuButtonImage(int pos, int imageResource) {
        setMenuButtonImage(pos, getResources().getDrawable(imageResource));
    }

    /**
     * Set image drawable for a menu button
     */
    public void setMenuButtonImage(int pos, Drawable drawable) {
        list.get(pos).getBtn().setImageDrawable(drawable);
    }

    /**
     * Set string resource displayed under a menu button
     */
    public void setMenuButtonText(int pos, int stringResource) {
        setMenuButtonText(pos, getContext().getString(stringResource));
    }

    /**
     * Set text displayed under a menu button
     */
    public void setMenuButtonText(int pos, String text) {
        list.get(pos).getText().setText(text);
    }

    public void setAllowOverlayClose(boolean allow) {
        mAllowOverlayClose = allow;
    }

    public void setAnimating(boolean isAnimating) {
        mAnimating = isAnimating;
    }

    public boolean isExpanded() {
        return mExpanded;
    }

    public boolean isAllowOverlayClose() {
        return mAllowOverlayClose;
    }

    public float getBottomPadding() {
        return bottomPadding;
    }

    public float getTranslationY() {
        return TRANSLATION_Y;
    }

    public float getTranslationX() {
        return TRANSLATION_X;
    }


    /**
     * Toggle the expandable menu button, expanding or collapsing it
     */
    public void toggle() {
        if (!mAnimating) {
            mAnimating = true;
            if (mExpanded) {
                animateCollapse();
            } else {
                animateExpand();
            }
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.ebm__menu_overlay) {
            if (mExpanded && mAllowOverlayClose) toggle();
        } else if (id == R.id.ebm__menu_close_image) {
            toggle();
        } else {
            int pos = (int) v.getTag();
            if (mListener != null) mListener.onClick(pos);
        }
    }


    /**
     * Inflates the view
     */
    private void inflate() {
        ((LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.ebm__menu, this, true);

        mOverlay = findViewById(R.id.ebm__menu_overlay);

        mCloseBtn = findViewById(R.id.ebm__menu_close_image);
        rl_decor = findViewById(R.id.rl_decor);
        sWidth = ScreenHelper.getScreenWidth(getContext());
        sHeight = ScreenHelper.getScreenHeight(getContext());
        mCloseBtn.setOnClickListener(this);
        mOverlay.setOnClickListener(this);
    }

    /**
     * Parses custom XML attributes
     *
     * @param attrs
     */
    private void parseAttributes(AttributeSet attrs) {
        if (attrs != null) {
            TypedArray a = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.ExpandableMenuOverlay, 0, 0);
            try {
                // button metrics
                bottomPadding = a.getDimension(R.styleable.ExpandableMenuOverlay_bottomPad, DEFAULT_BOTTOM_PADDING);
                buttonDistanceY = a.getFloat(R.styleable.ExpandableMenuOverlay_distanceY, DEFAULT_BUTTON_DISTANCE_Y);
                buttonDistanceX = a.getFloat(R.styleable.ExpandableMenuOverlay_distanceX, DEFAULT_BUTTON_DISTANCE_X);
                itemSize = a.getDimension(R.styleable.ExpandableMenuOverlay_itemSize, DEFAULT_ITEM_SIZE);
                closeSize = a.getDimension(R.styleable.ExpandableMenuOverlay_closeSize, DEFAULT_ITEM_SIZE);
                numColumns = a.getInt(R.styleable.ExpandableMenuOverlay_numColumn, DEFAULT_NUN_COLUMN);
                lines = a.getInt(R.styleable.ExpandableMenuOverlay_android_lines, DEFAULT_LINES);
                textColor = a.getColorStateList(R.styleable.ExpandableMenuOverlay_android_textColor);
                backColor = a.getColor(R.styleable.ExpandableMenuOverlay_backColor, getResources().getColor(R.color.back_default));

                // button resources
                mCloseBtn.setBackgroundResource(a.getResourceId(R.styleable.ExpandableMenuOverlay_closeButtonSrc, 0));
            } finally {
                a.recycle();
            }
        }
    }

    /**
     * Initialized the layout of menu buttons. Sets button sizes and distances between them
     * by a % of screen width or height accordingly.
     * Some extra padding between buttons is added by default to avoid intersections.
     */
    private void init() {
        // Some extra margin to center other buttons in the center of the main button
        LayoutParams rParams = (LayoutParams) mCloseBtn.getLayoutParams();
        rParams.width = (int) (closeSize);
        rParams.height = (int) (closeSize);
        rParams.setMargins(0, 0, 0, (int) bottomPadding);

        rl_decor.setBackgroundColor(backColor);
    }

    /**
     * Initialized animation properties
     */
    private void calculateAnimationProportions() {
        TRANSLATION_Y = sHeight * buttonDistanceY;
        TRANSLATION_X = sWidth * buttonDistanceX;

        anticipation = new AnticipateInterpolator(INTERPOLATOR_WEIGHT);
        overshoot = new OvershootInterpolator(INTERPOLATOR_WEIGHT);
    }

    /**
     * Start expand animation
     */
    private void animateExpand() {
        mCloseBtn.setVisibility(View.VISIBLE);
        ANIMATION_COUNTER = 0;
        if (list != null)
            for (int i = 0; i < list.size(); i++) {
                ExpandableButtonEntity entity = list.get(i);
                entity.getContainer().setVisibility(VISIBLE);
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                    ViewHelper.setAlpha(entity.getContainer(), 1.0f);
                }

                float transX = ((i % numColumns) - ((numColumns + (2 * (numColumns % 2) - 1) - 2 * (numColumns % 2)) / 2.0f)) * TRANSLATION_X;
                float transY = -TRANSLATION_Y * ((i / numColumns) + 1);

                ViewPropertyAnimator.animate(entity.getContainer()).setDuration(ANIMATION_DURATION).translationYBy(transY).translationXBy(transX).alpha(1.0f).setInterpolator(overshoot).setListener(ON_EXPAND_COLLAPSE_LISTENER);
            }
    }

    /**
     * Start collapse animation
     */
    private void animateCollapse() {
        mCloseBtn.setVisibility(View.VISIBLE);

        ANIMATION_COUNTER = 0;

        for (int i = 0; i < list.size(); i++) {
            float transX = -((i % numColumns) - ((numColumns + (2 * (numColumns % 2) - 1) - 2 * (numColumns % 2)) / 2.0f)) * TRANSLATION_X;
            float transY = TRANSLATION_Y * ((i / numColumns) + 1);

            ViewPropertyAnimator.animate(list.get(i).getContainer()).setDuration(ANIMATION_DURATION).translationYBy(transY).
                    translationXBy(transX).alpha(0.3f).setInterpolator(overshoot).setListener(ON_EXPAND_COLLAPSE_LISTENER);
        }
    }

    /**
     * Listener for expand and collapse animations
     */
    private Animator.AnimatorListener ON_EXPAND_COLLAPSE_LISTENER = new Animator.AnimatorListener() {
        @Override
        public void onAnimationStart(Animator animation) {
            if (mCloseBtn.isEnabled())
                mCloseBtn.setEnabled(false);
            if (mOverlay.isEnabled())
                mOverlay.setEnabled(false);
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            ANIMATION_COUNTER++;
            if (ANIMATION_COUNTER == 1 && mExpanded)
                mParent.showInitButton();

            if (ANIMATION_COUNTER == numColumns) {

                if (mExpanded) {
                    mCloseBtn.setVisibility(View.GONE);
                    for (ExpandableButtonEntity entity : list) {
                        entity.getContainer().setVisibility(GONE);
                    }

                    postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mParent.dismiss();
                            mParent.mDismissing = false;
                        }
                    }, 75);
                }

                mAnimating = false;
                mExpanded = !mExpanded;


                mCloseBtn.setEnabled(true);
                for (ExpandableButtonEntity entity : list) {
                    entity.getBtn().setEnabled(true);
                }
                mOverlay.setEnabled(true);
            }

        }

        @Override
        public void onAnimationCancel(Animator animation) {
        }

        @Override
        public void onAnimationRepeat(Animator animation) {
        }
    };

    /**
     * Button click callback interface
     */
    public interface OnMenuButtonClick {
        public void onClick(int pos);
    }

    private ImageButton getButton(int pos) {
        return list.get(pos).getBtn();

    }

    public TextView getTextView(int pos) {
        return list.get(pos).getText();
    }

    public void add(Context context, Drawable drawable, String txt) {
        ExpandableButtonEntity entity = new ExpandableButtonEntity(context, drawable, txt);
        list.add(entity);
        rl_decor.addView(entity.getContainer());
        invalidate();
    }

    public void clear() {
        rl_decor.removeAllViews();
        rl_decor.addView(mCloseBtn);
        list.clear();
        currentIndex = 0;
    }

    public class ExpandableButtonEntity {

        private LinearLayout container;
        private ImageButton btn;
        private TextView text;

        public ExpandableButtonEntity(Context context, Drawable drawable, String txt) {
            container = new LinearLayout(context);
            RelativeLayout.LayoutParams lp_container = new RelativeLayout.LayoutParams((int) itemSize, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp_container.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            lp_container.addRule(RelativeLayout.CENTER_HORIZONTAL);
            container.setLayoutParams(lp_container);
            container.setOrientation(LinearLayout.VERTICAL);

            btn = new ImageButton(context);
            LinearLayout.LayoutParams lp_btn = new LinearLayout.LayoutParams((int) itemSize, (int) itemSize);
            lp_btn.gravity = Gravity.CENTER_HORIZONTAL;
            btn.setLayoutParams(lp_btn);
            btn.setVisibility(View.VISIBLE);
            btn.setBackgroundColor(context.getResources().getColor(android.R.color.transparent));
            btn.setScaleType(ImageView.ScaleType.FIT_CENTER);
            btn.setPadding(0, 0, 0, 0);
            if (drawable != null)
                btn.setImageDrawable(drawable);

            text = new TextView(context);
            LinearLayout.LayoutParams lp_text = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp_text.setMargins(0, 0, 0, (int) ScreenHelper.dpToPx(context, 3));
            text.setLayoutParams(lp_text);
            text.setLines(lines);
            text.setMaxLines(3);
            text.setTextSize(TypedValue.COMPLEX_UNIT_SP,12);
            text.setGravity(Gravity.CENTER);
            text.setVisibility(View.VISIBLE);
            text.setPadding(0, 0, 0, 0);
            if (textColor != null)
                text.setTextColor(textColor);
            text.setText(txt);

            container.addView(btn);
            container.addView(text);
            btn.setTag(currentIndex);
            btn.setOnClickListener(ExpandableButtonMenu.this);
            currentIndex++;
        }

        public View getContainer() {
            return container;
        }


        public ImageButton getBtn() {
            return btn;
        }

        public void setImg(Drawable drawable) {
            btn.setImageDrawable(drawable);
        }

        public TextView getText() {
            return text;
        }

        public void setText(String txt) {
            text.setText(txt);
        }
    }

}


