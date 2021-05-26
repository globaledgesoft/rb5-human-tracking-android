## Person Tracking Android App

#### Introduction
This project is intended to build and deploy a person tracking android application onto an android device that sends person images and also gets person tracking information.
The main objective of this project is to send person images to flask server either by selecting from gallery or by capture using camera and also in this project send person name to trigger track and get tracking person information using MQTT communication.

#### Deploying the project
1. Download code from the github link 
2. Compile  the code and run the application from Android Studio to generate application (apk) file.

#### How to install Android application
1. Android Phone with version 4.1 and above.
2. ADB installed in the Windows/ Linux system.
3. Install the application using the below command
   ```sh
   adb install app-debug.apk
   ```

#### How does it work?
The application will establish connection with the flask server using the ip address and port number of the server and it is important that both android phone and flask server are connected to the same network because we’re using local IPv4 addresses. If they are connected to different networks, then the connection will fail. After establishing a connection with the flask server. In person image fragment we will enter person name and we will select person image from gallery by clicking on button “SELECT PERSON PICTURE FROM GALLERY ” or we will take person picture using camera by clicking on button “ TAKE PERSON PICTURE FROM CAMERA ” and then we will send image to flask server and in flask server we will save image with person name. In the tracking person fragment we will enter person name and then click on the “ TRACK PERSON ” button to publish person name to mosquitto mqtt broker with topic “track” and then in server side they trigger person track and then from android application we will also subscribe to “tracked” topic once the person is tracked in server side, from server they we will publish tracked person information to the “tracked” topic, then android application  will print person tracking information in the tracking person fragment.

#### Permissions used in this android application are
To read gallery image need below permission
```sh
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
```
To take picture using camera need below permissions
```sh
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.CAMERA" />
```
Let our app have permissions to access the Internet, access the network state, and let our app stay alive as a service. Need below permissions
```sh
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.WAKE_LOCK"/>
```
#### Selecting person image from gallery
Below code is to select person image from gallery and selected image file send using sendImageFile function
```sh
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
```
#### Take person picture using camera
Below code is to take person picture using camera and write image data to file and then send image file using sendImageFile function
```sh
if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
   if (resultCode == Activity.RESULT_OK) {
       Bitmap bmp = (Bitmap) data.getExtras().get("data");
       ByteArrayOutputStream stream = new ByteArrayOutputStream();
       bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
       byte[] byteArray2 = stream.toByteArray();
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
```
#### Send image file to flask server
To send an image file first we will read the image file as a Bitmap using the BitmapFactory.decodeFile() method. To allow such a Bitmap to be sent in the HTTP message, it will be converted into a byte array. An Instance of MultipartBody class is created, which supports sending multi-part data in the HTTP. To add a multipart object, the addFormDataPart() method is used. The addFormDataPart() method as 3 arguments: 
1. Field name: Field name that will be used at the server to retrieve the file.
2. Filename: The filename of the uploaded file.
3. Body: The content of the body as a RequestBody instance.
```sh
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
```
#### Publish to a mosquitto mqtt broker
The publishMessage() method to publish messages with the specified payload, In our app payload is set to person name and topic is “track”. When we publish a person name to mosquitto broker on the server side they will start tracking the person.
```sh
private void publishMessage() {
   try {
       MqttMessage message = new MqttMessage();
       message.setPayload(payload.getBytes());
       message.setQos(0);
       mqttAndroidClient.publish(PUB_TOPIC, message, null, new IMqttActionListener() {
           @Override
           public void onSuccess(IMqttToken asyncActionToken) {
               Log.d("Mqtt", "publish succeed!");
           }
           @Override
           public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
               Log.d("Mqtt", "publish failed!");
           }
       });
   } catch (MqttException e) {
       Log.d("Mqtt", "Exception for publishing");
       e.printStackTrace();
   }
}
```
#### Subscribe to a mosquitto mqtt broker
The subscribeToTopic() method is used to subscribe to mosquitto mqtt broker with topic “tracked”. When a person is tracked on the server side they will publish tracked person information to the topic “tracked” that time in the app we will get tracked person information.
```sh
private void subscribeToTopic() {
   try {
       mqttAndroidClient.subscribe(subscriptionTopic, 0, null, new IMqttActionListener() {
           @Override
           public void onSuccess(IMqttToken asyncActionToken) {
               Log.d("Mqtt", "Subscribed!");
           }
           @Override
           public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
               Log.d("Mqtt", "Subscribed fail!");
           }
       });
   } catch (MqttException e) {
       Log.d("Mqtt", "Exception for subscribing");
       e.printStackTrace();
               }
}
```





