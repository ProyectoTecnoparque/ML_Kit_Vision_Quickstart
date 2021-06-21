    package com.google.mlkit.vision.demo;
    import android.content.Intent;
    import android.os.Bundle;
    import androidx.annotation.NonNull;
    import androidx.appcompat.app.AppCompatActivity;
    import androidx.appcompat.widget.Toolbar;
    import android.util.Log;
    import android.view.View;
    import android.widget.Button;
    import android.widget.EditText;
    import android.widget.Toast;

    import com.google.android.gms.auth.api.signin.GoogleSignIn;
    import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
    import com.google.android.gms.auth.api.signin.GoogleSignInClient;
    import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
    import com.google.android.gms.common.api.ApiException;
    import com.google.firebase.auth.AuthCredential;
    import com.google.firebase.auth.FirebaseAuth;
    import com.google.android.gms.tasks.OnCompleteListener;
    import com.google.android.gms.tasks.Task;
    import com.google.firebase.auth.AuthResult;
    import com.google.firebase.auth.FirebaseUser;
    import com.google.firebase.auth.GoogleAuthProvider;
    import com.google.firebase.database.core.utilities.Validation;

    import org.jetbrains.annotations.NotNull;
    import org.w3c.dom.Text;

    public class LoginActivity extends AppCompatActivity {
        Button btn_iniciar,btn_google;
        EditText campo_email,campo_password;
        int RC_SIGN_IN = 1;
        String TAG = "GoogleSignIn";

        private FirebaseAuth FirebaseAunt;
        private FirebaseAuth FirebaseAunt2;
        private FirebaseAuth.AuthStateListener firebaseAuthListener;
        private GoogleSignInClient mGoogleSignInClient;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_login);

            FirebaseAunt = FirebaseAuth.getInstance();

            campo_email = findViewById(R.id.campo_email);
            campo_password = findViewById(R.id.campo_password);
            btn_iniciar = findViewById(R.id.btn_iniciar);
            btn_google = findViewById(R.id.btn_google);

            btn_iniciar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view){
                    FirebaseAunt2 = FirebaseAuth.getInstance();
                    String correo = campo_email.getText().toString();
                    String password = campo_password.getText().toString();
                    if (!correo.equalsIgnoreCase("NaN") && !password.equalsIgnoreCase("NaN")){
                        FirebaseAunt.createUserWithEmailAndPassword(correo,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull @NotNull Task<AuthResult> task) {
                                Toast.makeText( LoginActivity.this, "Registro Exitoso!!", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        });

                    }else{
                        Toast.makeText( LoginActivity.this, "Error ingreso de datos!!", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            btn_google.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    singIn();
                }
            });
            // Configure Google Sign In
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build();

            mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

            //Controlar el estado del usuario
            firebaseAuthListener = new FirebaseAuth.AuthStateListener() {
                @Override
                public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                    if (firebaseAuth.getCurrentUser() != null){ //si no es null redirigir
                        Intent intent = new Intent(getApplicationContext(), EntryChoiceActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    }
                }
            };
        }
        private void singIn() {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);

            // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
            if (requestCode == RC_SIGN_IN) {
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                try {
                    // Google Sign In was successful, authenticate with Firebase
                    GoogleSignInAccount account = task.getResult(ApiException.class);
                    Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
                    firebaseAuthWithGoogle(account.getIdToken());
                } catch (ApiException e) {
                    // Google Sign In failed, update UI appropriately
                    Log.w(TAG, "Google sign in failed", e);
                }
            }
        }
        protected void onStart() {
            FirebaseAunt.addAuthStateListener(firebaseAuthListener);
            super.onStart();
        }
        private void firebaseAuthWithGoogle(String idToken) {
            AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
            FirebaseAunt.signInWithCredential(credential)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(TAG, "signInWithCredential:success");
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "signInWithCredential:failure", task.getException());
                            }
                        }
                    });
        }
    }
