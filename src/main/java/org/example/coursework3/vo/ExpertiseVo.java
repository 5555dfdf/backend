package org.example.coursework3.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.coursework3.entity.Expertise;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExpertiseVo {
    private String id;
    private String name;
    private String description;

    public static ExpertiseVo toVo(Expertise expertise){
        return new ExpertiseVo(expertise.getId(), expertise.getName(), expertise.getDescription());
    }
}
