package eu.legnica.iilo.numerki;

import retrofit2.Call;
import retrofit2.http.GET;

public interface Api {

    @GET("/api/numerki")
    Call<ApiResponse> getNumbers();
}