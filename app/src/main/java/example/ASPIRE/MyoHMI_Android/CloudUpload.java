/*
 * Copyright 2015 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package example.ASPIRE.MyoHMI_Android;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ListActivity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.SimpleAdapter;
import android.widget.SimpleAdapter.ViewBinder;
import android.widget.TextView;
import android.widget.Toast;

//import com.amazonaws.demo.s3transferutility.Constants;import com.amazonaws.demo.s3transferutility.R;import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferType;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferType;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * UploadActivity is a ListActivity of uploading, and uploaded records as well
 * as buttons for managing the uploads and creating new ones.
 */
public class  CloudUpload {

    View view;
    Button cloudButton;
    Activity activity;
    Context context;
    // Indicates that no upload is currently selected
    private static final int INDEX_NOT_CHECKED = -1;

    // TAG for logging;
    private static final String TAG = "UploadActivity";

    // Button for upload operations
    private Button btnUploadFile;
    private Button btnUploadImage;

    // The TransferUtility is the primary class for managing transfer to S3
    private TransferUtility transferUtility;

    public CloudUpload(Context context) {
        transferUtility = Util.getTransferUtility(context);
        this.context = context;
//        activity = a;
//        cloudButton =(Button)view.findViewById(R.id.bt_cloud);
    }

//    public CloudUpload(){
//        transferUtility = util.getTransferUtility();
//    }

//    cloudButton.setOnClickListener(new
//
//    OnClickListener() {
//        @Override
//        public void onClick (View v){
//            Intent intent = new Intent();
//            if (Build.VERSION.SDK_INT >= 19) {
//                // For Android KitKat, we use a different intent to ensure
//                // we can
//                // get the file path from the returned intent URI
//                intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
//                intent.addCategory(Intent.CATEGORY_OPENABLE);
//                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
//                intent.setType("*/*");
//            } else {
//                intent.setAction(Intent.ACTION_GET_CONTENT);
//                intent.setType("file/*");
//            }
//
//            startActivityForResult(intent, 0);
//        }
//    });

    /*
      * Begins to upload the file specified by the file path.
      */
    public void beginUpload(File file) {

//        File file = new File(filePath);
        TransferObserver observer = transferUtility.upload(Credentials.BUCKET_NAME, file.getName(), file);
        TransferState state = observer.getState();
        TransferListener listener = new UploadListener();
//        observer.setTransferListener(new UploadListener());
//        Log.d(state.getState(), " Uploaded");
//        Log.d("Pool ID: ", Credentials.COGNITO_POOL_ID);
//        Log.d("Pool ID: ", Credentials.COGNITO_POOL_REGION);
//        Log.d("Pool ID: ", Credentials.BUCKET_REGION);
//        Log.d(file.getName(), " Uploaded");
        Log.d("State ", state.name());

//        while(state.name()=="WAITING"){
//            //wait for it to finish
//            observer.refresh();
//            state = observer.getState();
//            Log.d("State ", state.name());
//        }

//        if (TransferState.WAITING.equals(observer.getState())
//                || TransferState.WAITING_FOR_NETWORK.equals(observer.getState())
//                || TransferState.IN_PROGRESS.equals(observer.getState())) {
//            observer.setTransferListener(listener);
//        }
        /*
         * Note that usually we set the transfer listener after initializing the
         * transfer. However it isn't required in this sample app. The flow is
         * click upload button -> start an activity for image selection
         * startActivityForResult -> onActivityResult -> beginUpload -> onResume
         * -> set listeners to in progress transfers.
         */
//        observer.setTransferListener(new UploadListener());
    }
}

class UploadListener implements TransferListener {

    // Simply updates the UI list when notified.
    @Override
    public void onError(int id, Exception e) {
        Log.e("asdfdsfd", "Error during upload: " + id, e);
//        updateList();
    }

    @Override
    public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
        Log.d("asdfdsfd", String.format("onProgressChanged: %d, total: %d, current: %d",
                id, bytesTotal, bytesCurrent));
//        updateList();
    }

    @Override
    public void onStateChanged(int id, TransferState newState) {
        Log.d("asdfdsfd", "onStateChanged: " + id + ", " + newState);
//        updateList();
    }
}
    /*
     * Gets the file path of the given Uri.
     */
//    @SuppressLint("NewApi")
//    private String getPath(Uri uri) throws URISyntaxException {
//        final boolean needToCheckUri = Build.VERSION.SDK_INT >= 19;
//        String selection = null;
//        String[] selectionArgs = null;
//        // Uri is different in versions after KITKAT (Android 4.4), we need to
//        // deal with different Uris.
//        if (needToCheckUri && DocumentsContract.isDocumentUri(context, uri)) {
//            if (isExternalStorageDocument(uri)) {
//                final String docId = DocumentsContract.getDocumentId(uri);
//                final String[] split = docId.split(":");
//                return Environment.getExternalStorageDirectory() + "/" + split[1];
//            } else if (isDownloadsDocument(uri)) {
//                final String id = DocumentsContract.getDocumentId(uri);
//                uri = ContentUris.withAppendedId(
//                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
//            } else if (isMediaDocument(uri)) {
//                final String docId = DocumentsContract.getDocumentId(uri);
//                final String[] split = docId.split(":");
//                final String type = split[0];
//                if ("image".equals(type)) {
//                    uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
//                } else if ("video".equals(type)) {
//                    uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
//                } else if ("audio".equals(type)) {
//                    uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
//                }
//                selection = "_id=?";
//                selectionArgs = new String[]{
//                        split[1]
//                };
//            }
//        }
//        if ("content".equalsIgnoreCase(uri.getScheme())) {
//            String[] projection = {
//                    MediaStore.Images.Media.DATA
//            };
//            Cursor cursor = null;
//            try {
//                cursor = getContentResolver().query(uri, projection, selection, selectionArgs, null);
//                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
//                if (cursor.moveToFirst()) {
//                    return cursor.getString(column_index);
//                }
//            } catch (Exception e) {
//            }
//        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
//            return uri.getPath();
//        }
//        return null;
//    }
////
//    /**
//     * @param uri The Uri to check.
//     * @return Whether the Uri authority is ExternalStorageProvider.
//     */
//    public static boolean isExternalStorageDocument(Uri uri) {
//        return "com.android.externalstorage.documents".equals(uri.getAuthority());
//    }
//
//    /**
//     * @param uri The Uri to check.
//     * @return Whether the Uri authority is DownloadsProvider.
//     */
//    public static boolean isDownloadsDocument(Uri uri) {
//        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
//    }
//
//    /**
//     * @param uri The Uri to check.
//     * @return Whether the Uri authority is MediaProvider.
//     */
//    public static boolean isMediaDocument(Uri uri) {
//        return "com.android.providers.media.documents".equals(uri.getAuthority());
//    }
//}