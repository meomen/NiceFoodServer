package com.vuducminh.nicefoodserver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.vuducminh.nicefoodserver.Common.Common;
import com.vuducminh.nicefoodserver.Common.CommonAgr;
import com.vuducminh.nicefoodserver.Model.ServerUserModel;

import java.util.Arrays;
import java.util.List;

import dmax.dialog.SpotsDialog;

public class MainActivity extends AppCompatActivity {

    private static int API_REQUEST_CODE = 1999;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener listener;
    private AlertDialog dialog;
    private DatabaseReference serverRef;
    private List<AuthUI.IdpConfig> providers;

    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(listener);
    }

    @Override
    protected void onStop() {
        if(listener != null) {
            firebaseAuth.removeAuthStateListener(listener);
        }
        super.onStop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    private void init() {

        providers = Arrays.asList(new AuthUI.IdpConfig.PhoneBuilder().build());

        firebaseAuth = FirebaseAuth.getInstance();
        serverRef = FirebaseDatabase.getInstance().getReference(CommonAgr.SERVER_REF);
        dialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();
        listener = firebaseAuthLocal -> {
            FirebaseUser user = firebaseAuthLocal.getCurrentUser();
            if(user != null) {
                // Check user from Firebase
                checkUserServerFromFirebase(user);
            }
            else {
                phoneLogin();
            }
        };
    }

    private void checkUserServerFromFirebase(FirebaseUser user) {
        dialog.show();
        serverRef.child(user.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()) {
                            ServerUserModel userModel = dataSnapshot.getValue(ServerUserModel.class);
                            if(userModel.isActive()) {
                                gotoHomeActivity(userModel);
                            }
                            else {
                                dialog.dismiss();
                                Toast.makeText(MainActivity.this,"You must be allowed from Admin to access this app",Toast.LENGTH_SHORT).show();
                            }
                        }
                        else{
                            // Usernot exists in database
                            dialog.dismiss();
                            showRegisterDialog(user);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(MainActivity.this,""+databaseError.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showRegisterDialog(FirebaseUser user) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Register");
        builder.setMessage("Please fill information \n " +
                "Admin will accept your account late");

        View itemView = LayoutInflater.from(this).inflate(R.layout.layout_register,null);
        EditText edt_name = (EditText)itemView.findViewById(R.id.edt_name);
        EditText edt_phone = (EditText)itemView.findViewById(R.id.edt_phone);

        //Set Data

        edt_phone.setText(user.getPhoneNumber());
        builder.setNegativeButton("CANCLE", (dialogInterface, which) -> {
            dialogInterface.dismiss();

        }).setPositiveButton("OK", (dialogInterface, which) -> {
            if(TextUtils.isEmpty(edt_name.getText().toString())) {
                Toast.makeText(MainActivity.this,"Please enter your name",Toast.LENGTH_SHORT).show();
                return;
            }

            ServerUserModel serverUserModel = new ServerUserModel();
            serverUserModel.setUid(user.getUid());
            serverUserModel.setName(user.getDisplayName());
            serverUserModel.setPhone(user.getPhoneNumber());
            serverUserModel.setActive(false);

            dialog.show();

            serverRef.child(serverUserModel.getUid())
                    .setValue(serverUserModel)
                    .addOnFailureListener(e -> {
                        dialog.dismiss();
                        Toast.makeText(MainActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
                    })
                    .addOnCompleteListener(task -> {
                        dialog.dismiss();
                        Toast.makeText(MainActivity.this,"Congratulation ! Register success ! Admin will check and active you soon",Toast.LENGTH_SHORT).show();
//                        gotoHomeActivity(serverUserModel);
                    });
        });

        builder.setView(itemView);
        androidx.appcompat.app.AlertDialog registerDialog = builder.create();
        registerDialog.show();
    }

    private void gotoHomeActivity(ServerUserModel serverUserModel) {

        dialog.dismiss();
        Common.currentServerUser = serverUserModel;
        startActivity(new Intent(this,HomeActivity.class));
        finish();
    }


    private void phoneLogin() {
        startActivityForResult(AuthUI.getInstance()
        .createSignInIntentBuilder()
        .setAvailableProviders(providers)
        .build(),API_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == API_REQUEST_CODE) {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            if(resultCode == RESULT_OK) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            }
            else {
                Toast.makeText(this,"Failed to sign in",Toast.LENGTH_SHORT).show();
            }
        }
    }
}
