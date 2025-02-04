package pt.ipp.estg.cachyhunt.data.retrofit

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.HttpURLConnection
import java.net.URL

object RetrofitHelper {
    // Base URL da Geoapify para a API de detalhes de local
    private const val GEOAPIFY_BASE_URL = "https://api.geoapify.com/v1/"
    // Base URL do OpenWeatherMap para a API de clima
    private const val OPENWEATHERMAP_BASE_URL = "https://api.openweathermap.org/data/2.5/"

    private const val OPENWEATHER_ICON_URL = "https://openweathermap.org/img/wn/"

    fun getGeoapifyInstance(): Retrofit {
        Log.e("Retrofit", GEOAPIFY_BASE_URL)
        return Retrofit.Builder()
            .baseUrl(GEOAPIFY_BASE_URL) // Usando a base URL da Geoapify
            .addConverterFactory(GsonConverterFactory.create()) // Para conversão de JSON para objetos Kotlin
            .build()
    }

    fun getOpenWeatherMapInstance(): Retrofit {
        Log.e("Retrofit", OPENWEATHERMAP_BASE_URL)
        return Retrofit.Builder()
            .baseUrl(OPENWEATHERMAP_BASE_URL) // Usando a base URL do OpenWeatherMap
            .addConverterFactory(GsonConverterFactory.create()) // Para conversão de JSON para objetos Kotlin
            .build()
    }

    fun getOpenWeatherIconUrl(iconCode: String): String {
        return "$OPENWEATHER_ICON_URL$iconCode@2x.png"
    }

    suspend fun fetchBitmapFromUrl(url: String): Bitmap? {
        return withContext(Dispatchers.IO) {
            try {
                val connection = URL(url).openConnection() as HttpURLConnection
                connection.doInput = true
                connection.connect()
                val inputStream = connection.inputStream
                BitmapFactory.decodeStream(inputStream)
            } catch (e: Exception) {
                Log.e("RetrofitHelper", "Error fetching bitmap from URL", e)
                null
            }
        }
    }
}
