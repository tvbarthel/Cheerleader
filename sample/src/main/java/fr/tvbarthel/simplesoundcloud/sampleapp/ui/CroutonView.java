package fr.tvbarthel.simplesoundcloud.sampleapp.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import fr.tvbarthel.simplesoundcloud.sampleapp.R;


/**
 * Simple compoundView used to display custom crouton.
 * <p/>
 * Basically, simply add a shadow.
 */
public class CroutonView extends LinearLayout {

    /**
     * CompoundView to display a person.
     *
     * @param context calling context.
     * @param text    displayed.
     */
    public CroutonView(Context context, String text) {
        super(context);
        if (!isInEditMode()) {
            init(context, text);
        }
    }

    /**
     * Initialize internal component.
     *
     * @param context calling context.
     * @param text    text displayed.
     */
    private void init(Context context, String text) {
        this.setOrientation(VERTICAL);
        LayoutInflater.from(context).inflate(R.layout.crouton_view, this);
        ((TextView) this.findViewById(R.id.crouton_view_message)).setText(text);

        setOrientation(VERTICAL);
    }
}
