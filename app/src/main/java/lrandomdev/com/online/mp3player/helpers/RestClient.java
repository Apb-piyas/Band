package lrandomdev.com.online.mp3player.helpers;



import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Lrandom on 26/05/2017.
 */

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Lrandom on 26/05/2017.
 */
public class RestClient {
//    public static final String BASE_URL="http://lrandomdev.com/demo/cherry/";
//    public static final String ROOT_URL="http://lrandomdev.com/demo/cherry/api/";


    public static final String BASE_URL="http://banglamusic.xyz";
    public static final String ROOT_URL="http://banglamusic.xyz/api/";

    public static Retrofit getRetrofitInstance(){
        return new Retrofit.Builder()
                .baseUrl(ROOT_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public static ApiServices getApiService(){
        return getRetrofitInstance().create(ApiServices.class);
    }

}
