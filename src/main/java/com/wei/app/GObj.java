package com.wei.app;

import java.util.ArrayList;

public class GObj {

	public String name,desc,picture,rating;

	public GObj(String name, String desc, String picture, String rating) {
		
		this.name = name;
		this.desc = desc;
		this.picture = picture;
		this.rating = rating;
		planslist = new ArrayList<>();
	}
	
	public ArrayList<Gplan> planslist;
	
	public GObj(){
		planslist = new ArrayList<>();
	}
}

