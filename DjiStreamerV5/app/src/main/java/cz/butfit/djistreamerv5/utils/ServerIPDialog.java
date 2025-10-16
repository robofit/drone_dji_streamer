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

public class ServerIPDialog extends AppCompatDialogFragment {
    private EditText editServerIP;
    private EditText editServerPort;
    private ServerIPDialogListener listener;

    private String serverIP = "";
    private String serverPort = "";

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.server_ip_dialog, null);

        builder.setView(view)
                .setTitle("Connection to the Server")
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton("connect", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String ip = editServerIP.getText().toString();
                        String port = editServerPort.getText().toString();
                        listener.applyInputs(ip, port);
                    }
                });

        editServerIP = view.findViewById(R.id.edit_server_ip);
        editServerPort = view.findViewById(R.id.edit_server_port);

        if(!serverIP.isEmpty()) {
            editServerIP.setHint(serverIP);
            editServerIP.setText(serverIP);
        }
        if(!serverPort.isEmpty()) {
            editServerPort.setHint(serverPort);
            editServerPort.setText(serverPort);
        }

        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            listener = (ServerIPDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement ServerIPDialogListener");
        }
    }

    public interface ServerIPDialogListener {
        void applyInputs(String ip, String port);
    }

    public void SetHint(String serverIP, String serverPort) {
        this.serverIP = serverIP;
        this.serverPort = serverPort;
    }
}
