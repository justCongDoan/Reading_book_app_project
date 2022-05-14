package com.example.bookapp1.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.bookapp1.MyApplication;
import com.example.bookapp1.R;
import com.example.bookapp1.databinding.RowCommentBinding;
import com.example.bookapp1.models.CommentModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.HolderComment> {

    // context
    private Context context;
    // create arraylist to hold comments
    private ArrayList<CommentModel> commentModelArrayList;

    private FirebaseAuth firebaseAuth;

    // view binding
    private RowCommentBinding binding;

    // create constructor
    public CommentAdapter(Context context, ArrayList<CommentModel> commentModelArrayList) {
        this.context = context;
        this.commentModelArrayList = commentModelArrayList;

        firebaseAuth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public HolderComment onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // inflating/ binding the view xml
        binding = RowCommentBinding.inflate(LayoutInflater.from(context), parent, false);
        return new HolderComment(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderComment holder, int position) {
        // get data from specific position of the list, set data, handle click, etc

        // get data
        CommentModel commentModel = commentModelArrayList.get(position);
        String id = commentModel.getId(),
                bookId = commentModel.getBookId(),
                comment = commentModel.getComment(),
                uid = commentModel.getComment(),
                timestamp = commentModel.getTimestamp();

        // formatting date (this function has been created in MyApplication class already)
        String date = MyApplication.timestampFormat(Long.parseLong(timestamp));

        // setting data
        holder.dateTV.setText(date);
        holder.commentTV.setText(comment);
        
        // since we don't have user's name, profile picture => we'll load it using uid we stored in each comment
        loadUserDetails(commentModel, holder);

        // handle click => show option to delete comment
        holder.itemView.setOnClickListener
                (
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // In order to delete a comment:
                                //      1) User must have logged in.
                                //      2) uid in comment (to be deleted) must be the same with the uid of logged in user
                                if (firebaseAuth.getCurrentUser() != null && uid.equals(firebaseAuth.getUid()))
                                {
                                    deleteComment(commentModel, holder);
                                }
                            }
                        }
                );
    }

    private void deleteComment(CommentModel commentModel, HolderComment holder) {
        // show confirm dialog before deleting comment
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Delete comment")
                .setMessage("Are you sure you want to delete this comment?")
                .setPositiveButton
                        (
                                "DELETE",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        // Delete from dialog has been clicked, begin deleting
                                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Books");
                                        reference.child(commentModel.getBookId())
                                                .child("Comments")
                                                .child(commentModel.getBookId())
                                                .removeValue()
                                                .addOnSuccessListener
                                                        (
                                                                new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void unused) {
                                                                        Toast.makeText(context, "Deleted comment successfully", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                }
                                                        )
                                                .addOnFailureListener
                                                        (
                                                                new OnFailureListener() {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception e) {
                                                                        Toast.makeText(context, "Failed to delete due to " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                                    }
                                                                }
                                                        );
                                    }
                                }
                        )
                .setNegativeButton
                        (
                                "CANCEL",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        // cancel has been clicked
                                        dialogInterface.dismiss();
                                    }
                                }
                        ).show();
    }

    private void loadUserDetails(CommentModel commentModel, HolderComment holder) {
        String uid = commentModel.getUid();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(uid)
                .addListenerForSingleValueEvent
                        (
                                new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        // get data
                                        String name = "" + snapshot.child("name").getValue(),
                                                profileImage = "" + snapshot.child("profileImage").getValue();

                                        // set data
                                        holder.nameTV.setText(name);
                                        try {
                                            Glide.with(context)
                                                    .load(profileImage)
                                                    .placeholder(R.drawable.ic_person_gray)
                                                    .into(holder.profileSIV);
                                        }
                                        catch (Exception e) {
                                            holder.profileSIV.setImageResource(R.drawable.ic_person_gray);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                }
                        );
    }

    @Override
    public int getItemCount() {
        return commentModelArrayList.size();   // return comments size, the number of records
    }

    // create view holder class for row_comment.xml
    class HolderComment extends RecyclerView.ViewHolder
    {
        // UI views of row_comment.xml
        ShapeableImageView profileSIV;
        TextView nameTV, dateTV, commentTV;

        public HolderComment(@NonNull View itemView) {
            super(itemView);

            // initializing UI views
            profileSIV = binding.profileSIV2ID;
            nameTV = binding.nameTVID;
            dateTV = binding.dateTV2ID;
            commentTV = binding.commentTVID;
        }
    }
}
