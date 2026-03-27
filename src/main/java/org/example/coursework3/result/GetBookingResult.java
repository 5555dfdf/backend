package org.example.coursework3.result;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class GetBookingResult {
    private List<Item2> items;
    private int total;
    private int page;
    private int pageSize;

    public GetBookingResult(){
        this.items = new ArrayList<>();
        this.items.add(new Item2());
        this.items.add(new Item2());
        this.total =1;
        this.page = 1;
        this.pageSize =10;

    }

}
@Data
class Item2{
    private String id = "id";
    private String customerName = "lmh";
    private LocalDateTime time = LocalDateTime.now();
    private String status = "Pending";
}
