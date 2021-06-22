package de.calitobundo.twitch.desktop.api.pwn;

import java.util.concurrent.TimeUnit;

import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Converter;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

public class PwnApiServiceGenerator {
 
    private static final OkHttpClient sharedClient;
    private static final Converter.Factory converterFactory = JacksonConverterFactory.create();

    static {

        Dispatcher dispatcher = new Dispatcher();
        dispatcher.setMaxRequestsPerHost(500);
        dispatcher.setMaxRequests(500);
        sharedClient = new OkHttpClient.Builder()
                .dispatcher(dispatcher)
                .pingInterval(20, TimeUnit.SECONDS)
                //.addInterceptor(new AccessTokenInterceptor())
                .build();


    }


    public static <S> S createService(Class<S> serviceClass) {

        Retrofit.Builder retrofitBuilder = new Retrofit.Builder()
                .baseUrl("https://pwn.sh/")
                .addConverterFactory(converterFactory)
                .client(sharedClient);

        Retrofit retrofit = retrofitBuilder.build();
        return retrofit.create(serviceClass);

    }


    public static <T> T executeSync(Call<T> call) {
        try {
            Response<T> response = call.execute();
            //System.out.println("ResponseCode: "+response.code());
            if (response.isSuccessful()) {
                return response.body();
            } else {
               throw new Exception(response.errorBody().string());
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);

        }
    }
}
