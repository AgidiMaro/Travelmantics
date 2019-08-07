package com.travelmantics;


import android.content.Intent;
import android.content.res.Resources;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;


public class DealActivity extends AppCompatActivity {
    private DatabaseReference mDatabaseReference;
    private FirebaseDatabase mFirebaseDatabase;
    private ChildEventListener mChildEventListener;
    public static final int PICTURE_RESULT=42;
    EditText txtPrice;
    EditText txtDescription;
    EditText txtTitle;
    TravelDeal deal;
    ImageView imageView;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICTURE_RESULT && resultCode == RESULT_OK){
            if(data ==null){
                return;
            }

            else{

                Uri imageUri = data.getData();
                final StorageReference ref = FirebaseUtil.mStorageRef.child(imageUri.getLastPathSegment());
                final UploadTask uploadTask = ref.putFile(imageUri);

                final Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }
                        return ref.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Uri downloadUrl = task.getResult();
                             String url = downloadUrl.toString();
                             String pictureName = task.getResult().getPath();
                             deal.setImageURL(url);
                             deal.setImageName(pictureName);
                             showImage(url);
                        } else {

                        }
                    }
                });


            }



            }

                 // upload file to Firebase

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deal);

        // Instantiate the FirebaseBase using the Firebase Util class
        mFirebaseDatabase=FirebaseUtil.mFirebaseDatabase;
        mDatabaseReference = FirebaseUtil.mDatabaseReference;

        txtDescription = (EditText)findViewById(R.id.txtDescription);
        txtPrice = (EditText)findViewById(R.id.txtPrice);
        txtTitle = (EditText)findViewById(R.id.txtTitle);
        imageView = (ImageView)findViewById(R.id.image) ;

        Button btnImage = findViewById(R.id.btnImage);
        btnImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpeg");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY,true);
                startActivityForResult(intent.createChooser(intent,"Insert Picture"),PICTURE_RESULT);
            }
        });

        Intent intent = getIntent();
        TravelDeal deal = (TravelDeal) intent.getSerializableExtra("Deal");
        if(deal==null){
            deal = new TravelDeal();
        }
        this.deal = deal;
        txtTitle.setText(deal.getTitle());
        txtPrice.setText(deal.getPrice());
        txtDescription.setText(deal.getDescription());
        showImage(deal.getImageURL());
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.save_menu,menu);

        if(FirebaseUtil.isAdmin){
            menu.findItem(R.id.delete_menu).setVisible(true);
            menu.findItem(R.id.save_menu).setVisible(true);
            enableEditText(true);
            findViewById(R.id.btnImage).setEnabled(true);

        }

        else{
            menu.findItem(R.id.delete_menu).setVisible(false);
            menu.findItem(R.id.save_menu).setVisible(false);
            enableEditText(false);
            findViewById(R.id.btnImage).setEnabled(false);
        }

        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.save_menu:
                saveDeal();
                Toast.makeText(this,"Deal Saved",Toast.LENGTH_LONG).show();
                clean();
                bactToList();
                return true;
            case R.id.delete_menu:
                deleteDeal();
                Toast.makeText(this,"Deal Deleted",Toast.LENGTH_LONG).show();
                bactToList();
                return true;

                default:
                    return super.onOptionsItemSelected(item);
        }

    }

    private void saveDeal() {
        deal.setTitle(txtTitle.getText().toString());
        deal.setPrice(txtPrice.getText().toString());
        deal.setDescription(txtDescription.getText().toString());

        if(deal.getId()==null){
            mDatabaseReference.push().setValue(deal); // new deal
        }
        else {
            mDatabaseReference.child(deal.getId()).setValue(deal);  // Edit
        }
        }


    private void deleteDeal(){
        if(deal==null){
            Toast.makeText(this,"Please save the deal before delete", Toast.LENGTH_LONG).show();
            return;
        }

        mDatabaseReference.child(deal.getId()).removeValue();
        if(deal.getImageName()!=null && !deal.getImageName().isEmpty() ){
            StorageReference picRef = FirebaseUtil.mStorage.getReference().child(deal.getImageName());
            picRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });
        }
    }

    private void bactToList(){
        Intent intent = new Intent (this, ListActivity.class);
        startActivity(intent);

    }

    private void clean(){
         txtTitle.setText("");
        txtPrice.setText("");
        txtDescription.setText("");
        txtTitle.requestFocus();
    }

    private void enableEditText(boolean isEnabled){
        txtTitle.setEnabled(isEnabled);
        txtPrice.setEnabled(isEnabled);
        txtDescription.setEnabled(isEnabled);
    }

    private void showImage(String url){
        if(url!=null && url.isEmpty()==false){
            int width = Resources.getSystem().getDisplayMetrics().widthPixels;
            Picasso.with(this)
                    .load(url)
                    .resize(width,width*2/3)
                    .centerCrop()
                    .into(imageView);
        }
    }



}
