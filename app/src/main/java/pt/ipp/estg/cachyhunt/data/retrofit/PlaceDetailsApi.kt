package pt.ipp.estg.cachyhunt.data.retrofit

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface PlaceDetailsApi {

    // A função foi ajustada para usar parâmetros fixos, então só vamos passar o "center" e "apiKey"
    @GET("staticmap")
    fun getStaticMap(
        @Query("style") style: String = MapConfig.STYLE,              // Usando o estilo fixo
        @Query("width") width: Int = MapConfig.WIDTH,                // Usando a largura fixa
        @Query("height") height: Int = MapConfig.HEIGHT,             // Usando a altura fixa
        @Query("center") center: String,                              // Coordenadas do centro
        @Query("zoom") zoom: Double = MapConfig.ZOOM,                // Usando o zoom fixo
        @Query("scaleFactor") scaleFactor: Int = MapConfig.SCALE_FACTOR,  // Usando o fator de escala fixo
        @Query("apiKey") apiKey: String  = MapConfig.API_KEY       //Usando API_KEY                     // Chave da API
    ): Call<ResponseBody>
}
