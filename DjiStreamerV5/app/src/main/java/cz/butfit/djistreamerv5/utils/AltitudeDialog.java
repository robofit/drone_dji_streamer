package cz.butfit.djistreamerv5.utils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatDialogFragment;

import cz.butfit.djistreamerv5.R;

public class AltitudeDialog extends AppCompatDialogFragment {
    private EditText editAltitude;
    private AltitudeDialogListener listener;

    private String altitude = "";

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.altitude_dialog, null);

        builder.setView(view)
                .setTitle("Takeoff Altitude")
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton("set", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String altitude = editAltitude.getText().toString();
                        listener.applyInputs(altitude);
                    }
                });

        editAltitude = view.findViewById(R.id.edit_altitude);

        if(!altitude.isEmpty()) {
            editAltitude.setHint(altitude);
            editAltitude.setText(altitude);
        }

        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            listener = (AltitudeDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement AltitudeDialogListener");
        }
    }

    public interface AltitudeDialogListener {
        void applyInputs(String altitude);
    }

    public void SetHint(String altitude) {
        this.altitude = altitude;
    }
}
