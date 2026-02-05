package com.korea.boardEx.bean.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConcertDTO {
	private String id;
    private String title;
    private String date;
    private String image;
    private String link;
}
