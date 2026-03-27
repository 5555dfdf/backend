package org.example.coursework3.result;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SpecialistListResult {
    private List<Item> items;
    private int total;
    private int page;
    private int pageSize;

    public SpecialistListResult(){
        this.items = new ArrayList<>();
        this.items.add(new Item());
        this.items.add(new Item());
        this.total =1;
        this.page = 1;
        this.pageSize =10;
    }
}

@Data
class Item {
    private String id = "id";
    private String name = "lmh";
    private String[] expertiseIds = {"e1","e2"};
    private double price = 300;
}

