package com.example.quickcash.utilities;

import com.example.quickcash.entities.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class FirebaseCRUD {

    private final DatabaseReference databaseReference;
    private final FirebaseAuth auth;

    /**
     * Constructor for FirebaseCRUD objects
     */
    public FirebaseCRUD() {
        this.databaseReference = FirebaseDatabase.getInstance().getReference("users");
        this.auth = FirebaseAuth.getInstance();
    }

    /**
     * Method to register a user object into the Firebase database
     * @param user The User object of the user to add
     * @param password The password of the user
     * @param authListener Listener for the Google Authentication
     * @param dbListener Listener for the Firbase RTS
     */
    public void registerUser(User user, String password, OnCompleteListener<AuthResult> authListener, OnCompleteListener<Void> dbListener) {
        auth.createUserWithEmailAndPassword(user.getEmail(), password)
                .addOnCompleteListener(authTask -> {
                    authListener.onComplete(authTask);
                    if (authTask.isSuccessful()) {
                        FirebaseUser firebaseUser = auth.getCurrentUser();
                        if (firebaseUser != null) {
                            databaseReference.child(user.getUsername()).setValue(user).addOnCompleteListener(dbListener);
                        }
                    }
                });
    }

    public void readUser(String username, ValueEventListener valueEventListener) {
        databaseReference.child(username).addListenerForSingleValueEvent(valueEventListener);
    }

    /**
     * Method to update a user in firebase
     *
     * @param username The user's username
     * @param updatedUser The updated User object containing the user's information
     * @param onCompleteListener Function to execute after updating the user
     */
    public void updateUser(String username, User updatedUser, OnCompleteListener<Void> onCompleteListener) {
        databaseReference.child(username).setValue(updatedUser).addOnCompleteListener(onCompleteListener);
    }

    /**
     * Method to remove a user from the firebase
     * @param username The username of the user to remove from the database
     * @param onCompleteListener Function to execute after removing the user
     */
    public void deleteUser(String username, OnCompleteListener<Void> onCompleteListener) {
        databaseReference.child(username).removeValue().addOnCompleteListener(onCompleteListener);
    }
}
