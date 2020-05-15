package com.example.proyecto_applepie;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.example.proyecto_applepie.Retrofit.IMyService;
import com.example.proyecto_applepie.Retrofit.RetrofitClient;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;

public class MainScreen extends AppCompatActivity {

    GoogleSignInClient mGoogleSignInClient;

    Button sign_out;
    TextView nameTV;
    Intent onBoardIntent;
    TextView emailTV;
    TextView idTV;
    ImageView photoIV;
    CompositeDisposable compositeDisposable = new CompositeDisposable();
    IMyService iMyService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);

        //Init servicio
        Retrofit retrofitClient = RetrofitClient.getInstance();
        iMyService = retrofitClient.create(IMyService.class);

        sign_out = findViewById(R.id.log_out);
        nameTV = findViewById(R.id.name);
        //emailTV = findViewById(R.id.email);
        //idTV = findViewById(R.id.id);
        photoIV = findViewById(R.id.photo);

        // Conf sign-in para pedir el ID del usuario, su direccion de correo e informacion de perfil basica
        // el ID y dicha informacion estan incluidas en DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        // Crear un GoogleSignInClient con las opciones especificadas en gso
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(MainScreen.this);
        if (acct != null) {

            String personName = acct.getDisplayName();
            String personGivenName = acct.getGivenName();
            String personFamilyName = acct.getFamilyName();
            String personEmail = acct.getEmail();
            String personId = acct.getId();
            Uri personPhoto = acct.getPhotoUrl();
            loginUser(personEmail, personName, personId);
            nameTV.setText(personName);
            //emailTV.setText("Email: "+personEmail);
            //idTV.setText("ID: "+personId);
            Glide.with(this).load(personPhoto).into(photoIV);


        }

        sign_out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signOut();
            }
        });
    }

    // Funcion de signOut
    private void signOut() {
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(MainScreen.this,"Successfully signed out",Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(MainScreen.this, MainActivity.class));
                        finish();
                    }
                });
    }

    // Funcion de signUp que se llamara cada vez que el usuario acceda a la aplicacion
    private void loginUser(String email, String name, String google_id) {
        compositeDisposable.add(iMyService.loginUser(email, name, google_id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String response) throws Exception {
                        Toast.makeText(MainScreen.this, ""+response, Toast.LENGTH_LONG).show();
                    }
                }));
    }

    public void launchOnboard(View v){
        onBoardIntent = new Intent(MainScreen.this, OnBoarding.class);
        startActivity(onBoardIntent);
    }

    @Override
    protected void onStop() {
        compositeDisposable.clear();
        super.onStop();
    }
}
