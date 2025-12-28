package com.example.moresqplore.data.network;

import com.google.gson.annotations.SerializedName;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface WikiService {
    @GET("page/summary/{title}")
    Call<WikiSummary> getSummary(@Path("title") String title);

    class WikiSummary {
        @SerializedName("extract")
        public String extract;
        
        @SerializedName("thumbnail")
        public WikiImage thumbnail;
        
        @SerializedName("content_urls")
        public ContentUrls contentUrls;
    }

    class WikiImage {
        @SerializedName("source")
        public String source;
    }
    
    class ContentUrls {
        @SerializedName("mobile")
        public MobileUrl mobile;
    }
    
    class MobileUrl {
        @SerializedName("page")
        public String page;
    }
}
