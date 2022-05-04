package com.example.bookapp1;

import static com.example.bookapp1.Constants.MAX_BYTES_PDF;

import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.util.Calendar;
import java.util.Locale;

// application class will run before launching activity
public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
    }

    // create a static method to convert timestamp to proper date format,
    // no need to rewrite again
    public static final String timestampFormat(long timeStamp)
    {
        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(timeStamp);

        // formatting timestamp to dd/mm/yyyy
        String date = DateFormat.format("dd/MM/yyyy", calendar).toString();

        return date;
    }

    public static void deleteBook(Context context, String bookId, String bookUrl, String bookTitle) {
        String TAG = "DELETE_BOOK_TAG";

        Log.d(TAG, "deleteBook: Deleting...");
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Please wait");
        progressDialog.setMessage("Deleting " + bookTitle + "...");
        progressDialog.show();

        Log.d(TAG, "deleteBook: Deleting from the storage....");
        StorageReference reference = FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl);
        reference.delete()
                .addOnSuccessListener
                        (
                                new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Log.d(TAG, "onSuccess: Deleted from storage");
                                        Log.d(TAG, "onSuccess: Deleting info from the database...");

                                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Books");
                                        databaseReference.child(bookId)
                                                .removeValue()
                                                .addOnSuccessListener
                                                        (
                                                                new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void unused) {
                                                                        Log.d(TAG, "onSuccess: Deleted from the database");
                                                                        progressDialog.dismiss();
                                                                        Toast.makeText(context, "Book deleted successfully!", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                }
                                                        )
                                                .addOnFailureListener
                                                        (
                                                                new OnFailureListener() {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception e) {
                                                                        Log.d(TAG, "onFailure: Deleting from the database failed");
                                                                        progressDialog.dismiss();
                                                                        Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                                    }
                                                                }
                                                        );
                                    }
                                }
                        )
                .addOnFailureListener
                        (
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d(TAG, "onFailure: Failed to delete from the storage due to " + e.getMessage());
                                        progressDialog.dismiss();
                                        Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                        );
    }

    public static void loadPdfSize(String pdfUrl, String pdfTitle, TextView sizeTV) {
        String TAG = "PDF_SIZE_TAG";
        // get file and its metadata from firebase storage by using url

        StorageReference reference = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);
        reference.getMetadata()
                .addOnSuccessListener
                        (
                                new OnSuccessListener<StorageMetadata>() {
                                    @Override
                                    public void onSuccess(StorageMetadata storageMetadata) {
                                        // getting size in bytes
                                        double bytes = storageMetadata.getSizeBytes();

                                        Log.d(TAG, "onSuccess: " + pdfTitle + " - " + bytes);

                                        // convert bytes to KB, MB
                                        double KB = bytes / 1024,
                                                MB = KB / 1024;

                                        if(MB >= 1)
                                        {
                                            sizeTV.setText(String.format("%.2f", MB) + "MB");
                                        }
                                        else if(KB >= 1)
                                        {
                                            sizeTV.setText(String.format("%.2f", KB) + "KB");
                                        }
                                        else
                                        {
                                            sizeTV.setText(String.format("%.2f", bytes) + "Bytes");
                                        }
                                    }
                                }
                        )
                .addOnFailureListener
                        (
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // failed getting metadata
                                        Log.d(TAG, "onFailure: " + e.getMessage());
                                    }
                                }
                        );
    }

    public static void loadPdfFromUrlSinglePage(String pdfUrl, String pdfTitle, PDFView pdfView, ProgressBar progressBar) {
        // get file and its metadata from firebase storage by using url
        String TAG = "PDF_LOAD_SINGLE_TAG";

        StorageReference reference = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);
        reference.getBytes(MAX_BYTES_PDF)
                .addOnSuccessListener
                        (
                                new OnSuccessListener<byte[]>() {
                                    @Override
                                    public void onSuccess(byte[] bytes) {
                                        Log.d(TAG, "onSuccess: " + pdfTitle + " successfully got the file!");

                                        // set to pdf view
                                        pdfView.fromBytes(bytes)
                                                .pages(0)
                                                .spacing(0)
                                                .swipeHorizontal(false)
                                                .enableSwipe(false)
                                                .onError
                                                        (
                                                                new OnErrorListener() {
                                                                    @Override
                                                                    public void onError(Throwable t) {
                                                                        // hide progress
                                                                        progressBar.setVisibility(View.INVISIBLE);
                                                                        Log.d(TAG, "onError: " + t.getMessage());
                                                                    }
                                                                }
                                                        )
                                                .onPageError
                                                        (
                                                                new OnPageErrorListener() {
                                                                    @Override
                                                                    public void onPageError(int page, Throwable t) {
                                                                        // hide progress
                                                                        progressBar.setVisibility(View.INVISIBLE);
                                                                        Log.d(TAG, "onPageError: " + t.getMessage());
                                                                    }
                                                                }
                                                        )
                                                .onLoad
                                                        (
                                                                new OnLoadCompleteListener() {
                                                                    @Override
                                                                    public void loadComplete(int nbPages) {
                                                                        // pdf loaded
                                                                        // hide progress
                                                                        progressBar.setVisibility(View.INVISIBLE);
                                                                        Log.d(TAG, "loadComplete: PDF file loaded!");
                                                                    }
                                                                }
                                                        ).load();
                                    }
                                }
                        )
                .addOnFailureListener
                        (
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        progressBar.setVisibility(View.INVISIBLE);
                                        Log.d(TAG, "onFailure: failed getting file from url due to " + e.getMessage());
                                    }
                                }
                        );
    }

    public static void loadCategory(String categoryId, TextView categoryTV) {
        // get category using categoryId

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Categories");
        reference.child(categoryId)
                .addListenerForSingleValueEvent
                        (
                                new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        // get category
                                        String category = "" + snapshot.child("category").getValue();

                                        // set to category text view
                                        categoryTV.setText(category);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                }
                        );
    }
}
