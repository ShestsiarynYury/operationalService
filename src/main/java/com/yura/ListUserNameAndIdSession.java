/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yura;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 *
 * @author user
 */
public class ListUserNameAndIdSession {
    private Map<String, String> map = new HashMap<String, String>();
    
    public void setMap(Map<String, String> map) {
        this.map = map;
    }
    
    public Map<String, String> getMap() {
        return this.map;
    }
}
