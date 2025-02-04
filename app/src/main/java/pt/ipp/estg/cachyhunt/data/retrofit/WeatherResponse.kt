package pt.ipp.estg.cachyhunt.data.retrofit

data class WeatherResponse(
    val weather: List<Weather>
)

data class Weather(
    val icon: String,
    val description : String
)