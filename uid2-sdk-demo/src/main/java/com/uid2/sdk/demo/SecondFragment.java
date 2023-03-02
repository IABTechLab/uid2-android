package com.uid2.sdk.demo;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Base64;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.navigation.fragment.NavHostFragment;

import com.uid2.client.IdentityTokens;
import com.uid2.client.PublisherUid2Client;
import com.uid2.client.TokenGenerateInput;
import com.uid2.client.UID2Client;
import com.uid2.client.UID2Manager;
import com.uid2.client.data.IdentityStatus;
import com.uid2.client.data.UID2Identity;
import com.uid2.sdk.demo.databinding.FragmentSecondBinding;

import org.json.JSONException;

import java.util.regex.Pattern;

public class SecondFragment extends Fragment {

    private FragmentSecondBinding binding;
    private PublisherUid2Client publisherUid2Client;
    private boolean autoRefreshEnabled;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        publisherUid2Client = new PublisherUid2Client(BuildConfig.UID2_BASE_URL, BuildConfig.CLIENT_API_KEY, BuildConfig.SECRET_KEY);
        final Observer<IdentityStatus> uid2IdentityStatusObserver = new Observer<IdentityStatus>() {
            @Override
            public void onChanged(@Nullable final IdentityStatus status) {
                if (status == null) {
                    return;
                }
                binding.identityStatus.setText(status.toString());
            }
        };
        UID2Manager.shared.getIdentityStatus().observe(this.getViewLifecycleOwner(), uid2IdentityStatusObserver);

        final Observer<UID2Identity> uid2TokenObserver = new Observer<UID2Identity>() {
            @Override
            public void onChanged(@Nullable final UID2Identity identity) {
                if (identity == null) {
                    binding.advertisingToken.setText("");
                    binding.refreshToken.setText("");
                    binding.identityExpires.setText("");
                    binding.refreshFrom.setText("");
                    binding.refreshExpires.setText("");
                    binding.refreshResponseKey.setText("");
                } else {
                    binding.advertisingToken.setText(identity.advertisingToken);
                    binding.refreshToken.setText(identity.refreshToken);
                    binding.identityExpires.setText(identity.identityExpires.toString());
                    binding.refreshFrom.setText(identity.refreshFrom.toString());
                    binding.refreshExpires.setText(identity.refreshExpires.toString());
                    binding.refreshResponseKey.setText(identity.refreshResponseKey.toString());
                }
            }
        };
        UID2Manager.shared.getIdentity().observe(this.getViewLifecycleOwner(), uid2TokenObserver);

        binding = FragmentSecondBinding.inflate(inflater, container, false);
        autoRefreshEnabled = UID2Manager.shared.getAutoRefreshed();
        binding.autoRefreshToggle.setChecked(autoRefreshEnabled);

        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        int SDK_INT = android.os.Build.VERSION.SDK_INT;
        if (SDK_INT > 8)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);
            //your codes here

        }

        binding.generateButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                String pii = binding.piiInputTextinput.getText().toString();
                TokenGenerateInput tokenGenerateInput;
                if (isEmail(pii)) {
                    tokenGenerateInput = TokenGenerateInput.fromEmail(pii);
                } else if (isPhoneNumber(pii)) {
                    tokenGenerateInput = TokenGenerateInput.fromPhone(pii);
                } else {
                    new AlertDialog.Builder(getContext())
                            .setTitle("Error!").setMessage("What you input is neither email nor phone").show();
                    return;
                }
                IdentityTokens it = publisherUid2Client.generateToken(tokenGenerateInput);
                try {
                    UID2Manager.shared.setIdentity(UID2Identity.fromJsonString(it.getJsonString()));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        binding.resetButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                UID2Manager.shared.resetIdentity();
            }
        });

        binding.autoRefreshToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                UID2Manager.shared.toggleAutoTokenRefresh(isChecked);
            }
        });

        binding.manualRefreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UID2Manager.shared.refreshIdentity();
            }
        });

        binding.buttonSecond.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(SecondFragment.this)
                        .navigate(R.id.action_SecondFragment_to_FirstFragment);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private boolean isEmail(String str) {
        return Patterns.EMAIL_ADDRESS.matcher(str).matches();
    }

    private boolean isPhoneNumber(String str) {
        return Patterns.PHONE.matcher(str).matches();
    }

}