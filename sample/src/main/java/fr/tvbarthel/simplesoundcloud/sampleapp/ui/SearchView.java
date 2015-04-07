package fr.tvbarthel.simplesoundcloud.sampleapp.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import fr.tvbarthel.simplesoundcloud.sampleapp.R;

/**
 * View used to search a user.
 */
public class SearchView extends FrameLayout implements TextView.OnEditorActionListener {

    /**
     * Dummy listener.
     */
    private static Listener sDummyListener = new Listener() {
        @Override
        public void onSearchRequested(String userName) {

        }
    };

    /**
     * Current dummy listener.
     */
    private Listener mListener = sDummyListener;

    /**
     * Simple view used to render data of a user.
     *
     * @param context calling context.
     */
    public SearchView(Context context) {
        super(context);
        if (!isInEditMode()) {
            init(context);
        }
    }

    /**
     * Simple view used to render data of a user.
     *
     * @param context calling context.
     * @param attrs   attr from xml.
     */
    public SearchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            init(context);
        }
    }

    /**
     * Simple view used to render data of a user.
     *
     * @param context      calling context.
     * @param attrs        attr from xml.
     * @param defStyleAttr style from xml.
     */
    public SearchView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (!isInEditMode()) {
            init(context);
        }
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
            mListener.onSearchRequested(String.valueOf(v.getText()));
            return true;
        }
        return false;
    }

    /**
     * Set listener which will catch view events.
     *
     * @param listener listener used to catch view callbacks.
     */
    public void setListener(Listener listener) {
        if (listener == null) {
            mListener = sDummyListener;
        } else {
            mListener = listener;
        }
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.search_view, this);

        ((EditText) findViewById(R.id.search_view_search_field)).setOnEditorActionListener(this);

        int padding = context.getResources().getDimensionPixelOffset(R.dimen.activity_horizontal_margin);
        setPadding(padding, padding, padding, padding);
        setBackgroundColor(getResources().getColor(R.color.dark_grey));
    }

    /**
     * Listener used to catch search view callbacks.
     */
    public interface Listener {
        /**
         * Called when a user wants to retrieve tracks of a user.
         *
         * @param userName sound cloud user name.
         */
        public void onSearchRequested(String userName);
    }
}
