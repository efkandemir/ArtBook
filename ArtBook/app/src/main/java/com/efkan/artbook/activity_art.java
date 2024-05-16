package com.efkan.artbook;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.service.credentials.Action;
import android.view.View;
import android.os.Bundle;
import android.widget.Toast;

import com.efkan.artbook.databinding.ActivityArtBinding;
import com.google.android.material.snackbar.Snackbar;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class activity_art extends AppCompatActivity {
    private ActivityArtBinding binding;
    ActivityResultLauncher<Intent> activityResultLauncher;  //galeriye eriştiğimiz kısım
    ActivityResultLauncher<String> permissionlauncher;   //izin istediğimiz kısım
    Bitmap selectedImage;
    SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityArtBinding.inflate(getLayoutInflater());
        View view=binding.getRoot();
        setContentView(view);
        registerLauncher();
        database=this.openOrCreateDatabase("Arts",MODE_PRIVATE,null);

        Intent intent=getIntent();
        String info=intent.getStringExtra("info");
        if(info.equals("new")){
            //new art
            binding.nameText.setText("");
            binding.artistName.setText("");
            binding.yearText.setText("");
            binding.button.setVisibility(View.VISIBLE);
            binding.imageView.setImageResource(R.drawable.selectimage);
        }
        else {
            int artId=intent.getIntExtra("id",0);
            binding.button.setVisibility(View.INVISIBLE);
            try{
                Cursor cursor=database.rawQuery("SELECT * FROM arts WHERE id=?",new String[]{String.valueOf(artId)});
                int nameIx=cursor.getColumnIndex("artName");
                int painterIx=cursor.getColumnIndex("painterName");
                int yearIx=cursor.getColumnIndex("year");
                int imageIx=cursor.getColumnIndex("image");
                while(cursor.moveToNext())
                {
                    binding.nameText.setText(cursor.getString(nameIx));
                    binding.artistName.setText(cursor.getString(painterIx));
                    binding.yearText.setText(cursor.getString(yearIx));

                    byte[] bytes=cursor.getBlob(imageIx);
                    Bitmap bitmap= BitmapFactory.decodeByteArray(bytes,0,bytes.length);    //byte dizisini çöz manasında. byte dizisini burada bitmapa çeviriyor.
                    binding.imageView.setImageBitmap(bitmap);
                    binding.imageView.setEnabled(false);
                    binding.nameText.setEnabled(false);
                    binding.artistName.setEnabled(false);
                    binding.yearText.setEnabled(false);
                }
                cursor.close();
            }catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    public void save(View view)
    {
    String name=binding.nameText.getText().toString();
    String artistName=binding.artistName.getText().toString();
    String year=binding.yearText.getText().toString();

    //Burada küçülttüğüm resmi 1lere 0lara çevirerek sqlite'a kaydettim.
    Bitmap smallImage=makeSmallerImage(selectedImage,300);
    ByteArrayOutputStream outputStream=new ByteArrayOutputStream();
    smallImage.compress(Bitmap.CompressFormat.PNG,50,outputStream);   //burada compress yaparak resmi sıkıştırdı.
    byte[] byteArray=outputStream.toByteArray();   //byteArray dizisine 1 ve 0 halinde kaydetti.

        try{
            database.execSQL("CREATE TABLE IF NOT EXISTS arts(id INTEGER PRIMARY KEY,artName VARCHAR,painterName VARCHAR,year VARCHAR,image BLOB)");
            String sqlString="INSERT INTO arts(artName,painterName,year,image) VALUES(?,?,?,?)";
            SQLiteStatement sqLiteStatement= database.compileStatement(sqlString);
            sqLiteStatement.bindString(1,name);
            sqLiteStatement.bindString(2,artistName);
            sqLiteStatement.bindString(3,year);
            sqLiteStatement.bindBlob(4,byteArray);
            sqLiteStatement.execute();

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        //ilk baştaki ekrana geri gelmek için bu kodlara yazacağım
        Intent intent=new Intent(activity_art.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        /*Şu an içinde olduğum aktivite dahil bütün aktiviteleri kapat ve yeni açtığım aktiviteyi çalıştır .
        */
        startActivity(intent);

    }
    private Bitmap makeSmallerImage(Bitmap image,int maximumSize)    //resimleri veri tabanına kaydetmek için boyutunu küçültmek lazım o yüzden bu şekilde yaptık .
    {
        int width=image.getWidth();
        int height=image.getHeight();
        float bitmapRatio=(float)width/(float)height;  //ratio=oran
        if(bitmapRatio>1)
        {
            //landscape ımage = yatay resim
            width=maximumSize;
            height=(int)(width/bitmapRatio);
        }
        else {
            //portrait image
            height=maximumSize;
            width=(int)(height*bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image,width,height,true);      //eşit oranda küçültüp veri tabanına kaydetmek için makeSmallerImage sınıfını oluşturduk.
        //scaled ölçek demek . ya yukarı ölçeklendir ya da aşağı ölçeklendir yani küçült.
    }

    public void selectImage(View view)
    {
        /*read  external storage iznini alacağız . Alacağımız izin tehlikeli olduğu için ilk önce manifeste ekliyoruz
        ve sadece manifeste eklemek de yetmiyor.Daha önceki android versiyonlarında bazı izinleri istemeye gerek olmayabilir.
        adamın hangi api sürümünü kullandığını bilmediğimiz için ContextCompat kullanırız .
         */


         if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.TIRAMISU)
         {
             //android 33+(33 ve üstüyse) (bu izni kullanıyoruz READ_MEDIA_IMAGES)
             if(ContextCompat.checkSelfPermission(this,Manifest.permission.READ_MEDIA_IMAGES)!=PackageManager.PERMISSION_GRANTED)  //granted=varsa
             {
                 if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_MEDIA_IMAGES))  //izin isteme mantığını göstereyim mi?
                 {
                     Snackbar.make(view,"Permission needed for gallery",Snackbar.LENGTH_INDEFINITE).setAction("Give Permisson", new View.OnClickListener() {
                         @Override
                         public void onClick(View v) {
                             // request permission
                             permissionlauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
                         }
                     }).show();

                 }
                 else {
                     //request permission
                     permissionlauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
                 }
             }
             else
             {
                 Intent intentTogallery=new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);  //pick seçip almak gibi.  yan taraftaki biraz ağır gibi.ezber yap.
                 activityResultLauncher.launch(intentTogallery);
             }

         }
         else {
             if(ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED)  //granted=varsa
             {
                 if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_EXTERNAL_STORAGE))  //izin isteme mantığını göstereyim mi?
                 {
                     Snackbar.make(view,"Permission needed for gallery",Snackbar.LENGTH_INDEFINITE).setAction("Give Permisson", new View.OnClickListener() {
                         @Override
                         public void onClick(View v) {
                             // request permission
                             permissionlauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                         }
                     }).show();

                 }
                 else {
                     //request permission
                     permissionlauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                 }
             }
             else
             {
                 Intent intentTogallery=new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);  //pick seçip almak gibi.  yan taraftaki biraz ağır gibi.ezber yap.
                 activityResultLauncher.launch(intentTogallery);
             }
         }





    }

    private void registerLauncher()
    {
        //burası galeriye gittikten sonraki kontrol
       activityResultLauncher=registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
           @Override
           public void onActivityResult(ActivityResult o) {
           if(o.getResultCode()==RESULT_OK)
           {
               Intent intentFromResult=o.getData();
               if(intentFromResult!=null)
               {
                   Uri imageData=intentFromResult.getData();
                   //binding.imageView.setImageURI(imageData);     bu şekilde veri tabanına kaydedemeyiz bitmapa çevirmek lazım.
                   try {
                       if(Build.VERSION.SDK_INT >=28)
                       {
                           ImageDecoder.Source source = ImageDecoder.createSource(activity_art.this.getContentResolver(),imageData);
                           selectedImage=ImageDecoder.decodeBitmap(source);
                           binding.imageView.setImageBitmap(selectedImage);
                       }
                       else {  //versiyon 28den küçükse burası
                           selectedImage=MediaStore.Images.Media.getBitmap(activity_art.this.getContentResolver(),imageData);
                           binding.imageView.setImageBitmap(selectedImage);
                       }
                   } catch (IOException e) {

                       e.printStackTrace();
                   }
               }
           }
           }
       });

        //burası izini istediğimiz kısım.
        permissionlauncher=registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean o) {
                if(o)
                {
                    Intent intentTogallery=new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    activityResultLauncher.launch(intentTogallery);
                }
                else {
                    Toast.makeText(activity_art.this,"Permission needed!",Toast.LENGTH_LONG).show();
                }
            }
        });

    }

}