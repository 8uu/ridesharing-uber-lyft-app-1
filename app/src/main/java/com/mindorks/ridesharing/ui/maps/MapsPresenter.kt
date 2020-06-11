package com.mindorks.ridesharing.ui.maps

import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.mindorks.ridesharing.data.network.NetworkService
import com.mindorks.ridesharing.simulator.WebSocket
import com.mindorks.ridesharing.simulator.WebSocketListener
import org.json.JSONObject
import com.mindorks.ridesharing.utils.Constants
import com.mindorks.ridesharing.utils.Constants.LAT
import com.mindorks.ridesharing.utils.Constants.LNG
import com.mindorks.ridesharing.utils.Constants.LOCATIONS
import com.mindorks.ridesharing.utils.Constants.NEAR_BY_CABS
import com.mindorks.ridesharing.utils.Constants.TYPE

class MapsPresenter(private val networkService: NetworkService) : WebSocketListener {

    private var view:MapsView? =null
    private lateinit var webSocket:WebSocket

    companion object{
        private const val TAG = "MapsPresenter"
    }

    fun onAttach(view: MapsView){
        this.view = view
        webSocket = networkService.createWebSocket(this)
        webSocket.connect()
    }

    fun requestNearbyCabs(latLng: LatLng){
        val jsonObject = JSONObject()
        jsonObject.put(TYPE,NEAR_BY_CABS)
        jsonObject.put(LAT,latLng.latitude)
        jsonObject.put(LNG,latLng.longitude)
        webSocket.sendMessage(jsonObject.toString())
    }

    override fun onConnect() {
        Log.d(TAG,"onConnect")
    }

    override fun onMessage(data: String) {
        Log.d(TAG,"onMessage : $data")
        val jsonObject = JSONObject(data)
        when(jsonObject.getString(TYPE))
        {
            NEAR_BY_CABS ->{
                handleOnMessageNearbyCabs(jsonObject)
            }
        }
    }

    private fun handleOnMessageNearbyCabs(jsonObject: JSONObject) {
        val nearByCabLocations = ArrayList<LatLng>()
        val jsonArray = jsonObject.getJSONArray(LOCATIONS)
        for (i in 0 until jsonArray.length()){
            val lat = (jsonArray.get(i) as JSONObject).getDouble(LAT)
            val lng = (jsonArray.get(i) as JSONObject).getDouble(LNG)

            nearByCabLocations.add(LatLng(lat,lng))
        }
        view?.showNearByCabs(nearByCabLocations)
    }

    override fun onDisconnect() {
        Log.d(TAG,"onDisconnect")
    }

    override fun onError(error: String) {
        Log.d(TAG,"onError : $error")
    }

    fun onDetach(){
        webSocket.disconnect()
        view = null
    }
}