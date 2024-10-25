package com.manage.carrivedriver.custom;

import com.manage.carrive.entity.Driver;
import com.manage.carrive.enumeration.UserTypeEnum;
import com.manage.carrivedriver.security.JwtRequestFilter;
import org.springframework.stereotype.Service;

@Service
public class CustomAuthorizationService {

    public boolean hasPermission(String userType){
        if (userType == null || userType.isEmpty()){
            return false;
        }
        Driver investor = JwtRequestFilter.driver;
        if (investor == null){
            return false;
        }
        return investor.getUserType().equals(UserTypeEnum.valueOf(userType));
    }
}
