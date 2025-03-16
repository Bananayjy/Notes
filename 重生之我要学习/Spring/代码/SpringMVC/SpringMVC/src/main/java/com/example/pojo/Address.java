package com.example.pojo;

/**
 * @author banana
 * @create 2023-09-17 20:11
 */
public class Address {

    private String city;
    private String area;

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    @Override
    public String toString() {
        return "Address{" +
                "city='" + city + '\'' +
                ", area='" + area + '\'' +
                '}';
    }
}
