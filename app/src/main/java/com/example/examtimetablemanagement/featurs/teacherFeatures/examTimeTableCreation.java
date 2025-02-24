package com.example.examtimetablemanagement.featurs.teacherFeatures;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.TimePickerDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.media.MediaScannerConnection;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CalendarView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.examtimetablemanagement.R;
import com.example.examtimetablemanagement.authenTication.login.loginActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class examTimeTableCreation extends AppCompatActivity {

    private AutoCompleteTextView subjectInput;
    private TextInputEditText marksInput, durationInput;
    private AutoCompleteTextView examTypeInput;
    private CalendarView calendarView;
    private TextView selectedDateText, selectedTimeText;
    private MaterialButton timePickerButton;
    private AutoCompleteTextView roomInput;
    private TextInputEditText capacityInput;
    private AutoCompleteTextView invigilatorInput;
    private ChipGroup selectedInvigilatorsChipGroup;
    private TextInputEditText instructionsInput;
    private MaterialButton submitButton;
    private String selectedDate;
    private String selectedDay;
    private String selectedTime = "";
    private String userCollege;
    private ArrayList<ExamSchedule> examScheduleList = new ArrayList<>();
    private static final int REQUEST_WRITE_PERMISSION = 100;

    public static class ExamSchedule {
        String subject, marks, duration, examType, date, day, time, room, capacity, invigilator, instructions;
        public ExamSchedule(String subject, String marks, String duration, String examType, String date, String day,
                            String time, String room, String capacity, String invigilator, String instructions) {
            this.subject = subject;
            this.marks = marks;
            this.duration = duration;
            this.examType = examType;
            this.date = date;
            this.day = day;
            this.time = time;
            this.room = room;
            this.capacity = capacity;
            this.invigilator = invigilator;
            this.instructions = instructions;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exam_time_table_creation);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_WRITE_PERMISSION);
            }
        }
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
        View mainView = findViewById(R.id.main);
        ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        subjectInput = findViewById(R.id.subjectInput);
        marksInput = findViewById(R.id.marksInput);
        durationInput = findViewById(R.id.durationInput);
        examTypeInput = findViewById(R.id.examTypeInput);
        String[] examTypes = {"Internal", "External"};
        ArrayAdapter<String> examTypeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, examTypes);
        examTypeInput.setAdapter(examTypeAdapter);
        calendarView = findViewById(R.id.calendarView);
        selectedDateText = findViewById(R.id.selectedDateText);
        timePickerButton = findViewById(R.id.timePickerButton);
        selectedTimeText = findViewById(R.id.selectedTimeText);
        roomInput = findViewById(R.id.roomInput);
        capacityInput = findViewById(R.id.capacityInput);
        invigilatorInput = findViewById(R.id.invigilatorInput);
        selectedInvigilatorsChipGroup = findViewById(R.id.selectedInvigilatorsChipGroup);
        instructionsInput = findViewById(R.id.instructionsInput);
        submitButton = findViewById(R.id.submitButton);
        Calendar calendar = Calendar.getInstance();
        selectedDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(calendar.getTime());
        selectedDay = new SimpleDateFormat("EEEE", Locale.getDefault()).format(calendar.getTime());
        selectedDateText.setText(selectedDate);
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Calendar selectedCal = Calendar.getInstance();
            selectedCal.set(year, month, dayOfMonth);
            selectedDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(selectedCal.getTime());
            selectedDay = new SimpleDateFormat("EEEE", Locale.getDefault()).format(selectedCal.getTime());
            selectedDateText.setText(selectedDate);
        });
        timePickerButton.setOnClickListener(v -> {
            Calendar currentTime = Calendar.getInstance();
            int hour = currentTime.get(Calendar.HOUR_OF_DAY);
            int minute = currentTime.get(Calendar.MINUTE);
            new TimePickerDialog(examTimeTableCreation.this, (TimePicker view, int selectedHour, int selectedMinute) -> {
                selectedTime = String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute);
                selectedTimeText.setText(selectedTime);
            }, hour, minute, true).show();
        });
        submitButton.setOnClickListener(v -> {
            String subject = subjectInput.getText().toString().trim();
            String marks = marksInput.getText().toString().trim();
            String duration = durationInput.getText().toString().trim();
            String examType = examTypeInput.getText().toString().trim();
            String room = roomInput.getText().toString().trim();
            String capacity = capacityInput.getText().toString().trim();
            String invigilator = invigilatorInput.getText().toString().trim();
            String instructions = instructionsInput.getText().toString().trim();
            if (subject.isEmpty() || marks.isEmpty() || selectedTime.isEmpty()) {
                Toast.makeText(examTimeTableCreation.this, "Please fill in the mandatory fields", Toast.LENGTH_SHORT).show();
                return;
            }
            ExamSchedule exam = new ExamSchedule(subject, marks, duration, examType, selectedDate, selectedDay, selectedTime, room, capacity, invigilator, instructions);
            examScheduleList.add(exam);
            updateExamScheduleTable();
            Toast.makeText(examTimeTableCreation.this, "Exam schedule created successfully.", Toast.LENGTH_SHORT).show();
            subjectInput.setText("");
            marksInput.setText("");
            durationInput.setText("");
            examTypeInput.setText("");
            selectedTimeText.setText("");
            selectedTime = "";
            roomInput.setText("");
            capacityInput.setText("");
            invigilatorInput.setText("");
            instructionsInput.setText("");
            selectedInvigilatorsChipGroup.removeAllViews();
        });
        MaterialButton getImageButton = findViewById(R.id.getImageButton);
        getImageButton.setOnClickListener(v -> {
            if (examScheduleList.size() <= 1) {
                Toast.makeText(examTimeTableCreation.this, "Not enough exam schedules to export", Toast.LENGTH_SHORT).show();
                return;
            }
            TableLayout table = findViewById(R.id.examTable);
            table.measure(View.MeasureSpec.makeMeasureSpec(table.getWidth(), View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            table.layout(0, 0, table.getMeasuredWidth(), table.getMeasuredHeight());
            int tableWidth = table.getMeasuredWidth();
            int tableHeight = table.getMeasuredHeight();
            int maxDimension = Math.max(tableWidth, tableHeight);
            int scale = 4;
            int finalDimension = maxDimension * scale;
            Bitmap finalBitmap = Bitmap.createBitmap(finalDimension, finalDimension, Bitmap.Config.ARGB_8888);
            Canvas finalCanvas = new Canvas(finalBitmap);
            finalCanvas.drawColor(Color.WHITE);
            int left = (finalDimension - tableWidth * scale) / 2;
            int top = (finalDimension - tableHeight * scale) / 2;
            finalCanvas.save();
            finalCanvas.translate(left, top);
            finalCanvas.scale(scale, scale);
            table.draw(finalCanvas);
            finalCanvas.restore();
            String savedImagePath = saveBitmapToDownloads(finalBitmap);
            if (savedImagePath != null) {
                Toast.makeText(this, "Image saved: " + savedImagePath, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(examTimeTableCreation.this, "Error saving image", Toast.LENGTH_SHORT).show();
            }
        });
        MaterialButton exportTextButton = findViewById(R.id.exportTextButton);
        exportTextButton.setOnClickListener(v -> {
            if (examScheduleList.size() == 0) {
                Toast.makeText(examTimeTableCreation.this, "No exam schedules to export", Toast.LENGTH_SHORT).show();
                return;
            }
            String textData = generateTextData();
            String savedTextPath = saveTextToDownloads(textData);
            ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Exam Schedule", textData);
            clipboard.setPrimaryClip(clip);
            if (savedTextPath != null) {
                Toast.makeText(this, "Text exported and copied to clipboard", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(examTimeTableCreation.this, "Error saving text data", Toast.LENGTH_SHORT).show();
            }
        });
        // (Quick share button removed)
        loginActivity.SessionManagement session = new loginActivity.SessionManagement(this);
        userCollege = session.getCollege();
        if (userCollege == null || userCollege.isEmpty()) {
            Toast.makeText(this, "College not found!", Toast.LENGTH_SHORT).show();
            finish();
        }
        setupCollegeData();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_WRITE_PERMISSION) {
            if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                Toast.makeText(this, "Storage permission is required to save files.", Toast.LENGTH_SHORT).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void setupCollegeData() {
        DatabaseReference collegesRef = FirebaseDatabase.getInstance().getReference("colleges");
        collegesRef.orderByChild("name").equalTo(userCollege).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot collegeSnapshot : snapshot.getChildren()) {
                        List<String> invigilators = new ArrayList<>();
                        DataSnapshot invigilatorList = collegeSnapshot.child("invigilators").child("list");
                        for (DataSnapshot invigilator : invigilatorList.getChildren()) {
                            String name = invigilator.child("name").getValue(String.class);
                            if (name != null) {
                                invigilators.add(name);
                            }
                        }
                        Map<String, Integer> roomMap = new HashMap<>();
                        List<String> roomNumbers = new ArrayList<>();
                        DataSnapshot roomList = collegeSnapshot.child("room").child("listout");
                        for (DataSnapshot room : roomList.getChildren()) {
                            String roomNo = room.getKey();
                            Integer capacity = room.child("capacity").getValue(Integer.class);
                            if (roomNo != null && capacity != null) {
                                roomMap.put(roomNo, capacity);
                                roomNumbers.add(roomNo);
                            }
                        }
                        setupAdapters(invigilators, roomNumbers, roomMap);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(examTimeTableCreation.this, "Database error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupAdapters(List<String> invigilators, List<String> rooms, Map<String, Integer> roomCapacities) {
        ArrayAdapter<String> invigilatorAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, invigilators);
        invigilatorInput.setAdapter(invigilatorAdapter);
        ArrayAdapter<String> roomAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, rooms);
        roomInput.setAdapter(roomAdapter);
        roomInput.setOnItemClickListener((parent, view, position, id) -> {
            String selectedRoom = roomAdapter.getItem(position);
            if (selectedRoom != null && roomCapacities.containsKey(selectedRoom)) {
                capacityInput.setText(String.valueOf(roomCapacities.get(selectedRoom)));
            }
        });
    }

    private void updateExamScheduleTable() {
        TableLayout table = findViewById(R.id.examTable);
        int childCount = table.getChildCount();
        if (childCount > 1) {
            table.removeViews(1, childCount - 1);
        }
        for (ExamSchedule exam : examScheduleList) {
            TableRow row = new TableRow(this);
            row.setPadding(8, 8, 8, 8);
            TextView subjectView = new TextView(this);
            subjectView.setText(exam.subject);
            subjectView.setTextColor(getResources().getColor(android.R.color.black));
            row.addView(subjectView);
            TextView dateView = new TextView(this);
            dateView.setText(exam.date);
            dateView.setTextColor(getResources().getColor(android.R.color.black));
            row.addView(dateView);
            TextView timeView = new TextView(this);
            timeView.setText(exam.time);
            timeView.setTextColor(getResources().getColor(android.R.color.black));
            row.addView(timeView);
            TextView durationView = new TextView(this);
            durationView.setText(exam.duration + " mins");
            durationView.setTextColor(getResources().getColor(android.R.color.black));
            row.addView(durationView);
            TextView roomView = new TextView(this);
            roomView.setText(exam.room + " (" + exam.capacity + ")");
            roomView.setTextColor(getResources().getColor(android.R.color.black));
            row.addView(roomView);
            TextView invigilatorView = new TextView(this);
            invigilatorView.setText(exam.invigilator);
            invigilatorView.setTextColor(getResources().getColor(android.R.color.black));
            row.addView(invigilatorView);
            TextView actionView = new TextView(this);
            actionView.setText("View");
            actionView.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
            row.addView(actionView);
            table.addView(row);
        }
    }

    private String generateTextData() {
        StringBuilder sb = new StringBuilder();
        sb.append("Exam Schedule:\n\n");
        for (ExamSchedule exam : examScheduleList) {
            sb.append("Subject: ").append(exam.subject).append("\n");
            sb.append("Marks: ").append(exam.marks).append("\n");
            sb.append("Duration: ").append(exam.duration).append(" mins\n");
            sb.append("Type: ").append(exam.examType).append("\n");
            sb.append("Date: ").append(exam.date).append(" (").append(exam.day).append(")\n");
            sb.append("Time: ").append(exam.time).append("\n");
            sb.append("Room: ").append(exam.room).append(" (Capacity: ").append(exam.capacity).append(")\n");
            sb.append("Invigilator: ").append(exam.invigilator).append("\n");
            sb.append("Instructions: ").append(exam.instructions).append("\n");
            sb.append("---------------------------\n");
        }
        return sb.toString();
    }

    private String saveTextToDownloads(String textData) {
        String textFileName = "ExamSchedule_" + System.currentTimeMillis() + ".txt";
        String savedTextPath = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Files.FileColumns.DISPLAY_NAME, textFileName);
                values.put(MediaStore.Files.FileColumns.MIME_TYPE, "text/plain");
                values.put(MediaStore.Files.FileColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
                Uri uri = getContentResolver().insert(MediaStore.Files.getContentUri("external"), values);
                if (uri != null) {
                    OutputStream fos = getContentResolver().openOutputStream(uri);
                    if (fos != null) {
                        fos.write(textData.getBytes());
                        fos.flush();
                        fos.close();
                        savedTextPath = uri.toString();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                if (!storageDir.exists()) {
                    storageDir.mkdirs();
                }
                File textFile = new File(storageDir, textFileName);
                FileOutputStream fos = new FileOutputStream(textFile);
                fos.write(textData.getBytes());
                fos.flush();
                fos.close();
                savedTextPath = textFile.getAbsolutePath();
                MediaScannerConnection.scanFile(this, new String[]{savedTextPath}, null, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return savedTextPath;
    }

    private String saveBitmapToDownloads(Bitmap bitmap) {
        String imageFileName = "ExamSchedule_" + System.currentTimeMillis() + ".png";
        String savedImagePath = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DISPLAY_NAME, imageFileName);
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
                values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/ExamSchedules");
                Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                if (uri != null) {
                    OutputStream fos = getContentResolver().openOutputStream(uri);
                    if (fos != null) {
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                        fos.flush();
                        fos.close();
                        savedImagePath = uri.toString();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                File storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "ExamSchedules");
                if (!storageDir.exists()) {
                    storageDir.mkdirs();
                }
                File imageFile = new File(storageDir, imageFileName);
                FileOutputStream fos = new FileOutputStream(imageFile);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.flush();
                fos.close();
                savedImagePath = imageFile.getAbsolutePath();
                MediaScannerConnection.scanFile(this, new String[]{savedImagePath}, null, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return savedImagePath;
    }

    private void sendNotificationWithImage(String imagePath) {
        Bitmap bitmap = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                Uri uri = Uri.parse(imagePath);
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            bitmap = BitmapFactory.decodeFile(imagePath);
        }
        if (bitmap == null) {
            Toast.makeText(this, "Failed to load exported image", Toast.LENGTH_SHORT).show();
            return;
        }
        String channelId = "exam_schedule_channel";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Exam Schedule Notifications", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_email)
                .setContentTitle("Exam Schedule Exported")
                .setContentText("Your exam schedule image has been saved.")
                .setStyle(new NotificationCompat.BigPictureStyle().bigPicture(bitmap).setSummaryText("Tap to view"))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(1, builder.build());
    }

    private void sendNotificationForText(String textPath) {
        String channelId = "exam_schedule_text_channel";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Exam Schedule Text Notifications", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_email)
                .setContentTitle("Exam Schedule Exported")
                .setContentText("Your exam schedule text has been saved and copied.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(2, builder.build());
    }
}
