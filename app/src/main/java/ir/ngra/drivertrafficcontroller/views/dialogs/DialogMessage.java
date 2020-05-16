package ir.ngra.drivertrafficcontroller.views.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import ir.ngra.drivertrafficcontroller.R;
import ir.ngra.drivertrafficcontroller.databinding.DialogMessageBinding;

public class DialogMessage extends DialogFragment {

    private Context context;
    private String Title;
    private int color;
    private Drawable icon;

    @BindView(R.id.DialogIgnor)
    Button DialogIgnor;

    @BindView(R.id.DialogTitle)
    TextView DialogTitle;

    @BindView(R.id.layout)
    LinearLayout layout;

    @BindView(R.id.DialogImg)
    ImageView DialogImg;


    public DialogMessage(Context context, String title, int color, Drawable icon) {//_______________ Start DialogMessage
        this.context = context;
        Title = title;
        this.color = color;
        this.icon = icon;

    }//_____________________________________________________________________________________________ End DialogMessage


    public Dialog onCreateDialog(Bundle savedInstanceState) {//_____________________________________ Start onCreateDialog
        View view = null;
        DialogMessageBinding binding = DataBindingUtil
                .inflate(LayoutInflater
                                .from(this.context),
                        R.layout.dialog_message,
                        null,
                        false);
        binding.setTitel("");
        view = binding.getRoot();
        ButterKnife.bind(this, view);
        layout.setBackgroundColor(color);
        DialogTitle.setText(Title);
        DialogImg.setImageDrawable(icon);
        DialogIgnor.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                DialogMessage.this.dismiss();
            }
        });
        return new AlertDialog.Builder(context).setView(view).create();
    }//_____________________________________________________________________________________________ End onCreateDialog

}
