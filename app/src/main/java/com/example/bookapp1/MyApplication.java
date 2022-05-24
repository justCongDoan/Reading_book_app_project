package com.example.bookapp1;

import static com.example.bookapp1.Constants.MAX_BYTES_PDF;

import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Environment;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

// application class will run before launching activity
public class MyApplication extends Application {

    private static final String TAG = "DOWNLOAD_TAG";

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

    public static void loadPdfFromUrlSinglePage(String pdfUrl, String pdfTitle, PDFView pdfView, ProgressBar progressBar, TextView pagesTV) {
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

                                                                        // if pagesTV param is not null => set the page number
                                                                        if (pagesTV != null)
                                                                        {
                                                                            // concatenate with double quotes because we can't set in the textView
                                                                            pagesTV.setText("" + nbPages);
                                                                        }
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

    public static void increasingBookViewCount(String bookId)
    {
        // 1) get book views count
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Books");
        reference.child(bookId)
                .addListenerForSingleValueEvent
                        (
                                new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        // get views count
                                        String viewsCount = "" + snapshot.child("viewsCount").getValue();

                                        // in case the null value is replaced with 0
                                        if(viewsCount.equals("") || viewsCount.equals("null"))
                                        {
                                            viewsCount = "0";
                                        }

                                        // 2) increase view count
                                        long newViewsCount = Long.parseLong(viewsCount) + 1;

                                        HashMap<String, Object> hashMap = new HashMap<>();
                                        hashMap.put("viewsCount", newViewsCount);

                                        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("Books");
                                        reference1.child(bookId)
                                                .updateChildren(hashMap);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                }
                        );
    }

    public static void downloadBook(Context context, String bookId, String bookTitle, String bookUrl)
    {
        Log.d(TAG, "downloadBook: downloading book...");

        String nameWithExtension = bookTitle + ".pdf";
        Log.d(TAG, "downloadBook: NAME" + nameWithExtension);

        // progress dialog
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Please wait.");
        progressDialog.setMessage("Downloading " + nameWithExtension + "...");  // e.g: abc.pdf
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        // download from firebase storage using url
        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl);
        storageReference.getBytes(MAX_BYTES_PDF)
                .addOnSuccessListener
                        (
                                new OnSuccessListener<byte[]>() {
                                    @Override
                                    public void onSuccess(byte[] bytes) {
                                        Log.d(TAG, "onSuccess: Book downloaded!");
                                        Log.d(TAG, "onSuccess: Saving book...");
                                        saveDownloadedBook(context, progressDialog, bytes, nameWithExtension, bookId);
                                    }
                                }
                        )
                .addOnFailureListener
                        (
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d(TAG, "onFailure: Failed to download due to " + e.getMessage());
                                        progressDialog.dismiss();
                                        Toast.makeText(context, "Failed to download due to" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                        );
    }

    private static void saveDownloadedBook(Context context, ProgressDialog progressDialog, byte[] bytes, String nameWithExtension, String bookId) {
        Log.d(TAG, "saveDownloadedBook: Saving downloaded book...");
        try
        {
            File downloadsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            downloadsFolder.mkdirs();

            String filePath = downloadsFolder.getPath() + "/" + nameWithExtension;

            FileOutputStream outputStream = new FileOutputStream(filePath);
            outputStream.write(bytes);
            outputStream.close();

            Toast.makeText(context, "Saved to Download Folder", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "saveDownloadedBook: Saved to Download Folder");
            progressDialog.dismiss();

            increasingDownloadedBook(bookId);
        }
        catch (Exception e)
        {
            Log.d(TAG, "saveDownloadedBook: Failed saving to Download Folder due to " + e.getMessage());
            Toast.makeText(context, "Failed saving to Download Folder due to " + e.getMessage(), Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
        }
    }

    private static void increasingDownloadedBook(String bookId) {
        Log.d(TAG, "increasingDownloadedBook: Increasing downloaded book count");

        // step 1) get previous download count
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Books");
        reference.child(bookId)
                .addListenerForSingleValueEvent
                        (
                                new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        String downloadsCount = "" + snapshot.child("downloadsCount").getValue();
                                        Log.d(TAG, "onDataChange: Downloads count: " + downloadsCount);

                                        if (downloadsCount.equals("") || downloadsCount.equals("null"))
                                        {
                                            downloadsCount = "0";
                                        }

                                        // convert to long and add 1
                                        long newDownloadsCount = Long.parseLong(downloadsCount) + 1;
                                        Log.d(TAG, "onDataChange: New download count: " + newDownloadsCount);

                                        // setup data to update
                                        HashMap<String, Object> hashMap = new HashMap<>();
                                        hashMap.put("downloadsCount", newDownloadsCount);

                                        // step 2) update new increased downloads count to the database
                                        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("Books");
                                        reference1.child(bookId).updateChildren(hashMap)
                                                .addOnSuccessListener
                                                        (
                                                                new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void unused) {
                                                                        Log.d(TAG, "onSuccess: Downloads count updated.");
                                                                    }
                                                                }
                                                        )
                                                .addOnFailureListener
                                                        (
                                                                new OnFailureListener() {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception e) {
                                                                        Log.d(TAG, "onFailure: Failed to update Downloads count due to " + e.getMessage());
                                                                    }
                                                                }
                                                        );
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                }
                        );
    }





    public static void addToFavourite(Context context, String bookId)
    {
        // we can add only if the user has logged in
        //      1) Check if the user has logged in
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() == null)
        {
            // not logged in => can't add the fav
            Toast.makeText(context, "You have not logged in yet!", Toast.LENGTH_SHORT).show();
        }
        else
        {
            long timestamp = System.currentTimeMillis();

            // setup data to add in the firebase database of current user for favorite book
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("bookId", "" + bookId);
            hashMap.put("timeStamp", "" + timestamp);

            // save to the database
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
            reference.child(firebaseAuth.getUid()).child("Favourite").child(bookId)
                    .setValue(hashMap)
                    .addOnSuccessListener
                            (
                                    new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            Toast.makeText(context, "Added to your favourite list!", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                            )
                    .addOnFailureListener
                            (
                                    new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(context, "Failed to add to favourite due to " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                            );
        }
    }

    public static void removeFromFavourite(Context context, String bookId)
    {
        // we can add and remove if the user has logged in
        //      1) Check if the user has logged in
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() == null)
        {
            // not logged in => can't remove from the fav
            Toast.makeText(context, "You have not logged in yet!", Toast.LENGTH_SHORT).show();
        }
        else
        {
            // remove the database
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
            reference.child(firebaseAuth.getUid()).child("Favourite").child(bookId)
                    .removeValue()
                    .addOnSuccessListener
                            (
                                    new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            Toast.makeText(context, "Removed to your favourite list!", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                            )
                    .addOnFailureListener
                            (
                                    new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(context, "Failed to remove to favourite due to " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                            );
        }
    }
}
