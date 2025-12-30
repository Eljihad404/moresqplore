package com.example.moresqplore;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import android.util.Log;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
public class FirestoreIntegrationTest {

    private static final String TAG = "FirestoreTest";

    @Test
    public void checkCityDataExists() throws Exception {
        // 0. Ensure Data is Seeded (This fixes the failure if app hasn't run)
        CountDownLatch seedLatch = new CountDownLatch(1);
        com.example.moresqplore.data.FirebaseSeeder.seedData()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Log.d(TAG, "Seeding complete for test.");
                } else {
                    Log.e(TAG, "Seeding failed for test.", task.getException());
                }
                seedLatch.countDown();
            });
        
        // Wait for seeding (max 5 seconds)
        seedLatch.await(5, TimeUnit.SECONDS);


        // 1. Get Firestore Instance
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        // Use a latch to wait for the async callback
        CountDownLatch latch = new CountDownLatch(1);
        
        final boolean[] success = {false};

        // 2. Fetch "Casablanca"
        db.collection("cities").document("Casablanca").get()
                .addOnSuccessListener(documentSnapshot -> {
                    Log.d(TAG, "Document Snapshot: " + documentSnapshot);
                    if (documentSnapshot.exists()) {
                        success[0] = true;
                        Log.d(TAG, "Data exists! Name: " + documentSnapshot.getString("name"));
                    } else {
                        Log.e(TAG, "Document does not exist!");
                    }
                    latch.countDown();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching document", e);
                    latch.countDown();
                });

        // 3. Wait for result (timeout after 10 seconds)
        boolean completed = latch.await(10, TimeUnit.SECONDS);
        
        // 4. Assertions
        assertTrue("Timed out waiting for Firestore. Check internet connection.", completed);
        assertTrue("City 'Casablanca' should exist in Firestore. The seeder might have failed or permissions denied.", success[0]);
    }
}
