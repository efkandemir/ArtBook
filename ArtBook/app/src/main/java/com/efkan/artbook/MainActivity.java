package com.efkan.artbook;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.View;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.efkan.artbook.databinding.ActivityMainBinding;

import java.util.ArrayList;

//bar kısmına options menüsü eklenmedi çözümü res->values->themes->themes dosyalarındaki action bar kısmını Theme.AppCompat.Light.DarkActionBar yap.!!!!
public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    ArrayList<Art> artArrayList;
    ArtAdapter artAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityMainBinding.inflate(getLayoutInflater());
        View view=binding.getRoot();
        setContentView(view);

        artArrayList=new ArrayList<>();

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        artAdapter=new ArtAdapter(artArrayList);
        binding.recyclerView.setAdapter(artAdapter); //artAdapter.notifyDataSetChanged();  verileri aldığımız kısma bunu eklemeyi unutma.
        getData();

    }
    private void getData()
    {
        try{
            SQLiteDatabase sqLiteDatabase=this.openOrCreateDatabase("Arts",MODE_PRIVATE,null);
            Cursor cursor=sqLiteDatabase.rawQuery("SELECT * FROM arts",null);
            int nameIx=cursor.getColumnIndex("artName");
            int idIx=cursor.getColumnIndex("id");
            while(cursor.moveToNext())
            {
             String name=cursor.getString(nameIx);
             int id=cursor.getInt(idIx);
             Art art=new Art(name,id);
             artArrayList.add(art);
            }
            artAdapter.notifyDataSetChanged();   /* veri seti değişmiş olabilir . yeni veriler geldiğinden recycler viewin haberi
            olmayabilir.o yüzden bir nevi kendini güncelle diye uyarıyoruz burada .        (veri kümesinin değiştiğini bildir)  */
            cursor.close();

        }catch (Exception e)
        {
            e.printStackTrace();
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {  //menu layoutunu kodumuza bağladığımız kısım
        MenuInflater menuInflater=getMenuInflater();   
        menuInflater.inflate(R.menu.art_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {  //options menü seçilirse ne olacak .onu anlatan kısım.
        if(item.getItemId()==R.id.add_art){//birden fazla options menü olabilir seçilen item eğer buysa bunları yap gibisinden if yazdık.
            Intent intent=new Intent(this, activity_art.class);
            intent.putExtra("info","new");
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }


}