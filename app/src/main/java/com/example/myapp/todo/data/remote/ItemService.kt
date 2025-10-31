package com.example.myapp.todo.data.remote

import com.example.myapp.todo.data.Item
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ItemService {
    @GET("/item")
    suspend fun find(): List<Item>

    @GET("/item/{id}")
    suspend fun read(@Path("id") itemId: String?): Item;

    @Headers("Content-Type: application/json")
    @POST("/item")
    suspend fun create(@Body item: Item): Item

    @Headers("Content-Type: application/json")
    @PUT("/item/{id}")
    suspend fun update(@Path("id") itemId: String?, @Body item: Item): Item
}
