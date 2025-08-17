package com.example.our_project;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.telephony.IccOpenLogicalChannelResponse;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

public class search_fragment extends Fragment
{
    public search_fragment()
    {

    }
    private BroadcastReceiver receiver;
    private ImageView send_message;
    private EditText message;
    private LinearLayout chats;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view= inflater.inflate(R.layout.search_fragment, container, false);
        send_message = view.findViewById(R.id.send_message);
        message =view.findViewById(R.id.message);
        chats=view.findViewById(R.id.chats);

        message.setFocusableInTouchMode(true);
        message.requestFocus();
        send_message.bringToFront();

        send_message.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Log.d("Debug", "Send button clicked!");
                String temp_msq=message.getText().toString().trim();
                if(!temp_msq.isEmpty())
                {
                    Intent serviceIntent = new Intent(getContext(), MessageQueryService.class);
                    serviceIntent.putExtra(MessageQueryService.EXTRA_MESSAGE, temp_msq);
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
                    message.postDelayed(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            message.requestFocus();
                        }
                    }, 50);
                }
            }
        });

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (MessageQueryService.ACTION_REPLY_RECEIVED.equals(intent.getAction())) {
                    String reply = intent.getStringExtra(MessageQueryService.EXTRA_REPLY);

                    TextView replyView = new TextView(getContext());
                    replyView.setText("KLUER: " + reply);
                    replyView.setTextSize(16);
                    replyView.setTextColor(getResources().getColor(R.color.primary, null));
                    replyView.setPadding(20, 10, 20, 10);
                    replyView.setBackgroundResource(R.drawable.received_message);

                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    params.setMargins(10, 10, 10, 10);
                    replyView.setLayoutParams(params);
                    chats.addView(replyView);
                }
            }
        };
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requireContext().registerReceiver(receiver, new android.content.IntentFilter(MessageQueryService.ACTION_REPLY_RECEIVED),Context.RECEIVER_EXPORTED);
        } else {
            requireContext().registerReceiver(receiver, new android.content.IntentFilter(MessageQueryService.ACTION_REPLY_RECEIVED));
        }
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        requireContext().unregisterReceiver(receiver);
    }

}
