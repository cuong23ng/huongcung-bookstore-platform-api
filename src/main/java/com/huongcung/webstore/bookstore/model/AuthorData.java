package com.huongcung.webstore.bookstore.model;

import lombok.Data;

@Data
public class AuthorData {
    private String name;
    private String biography;
    private String photoUrl;
    private String birthDate;
    private String nationality;
}
