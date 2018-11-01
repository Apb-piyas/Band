package lrandomdev.com.online.mp3player;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.gson.JsonObject;

import lrandomdev.com.online.mp3player.helpers.ApiServices;
import lrandomdev.com.online.mp3player.helpers.RestClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivityPrivacyPolicy extends ActivityParent {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_policy);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.privacy_policy));

        ApiServices apiService= RestClient.getApiService();
        Call<JsonObject> call = apiService.getGeneralSetting();
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                JsonObject jsonObject= response.body();
                String content = jsonObject.get("privacy_policy").getAsString();
                TextView tvContent = findViewById(R.id.tvContent);
                content=content.replace("\n", "").replace("\r", "");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    tvContent.setText(Html.fromHtml(content,Html.FROM_HTML_MODE_COMPACT));
                }else{
                    tvContent.setText(Html.fromHtml(content));
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

