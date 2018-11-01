package lrandomdev.com.online.mp3player;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.JsonObject;

import lrandomdev.com.online.mp3player.helpers.ApiServices;
import lrandomdev.com.online.mp3player.helpers.Helpers;
import lrandomdev.com.online.mp3player.helpers.RestClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Lrandom on 4/13/18.
 */

public class ActivityAboutUs extends AppCompatActivity {
    ImageView btnFacebook,btnInstagram,btnTwitter,btnYoutube;
    LinearLayout wrapVersion,wrapEmail,wrapPhone,wrapWebsite,wrapAddress;
    TextView txtVersion,txtEmail,txtPhone,txtWebsite,txtAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.about));

        ApiServices apiService= RestClient.getApiService();
        Call<JsonObject> call = apiService.getGeneralSetting();
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                JsonObject jsonObject= response.body();
                String content = jsonObject.get("about").getAsString();
                TextView tvContent = findViewById(R.id.tvContent);
                content=content.replace("\n", "").replace("\r", "");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    tvContent.setText(Html.fromHtml(content,Html.FROM_HTML_MODE_COMPACT));
                }else{
                    tvContent.setText(Html.fromHtml(content));
                }

                String version = jsonObject.get("version").getAsString();
                if(version!=null && !version.equalsIgnoreCase("")){
                    txtVersion=(TextView)findViewById(R.id.txtVersion);
                    txtVersion.setText(version);
                }else{
                    wrapVersion=(LinearLayout)findViewById(R.id.wrapVersion);
                    wrapVersion.setVisibility(View.GONE);
                }

                String email = jsonObject.get("mail").getAsString();
                if(email!=null && !email.equalsIgnoreCase("")){
                    txtEmail=(TextView)findViewById(R.id.txtEmail);
                    txtEmail.setText(email);
                }else{
                    wrapEmail=(LinearLayout)findViewById(R.id.wrapEmail);
                    wrapEmail.setVisibility(View.GONE);
                }

                String phone = jsonObject.get("phone").getAsString();
                if(phone!=null && !phone.equalsIgnoreCase("")){
                    txtPhone=(TextView)findViewById(R.id.txtPhone);
                    txtPhone.setText(phone);
                }else{
                    wrapPhone=(LinearLayout)findViewById(R.id.wrapPhone);
                    wrapPhone.setVisibility(View.GONE);
                }

                String website = jsonObject.get("website").getAsString();
                if(website!=null && !website.equalsIgnoreCase("")){
                    txtWebsite=(TextView)findViewById(R.id.txtWebsite);
                    txtWebsite.setText(website);
                }else{
                    wrapWebsite=(LinearLayout)findViewById(R.id.wrapWebsite);
                    wrapWebsite.setVisibility(View.GONE);
                }

                String address = jsonObject.get("address").getAsString();
                if(address!=null && !address.equalsIgnoreCase("")){
                    txtAddress=(TextView)findViewById(R.id.txtAddress);
                    txtAddress.setText(address);
                }else{
                    wrapAddress=(LinearLayout)findViewById(R.id.wrapAddress);
                    wrapAddress.setVisibility(View.GONE);
                }

                final String facebook = jsonObject.get("facebook").getAsString();
                btnFacebook=(ImageView)findViewById(R.id.btnFacebook);
                if(facebook!=null && !facebook.equalsIgnoreCase("")){
                    btnFacebook.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Helpers.openWebsite(ActivityAboutUs.this,facebook);
                        }
                    });
                }else{
                    btnFacebook.setVisibility(View.GONE);
                }

                final String youtube = jsonObject.get("youtube").getAsString();
                btnYoutube=(ImageView)findViewById(R.id.btnYoutube);
                if(youtube!=null && !youtube.equalsIgnoreCase("")){
                    btnYoutube.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Helpers.openWebsite(ActivityAboutUs.this,youtube);
                        }
                    });
                }else{
                    btnYoutube.setVisibility(View.GONE);
                }

                final String insta = jsonObject.get("instagram").getAsString();
                btnInstagram=(ImageView)findViewById(R.id.btnInstagram);
                if(insta!=null && !insta.equalsIgnoreCase("")){
                    btnInstagram.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Helpers.openWebsite(ActivityAboutUs.this,insta);
                        }
                    });
                }else{
                    btnInstagram.setVisibility(View.GONE);
                }

                final String twitter = jsonObject.get("twitter").getAsString();
                btnTwitter=(ImageView)findViewById(R.id.btnTwitter);
                if(twitter!=null && !twitter.equalsIgnoreCase("")){
                    btnTwitter.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Helpers.openWebsite(ActivityAboutUs.this,twitter);
                        }
                    });
                }else{
                    btnTwitter.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {

            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }
}