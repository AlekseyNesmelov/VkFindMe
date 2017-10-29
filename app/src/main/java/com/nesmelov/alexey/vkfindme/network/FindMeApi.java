package com.nesmelov.alexey.vkfindme.network;

import com.nesmelov.alexey.vkfindme.models.FullUserInfoModel;
import com.nesmelov.alexey.vkfindme.models.LatLonUserModel;
import com.nesmelov.alexey.vkfindme.models.LatLonUsersModel;
import com.nesmelov.alexey.vkfindme.models.StatusModel;
import com.nesmelov.alexey.vkfindme.models.UserModel;
import com.nesmelov.alexey.vkfindme.models.UsersModel;
import com.nesmelov.alexey.vkfindme.models.VisibilityUserModel;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface FindMeApi {

    @POST("Server?action=add")
    Call<StatusModel> postAddUser(@Body UserModel userModel);

    @POST("Server?action=set_visible")
    Call<StatusModel> postSetVisible(@Body FullUserInfoModel userModel);

    @POST("Server?action=set_visible")
    Call<StatusModel> postSetInvisible(@Body VisibilityUserModel userModel);

    @POST("Server?action=set_pos")
    Call<StatusModel> postSetPosition(@Body LatLonUserModel userModel);

    @POST("Server?action=check")
    Call<UsersModel> postCheckUsers(@Body UsersModel userModel);

    @POST("Server?action=get_pos")
    Call<LatLonUsersModel> postGetPositions(@Body UsersModel userModel);
}
