package com.example.our_project;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class text_form extends Fragment {
    private ImageView send_message;
    private EditText message;
    private LinearLayout chats;
    private BroadcastReceiver receiver;

    public text_form() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.text_input, container, false);
        send_message = view.findViewById(R.id.send_message);
        message = view.findViewById(R.id.message);
        chats = view.findViewById(R.id.chats);

        message.setFocusableInTouchMode(true);
        message.requestFocus();
        send_message.bringToFront();

        send_message.setOnClickListener(v -> {
            String temp_msq = message.getText().toString().trim();
            if (!temp_msq.isEmpty()) {

                Intent serviceIntent = new Intent(getContext(), MessageSendService.class);
                serviceIntent.putExtra(MessageSendService.EXTRA_MESSAGE, temp_msq);
                requireContext().startService(serviceIntent);


                TextView textView = new TextView(getContext());
                textView.setText(temp_msq);
                textView.setTextSize(18);
                textView.setTextColor(getResources().getColor(R.color.white, null));
                textView.setPadding(20, 10, 20, 10);
                textView.setBackgroundResource(R.drawable.sent_msg);

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(10, 10, 10, 10);
                params.gravity = Gravity.END;
                textView.setLayoutParams(params);

                chats.addView(textView);
                message.setText("");

                message.postDelayed(message::requestFocus, 50);
            }
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();


        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (MessageSendService.ACTION_MESSAGE_SENT.equals(intent.getAction())) {
                    String msg = intent.getStringExtra(MessageSendService.EXTRA_MESSAGE);
                    if (msg != null) {
                        TextView replyView = new TextView(getContext());
                        replyView.setText("Details updated " + msg);
                        replyView.setTextSize(16);
                        replyView.setTextColor(getResources().getColor(R.color.primary, null));
                        replyView.setPadding(20, 10, 20, 10);
                        replyView.setBackgroundResource(R.drawable.received_message);

                        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        );
                        layoutParams.setMargins(10, 10, 10, 10);
                        layoutParams.gravity = Gravity.START;
                        replyView.setLayoutParams(layoutParams);

                        chats.addView(replyView);
                    }
                }
            }
        };

        IntentFilter filter = new IntentFilter(MessageSendService.ACTION_MESSAGE_SENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requireContext().registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED);
        } else {
            requireContext().registerReceiver(receiver, filter);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (receiver != null) {
            requireContext().unregisterReceiver(receiver);
            receiver = null;
        }
    }
}
