package com.pine.media.db_server;

import com.pine.media.base.BaseUrlConstants;

public interface DbUrlConstants extends BaseUrlConstants {

    String Query_BundleSwitcher_Data = BASE_URL + "&q=bsd";
    String Query_Version_Data = BASE_URL + "&q=vd";

    //    String Login = BASE_URL + "&q=li";
//    String Logout = BASE_URL + "&q=lo";
//    String Verify_Code_Image = BASE_URL + "&q=vci";
    String Register_Account = BASE_URL + "&q=ra";

    // Test code begin
    String Login = "https://yanyangtian.purang.com/" + "/mobile/login.htm";
    String Logout = "https://yanyangtian.purang.com/" + "/mobile/logout.htm";
    String Verify_Code_Image = "http://yanyangtian.purang.com/" + "/picCode.htm";
    // Test code end

//    String Upload_Single_File = BASE_URL + "";
//    String Upload_Multi_File = BASE_URL + "";

    // Test code begin
    String Upload_Single_File = "https://yanyangtian.purang.com/" + "/mobile/bizFile/addBizFile.htm";
    String Upload_Multi_File = "https://yanyangtian.purang.com/" + "/mobile/bizFile/addBizFileList.htm";
    // Test code end
}
