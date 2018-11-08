package com.example.tristangriffin.projectx.Adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tristangriffin.projectx.Activities.MainActivity;
import com.example.tristangriffin.projectx.Activities.SignInActivity;
import com.example.tristangriffin.projectx.Fragments.AboutFragment;
import com.example.tristangriffin.projectx.R;
import com.example.tristangriffin.projectx.Resources.FirebaseCommands;

import java.util.ArrayList;

import static com.example.tristangriffin.projectx.Activities.MainActivity.ABOUT_FRAGMENT_TAG;

public class SettingsListAdapter extends RecyclerView.Adapter<SettingsListAdapter.MyViewHolder> {

    ArrayList<String> data;
    Activity activity;
    private SharedPreferences sharedPreferences;

    private static final int VIEW_ORDINARY = 0;
    private static final int VIEW_WITH_SWITCH = 1;

    public SettingsListAdapter(ArrayList<String> data, Activity activity) {
        this.data = data;
        this.activity = activity;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        MyViewHolder(@NonNull View itemView) {
            super(itemView);
            this.textView = itemView.findViewById(R.id.setting_noSwitch_textView);
        }
    }

    class MyViewHolderWithSwitch extends MyViewHolder {
        TextView textView;
        SwitchCompat aSwitch;

        MyViewHolderWithSwitch(@NonNull View itemView) {
            super(itemView);
            this.textView = itemView.findViewById(R.id.setting_switch_textView);
            this.aSwitch = itemView.findViewById(R.id.settings_privacy_switch);
        }
    }

    @NonNull
    @Override
    public SettingsListAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        Context context = viewGroup.getContext();

        if (i == VIEW_ORDINARY) {
            Log.d("demo", "VIEW_ORDINARY");
            View view = LayoutInflater.from(context).inflate(R.layout.no_switch_layout, viewGroup, false);
            MyViewHolder myViewHolder = new MyViewHolder(view);
            return myViewHolder;
        } else {
            Log.d("demo", "VIEW_SWITCH");
            View view = LayoutInflater.from(context).inflate(R.layout.switch_layout, viewGroup, false);
            MyViewHolderWithSwitch myViewHolder = new MyViewHolderWithSwitch(view);
            return myViewHolder;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull SettingsListAdapter.MyViewHolder viewHolder, int i) {
        final int position = i;

        if (viewHolder.getItemViewType() == 1) {
            ((MyViewHolderWithSwitch) viewHolder).textView.setText(data.get(i));
            switch (i) {
                case 1:
                    String currentTheme = sharedPreferences.getString("current_theme", "Light");
                    if (currentTheme.equals("Light")) {
                        ((MyViewHolderWithSwitch) viewHolder).aSwitch.setChecked(false);
                    } else {
                        ((MyViewHolderWithSwitch) viewHolder).aSwitch.setChecked(true);
                    }

                    ((MyViewHolderWithSwitch) viewHolder).aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            if (isChecked) {
                                sharedPreferences.edit().putString("current_theme", "Dark").apply();
                            } else {
                                sharedPreferences.edit().putString("current_theme", "Light").apply();
                            }
                            Intent mainIntent = new Intent(activity, MainActivity.class);
                            ((MainActivity) activity).createNewActivity(mainIntent);
                        }
                    });

                    break;
                case 2:
                    String privacySetting = sharedPreferences.getString("current_privacy", "Public");
                    if (privacySetting.equals("Public")) {
                        ((MyViewHolderWithSwitch) viewHolder).aSwitch.setChecked(true);
                    } else {
                        ((MyViewHolderWithSwitch) viewHolder).aSwitch.setChecked(false);
                    }

                    ((MyViewHolderWithSwitch) viewHolder).aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            if (isChecked) {
                                sharedPreferences.edit().putString("current_privacy", "Public").apply();
                                Toast.makeText(activity, "Public", Toast.LENGTH_SHORT).show();
                            } else {
                                sharedPreferences.edit().putString("current_privacy", "Private").apply();
                                Toast.makeText(activity, "Private", Toast.LENGTH_SHORT).show();

                            }
                        }
                    });
                    break;
            }
        } else {
            viewHolder.textView.setText(data.get(i));
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switch (position) {
                        case 0:
                            AboutFragment aboutFragment = new AboutFragment();
                            ((MainActivity) activity).setFragmentAndTransition(aboutFragment, ABOUT_FRAGMENT_TAG);
                            break;

                        case 3:
                            FirebaseCommands.getInstance().signOut();
                            Intent intent = new Intent(activity, SignInActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            ((MainActivity) activity).createNewActivity(intent);
                            break;
                    }
                }
            });
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 2 || position == 1) {
            return VIEW_WITH_SWITCH;
        } else {
            return VIEW_ORDINARY;
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }
}
