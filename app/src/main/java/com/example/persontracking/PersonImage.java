package com.example.persontracking;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.Button;
import java.io.ByteArrayOutputStream;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import android.widget.Toast;
import com.google.android.material.textfield.TextInputEditText;
import org.jetbrains.annotations.NotNull;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.CAMERA;


public class PersonImage extends Fragment {

    private String HOST_IP_ADDRESS = "0.00.00";
    private final int PORT_NUM = 5000;
    private String postUrl = null;
    private static String mFileName = null;
    private final String fileName = "/cap_img.jpg" ;
    private TextInputEditText personNametext;
    private String personName;
    public static final int REQUEST_CAMERA_PERMISSION_CODE = 1;
    public static final int REQUEST_SENT_PERMISSION_CODE = 2;
    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1888;
    Button button1;
    Button button;
    private static final int PICK_IMAGE = 100;
    Uri imageUri;

    public PersonImage() {
        // Required empty public constructor
    }


    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_personimage, container, false);
        button = view.findViewById(R.id.buttonLoadPicture);
        button1 = view.findViewById(R.id.buttonTakePicture);
        personNametext = view.findViewById(R.id.edit_text1);

        // Get Ip address from IpConnection
        Intent intent = getActivity().getIntent();
        HOST_IP_ADDRESS = intent.getStringExtra(String.valueOf(R.string.host_ip_address));

        postUrl= "http://"+HOST_IP_ADDRESS+":"+PORT_NUM+"/add_face";

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    if(galleryImageSentPermission()) {
                        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                        startActivityForResult(gallery, PICK_IMAGE);
                    }
                    else {
                        requestGalleryImageSentPermission();
                    }
            }
        });

        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(cameraImageSentPermission()) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent,
                            CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
                }
                else {
                    requestCameraImageSentPermission();
                }
            }
        });
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE ) {
            if (resultCode == Activity.RESULT_OK) {
                imageUri = data.getData();
                String[] filePathColumn = { MediaStore.Images.Media.DATA };
                Cursor cursor = getActivity().getContentResolver().query(imageUri,
                        filePathColumn, null, null, null);
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                mFileName = cursor.getString(columnIndex);
                cursor.close();
                sendImageFile(mFileName);
            }
        }

        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                Bitmap bmp = (Bitmap) data.getExtras().get("data");
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] byteArray2 = stream.toByteArray();
                //create a file to write bitmap data
                File file = null;
                try {
                    mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
                    mFileName += fileName;
                    file = new File(mFileName);
                    file.createNewFile();
                    //write the bytes in file
                    FileOutputStream fos = new FileOutputStream(file);
                    fos.write(byteArray2);
                    fos.flush();
                    fos.close();
                }catch (Exception e){
                    e.printStackTrace();
                }
                sendImageFile(mFileName);
            }
        }
    }

    public void postRequest(String postUrl, RequestBody postBody) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(postUrl)
                .post(postBody)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                Log.d("response from server :",""+response.body().string());

            }
            @Override
            public void onFailure(Call call, IOException e) {
                // Cancel the post on failure.
                call.cancel();
                Log.d("fail","failed to connect server");
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NotNull String[] permissions, @NotNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CAMERA_PERMISSION_CODE:
                if (grantResults.length> 0) {
                    boolean permissionToWrite = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean permissionToRead = grantResults[1] ==  PackageManager.PERMISSION_GRANTED;
                    boolean permissionToCamera = grantResults[2] ==  PackageManager.PERMISSION_GRANTED;
                    if (permissionToWrite && permissionToRead && permissionToCamera) {
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(intent,
                                CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
                        Log.d("permission","permission granted");
                    } else {
                        Log.d("permission","permission denied");
                    }
                }
                break;
            case REQUEST_SENT_PERMISSION_CODE:
                if (grantResults.length> 0) {
                    boolean permissionToRead = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (permissionToRead) {
                        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                        startActivityForResult(gallery, PICK_IMAGE);
                        Log.d("permission","permission granted");
                    } else {
                        Log.d("permission","permission denied");
                    }
                }
                break;
        }
    }

    public boolean galleryImageSentPermission() {
        int result1 = ContextCompat.checkSelfPermission(getContext(), READ_EXTERNAL_STORAGE);
        return result1 == PackageManager.PERMISSION_GRANTED;
    }

    public void requestGalleryImageSentPermission(){
            requestPermissions( new String[]{READ_EXTERNAL_STORAGE}, REQUEST_SENT_PERMISSION_CODE);
        }


    public boolean cameraImageSentPermission() {
        int result = ContextCompat.checkSelfPermission(getContext(), WRITE_EXTERNAL_STORAGE);
        int result1 = ContextCompat.checkSelfPermission(getContext(), READ_EXTERNAL_STORAGE);
        int result2 = ContextCompat.checkSelfPermission(getContext(), CAMERA);
        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED && result2 == PackageManager.PERMISSION_GRANTED;
    }

    public void requestCameraImageSentPermission() {
        requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE, CAMERA}, REQUEST_CAMERA_PERMISSION_CODE);
    }

    public void sendImageFile(String mFileName){

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        Bitmap bitmap = BitmapFactory.decodeFile(mFileName, options);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        personName = personNametext.getText().toString();
        if (personName.isEmpty()) {
            Toast.makeText(getContext(),"Please Enter Person Name",Toast.LENGTH_LONG).show();
            personName = personNametext.getText().toString();
        }
        personNametext.setText(null);
        if(!personName.isEmpty()) {
            RequestBody postBodyImage = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("image", personName + ".jpg", RequestBody.create(MediaType.parse("image/*jpg"), byteArray))
                    .build();
            postRequest(postUrl, postBodyImage);
        }
    }

}