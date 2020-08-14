package com.example.chat.Adapter;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chat.Activity.ImageViewActivity;
import com.example.chat.Activity.VideoViewActivity;
import com.example.chat.Model.Messages;
import com.example.chat.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Messages> UserMessageList;
    private DatabaseReference userRef;
    private FirebaseAuth mAuth;

    Handler seekHandler = new Handler();
    Runnable run;

    public MessageAdapter(List<Messages> UserMessageList) {
        this.UserMessageList = UserMessageList;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.
                custom_messages_layout, parent, false);
        MessageViewHolder messageViewHolder = new MessageViewHolder(view);
        mAuth = FirebaseAuth.getInstance();

        return messageViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, final int position) {
        String messagesenderid = mAuth.getCurrentUser().getUid();
        final Messages messages = UserMessageList.get(position);

        String fromuserid = messages.getFrom();
        String frommessagetype = messages.getType();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(fromuserid);
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("image")) {
                    String receiverprofileimage = dataSnapshot.child("image").getValue().toString();
                    Picasso.get().load(receiverprofileimage).placeholder(R.drawable.profile_image).into(holder.receiverprofileimage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

///////////////////////////////////////////////////////////////
        holder.receivermessagetext.setVisibility(View.GONE);
        holder.receiverprofileimage.setVisibility(View.GONE);
        holder.sendermessagetext.setVisibility(View.GONE);
        holder.messageSenderPicture.setVisibility(View.GONE);
        holder.messageReceiverPicture.setVisibility(View.GONE);
        holder.messageSenderVideo.setVisibility(View.GONE);
        holder.messageReceiverVideo.setVisibility(View.GONE);
        holder.messageSenderMusic.setVisibility(View.GONE);
        holder.messageReceiverMusic.setVisibility(View.GONE);

        //////////////////////////////////////////////////////////////////
        if (frommessagetype.equals("text")) {
            if (fromuserid.equals(messagesenderid)) {
                holder.sendermessagetext.setVisibility(View.VISIBLE);
                holder.sendermessagetext.setBackgroundResource(R.drawable.sender_message_layout);
                holder.sendermessagetext.setText(messages.getMessage() + "\n \n" + messages.getTime() + " - " + messages.getDate());
            } else {
                holder.receivermessagetext.setVisibility(View.VISIBLE);
                holder.receiverprofileimage.setVisibility(View.VISIBLE);


                holder.receivermessagetext.setBackgroundResource(R.drawable.receiver_message_layout);
                holder.receivermessagetext.setText(messages.getMessage() + "\n \n" + messages.getTime() + " - " + messages.getDate());
            }
        } else if (frommessagetype.equals("image")) {

            if (fromuserid.equals(messagesenderid)) {
                holder.messageSenderPicture.setVisibility(View.VISIBLE);
                Picasso.get().load(messages.getMessage()).into(holder.messageSenderPicture);
            } else {

                holder.messageReceiverPicture.setVisibility(View.VISIBLE);
                holder.receiverprofileimage.setVisibility(View.VISIBLE);
                Picasso.get().load(messages.getMessage()).into(holder.messageReceiverPicture);
            }
        }
        //////////////////////////////////////////////////////
        else if (frommessagetype.equals("video")) {
            Messages upload = UserMessageList.get(position);
            if (fromuserid.equals(messagesenderid)) {
                if (upload.getMessage() != null) {
                    holder.messageSenderVideo.setVisibility(View.VISIBLE);
                    holder.messageSenderVideo.start();
                }
                holder.messageSenderVideo.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        holder.messageSenderVideo.start();
                        return false;
                    }
                });
            } else {
                if (upload.getMessage() != null) {
                    holder.messageReceiverVideo.setVisibility(View.VISIBLE);
                    holder.receiverprofileimage.setVisibility(View.VISIBLE);
                    holder.messageReceiverVideo.start();
                }
                holder.messageReceiverVideo.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        holder.messageReceiverVideo.start();
                        return false;
                    }
                });
            }

        }
        //////////////////////////////////////////////////
        else if (frommessagetype.equals("audio")) {
            if (fromuserid.equals(messagesenderid)) {
                final Messages upload = UserMessageList.get(position);
                final MediaPlayer mediaPlayer = new MediaPlayer();
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                try {
                    mediaPlayer.setDataSource(upload.getMessage());
                    mediaPlayer.prepare();// might take long for buffering.
                } catch (IOException e) {
                    e.printStackTrace();
                }


                if (upload.getMessage() != null) {
                    holder.messageSenderMusic.setVisibility(View.VISIBLE);
                    holder.seekBarSender.setMax(mediaPlayer.getDuration());
                    holder.seekBarSender.setTag(position);

                    holder.seekBarSender.setOnSeekBarChangeListener(new SeekBar.
                            OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress,
                                                      boolean fromUser) {
                            if (mediaPlayer != null && fromUser) {
                                mediaPlayer.seekTo(progress);
                            }
                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {

                        }

                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {

                        }
                    });

                    holder.playSender.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (!mediaPlayer.isPlaying()) {
                                mediaPlayer.start();
                                holder.playSender.setBackgroundResource(R.drawable.pause);
                                run = new Runnable() {
                                    @Override
                                    public void run() {
                                        // Updateing SeekBar every 100 miliseconds
                                        holder.seekBarSender.setProgress(mediaPlayer.
                                                getCurrentPosition());
                                        seekHandler.postDelayed(run, 100);
                                        //For Showing time of audio(inside runnable)
                                        int miliSeconds = mediaPlayer.getCurrentPosition();
                                        if (miliSeconds != 0) {
                                            //if audio is playing, showing current time;
                                            long minutes = TimeUnit.MILLISECONDS.toMinutes(miliSeconds);
                                            long seconds = TimeUnit.MILLISECONDS.toSeconds(miliSeconds);
                                            if (minutes == 0) {
                                                holder.tvAudioLengthSender.setText("0:" + seconds + "/" + calculateDuration(mediaPlayer.getDuration()));
                                            } else {
                                                if (seconds >= 60) {
                                                    long sec = seconds - (minutes * 60);
                                                    holder.tvAudioLengthSender.setText(minutes + ":" + sec + "/" + calculateDuration(mediaPlayer.getDuration()));
                                                }
                                            }
                                        } else {
                                            //Displaying total time if audio not playing
                                            int totalTime = mediaPlayer.getDuration();
                                            long minutes = TimeUnit.MILLISECONDS.toMinutes(totalTime);
                                            long seconds = TimeUnit.MILLISECONDS.toSeconds(totalTime);
                                            if (minutes == 0) {
                                                holder.tvAudioLengthSender.setText("0:" + seconds);
                                            } else {
                                                if (seconds >= 60) {
                                                    long sec = seconds - (minutes * 60);
                                                    holder.tvAudioLengthSender.setText(minutes + ":" + sec);
                                                }
                                            }
                                        }
                                    }

                                };
                                run.run();
                            } else {
                                mediaPlayer.pause();
                                holder.playSender.setBackgroundResource(R.drawable.play);
                            }
                        }
                    });
                }

            } else {
                final Messages upload = UserMessageList.get(position);
                final MediaPlayer mediaPlayer = new MediaPlayer();
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                try {
                    mediaPlayer.setDataSource(upload.getMessage());
                    mediaPlayer.prepare();// might take long for buffering.
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (upload.getMessage() != null) {
                    holder.messageReceiverMusic.setVisibility(View.VISIBLE);
                    holder.seekBarReceiver.setMax(mediaPlayer.getDuration());
                    holder.seekBarReceiver.setTag(position);

                    holder.seekBarReceiver.setOnSeekBarChangeListener(new SeekBar.
                            OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress,
                                                      boolean fromUser) {
                            if (mediaPlayer != null && fromUser) {
                                mediaPlayer.seekTo(progress);
                            }
                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {

                        }

                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {

                        }
                    });

                    holder.playReceiver.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (!mediaPlayer.isPlaying()) {
                                mediaPlayer.start();
                                holder.playReceiver.setBackgroundResource(R.drawable.pause);
                                run = new Runnable() {
                                    @Override
                                    public void run() {
                                        // Updateing SeekBar every 100 miliseconds
                                        holder.seekBarReceiver.setProgress(mediaPlayer.
                                                getCurrentPosition());
                                        seekHandler.postDelayed(run, 100);
                                        //For Showing time of audio(inside runnable)
                                        int miliSeconds = mediaPlayer.getCurrentPosition();
                                        if (miliSeconds != 0) {
                                            //if audio is playing, showing current time;
                                            long minutes = TimeUnit.MILLISECONDS.toMinutes(miliSeconds);
                                            long seconds = TimeUnit.MILLISECONDS.toSeconds(miliSeconds);
                                            if (minutes == 0) {
                                                holder.tvAudioLengthReceiver.setText("0:" + seconds + "/" + calculateDuration(mediaPlayer.getDuration()));
                                            } else {
                                                if (seconds >= 60) {
                                                    long sec = seconds - (minutes * 60);
                                                    holder.tvAudioLengthReceiver.setText(minutes + ":" + sec + "/" + calculateDuration(mediaPlayer.getDuration()));
                                                }
                                            }
                                        } else {
                                            //Displaying total time if audio not playing
                                            int totalTime = mediaPlayer.getDuration();
                                            long minutes = TimeUnit.MILLISECONDS.toMinutes(totalTime);
                                            long seconds = TimeUnit.MILLISECONDS.toSeconds(totalTime);
                                            if (minutes == 0) {
                                                holder.tvAudioLengthReceiver.setText("0:" + seconds);
                                            } else {
                                                if (seconds >= 60) {
                                                    long sec = seconds - (minutes * 60);
                                                    holder.tvAudioLengthReceiver.setText(minutes + ":" + sec);
                                                }
                                            }
                                        }
                                    }

                                };
                                run.run();
                            } else {
                                mediaPlayer.pause();
                                holder.playReceiver.setBackgroundResource(R.drawable.play);
                            }
                        }
                    });

                }
            }
        }
        /////////////////////////////////////////////////////
        else if (frommessagetype.equals("pdf")) {
            if (fromuserid.equals(messagesenderid)) {
                holder.messageSenderPicture.setVisibility(View.VISIBLE);
                Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/chatapp-9849c.appspot.com/o/Image%20Files%2Ffile.png?alt=media&token=34fb4f27-54c8-4c06-b6de-59b6b8deddd2")
                        .into(holder.messageSenderPicture);


            } else {

                holder.messageReceiverPicture.setVisibility(View.VISIBLE);
                Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/chatapp-9849c.appspot.com/o/Image%20Files%2Ffile.png?alt=media&token=34fb4f27-54c8-4c06-b6de-59b6b8deddd2")
                        .into(holder.messageReceiverPicture);

                holder.receiverprofileimage.setVisibility(View.VISIBLE);

            }
        }


        ///////////////////////////////////////////////////////
        if (fromuserid.equals(messagesenderid)) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (UserMessageList.get(position).getType().equals("pdf")) {
                        CharSequence options[] = new CharSequence[]
                                {
                                        "Delete for me", "Download and view content", "Cancel", "Delete for everyone"
                                };

                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0) {
                                    deleteSentMessage(position, holder);

                                } else if (which == 1) {
                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(UserMessageList.get(position).getMessage()));
                                    holder.itemView.getContext().startActivity(intent);
                                } else if (which == 2) {
                                    //for cancel do not do anything
                                } else if (which == 3) {
                                    deleteMessageForEveryone(position, holder);

                                }
                            }
                        });

                        builder.show();
                    } else if (UserMessageList.get(position).getType().equals("text")) {
                        CharSequence options[] = new CharSequence[]
                                {
                                        "Delete for me", "Cancel", "Delete for everyone"
                                };

                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0) {
                                    deleteSentMessage(position, holder);

                                } else if (which == 1) {
                                    //for cancel do not do anything
                                } else if (which == 2) {
                                    deleteMessageForEveryone(position, holder);

                                }

                            }
                        });

                        builder.show();
                    }
                    else if (UserMessageList.get(position).getType().equals("image")) {
                        CharSequence options[] = new CharSequence[]
                                {
                                        "Delete for me", "View This Image", "Cancel", "Delete for everyone"
                                };

                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0) {
                                    deleteSentMessage(position, holder);

                                } else if (which == 1) {
                                    Intent intent = new Intent(holder.itemView.getContext(), ImageViewActivity.class);
                                    intent.putExtra("url", UserMessageList.get(position).getMessage());
                                    holder.itemView.getContext().startActivity(intent);

                                } else if (which == 2) {
                                    //for cancel do not do anything
                                } else if (which == 3) {
                                    deleteMessageForEveryone(position, holder);

                                }
                            }
                        });

                        builder.show();
                    }
                    else if (UserMessageList.get(position).getType().equals("video")) {
                        CharSequence options[] = new CharSequence[]
                                {
                                        "Delete for me", "View This Video", "Cancel", "Delete for everyone"
                                };

                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0) {
                                    deleteSentMessage(position, holder);

                                } else if (which == 1) {
                                    Intent intent = new Intent(holder.itemView.getContext(), VideoViewActivity.class);
                                    intent.putExtra("video", UserMessageList.get(position).getMessage());
                                    holder.itemView.getContext().startActivity(intent);

                                } else if (which == 2) {
                                    //for cancel do not do anything
                                } else if (which == 3) {
                                    deleteMessageForEveryone(position, holder);

                                }
                            }
                        });

                        builder.show();
                    }
                    else if (UserMessageList.get(position).getType().equals("audio")) {
                        CharSequence options[] = new CharSequence[]
                                {
                                        "Delete for me", "Cancel", "Delete for everyone"
                                };

                        AlertDialog.Builder builder = new AlertDialog.Builder(holder
                                .itemView.getContext());
                        builder.setTitle("Delete Message?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0) {
                                    deleteSentMessage(position, holder);

                                } else if (which == 1) {
                                    //for cancel do not do anything
                                } else if (which == 2) {
                                    deleteMessageForEveryone(position, holder);

                                }

                            }
                        });

                        builder.show();
                    }

                }
            });
        } else {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (UserMessageList.get(position).getType().equals("pdf")) {
                        CharSequence options[] = new CharSequence[]
                                {
                                        "Delete for me", "Download and view content", "Cancel"
                                };

                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0) {
                                    deleteReceiveMessage(position, holder);

                                } else if (which == 1) {
                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(UserMessageList.get(position).getMessage()));
                                    holder.itemView.getContext().startActivity(intent);
                                } else if (which == 2) {
                                    //for cancel do not do anything
                                }

                            }
                        });

                        builder.show();
                    } else if (UserMessageList.get(position).getType().equals("text")) {
                        CharSequence options[] = new CharSequence[]
                                {
                                        "Delete for me", "Cancel"
                                };

                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0) {
                                    deleteReceiveMessage(position, holder);

                                } else if (which == 1) {
                                    //for cancel do not do anything
                                }

                            }
                        });

                        builder.show();
                    }
                    else if (UserMessageList.get(position).getType().equals("image")) {
                        CharSequence options[] = new CharSequence[]
                                {
                                        "Delete for me", "View This Image", "Cancel"
                                };

                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0) {
                                    deleteReceiveMessage(position, holder);

                                } else if (which == 1) {
                                    Intent intent = new Intent(holder.itemView.getContext(), ImageViewActivity.class);
                                    intent.putExtra("url", UserMessageList.get(position).getMessage());
                                    holder.itemView.getContext().startActivity(intent);

                                } else if (which == 2) {
                                    //for cancel do not do anything
                                }

                            }
                        });

                        builder.show();
                    }
                    else if (UserMessageList.get(position).getType().equals("video")) {
                        CharSequence options[] = new CharSequence[]
                                {
                                        "Delete for me", "View This Video", "Cancel"
                                };

                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0) {
                                    deleteReceiveMessage(position, holder);

                                } else if (which == 1) {
                                    Intent intent = new Intent(holder.itemView.getContext(), VideoViewActivity.class);
                                    intent.putExtra("video", UserMessageList.get(position).getMessage());
                                    holder.itemView.getContext().startActivity(intent);

                                } else if (which == 2) {
                                    //for cancel do not do anything
                                }

                            }
                        });

                        builder.show();
                    }
                    else if (UserMessageList.get(position).getType().equals("audio")) {
                        CharSequence options[] = new CharSequence[]
                                {
                                        "Delete for me", "Cancel"
                                };

                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.
                                itemView.getContext());
                        builder.setTitle("Delete Message?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0) {
                                    deleteReceiveMessage(position, holder);

                                } else if (which == 2) {
                                    //for cancel do not do anything
                                }

                            }
                        });

                        builder.show();
                    }
                }
            });
        }

    }

    /////////////////////////////////////////////////////////////
    private void deleteSentMessage(final int position, final MessageViewHolder holder) {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Messages").child(UserMessageList.get(position).getFrom())
                .child(UserMessageList.get(position).getTo()).child(UserMessageList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    notifyItemRemoved(position);
                    UserMessageList.remove(position);
                    notifyItemRangeChanged(position, UserMessageList.size());
                    Toast.makeText(holder.itemView.getContext(), "Message deleted...", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(holder.itemView.getContext(), "Error...", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void deleteReceiveMessage(final int position, final MessageViewHolder holder) {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Messages").child(UserMessageList.get(position).getTo())
                .child(UserMessageList.get(position).getFrom()).child(UserMessageList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    notifyItemRemoved(position);
                    UserMessageList.remove(position);
                    notifyItemRangeChanged(position, UserMessageList.size());
                    Toast.makeText(holder.itemView.getContext(), "Message deleted...", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(holder.itemView.getContext(), "Error...", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void deleteMessageForEveryone(final int position, final MessageViewHolder holder) {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Messages").child(UserMessageList.get(position).getFrom())
                .child(UserMessageList.get(position).getTo()).child(UserMessageList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
                    rootRef.child("Messages").child(UserMessageList.get(position).getTo())
                            .child(UserMessageList.get(position).getFrom()).child(UserMessageList.get(position).getMessageID())
                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                notifyItemRemoved(position);
                                UserMessageList.remove(position);
                                notifyItemRangeChanged(position, UserMessageList.size());
                                Toast.makeText(holder.itemView.getContext(), "Message deleted...", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(holder.itemView.getContext(), "Error...", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else {
                    Toast.makeText(holder.itemView.getContext(), "Error...", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return UserMessageList.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {

        public TextView sendermessagetext, receivermessagetext;
        public CircularImageView receiverprofileimage;
        public ImageView messageSenderPicture, messageReceiverPicture;
        public VideoView messageSenderVideo, messageReceiverVideo;

        public LinearLayout messageReceiverMusic, messageSenderMusic;
        public SeekBar seekBarReceiver, seekBarSender;
        public Button playReceiver, playSender;
        public TextView tvAudioLengthReceiver, tvAudioLengthSender;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            sendermessagetext = itemView.findViewById(R.id.sender_message_text);
            receivermessagetext = itemView.findViewById(R.id.receiver_message_text);
            receiverprofileimage = itemView.findViewById(R.id.message_profile_image);
            messageSenderPicture = itemView.findViewById(R.id.message_sender_image_view);
            messageReceiverPicture = itemView.findViewById(R.id.message_receiver_image_view);
            messageSenderVideo = itemView.findViewById(R.id.message_sender_video_view);
            messageReceiverVideo = itemView.findViewById(R.id.message_receiver_video_view);
            messageSenderMusic = itemView.findViewById(R.id.message_sender_music);
            messageReceiverMusic = itemView.findViewById(R.id.message_receiver_music);

            playReceiver = itemView.findViewById(R.id.play_receiver);
            seekBarReceiver = itemView.findViewById(R.id.position_receiver);
            tvAudioLengthReceiver = itemView.findViewById(R.id.remainingTimeLabel_receiver);

            playSender = itemView.findViewById(R.id.play_sender);
            seekBarSender = itemView.findViewById(R.id.position_sender);
            tvAudioLengthSender = itemView.findViewById(R.id.remainingTimeLabel_sender);
        }
    }

    private String calculateDuration(int duration) {
        String finalDuration = "";
        long minutes = TimeUnit.MILLISECONDS.toMinutes(duration);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(duration);
        if (minutes == 0) {
            finalDuration = "0:" + seconds;
        } else {
            if (seconds >= 60) {
                long sec = seconds - (minutes * 60);
                finalDuration = minutes + ":" + sec;
            }
        }
        return finalDuration;
    }

}
