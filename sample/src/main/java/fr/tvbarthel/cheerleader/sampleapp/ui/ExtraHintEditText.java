package fr.tvbarthel.cheerleader.sampleapp.ui;

import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;

import java.lang.ref.WeakReference;


/**
 * Simple {@link android.widget.EditText} which can be linked to an extra hint {@link android.view.View}.
 * representing the hint when not displayed in the editText.
 * hint.
 * <p/>
 * This {@link android.view.View} will be automatically hidden or displayed according
 * to the edit text hint visibility.
 */
public class ExtraHintEditText extends EditText {

    private WeakReference<View> mHintViewReference;

    /**
     * FriendlyEditText.
     *
     * @param context calling context.
     */
    public ExtraHintEditText(Context context) {
        super(context);
        if (!isInEditMode()) {
            init();
        }
    }

    /**
     * FriendlyEditText.
     *
     * @param context calling context.
     * @param attrs   attr from xml.
     */
    public ExtraHintEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            init();
        }
    }

    /**
     * FriendlyEditText.
     *
     * @param context      calling context.
     * @param attrs        attr from xml.
     * @param defStyleAttr style from xml.
     */
    public ExtraHintEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (!isInEditMode()) {
            init();
        }
    }

    /**
     * Set the hintView which will be displayed when editText hint is hidden and hidden when
     * the editText hint will be displayed.
     *
     * @param hintView extra hint view.
     */
    public void setExtraHintView(View hintView) {
        hintView.setAlpha(0.0f);
        mHintViewReference = new WeakReference<>(hintView);
    }

    private void init() {

        this.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (TextUtils.isEmpty(s.toString()) && mHintViewReference.get() != null) {
                    mHintViewReference.get().setTranslationY(mHintViewReference.get().getHeight() / 2);
                    mHintViewReference.get().animate().alpha(1).translationY(0).start();
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (TextUtils.isEmpty(s.toString()) && mHintViewReference.get() != null) {
                    mHintViewReference.get().animate().alpha(0)
                        .translationY(mHintViewReference.get().getHeight() / 2).start();
                }
            }
        });
    }
}
